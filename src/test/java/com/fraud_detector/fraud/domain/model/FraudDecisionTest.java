package com.fraud_detector.fraud.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraudDecisionTest {

    @Test
    void shouldContainExpectedFraudDecisions() {
        assertArrayEquals(
                new FraudDecision[]{
                        FraudDecision.APPROVED,
                        FraudDecision.REVIEW,
                        FraudDecision.BLOCKED
                },
                FraudDecision.values()
        );
    }
}