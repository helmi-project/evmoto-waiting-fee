package com.evmoto.fee.model;

public record FeeCalculationResult(
        long pausedSeconds,
        long pausedMinutes,
        long billableMinutes,
        long waitingFee,
        long cancellationFee,
        long totalFee,
        boolean waitingFeeCapped,
        boolean cancellationFeeCapped) {}
