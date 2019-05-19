/*
 * PaymentRepositoryTest.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.repositories;

import org.junit.ClassRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * PaymentRepository test is based on postgres and migration script test20191205_01__init.sql
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {PaymentRepositoryTest.Initializer.class})
class PaymentRepositoryTest {

    private static final String POSTGRES_DATABASE = "dbpayments";
    private static final String POSTGRES_USER = "dbpayments";
    private static final String POSTGRES_PASSWORD = "dbpayments";

    @ClassRule
    public static final PostgreSQLContainer POSTGRES_CONTAINER;
    static { POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:10.1").withDatabaseName(POSTGRES_DATABASE)
                                                                                          .withUsername(POSTGRES_USER)
                                                                                          .withPassword(POSTGRES_PASSWORD);
             POSTGRES_CONTAINER.start();
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("PaymentRepository test")
    void test() {
        try {
            final String sender1 = "sender1";
            final String sender2 = "sender2";
            final String receiver1 = "receiver1";
            final String receiver2 = "receiver2";
            assertEquals(paymentRepository.countBySender(sender1), Double.valueOf(100.0d));
            assertEquals(paymentRepository.countBySender(sender2), Double.valueOf(200.0d));
            assertEquals(paymentRepository.countBySender(receiver2), Double.valueOf(150.0d));
            assertEquals(paymentRepository.countByReceiver(receiver1), Double.valueOf(150.0d));
            assertEquals(paymentRepository.countByReceiver(receiver2), Double.valueOf(170.0d));
            assertEquals(paymentRepository.countByReceiver(sender2), Double.valueOf(150.0d));
            assertEquals(Double.valueOf(paymentRepository.countByReceiver(receiver1) - paymentRepository.countBySender(receiver1)), Double.valueOf(130.0d));
            assertEquals(Double.valueOf(paymentRepository.countByReceiver(sender2) - paymentRepository.countBySender(sender2)), Double.valueOf(-50.0d));
            assertEquals(Double.valueOf(paymentRepository.countByReceiver(receiver2) - paymentRepository.countBySender(receiver2)), Double.valueOf(20.0d));
            assertNull(paymentRepository.countByReceiver(sender1));
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRES_CONTAINER.getPassword()).applyTo(configurableApplicationContext.getEnvironment());
        }

    }

}
