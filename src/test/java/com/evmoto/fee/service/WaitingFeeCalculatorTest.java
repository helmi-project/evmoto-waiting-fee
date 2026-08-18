package com.evmoto.fee.service;

import com.evmoto.fee.model.*;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WaitingFeeCalculatorTest {
    private static final OffsetDateTime START = OffsetDateTime.parse("2026-08-10T09:00:00+07:00");
    private static final PickupPoint PICKUP = new PickupPoint(-6.21462, 106.84513);
    private final WaitingFeeCalculator calculator = new WaitingFeeCalculator();

    private FeeCalculationResult calculate(long seconds, EndReason reason) {
        return calculator.calculate(START, START.plusSeconds(seconds), reason, PICKUP, List.of());
    }

    @Test void fiveMinutesFree() { assertEquals(0, calculate(300, EndReason.TRIP_STARTED).waitingFee()); }
    @Test void fiveMinutesOneSecondIsOneMinute() { assertEquals(500, calculate(301, EndReason.TRIP_STARTED).waitingFee()); }
    @Test void sixMinutesIsOnePaidMinute() { assertEquals(1, calculate(360, EndReason.TRIP_STARTED).billableMinutes()); }
    @Test void sixMinutesOneSecondIsTwoPaidMinutes() { assertEquals(2, calculate(361, EndReason.TRIP_STARTED).billableMinutes()); }
    @Test void waitingCap() { assertEquals(15_000, calculate(36 * 60, EndReason.TRIP_STARTED).waitingFee()); }
    @Test void customerCancelBeforeFreeIsFree() { assertEquals(0, calculate(299, EndReason.CANCELLED_BY_CUSTOMER).totalFee()); }
    @Test void customerCancelAtFreeIsFree() { assertEquals(0, calculate(300, EndReason.CANCELLED_BY_CUSTOMER).totalFee()); }
    @Test void customerCancelAfterFreeAddsFiveThousand() { assertEquals(7_500, calculate(600, EndReason.CANCELLED_BY_CUSTOMER).totalFee()); }
    @Test void cancellationCap() { assertEquals(20_000, calculate(65 * 60, EndReason.CANCELLED_BY_CUSTOMER).totalFee()); }
    @Test void driverCancelIsFree() { assertEquals(0, calculate(60 * 60, EndReason.CANCELLED_BY_DRIVER).totalFee()); }
    @Test void emptyPingsDoNotPause() { assertEquals(0, calculate(600, EndReason.TRIP_STARTED).pausedSeconds()); }
    @Test void farPingPausesUntilReturn() {
        FeeCalculationResult r = calculator.calculate(START, START.plusMinutes(20), EndReason.TRIP_STARTED, PICKUP,
                List.of(new DriverPing(START, PICKUP.lat(), PICKUP.lng()),
                        new DriverPing(START.plusMinutes(8), -6.21980, 106.85110),
                        new DriverPing(START.plusMinutes(14), -6.21470, 106.84520)));
        assertEquals(360, r.pausedSeconds());
        assertEquals(9, r.billableMinutes());
    }
    @Test void firstFarPingStartsPauseAtPingTime() {
        FeeCalculationResult r = calculator.calculate(START, START.plusMinutes(10), EndReason.TRIP_STARTED, PICKUP,
                List.of(new DriverPing(START.plusMinutes(2), -6.21980, 106.85110)));
        assertEquals(480, r.pausedSeconds());
        assertEquals(0, r.waitingFee());
    }
    @Test void endedBeforeArrivedIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(START, START.minusSeconds(1),
                EndReason.TRIP_STARTED, PICKUP, List.of()));
    }
}
