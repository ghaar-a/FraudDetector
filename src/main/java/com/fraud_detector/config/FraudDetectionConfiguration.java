package com.fraud_detector.config;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.model.FraudReason;
import com.fraud_detector.fraud.domain.rule.FraudRule;
import com.fraud_detector.fraud.domain.rule.FraudRuleEngine;
import com.fraud_detector.fraud.domain.rule.UnknownDeviceRule;
import com.fraud_detector.fraud.domain.rule.UnusualAmountRule;
import com.fraud_detector.fraud.domain.rule.UnusualTimeRule;
import com.fraud_detector.fraud.domain.service.RiskScoringPolicy;
import com.fraud_detector.fraud.domain.service.RiskScoringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Configuration
public class FraudDetectionConfiguration {

    @Bean
    public UnusualAmountRule unusualAmountRule() {
        return new UnusualAmountRule();
    }

    @Bean
    public UnusualTimeRule unusualTimeRule() {
        return new UnusualTimeRule();
    }

    @Bean
    public UnknownDeviceRule unknownDeviceRule() {
        return new UnknownDeviceRule();
    }

    @Bean
    public FraudRuleEngine fraudRuleEngine(
            UnusualAmountRule unusualAmountRule,
            UnusualTimeRule unusualTimeRule,
            UnknownDeviceRule unknownDeviceRule
    ) {
        List<FraudRule> rules = List.of(
                unusualAmountRule,
                unusualTimeRule,
                unknownDeviceRule
        );

        return new FraudRuleEngine(
                rules
        );
    }

    @Bean
    public RiskScoringPolicy riskScoringPolicy() {
        return new RiskScoringPolicy(
                Map.of(
                        FraudReason.UNUSUAL_AMOUNT,
                        BigDecimal.valueOf(0.35),

                        FraudReason.UNUSUAL_TIME,
                        BigDecimal.valueOf(0.25),

                        FraudReason.UNKNOWN_DEVICE,
                        BigDecimal.valueOf(0.40)
                )
        );
    }

    @Bean
    public RiskScoringService riskScoringService(
            RiskScoringPolicy riskScoringPolicy
    ) {
        return new RiskScoringService(
                riskScoringPolicy
        );
    }

    @Bean
    public FraudDetectionService fraudDetectionService(
            FraudRuleEngine fraudRuleEngine,
            RiskScoringService riskScoringService
    ) {
        return new FraudDetectionService(
                fraudRuleEngine,
                riskScoringService
        );
    }
}