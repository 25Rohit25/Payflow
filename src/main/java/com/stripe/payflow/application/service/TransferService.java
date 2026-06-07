package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.TransferRequest;
import com.stripe.payflow.api.dto.response.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request, String idempotencyKey);
}
