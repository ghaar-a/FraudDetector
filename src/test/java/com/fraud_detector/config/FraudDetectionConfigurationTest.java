package com.fraud_detector.config;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.rule.FraudRuleEngine;
import com.fraud_detector.fraud.domain.rule.UnknownDeviceRule;
import com.fraud_detector.fraud.domain.rule.UnusualAmountRule;
import com.fraud_detector.fraud.domain.rule.UnusualTimeRule;
import com.fraud_detector.fraud.domain.service.RiskScoringPolicy;
import com.fraud_detector.fraud.domain.service.RiskScoringService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FraudDetectionConfigurationTest {

    @Test
    void shouldCreateFraudDetectionDependencies() {
        UnusualAmountRule unusualAmountRule =
                new UnusualAmountRule();

        UnusualTimeRule unusualTimeRule =
                new UnusualTimeRule();

        UnknownDeviceRule unknownDeviceRule =
                new UnknownDeviceRule();

        FraudRuleEngine fraudRuleEngine =
                new FraudRuleEngine(
                        List.of(
                                unusualAmountRule,
                                unusualTimeRule,
                                unknownDeviceRule
                        )
                );

        RiskScoringPolicy riskScoringPolicy =
                new RiskScoringPolicy(
                        Map.of(
                                com.fraud_detector.fraud.domain.model.FraudReason.UNUSUAL_AMOUNT,
                                BigDecimal.valueOf(0.35),

                                com.fraud_detector.fraud.domain.model.FraudReason.UNUSUAL_TIME,
                                BigDecimal.valueOf(0.25),

                                com.fraud_detector.fraud.domain.model.FraudReason.UNKNOWN_DEVICE,
                                BigDecimal.valueOf(0.40)
                        )
                );

        RiskScoringService riskScoringService =
                new RiskScoringService(
                        riskScoringPolicy
                );

        FraudDetectionService fraudDetectionService =
                new FraudDetectionService(
                        fraudRuleEngine,
                        riskScoringService
                );

        assertNotNull(
                unusualAmountRule
        );

        assertNotNull(
                unusualTimeRule
        );

        assertNotNull(
                unknownDeviceRule
        );

        assertNotNull(
                fraudRuleEngine
        );

        assertNotNull(
                riskScoringPolicy
        );

        assertNotNull(
                riskScoringService
        );

        assertNotNull(
                fraudDetectionService
        );
    }
}