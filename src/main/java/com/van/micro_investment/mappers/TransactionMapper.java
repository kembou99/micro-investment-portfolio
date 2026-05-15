package com.van.micro_investment.mappers;

import com.van.micro_investment.dto.responses.TransactionResponse;
import com.van.micro_investment.entities.AccountTransaction;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);


    AccountTransaction toEntity(TransactionResponse transactionResponse);

    @Mapping(target = "accountId", source = "account.id")
    TransactionResponse toDto(AccountTransaction accountTransaction);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AccountTransaction partialUpdate(TransactionResponse transactionResponse, @MappingTarget AccountTransaction accountTransaction);
}