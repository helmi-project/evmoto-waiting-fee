package com.evmoto.fee.service;

import com.evmoto.fee.model.DriverPing;
import com.evmoto.fee.model.EndReason;
import com.evmoto.fee.model.FeeCalculationResult;
import com.evmoto.fee.model.PickupPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Business logic utama. Class ini sengaja dibuat sederhana dan tidak bergantung
 * pada database, Spring, atau system clock.
 */
public final class WaitingFeeCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(WaitingFeeCalculator.class);
    private static final long FREE_SECONDS = 300;
    private static final long FEE_PER_MINUTE = 500;
    private static final long WAITING_CAP = 15_000;
    private static final long CANCEL_FEE = 5_000;
    private static final long CANCEL_CAP = 20_000;
    private static final double PAUSE_DISTANCE_METERS = 100.0;

    public FeeCalculationResult calculate(OffsetDateTime arrivedAt, OffsetDateTime endedAt,
                                           EndReason endReason, PickupPoint pickupPoint,
                                           List<DriverPing> driverPings) {
        validate(arrivedAt, endedAt, endReason, pickupPoint, driverPings);

        long totalSeconds = Duration.between(arrivedAt, endedAt).getSeconds();
        long pausedSeconds = calculatePausedSeconds(arrivedAt, endedAt, pickupPoint, driverPings);
        long activeSeconds = totalSeconds - pausedSeconds;
        long paidSeconds = Math.max(0, activeSeconds - FREE_SECONDS);
        long billableMinutes = ceilDiv(paidSeconds, 60);
        long rawWaitingFee = billableMinutes * FEE_PER_MINUTE;
        long waitingFee = Math.min(rawWaitingFee, WAITING_CAP);
        boolean waitingCapped = rawWaitingFee >= WAITING_CAP;

        long cancellationFee = 0;
        boolean cancellationCapped = false;

        if (endReason == EndReason.CANCELLED_BY_DRIVER) {
            waitingFee = 0;
            waitingCapped = false;
        } else if (endReason == EndReason.CANCELLED_BY_CUSTOMER && activeSeconds > FREE_SECONDS) {
            long rawCancellation = waitingFee + CANCEL_FEE;
            cancellationFee = Math.min(rawCancellation, CANCEL_CAP);
            cancellationCapped = rawCancellation >= CANCEL_CAP;
        }

        long totalFee = switch (endReason) {
            case TRIP_STARTED -> waitingFee;
            case CANCELLED_BY_CUSTOMER -> cancellationFee;
            case CANCELLED_BY_DRIVER -> 0;
        };

        LOG.info("Fee calculated: reason={}, billableMinutes={}, pausedMinutes={}, totalFee={}",
                endReason, billableMinutes, ceilDiv(pausedSeconds, 60), totalFee);

        return new FeeCalculationResult(pausedSeconds, ceilDiv(pausedSeconds, 60), billableMinutes,
                waitingFee, cancellationFee, totalFee, waitingCapped, cancellationCapped);
    }

    private long calculatePausedSeconds(OffsetDateTime arrivedAt, OffsetDateTime endedAt,
                                         PickupPoint pickup, List<DriverPing> driverPings) {
        List<DriverPing> pings = driverPings.stream()
                .filter(p -> !p.at().isBefore(arrivedAt) && !p.at().isAfter(endedAt))
                .sorted(Comparator.comparing(DriverPing::at)).toList();
        if (pings.isEmpty()) return 0;

        long paused = 0;
        OffsetDateTime pauseStart = null;
        for (DriverPing ping : pings) {
            boolean far = distanceMeters(pickup.lat(), pickup.lng(), ping.lat(), ping.lng()) > PAUSE_DISTANCE_METERS;
            if (far && pauseStart == null) {
                pauseStart = ping.at();
                LOG.debug("Timer paused at {}, distance > 100m", ping.at());
            } else if (!far && pauseStart != null) {
                paused += Duration.between(pauseStart, ping.at()).getSeconds();
                pauseStart = null;
                LOG.debug("Timer resumed at {}", ping.at());
            }
        }
        if (pauseStart != null) paused += Duration.between(pauseStart, endedAt).getSeconds();
        return Math.min(paused, Duration.between(arrivedAt, endedAt).getSeconds());
    }

    /** Haversine sederhana, tanpa library geo eksternal. */
    static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadius = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static long ceilDiv(long value, long divisor) {
        return value <= 0 ? 0 : (value + divisor - 1) / divisor;
    }

    private static void validate(OffsetDateTime arrivedAt, OffsetDateTime endedAt,
                                 EndReason reason, PickupPoint pickup, List<DriverPing> pings) {
        if (arrivedAt == null || endedAt == null || reason == null || pickup == null || pings == null)
            throw new IllegalArgumentException("arrivedAt, endedAt, endReason, pickupPoint and driverPings are required");
        if (endedAt.isBefore(arrivedAt)) throw new IllegalArgumentException("endedAt must not be earlier than arrivedAt");
    }
}
