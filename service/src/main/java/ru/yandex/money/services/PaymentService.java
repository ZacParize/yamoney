/*
 * PaymentService.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.money.repositories.PaymentRepository;
import ru.yandex.money.repositories.entities.Payment;

@Service
@Transactional
@Slf4j
public class PaymentService {

    /*const uses in unit tests*/
    public static final int MAX_BATCH_SIZE = 500;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepository = paymentRepo;
    }

    public List<Payment> addAll(List<Payment> listOfPayments) {
        if (CollectionUtils.isEmpty(listOfPayments)) {
            return Collections.emptyList();
        }
        if (listOfPayments.size() > MAX_BATCH_SIZE ) {
            throw new IllegalArgumentException("Too big butch size = " + listOfPayments.size());
        }
        final List<Payment> resultListOfPayments = new ArrayList<>(listOfPayments.size());
        listOfPayments.forEach(payment -> {
            long id = paymentRepository.create( payment.getSender()
                                              , payment.getReceiver()
                                              , payment.getCreateDate()
                                              , payment.getAmount().doubleValue());
            resultListOfPayments.add(new Payment(id, payment.getSender(), payment.getReceiver(), payment.getCreateDate(), payment.getAmount()));
            });
        return resultListOfPayments;
    }

    @Transactional(readOnly = true)
    public Double countBySender(String sender, ZonedDateTime from, ZonedDateTime to) {
        if (StringUtils.isBlank(sender)) {
            throw new IllegalArgumentException("Sender is empty in \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" request");
        }
        Double result;
        if (from != null && to != null) {
            result = paymentRepository.countBySender(sender, from, to);
        } else if (from != null) {
            result = paymentRepository.countBySender(sender, from);
        } else if (to != null) {
            result = paymentRepository.countBySenderReverse(sender, to);
        } else {
            result = paymentRepository.countBySender(sender);
        }
        return result == null ? NumberUtils.DOUBLE_ZERO : result;
    }

    @Transactional(readOnly = true)
    public Double countByReceiver(String receiver, ZonedDateTime from, ZonedDateTime to) {
        if (StringUtils.isBlank(receiver)) {
            throw new IllegalArgumentException("Receiver is empty in \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" request");
        }
        Double result;
        if (from != null && to != null) {
            result = paymentRepository.countByReceiver(receiver, from, to);
        } else if (from != null) {
            result = paymentRepository.countByReceiver(receiver, from);
        } else if (to != null) {
            result = paymentRepository.countByReceiverReverse(receiver, to);
        } else {
            result = paymentRepository.countByReceiver(receiver);
        }
        return result == null ? NumberUtils.DOUBLE_ZERO : result;
    }

    @Transactional(readOnly = true)
    public Double countBalance(String actor, ZonedDateTime from, ZonedDateTime to) {
        if (StringUtils.isBlank(actor)) {
            throw new IllegalArgumentException("Actor is empty in \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" request");
        }
        Double result;
        if (from != null && to != null) {
            result = ObjectUtils.firstNonNull(paymentRepository.countByReceiver(actor, from, to), NumberUtils.DOUBLE_ZERO) - ObjectUtils.firstNonNull(paymentRepository.countBySender(actor, from, to), NumberUtils.DOUBLE_ZERO);
        } else if (from != null) {
            result = ObjectUtils.firstNonNull(paymentRepository.countByReceiver(actor, from), NumberUtils.DOUBLE_ZERO) - ObjectUtils.firstNonNull(paymentRepository.countBySender(actor, from), NumberUtils.DOUBLE_ZERO);
        } else if (to != null) {
            result = ObjectUtils.firstNonNull(paymentRepository.countByReceiverReverse(actor, to), NumberUtils.DOUBLE_ZERO) - ObjectUtils.firstNonNull(paymentRepository.countBySenderReverse(actor, to), NumberUtils.DOUBLE_ZERO);
        } else {
            result = ObjectUtils.firstNonNull(paymentRepository.countByReceiver(actor), NumberUtils.DOUBLE_ZERO) - ObjectUtils.firstNonNull(paymentRepository.countBySender(actor), NumberUtils.DOUBLE_ZERO);
        }
        return result;
    }

}
