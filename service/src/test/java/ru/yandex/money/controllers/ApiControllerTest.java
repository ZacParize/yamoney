/*
 * ApiControllerTest.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.controllers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import ru.yandex.money.repositories.entities.Payment;
import ru.yandex.money.services.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

class ApiControllerTest {

    private static final String DEFAULT_REQ_ID = "defaultReqId";
    private static final String DEFAULT_ACTOR = "defaultActor";
    private PaymentService paymentService;
    private ApiController apiController;

    @BeforeEach
    void init() {
        paymentService = Mockito.mock(PaymentService.class);
        apiController = new ApiController(paymentService);
    }

    @Test
    @DisplayName("ApiController.load(correct request id; list is correct or too big)")
    void load_ok() {
        final List<Payment> emptyList = Collections.emptyList();
        Mockito.when(paymentService.addAll(null)).thenReturn(emptyList);
        Mockito.when(paymentService.addAll(Collections.emptyList())).thenReturn(emptyList);
        assertEquals(apiController.load(DEFAULT_REQ_ID, null).size(), 0);
        assertEquals(apiController.load(DEFAULT_REQ_ID, Collections.emptyList()).size(), 0);

        final Payment emptyPayment = new Payment();
        final List<Payment> listForLoading = new ArrayList<>(PaymentService.MAX_BATCH_SIZE);
        List<Payment> listOfLoadedPayments = new ArrayList<>(PaymentService.MAX_BATCH_SIZE);
        for (int idx = 0; idx < PaymentService.MAX_BATCH_SIZE; idx++) {
            listForLoading.add(emptyPayment);
            listOfLoadedPayments.add(emptyPayment);
        }
        Mockito.when(paymentService.addAll(listForLoading)).thenReturn(listOfLoadedPayments);
        assertIterableEquals(apiController.load(DEFAULT_REQ_ID, listForLoading), listOfLoadedPayments);
    }

    private static Stream<Arguments> goodRequests() {
        return Stream.of(
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "2010-04-16T16:30:07.109+07:00");
                    put("to", "2020-04-16T16:30:07.109+07:00");
                  }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "2020-04-16T16:30:07.109+07:00");
                    put("to", "2020-04-16T16:30:07.109+07:00");
                  }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "");
                    put("to", "");
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "");
                    put("to", null);
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", null);
                    put("to", "");
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", null);
                    put("to", null);
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", null);
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "");
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("to", null);
                }}),
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("to", "");
                }})
               );
    }

    private static Stream<Arguments> badRequests() {
        return Stream.of(
                Arguments.of( new LinkedHashMap<String, String>() {{
                    put("from", "2020-04-16T16:30:07.109+07:00");
                    put("to", "2010-04-16T16:30:07.109+07:00");
                }})
        );
    }

    @ParameterizedTest
    @MethodSource("goodRequests")
    @DisplayName("ApiController.countBySender(correct actor; correct request id; request is also correct)")
    void countBySender_ok(Map<String, String> request) {
        final Double senderSpent = 100.99;
        Mockito.when(paymentService.countBySender(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), isNull(), isNull())).thenReturn(senderSpent);
        assertEquals(apiController.countBySender(DEFAULT_ACTOR, DEFAULT_REQ_ID, request), senderSpent);
    }

    @ParameterizedTest
    @MethodSource("badRequests")
    @DisplayName("ApiController.countBySender(correct actor; correct request id; request is bad)")
    void countBySender_badRequest(Map<String, String> request) {
        assertThrows(IllegalArgumentException.class, () -> apiController.countBySender(DEFAULT_ACTOR, DEFAULT_REQ_ID, request));
    }

    @ParameterizedTest
    @MethodSource("goodRequests")
    @DisplayName("ApiController.countByReceiver(correct actor; correct request id; request is also correct)")
    void countByReceiver_ok(Map<String, String> request) {
        final Double receiverGet = 100.99;
        Mockito.when(paymentService.countByReceiver(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), isNull(), isNull())).thenReturn(receiverGet);
        assertEquals(apiController.countByReceiver(DEFAULT_ACTOR, DEFAULT_REQ_ID, request), receiverGet);
    }

    @ParameterizedTest
    @MethodSource("badRequests")
    @DisplayName("ApiController.countByReceiver(correct actor; correct request id; request is bad)")
    void countByReceiver_badRequest(Map<String, String> request) {
        assertThrows(IllegalArgumentException.class, () -> apiController.countByReceiver(DEFAULT_ACTOR, DEFAULT_REQ_ID, request));
    }

    @ParameterizedTest
    @MethodSource("goodRequests")
    @DisplayName("ApiController.countBalance(correct actor; correct request id; request is also correct)")
    void countBalance_ok(Map<String, String> request) {
        final Double receiverGet = 100.99;
        Mockito.when(paymentService.countBalance(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), isNull(), isNull())).thenReturn(receiverGet);
        assertEquals(apiController.countBalance(DEFAULT_ACTOR, DEFAULT_REQ_ID, request), receiverGet);
    }

    @ParameterizedTest
    @MethodSource("badRequests")
    @DisplayName("ApiController.countBalance(correct actor; correct request id; request is bad)")
    void countBalance_badRequest(Map<String, String> request) {
        assertThrows(IllegalArgumentException.class, () -> apiController.countBalance(DEFAULT_ACTOR, DEFAULT_REQ_ID, request));
    }

}