package com.stripe.payflow.api.controller;

import com.stripe.payflow.api.dto.response.NotificationResponse;
import com.stripe.payflow.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User Notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get Notifications", description = "Retrieves paginated notifications for a wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications")
    })
    @GetMapping("/wallets/{walletId}/notifications")
    public Page<NotificationResponse> getNotifications(
            @PathVariable UUID walletId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        return notificationService.getNotificationsForWallet(walletId, pageable);
    }

    @Operation(summary = "Mark Notification as Read", description = "Acknowledges a notification and clears unread status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification marked as read")
    })
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }
}
