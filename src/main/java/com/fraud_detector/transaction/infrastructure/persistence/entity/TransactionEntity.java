package com.fraud_detector.transaction.infrastructure.persistence.entity;

import com.fraud_detector.transaction.domain.model.Money;
import com.fraud_detector.transaction.domain.model.Transaction;
import com.fraud_detector.transaction.domain.model.TransactionCategory;
import com.fraud_detector.transaction.domain.model.TransactionId;
import com.fraud_detector.transaction.domain.model.TransactionLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(
            name = "user_id",
            nullable = false,
            length = 100
    )
    private String userId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "merchant_name",
            nullable = false,
            length = 255
    )
    private String merchant;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private TransactionCategory category;

    @Column(
            name = "transaction_timestamp",
            nullable = false
    )
    private Instant transactionTimestamp;

    @Column(
            name = "country",
            nullable = false,
            length = 2
    )
    private String country;

    @Column(
            name = "state",
            length = 100
    )
    private String state;

    @Column(
            name = "city",
            nullable = false,
            length = 150
    )
    private String city;

    @Column(
            name = "latitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal latitude;

    @Column(
            name = "longitude",
            precision = 10,
            scale = 7
    )
    private BigDecimal longitude;

    @Column(
            name = "device_id",
            nullable = false,
            length = 255
    )
    private String deviceId;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    private TransactionEntity(
            UUID id,
            String userId,
            BigDecimal amount,
            String currency,
            String merchant,
            TransactionCategory category,
            Instant transactionTimestamp,
            String country,
            String state,
            String city,
            BigDecimal latitude,
            BigDecimal longitude,
            String deviceId
    ) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.merchant = merchant;
        this.category = category;
        this.transactionTimestamp = transactionTimestamp;
        this.country = country;
        this.state = state;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceId = deviceId;
    }

    public static TransactionEntity fromDomain(
            Transaction transaction
    ) {
        TransactionLocation location =
                transaction.location();

        BigDecimal latitude =
                location.latitude() != null
                        ? BigDecimal.valueOf(
                        location.latitude()
                )
                        : null;

        BigDecimal longitude =
                location.longitude() != null
                        ? BigDecimal.valueOf(
                        location.longitude()
                )
                        : null;

        return new TransactionEntity(
                transaction.id().value(),
                transaction.userId(),
                transaction.amount().amount(),
                transaction.amount().currency(),
                transaction.merchant(),
                transaction.category(),
                transaction.timestamp(),
                location.country(),
                location.state(),
                location.city(),
                latitude,
                longitude,
                transaction.deviceId()
        );
    }

    public Transaction toDomain() {
        TransactionLocation location =
                new TransactionLocation(
                        country,
                        state,
                        city,
                        latitude != null
                                ? latitude.doubleValue()
                                : null,
                        longitude != null
                                ? longitude.doubleValue()
                                : null
                );

        Money money =
                new Money(
                        amount,
                        currency
                );

        return new Transaction(
                TransactionId.of(id),
                userId,
                money,
                merchant,
                category,
                transactionTimestamp,
                location,
                deviceId
        );
    }
}