package com.fraud_detector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
				"org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
				"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
		"spring.flyway.enabled=false"
})
class FraudDetectorApplicationTests {

	@Test
	void contextLoads() {
	}
}