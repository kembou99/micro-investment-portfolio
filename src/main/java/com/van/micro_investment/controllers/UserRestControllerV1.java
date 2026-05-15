package com.van.micro_investment.controllers;

import com.van.micro_investment.dto.requests.CreateUserRequest;
import com.van.micro_investment.dto.responses.UserResponse;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.mappers.UserMapper;
import com.van.micro_investment.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserRestControllerV1 {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserAccount user = userService.createUser(request.email(), request.fullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.INSTANCE.toDto(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserMapper.INSTANCE.toDto(userService.getById(id)));
    }
}
