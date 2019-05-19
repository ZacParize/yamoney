/*
 * ApiController.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.money.repositories.entities.Payment;
import ru.yandex.money.services.PaymentService;

@CrossOrigin
@Validated
@RestController
@Slf4j
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiController {

    private final PaymentService paymentService;

    /**
     * Inner class for date range
     */
    @Getter
    private class DateRange {

        private final ZonedDateTime from;

        private final ZonedDateTime to;

        DateRange(String from, String to) {
            ZonedDateTime tempFrom = StringUtils.isNotBlank(from) ? ZonedDateTime.parse(from, DateTimeFormatter.ISO_DATE_TIME) : null;
            ZonedDateTime tempTo = StringUtils.isNotBlank(to) ? ZonedDateTime.parse(to, DateTimeFormatter.ISO_DATE_TIME) : null;
            if (tempFrom != null && tempTo != null && tempTo.isBefore(tempFrom)) {
                throw new IllegalArgumentException("Incorrect time interval: end date \"" + tempTo.format(DateTimeFormatter.ISO_DATE_TIME) + "\" is less then start date \"" +  tempFrom.format(DateTimeFormatter.ISO_DATE_TIME) + "\"");
            }
            this.from = tempFrom;
            this.to = tempTo;
        }

    }

    public ApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Loads list of payments
     * @param reqId request id
     * @param listOfPayments list of payments for loading
     * @return list of loaded payments
     * @see PaymentService#addAll(List)
     */
    @PostMapping("payment/load")
    public List<Payment> load( @RequestParam("req_id") @NotBlank String reqId
                             , @RequestBody List<Payment> listOfPayments) {
        final List<Payment> loadedPayments = paymentService.addAll(listOfPayments);
        log.info("Request \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" №" + reqId + " has successfully processed");
        return loadedPayments;
    }

    /**
     * Returns sum of payments by actor for period
     * @param reqId request id
     * @param actor payment sender
     * @param request request contains start and end date of period
     * @return sum of payments
     * @see PaymentService#countBySender(String, ZonedDateTime, ZonedDateTime)
     */
    @PostMapping("payment/count-by-sender/{actor}")
    public Double countBySender( @PathVariable("actor") @NotBlank String actor
                               , @RequestParam("req_id") @NotBlank String reqId
                               , @RequestBody Map<String, String> request ) {
        final DateRange dateRange = new DateRange(request.get("from"), request.get("to"));
        final Double payedBySender = paymentService.countBySender(actor, dateRange.getFrom(), dateRange.getTo());
        log.info("Request \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" №" + reqId + " has successfully processed");
        return payedBySender;
    }

    /**
     * Returns sum of payments by actor for period
     * @param reqId request id
     * @param actor payment sender
     * @param request request contains start and end date of period
     * @return sum of payments
     * @see PaymentService#countByReceiver(String, ZonedDateTime, ZonedDateTime)
     */
    @PostMapping("payment/count-by-receiver/{actor}")
    public Double countByReceiver( @PathVariable("actor") @NotBlank String actor
                                 , @RequestParam("req_id") @NotBlank String reqId
                                 , @RequestBody Map<String, String> request ) {
        final DateRange dateRange = new DateRange(request.get("from"), request.get("to"));
        final Double earnedByReceiver = paymentService.countByReceiver(actor, dateRange.getFrom(), dateRange.getTo());
        log.info("Request \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" №" + reqId + " has successfully processed");
        return earnedByReceiver;
    }

    /**
     * Returns balance of payments by actor for period
     * @param reqId request id
     * @param actor payment sender
     * @param request request contains start and end date of period
     * @return balance of payments
     * @see PaymentService#countBalance(String, ZonedDateTime, ZonedDateTime)
     */
    @PostMapping("payment/count-balance/{actor}")
    public Double countBalance( @PathVariable("actor") @NotBlank String actor
                              , @RequestParam("req_id") @NotBlank String reqId
                              , @RequestBody Map<String, String> request) {
        final DateRange dateRange = new DateRange(request.get("from"), request.get("to"));
        final Double balanceByActor = paymentService.countBalance(actor, dateRange.getFrom(), dateRange.getTo());
        log.info("Request \"" + Thread.currentThread().getStackTrace()[1].getMethodName() + "\" №" + reqId + " has successfully processed");
        return balanceByActor;
    }

    @ExceptionHandler(Exception.class)
    public String processException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        String reqId = request.getParameter("req_id");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        reqId = StringUtils.isBlank(reqId) ? "№[empty req_id]" : "№" + reqId;
        log.error("Error during request {} processing: {}", reqId, e.getLocalizedMessage());
        return "Internal service error during request " + reqId + " processing";
    }
}
