package com.evmoto.fee.model;

import java.time.OffsetDateTime;
import java.util.List;

public record FeePreviewRequest(
        OffsetDateTime arrivedAt,
        OffsetDateTime endedAt,
        EndReason endReason,
        PickupPoint pickupPoint,
        List<DriverPing> driverPings) {}
