package com.van.micro_investment.services.impl;

import com.van.micro_investment.dto.requests.CreateUserRequest;
import com.van.micro_investment.dto.responses.UserResponse;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.exceptions.InternalErrorException;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.repositories.UserAccountRepository;
import com.van.micro_investment.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
   private final UserAccountRepository userAccountRepository;

    @Transactional
    public UserAccount createUser(String email,String fullName) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new InternalErrorException("A user with this email address already exists : " + email);
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName(fullName);

        UserAccount saved = userAccountRepository.saveAndFlush(user);
        log.info("User saved with id : {}, email : {},full name : {}", saved.getId(), saved.getEmail(), saved.getFullName());
        return saved;
    }

    public UserAccount getById(UUID id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found : " + id));
    }
}
