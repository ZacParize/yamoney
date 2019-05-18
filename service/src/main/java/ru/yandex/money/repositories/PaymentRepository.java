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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.money.repositories.entities.Payment;

@Repository
@Transactional
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query(nativeQuery = true, value = "select insert_data(:_sender, :_receiver, :_create_date, :_amount )")
    long create( @Param("_sender") String sender
               , @Param("_receiver") String receiver
               , @Param("_create_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createDate
               , @Param("_amount") Double amount);

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_sender and :_from_date <= p.createDate and :_to_date >= p.createDate")
    Double countBySender( @Param("_sender") String sender
                        , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate
                        , @Param("_to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDate);

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_sender and :_from_date >= p.createDate")
    Double countBySenderReverse( @Param("_sender") String sender
                               , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate );

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_sender and :_from_date <= p.createDate")
    Double countBySender( @Param("_sender") String sender
            , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate );

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.sender = :_sender ")
    Double countBySender( @Param("_sender") String sender );

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_receiver and :_from_date <= p.createDate and :_to_date >= p.createDate")
    Double countByReceiver( @Param("_receiver") String receiver
                          , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate
                          , @Param("_to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDate);

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_receiver and :_from_date <= p.createDate")
    Double countByReceiver( @Param("_receiver") String receiver
                          , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_receiver and :_from_date >= p.createDate")
    Double countByReceiverReverse( @Param("_receiver") String receiver
                                 , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    @Transactional(readOnly = true)
    @Query("select sum(p.amount) from Payment p where p.receiver = :_receiver")
    Double countByReceiver( @Param("_receiver") String receiver );

    @Transactional(readOnly = true)
    @Query("select sum(r.amount) - sum(s.amount) from Payment r, Payment s where (r.receiver = :_actor and :_from_date <= r.createDate and :_to_date >= r.createDate) or (s.sender = :_actor and :_from_date <= s.createDate and :_to_date >= s.createDate)")
    Double countBalance( @Param("_actor") String actor
                       , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate
                       , @Param("_to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDate);

    @Transactional(readOnly = true)
    @Query("select sum(r.amount) - sum(s.amount) from Payment r, Payment s where (r.receiver = :_actor and :_from_date <= r.createDate) or (s.sender = :_actor and :_from_date <= s.createDate)")
    Double countBalance( @Param("_actor") String actor
                       , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    @Transactional(readOnly = true)
    @Query("select sum(r.amount) - sum(s.amount) from Payment r, Payment s where (r.receiver = :_actor and :_from_date >= r.createDate) or (s.sender = :_actor and :_from_date >= s.createDate)")
    Double countBalanceReverse( @Param("_actor") String actor
                              , @Param("_from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDate);

    @Transactional(readOnly = true)
    @Query("select sum(r.amount) - sum(s.amount) from Payment r, Payment s where r.receiver = :_actor or s.sender = :_actor")
    Double countBalance( @Param("_actor") String actor );

    @Modifying(clearAutomatically = true)
    void deleteById(long id);

}