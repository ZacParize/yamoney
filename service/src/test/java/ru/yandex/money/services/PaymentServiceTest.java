/*
 * PaymentServiceTest.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.services;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.yandex.money.repositories.PaymentRepository;
import ru.yandex.money.repositories.entities.Payment;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class PaymentServiceTest {

    private static final String DEFAULT_SENDER = "defaultSender";
    private static final String DEFAULT_RECEIVER = "defaultReceiver";

    @MockBean
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void init() {
        paymentService = new PaymentService(paymentRepository);
    }

    private static Stream<Arguments> addAllGoodParameters() {
        return Stream.of(
                Arguments.of( null, Collections.emptyList()),
                Arguments.of( Collections.emptyList(), Collections.emptyList()),
                Arguments.of( new LinkedList<Payment>() {{
                                add(new Payment(1L, DEFAULT_SENDER, DEFAULT_RECEIVER, null, BigDecimal.TEN ));
                            }}, new LinkedList<Payment>() {{
                                add(new Payment(0L, DEFAULT_SENDER, DEFAULT_RECEIVER, null, BigDecimal.TEN ));
                            }}
                ),
                Arguments.of( new LinkedList<Payment>() {{
                                for (int idx = 1; idx <= PaymentService.MAX_BATCH_SIZE; idx++) {
                                    add(new Payment(1L, DEFAULT_SENDER, DEFAULT_RECEIVER, null, BigDecimal.TEN));
                                }
                             }}, new LinkedList<Payment>() {{
                                for (int idx = 1; idx <= PaymentService.MAX_BATCH_SIZE; idx++) {
                                    add(new Payment(0L, DEFAULT_SENDER, DEFAULT_RECEIVER, null, BigDecimal.TEN));
                                }
                             }}
                )
        );
    }

    @ParameterizedTest
    @MethodSource("addAllGoodParameters")
    void addAll_ok(List<Payment> paymentsForLoading, List<Payment> loadedPayments) {
        Mockito.when(paymentRepository.create(any(String.class), any(String.class), any(ZonedDateTime.class), any(Double.class))).thenReturn(0L);
        assertIterableEquals(paymentService.addAll(paymentsForLoading), loadedPayments);
    }

    private static Stream<Arguments> addAllBadLoadingList() {
        return Stream.of(
                Arguments.of( new LinkedList<Payment>() {{
                                  for (int idx = 1; idx <= PaymentService.MAX_BATCH_SIZE + 1; idx++) {
                                      add(new Payment(1L, DEFAULT_SENDER, DEFAULT_RECEIVER, null, BigDecimal.TEN));
                                  }
                              }}
                )
        );
    }

    @ParameterizedTest
    @MethodSource("addAllBadLoadingList")
    void addAll_badListOfLoadings(List<Payment> paymentsForLoading) {
        assertThrows(IllegalArgumentException.class, () -> paymentService.addAll(paymentsForLoading));
    }

}
