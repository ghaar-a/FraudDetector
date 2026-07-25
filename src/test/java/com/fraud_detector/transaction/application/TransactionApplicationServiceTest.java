package com.fraud_detector.transaction.application;

import com.fraud_detector.fraud.application.FraudDetectionService;
import com.fraud_detector.fraud.domain.model.FraudAnalysis;
import com.fraud_detector.fraud.domain.rule.FraudRuleContext;
import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import com.fraud_detector.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TransactionApplicationServiceTest {

    private final TransactionRepository transactionRepository =
            mock(TransactionRepository.class);

    private final FraudDetectionService fraudDetectionService =
            mock(FraudDetectionService.class);

    private final TransactionApplicationService service =
            new TransactionApplicationService(
                    transactionRepository,
                    fraudDetectionService
            );

    @Test
    void shouldPersistTransactionBeforeFraudDetection() {

        Transaction transaction =
                Transaction.create(
                        "user-123",
                        Money.of(
                                149.90,
                                "BRL"
                        ),
                        "Electronics Store",
                        TransactionCategory.ELECTRONICS,
                        Instant.parse(
                                "2026-07-24T12:00:00Z"
                        ),
                        new TransactionLocation(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        ),
                        "device-123"
                );

        FraudRuleContext context =
                new FraudRuleContext(
                        Money.of(
                                100.00,
                                "BRL"
                        ),
                        LocalTime.of(
                                8,
                                0
                        ),
                        LocalTime.of(
                                22,
                                0
                        ),
                        Set.of(
                                "device-123"
                        ),
                        new TransactionLocation(
                                "BR",
                                "SP",
                                "São Paulo",
                                -23.5505,
                                -46.6333
                        )
                );

        FraudAnalysis expectedAnalysis =
                mock(
                        FraudAnalysis.class
                );

        when(
                fraudDetectionService.analyze(
                        transaction,
                        context
                )
        ).thenReturn(
                expectedAnalysis
        );

        FraudAnalysis result =
                service.process(
                        transaction,
                        context
                );

        assertSame(
                expectedAnalysis,
                result
        );

        InOrder inOrder =
                inOrder(
                        transactionRepository,
                        fraudDetectionService
                );

        inOrder.verify(
                transactionRepository
        ).save(
                transaction
        );

        inOrder.verify(
                fraudDetectionService
        ).analyze(
                transaction,
                context
        );

        verifyNoMoreInteractions(
                transactionRepository,
                fraudDetectionService
        );
    }
}