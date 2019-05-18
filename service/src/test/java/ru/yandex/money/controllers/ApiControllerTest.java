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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @Test
    @DisplayName("ApiController.load(correct request id; empty list of payments)")
    void load_listIsEmpty() {
        final List<Payment> emptyList = Collections.emptyList();
        Mockito.when(paymentService.addAll(null)).thenReturn(emptyList);
        Mockito.when(paymentService.addAll(Collections.emptyList())).thenReturn(emptyList);
        assertEquals(apiController.load(DEFAULT_REQ_ID, null).size(), 0);
        assertEquals(apiController.load(DEFAULT_REQ_ID, Collections.emptyList()).size(), 0);
    }

    @Test
    @DisplayName("ApiController.countBySender(correct request id; request is also correct)")
    void countBySender_ok() {
        final Double senderSpent = 100.99;
        Mockito.when(paymentService.countBySender(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(senderSpent);
        Mockito.when(paymentService.countBySender(any(String.class), isNull(), isNull())).thenReturn(senderSpent);

        final Map<String, String> request = new HashMap<>();
        request.put("sender", "sender");
        request.put("from", "2010-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("from", "");
        request.put("to", "");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("from", "");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("to", "");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("from", null);
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("to", null);
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);

        request.clear();
        request.put("sender", "sender");
        request.put("from", null);
        request.put("to", null);
        assertEquals(apiController.countBySender(DEFAULT_REQ_ID, request), senderSpent);
    }

    @Test
    @DisplayName("ApiController.countBySender(correct request id; request is bad)")
    void countBySender_badRequestParams() {
        final Map<String, String> request = new HashMap<>();
        request.put("sender", "sender");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2010-04-16T16:30:07.109+07:00");
        assertThrows(IllegalArgumentException.class, () -> apiController.countBySender(DEFAULT_REQ_ID, request));
    }

    @Test
    @DisplayName("ApiController.countByReceiver(correct request id; request is also correct)")
    void countByReceiver_ok() {
        final Double receiverGet = 100.99;
        Mockito.when(paymentService.countByReceiver(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countByReceiver(any(String.class), isNull(), isNull())).thenReturn(receiverGet);
        final Map<String, String> request = new HashMap<>();
        request.put("receiver", "receiver");
        request.put("from", "2010-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("from", "");
        request.put("to", "");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("from", "");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("to", "");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("from", null);
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("to", null);
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("receiver", "receiver");
        request.put("from", null);
        request.put("to", null);
        assertEquals(apiController.countByReceiver(DEFAULT_REQ_ID, request), receiverGet);
    }

    @Test
    @DisplayName("ApiController.countByReceiver(correct request id; request is bad)")
    void countByReceiver_badRequestParams() {
        final Map<String, String> request = new HashMap<>();
        request.put("receiver", "receiver");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2010-04-16T16:30:07.109+07:00");
        assertThrows(IllegalArgumentException.class, () -> apiController.countByReceiver(DEFAULT_REQ_ID, request));
    }

    @Test
    @DisplayName("ApiController.countBalance(correct request id; request is also correct)")
    void countBalance_allIsOk() {
        final Double receiverGet = 100.99;
        Mockito.when(paymentService.countBalance(any(String.class), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), any(ZonedDateTime.class), isNull())).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), isNull(), any(ZonedDateTime.class))).thenReturn(receiverGet);
        Mockito.when(paymentService.countBalance(any(String.class), isNull(), isNull())).thenReturn(receiverGet);
        final Map<String, String> request = new HashMap<>();
        request.put("actor", "actor");
        request.put("from", "2010-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2020-04-16T16:30:07.109+07:00");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("from", "");
        request.put("to", "");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("from", "");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("to", "");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("from", null);
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("to", null);
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);

        request.clear();
        request.put("actor", "actor");
        request.put("from", null);
        request.put("to", null);
        assertEquals(apiController.countBalance(DEFAULT_REQ_ID, request), receiverGet);
    }

    @Test
    @DisplayName("ApiController.countBalance(correct request id; request is bad)")
    void countBalance_badRequestParams() {
        final Map<String, String> request = new HashMap<>();
        request.put("actor", "actor");
        request.put("from", "2020-04-16T16:30:07.109+07:00");
        request.put("to", "2010-04-16T16:30:07.109+07:00");
        assertThrows(IllegalArgumentException.class, () -> apiController.countBalance(DEFAULT_REQ_ID, request));
    }

}