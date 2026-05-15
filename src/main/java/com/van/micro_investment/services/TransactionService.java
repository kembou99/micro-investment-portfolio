package com.van.micro_investment.services;

import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.requests.WithdrawRequest;
import com.van.micro_investment.dto.responses.WithdrawalResponse;
import com.van.micro_investment.entities.AccountTransaction;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TransactionService {
    AccountTransaction recordPurchase(RecordPurchaseRequest request);
    WithdrawalResponse withdraw(WithdrawRequest request);
    Page<AccountTransaction> getHistory(UUID accountId, int page, int size);
}
