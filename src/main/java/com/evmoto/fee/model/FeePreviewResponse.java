package com.evmoto.fee.model;

/**
 * Response untuk endpoint fee preview.
 *
 * Breakdown dipisahkan agar rincian perhitungan lebih mudah dibaca
 * oleh consumer API.
 */
public record FeePreviewResponse(
        String orderId,
        Breakdown breakdown,
        long waitingFee,
        long cancellationFee,
        long totalFee) {

    /**
     * Rincian waktu dan status cap biaya.
     */
    public record Breakdown(
            long billableMinutes,
            long pausedSeconds,
            boolean waitingFeeCapped,
            boolean cancellationFeeCapped) {
    }
}