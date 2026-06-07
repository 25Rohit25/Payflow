package com.stripe.payflow.infrastructure.web;

import com.stripe.payflow.domain.model.IdempotencyRecord;
import com.stripe.payflow.domain.model.IdempotencyStatus;
import com.stripe.payflow.domain.repository.IdempotencyRecordRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    
    private final IdempotencyRecordRepository repository;
    private final TransactionTemplate transactionTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        IdempotencyRecord record = transactionTemplate.execute(status -> 
            repository.findByIdempotencyKey(idempotencyKey).orElse(null)
        );

        if (record != null) {
            if (record.getStatus() == IdempotencyStatus.COMPLETED) {
                log.info("Returning cached response for Idempotency-Key: {}", idempotencyKey);
                response.setStatus(record.getResponseStatus());
                response.setContentType("application/json");
                response.getWriter().write(record.getResponsePayload());
                return;
            } else if (record.getStatus() == IdempotencyStatus.STARTED) {
                log.warn("Concurrent request for Idempotency-Key: {}", idempotencyKey);
                response.sendError(HttpStatus.CONFLICT.value(), "Concurrent request in progress for this Idempotency-Key");
                return;
            }
        }

        IdempotencyRecord newRecord = new IdempotencyRecord();
        newRecord.setId(UUID.randomUUID());
        newRecord.setIdempotencyKey(idempotencyKey);
        newRecord.setRequestPath(request.getRequestURI());
        newRecord.setRequestHash("pending");
        newRecord.setStatus(IdempotencyStatus.STARTED);
        
        transactionTemplate.execute(status -> repository.save(newRecord));

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            String responseBody = new String(responseWrapper.getContentAsByteArray());
            String requestBody = new String(requestWrapper.getContentAsByteArray());
            String requestHash = DigestUtils.md5DigestAsHex(requestBody.getBytes());

            transactionTemplate.execute(status -> {
                IdempotencyRecord r = repository.findById(newRecord.getId()).orElseThrow();
                r.setRequestHash(requestHash);
                r.setResponseStatus(responseWrapper.getStatus());
                r.setResponsePayload(responseBody);
                r.setStatus(IdempotencyStatus.COMPLETED);
                return repository.save(r);
            });

        } catch (Exception e) {
            transactionTemplate.execute(status -> {
                IdempotencyRecord r = repository.findById(newRecord.getId()).orElseThrow();
                r.setStatus(IdempotencyStatus.FAILED);
                return repository.save(r);
            });
            throw e;
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }
}
