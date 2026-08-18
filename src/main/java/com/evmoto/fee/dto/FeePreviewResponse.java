package com.evmoto.fee.dto;

import com.evmoto.fee.model.EndReason;

public record FeePreviewResponse(
        String orderId,
        EndReason endReason,
        Breakdown breakdown,
        long waitingFee,
        long cancellationFee,
        long totalFee,
        boolean waitingFeeCapped,
        boolean cancellationFeeCapped) {

    public record Breakdown(
            long activeWaitingSeconds,
            long pausedSeconds,
            long pausedMinutes,
            long billableMinutes) {
    }
}
