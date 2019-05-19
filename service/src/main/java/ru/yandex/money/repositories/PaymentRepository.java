/*
 * CategoryRepo.java
 *
 * Copyright 2017-2019 BCS-Technologies. All Rights Reserved.
 *
 * This software is the proprietary information of BCS-Technologies.
 * Use is subject to license terms.
 */

package ru.yandex.money.repositories;

import java.time.ZonedDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.money.repositories.entities.Payment;

/**
 * Payment specific version of {@link org.springframework.data.jpa.repository.JpaRepository}.
 */
@Repository
@Transactional
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Inserts new record in database with some validation
     * @param sender payment sender
     * @param receiver payment receiver
     * @param createDate payment execution date
     * @param amount payment amount
     * @return id of new record
     */
    @Query(nativeQuery = true, value = "select insert_data(:_sender, :_receiver, :_create_date, :_amount )")
    long create( @Param("_sender") String sender
               , @Param("_receiver") String receiver
               , @Param("_create_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createDate
               , @Param("_amount") Double amount);

    /**
     * Returns sum of payments by sender for period of time
     * @param actor payment sender
     * @param fromDate start date of period
     * @param toDate end date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_actor and :_from_date <= p.createDate and :_to_date >= p.createDate")
    Double countBySender( @Param("_actor") String actor
                        , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate
                        , @Param("_to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDate);

    /**
     * Returns sum of payments by sender from date reverse
     * @param actor payment sender
     * @param fromDate start date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_actor and :_from_date >= p.createDate")
    Double countBySenderReverse( @Param("_actor") String actor
                               , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate );

    /**
     * Returns sum of payments by sender from date
     * @param actor payment sender
     * @param fromDate start date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_actor and :_from_date <= p.createDate")
    Double countBySender( @Param("_actor") String actor
                        , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate );

    /**
     * Returns sum of payments by sender from all time
     * @param actor payment sender
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_actor ")
    Double countBySender( @Param("_actor") String actor );

    /**
     * Returns sum of payments by receiver for period of time
     * @param actor payment sender
     * @param fromDate start date of period
     * @param toDate end date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_actor and :_from_date <= p.createDate and :_to_date >= p.createDate")
    Double countByReceiver( @Param("_actor") String actor
                          , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate
                          , @Param("_to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDate);

    /**
     * Returns sum of payments by receiver from date
     * @param actor payment sender
     * @param fromDate start date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_actor and :_from_date <= p.createDate")
    Double countByReceiver( @Param("_actor") String actor
                          , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    /**
     * Returns sum of payments by receiver from date reverse
     * @param actor payment sender
     * @param fromDate start date of period
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_actor and :_from_date >= p.createDate")
    Double countByReceiverReverse( @Param("_actor") String actor
                                 , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    /**
     * Returns sum of payments by receiver for all time
     * @param actor payment sender
     * @return sum of payments
     */
    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_actor")
    Double countByReceiver( @Param("_actor") String actor );

}