package com.podsadowski.dynamicpricemanager.web;

import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void register_createsClientAccount() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@test.local")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertThat(userRepository.findByEmail("newuser@test.local")).isPresent();
    }

    @Test
    void register_rejectsDuplicateEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "dup2@test.local")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "dup2@test.local")
                        .param("password", "other")
                        .param("confirmPassword", "other"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void register_rejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "mismatch@test.local")
                        .param("password", "a")
                        .param("confirmPassword", "b"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }
}
