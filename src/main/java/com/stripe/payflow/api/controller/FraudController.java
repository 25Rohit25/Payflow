package com.stripe.payflow.api.controller;

import com.stripe.payflow.domain.model.FraudLog;
import com.stripe.payflow.domain.repository.FraudLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud-logs")
@RequiredArgsConstructor
@Tag(name = "Fraud", description = "Fraud Operations & Audit Logs")
public class FraudController {

    private final FraudLogRepository fraudLogRepository;

    @Operation(summary = "List Fraud Logs", description = "Retrieve a paginated list of all fraud detection interventions and wallet blocks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping
    public Page<FraudLog> getFraudLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        return fraudLogRepository.findAll(pageable);
    }
}
