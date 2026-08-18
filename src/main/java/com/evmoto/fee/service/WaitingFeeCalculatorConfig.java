package com.evmoto.fee.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WaitingFeeCalculatorConfig {
    @Bean
    public WaitingFeeCalculator waitingFeeCalculator() {
        return new WaitingFeeCalculator();
    }
}
