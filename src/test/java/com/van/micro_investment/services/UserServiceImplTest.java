package com.van.micro_investment.services;

import com.van.micro_investment.dto.requests.CreateUserRequest;
import com.van.micro_investment.entities.UserAccount;
import com.van.micro_investment.exceptions.InternalErrorException;
import com.van.micro_investment.exceptions.ResourceNotFoundException;
import com.van.micro_investment.repositories.UserAccountRepository;
import com.van.micro_investment.services.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // ─────────────────── createUser ───────────────

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("crée et retourne un utilisateur quand l'email n'existe pas")
        void createUser_newEmail_savesAndReturnsUser() {
            String email    = "alice@test.com";
            String fullName = "Alice Martin";

            when(userAccountRepository.existsByEmail(email)).thenReturn(false);

            UserAccount saved = new UserAccount();
            saved.setEmail(email);
            saved.setFullName(fullName);

            when(userAccountRepository.saveAndFlush(any(UserAccount.class))).thenReturn(saved);

            UserAccount result = userService.createUser(email, fullName);

            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getFullName()).isEqualTo(fullName);
            verify(userAccountRepository).saveAndFlush(any(UserAccount.class));
        }

        @Test
        @DisplayName("lève InternalErrorException si l'email existe déjà")
        void createUser_duplicateEmail_throwsInternalError() {
            String email = "exists@test.com";
            when(userAccountRepository.existsByEmail(email)).thenReturn(true);

            assertThatExceptionOfType(InternalErrorException.class)
                    .isThrownBy(() -> userService.createUser(email, "Any Name"))
                    .withMessageContaining(email);

            verify(userAccountRepository, never()).saveAndFlush(any());
        }
    }

    // ─────────────────── getById ──────────────────

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("retourne l'utilisateur quand il existe")
        void getById_existingId_returnsUser() {
            UUID id   = UUID.randomUUID();
            UserAccount user = new UserAccount();
            user.setEmail("bob@test.com");

            when(userAccountRepository.findById(id)).thenReturn(Optional.of(user));

            UserAccount result = userService.getById(id);

            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("lève ResourceNotFoundException quand l'utilisateur est introuvable")
        void getById_unknownId_throwsResourceNotFound() {
            UUID id = UUID.randomUUID();
            when(userAccountRepository.findById(id)).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> userService.getById(id))
                    .withMessageContaining(id.toString());
        }
    }
}
