package com.tripflow.ai.planner.hotel.dao;

import org.apache.ibatis.annotations.Mapper;
import com.tripflow.ai.planner.hotel.dto.entity.PaymentTransaction;
import com.tripflow.ai.planner.hotel.dto.request.PaymentTransactionRequest;

@Mapper
public interface PaymentTransactionDao {
    PaymentTransaction selectPaymentTransactionById(Long id);
    void insertPaymentTransaction(PaymentTransactionRequest request);
    void updatePaymentTransaction(PaymentTransactionRequest request);
    void deletePaymentTransactionById(Long id);
}
