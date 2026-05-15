package com.van.micro_investment.mappers;

import com.van.micro_investment.dto.responses.AccountResponse;
import com.van.micro_investment.entities.Account;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    Account toEntity(AccountResponse accountResponse);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isDefault", source = "defaultAccount")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "balance", source = "balance")
    AccountResponse toDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account partialUpdate(AccountResponse accountResponse, @MappingTarget Account account);
}