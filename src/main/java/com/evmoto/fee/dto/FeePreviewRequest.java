package com.evmoto.fee.dto;

import com.evmoto.fee.model.EndReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record FeePreviewRequest(
        @NotNull OffsetDateTime arrivedAt,
        @NotNull OffsetDateTime endedAt,
        @NotNull EndReason endReason,
        @NotNull @Valid PickupPointRequest pickupPoint,
        @Valid List<DriverPingRequest> driverPings) {

    public FeePreviewRequest {
        if (driverPings == null) {
            driverPings = List.of();
        }
    }
}
