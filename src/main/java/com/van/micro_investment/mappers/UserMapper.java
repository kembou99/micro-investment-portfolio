package com.van.micro_investment.mappers;

import com.van.micro_investment.dto.responses.UserResponse;
import com.van.micro_investment.entities.UserAccount;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    UserAccount toEntity(UserResponse userResponse);

    UserResponse toDto(UserAccount userAccount);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserAccount partialUpdate(UserResponse userResponse, @MappingTarget UserAccount userAccount);
}