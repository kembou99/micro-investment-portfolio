package com.van.micro_investment.services;

import com.van.micro_investment.entities.UserAccount;

import java.util.List;
import java.util.UUID;

public interface UserService {
     UserAccount createUser(String email, String fullName);
     UserAccount getById(UUID id);
}
