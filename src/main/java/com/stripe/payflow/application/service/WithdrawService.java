package com.stripe.payflow.application.service;

import com.stripe.payflow.api.dto.request.WithdrawRequest;
import com.stripe.payflow.api.dto.response.WithdrawResponse;

public interface WithdrawService {
    WithdrawResponse withdraw(WithdrawRequest request, String idempotencyKey);
}
