package com.evmoto.fee.controller;

import com.evmoto.fee.model.FeeCalculationResult;
import com.evmoto.fee.model.FeePreviewRequest;
import com.evmoto.fee.model.FeePreviewResponse;
import com.evmoto.fee.service.WaitingFeeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
public class FeePreviewController {

    private static final Logger LOG =
            LoggerFactory.getLogger(FeePreviewController.class);

    private final WaitingFeeCalculator calculator =
            new WaitingFeeCalculator();

    /**
     * Menghitung preview biaya tunggu/cancellation untuk sebuah order.
     */
    @PostMapping("/{orderId}/fee-preview")
    public FeePreviewResponse preview(
            @PathVariable String orderId,
            @RequestBody FeePreviewRequest request) {

        LOG.info("Fee preview request: orderId={}", orderId);

        FeeCalculationResult result = calculator.calculate(
                request.arrivedAt(),
                request.endedAt(),
                request.endReason(),
                request.pickupPoint(),
                request.driverPings()
        );

        LOG.info(
                "Fee preview result: orderId={}, waitingFee={}, cancellationFee={}, totalFee={}",
                orderId,
                result.waitingFee(),
                result.cancellationFee(),
                result.totalFee()
        );

        FeePreviewResponse.Breakdown breakdown =
                new FeePreviewResponse.Breakdown(
                        result.billableMinutes(),
                        result.pausedSeconds(),
                        result.waitingFeeCapped(),
                        result.cancellationFeeCapped()
                );

        return new FeePreviewResponse(
                orderId,
                breakdown,
                result.waitingFee(),
                result.cancellationFee(),
                result.totalFee()
        );
    }
}