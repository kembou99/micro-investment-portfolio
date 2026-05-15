package com.van.micro_investment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.van.micro_investment.dto.requests.CreateUserRequest;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.exceptions.GlobalExceptionHandler;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.mappers.UserMapper;
import com.van.micro_investment.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestControllerV1.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("UserRestControllerV1")
class UserRestControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // ─────────────────── POST /api/v1/users ───────

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUserTests {

        @Test
        @DisplayName("201 – crée l'utilisateur avec des données valides")
        void createUser_validRequest_returns201() throws Exception {
            CreateUserRequest req = new CreateUserRequest("alice@test.com", "Alice Martin");

            UserAccount saved = new UserAccount();
            saved.setEmail("alice@test.com");
            saved.setFullName("Alice Martin");

            when(userService.createUser("alice@test.com", "Alice Martin")).thenReturn(saved);

            mockMvc.perform(post("/api/v1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("alice@test.com"))
                    .andExpect(jsonPath("$.fullName").value("Alice Martin"));
        }

        @Test
        @DisplayName("400 – email manquant → erreur de validation")
        void createUser_missingEmail_returns400() throws Exception {
            CreateUserRequest req = new CreateUserRequest("", "Alice Martin");

            mockMvc.perform(post("/api/v1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 – email invalide → erreur de validation")
        void createUser_invalidEmail_returns400() throws Exception {
            CreateUserRequest req = new CreateUserRequest("not-an-email", "Alice Martin");

            mockMvc.perform(post("/api/v1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 – fullName manquant → erreur de validation")
        void createUser_missingFullName_returns400() throws Exception {
            CreateUserRequest req = new CreateUserRequest("alice@test.com", "");

            mockMvc.perform(post("/api/v1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────── GET /api/v1/users/{id} ───

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("200 – retourne l'utilisateur existant")
        void getUserById_existing_returns200() throws Exception {
            UUID id = UUID.randomUUID();

            UserAccount user = new UserAccount();
            user.setEmail("bob@test.com");
            user.setFullName("Bob Dupont");

            when(userService.getById(id)).thenReturn(user);

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("bob@test.com"))
                    .andExpect(jsonPath("$.fullName").value("Bob Dupont"));
        }

        @Test
        @DisplayName("404 – utilisateur introuvable")
        void getUserById_notFound_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.getById(id))
                    .thenThrow(new ResourceNotFoundException("User not found : " + id));

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }
}
