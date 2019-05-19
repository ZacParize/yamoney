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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.yandex.money.repositories.PaymentRepository;
import ru.yandex.money.repositories.entities.Payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(SpringExtension.class)
class PaymentServiceTest {

    private static final String DEFAULT_SENDER = "defaultSender";
    private static final String DEFAULT_RECEIVER = "defaultReceiver";
    private static final String DEFAULT_ACTOR = "defaultActor";

    @MockBean
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void init() {
        paymentService = new PaymentService(paymentRepository);
    }

    private static Stream<Arguments> addAll_parametersForOk() {
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
    @DisplayName("PaymentService.addAll(correct list of payments for loading)")
    @MethodSource("addAll_parametersForOk")
    void addAll_ok(List<Payment> paymentsForLoading, List<Payment> loadedPayments) {
        Mockito.when(paymentRepository.create(any(String.class), any(String.class), any(ZonedDateTime.class), any(Double.class))).thenReturn(0L);
        assertIterableEquals(paymentService.addAll(paymentsForLoading), loadedPayments);
    }

    private static Stream<Arguments> addAll_parametersForFail() {
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
    @DisplayName("PaymentService.addAll(incorrect list of payments for loading)")
    @MethodSource("addAll_parametersForFail")
    void addAll_fail(List<Payment> paymentsForLoading) {
        assertThrows(IllegalArgumentException.class, () -> paymentService.addAll(paymentsForLoading));
    }

    private static Stream<Arguments> countBySender_parametersForOk() {
        return Stream.of(
                Arguments.of(DEFAULT_SENDER, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(DEFAULT_SENDER, ZonedDateTime.now(), null),
                Arguments.of(DEFAULT_SENDER, null, ZonedDateTime.now()),
                Arguments.of(DEFAULT_SENDER, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countBySender(correct sender; correct from date; correct to date)")
    @MethodSource("countBySender_parametersForOk")
    void countBySender_ok(String sender, ZonedDateTime from, ZonedDateTime to) {
        Mockito.when(paymentRepository.countBySender(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countBySenderReverse(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), isNull(), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countBySender(any(String.class))).thenReturn(10.0);
        assertEquals(paymentService.countBySender(sender, from, to), Double.valueOf(10.0));
    }

    private static Stream<Arguments> countBySender_parametersForFail() {
        return Stream.of(
                Arguments.of(null, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(null, null, ZonedDateTime.now()),
                Arguments.of(null, ZonedDateTime.now(), null),
                Arguments.of(null, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countBySender(incorrect sender; correct from date; correct to date)")
    @MethodSource("countBySender_parametersForFail")
    void countBySender_fail(String sender, ZonedDateTime from, ZonedDateTime to) {
        assertThrows(IllegalArgumentException.class, () -> paymentService.countBySender(sender, from, to));
    }

    private static Stream<Arguments> countByReceiver_parametersForOk() {
        return Stream.of(
                Arguments.of(DEFAULT_RECEIVER, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(DEFAULT_RECEIVER, ZonedDateTime.now(), null),
                Arguments.of(DEFAULT_RECEIVER, null, ZonedDateTime.now()),
                Arguments.of(DEFAULT_RECEIVER, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countByReceiver(correct receiver; correct from date; correct to date)")
    @MethodSource("countByReceiver_parametersForOk")
    void countByReceiver_ok(String receiver, ZonedDateTime from, ZonedDateTime to) {
        Mockito.when(paymentRepository.countByReceiver(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiverReverse(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), isNull(), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class))).thenReturn(10.0);
        assertEquals(paymentService.countByReceiver(receiver, from, to), Double.valueOf(10.0));
    }

    private static Stream<Arguments> countByReceiver_parametersForFail() {
        return Stream.of(
                Arguments.of(null, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(null, null, ZonedDateTime.now()),
                Arguments.of(null, ZonedDateTime.now(), null),
                Arguments.of(null, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countByReceiver(incorrect receiver; correct from date; correct to date)")
    @MethodSource("countByReceiver_parametersForFail")
    void countByReceiver_fail(String receiver, ZonedDateTime from, ZonedDateTime to) {
        assertThrows(IllegalArgumentException.class, () -> paymentService.countByReceiver(receiver, from, to));
    }

    private static Stream<Arguments> countBalance_parametersForOk() {
        return Stream.of(
                Arguments.of(DEFAULT_ACTOR, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(DEFAULT_ACTOR, ZonedDateTime.now(), null),
                Arguments.of(DEFAULT_ACTOR, null, ZonedDateTime.now()),
                Arguments.of(DEFAULT_ACTOR, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countBalance(correct actor; correct from date; correct to date)")
    @MethodSource("countBalance_parametersForOk")
    void countBalance_ok(String actor, ZonedDateTime from, ZonedDateTime to) {
        Mockito.when(paymentRepository.countBySender(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), any(ZonedDateTime.class))).thenReturn(5.0);
        Mockito.when(paymentRepository.countBySenderReverse(any(String.class), any(ZonedDateTime.class))).thenReturn(5.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), isNull(), isNull())).thenReturn(5.0);
        Mockito.when(paymentRepository.countBySender(any(String.class), isNull())).thenReturn(5.0);
        Mockito.when(paymentRepository.countBySender(any(String.class))).thenReturn(5.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiverReverse(any(String.class), any(ZonedDateTime.class))).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), isNull(), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class), isNull())).thenReturn(10.0);
        Mockito.when(paymentRepository.countByReceiver(any(String.class))).thenReturn(10.0);
        assertEquals(paymentService.countBalance(actor, from, to), Double.valueOf(5.0));
    }

    private static Stream<Arguments> countBalance_parametersForFail() {
        return Stream.of(
                Arguments.of(null, ZonedDateTime.now(), ZonedDateTime.now()),
                Arguments.of(null, null, ZonedDateTime.now()),
                Arguments.of(null, ZonedDateTime.now(), null),
                Arguments.of(null, null, null)
        );
    }

    @ParameterizedTest
    @DisplayName("PaymentService.countBalance(incorrect actor; correct from date; correct to date)")
    @MethodSource("countBalance_parametersForFail")
    void countBalance_fail(String actor, ZonedDateTime from, ZonedDateTime to) {
        assertThrows(IllegalArgumentException.class, () -> paymentService.countBalance(actor, from, to));
    }

}
