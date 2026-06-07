package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.DepositRequest;
import com.stripe.payflow.api.dto.response.DepositResponse;

public interface DepositService {
    DepositResponse deposit(DepositRequest request, String idempotencyKey);
}
