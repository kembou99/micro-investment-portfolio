package com.van.micro_investment.controllers;

import com.van.micro_investment.dto.requests.RecordPurchaseRequest;
import com.van.micro_investment.dto.requests.WithdrawRequest;
import com.van.micro_investment.dto.responses.PaginationResponse;
import com.van.micro_investment.dto.responses.TransactionResponse;
import com.van.micro_investment.dto.responses.WithdrawalResponse;
import com.van.micro_investment.mappers.TransactionMapper;
import com.van.micro_investment.services.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions")
public class TransactionRestControllerV1 {
    private final TransactionService transactionService;

    @PostMapping("purchases")
    public ResponseEntity<TransactionResponse> recordPurchase(
            @Valid @RequestBody RecordPurchaseRequest request) {

        var transaction = transactionService.recordPurchase( request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionMapper.INSTANCE.toDto(transaction));
    }

    @PostMapping("withdraw")
    public ResponseEntity<WithdrawalResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request) {

        WithdrawalResponse response = transactionService.withdraw(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<PaginationResponse<TransactionResponse>> getTransactionHistory(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
       var pages= transactionService.getHistory(accountId,page,size);
       var response = PaginationResponse.<TransactionResponse>builder()
               .page(page)
               .size(size)
               .totalElements(pages.getTotalElements())
               .totalPages(pages.getTotalPages())
               .content(pages.getContent().stream().map(TransactionMapper.INSTANCE::toDto).toList())
               .build();
        return ResponseEntity.ok(response);
    }
}
