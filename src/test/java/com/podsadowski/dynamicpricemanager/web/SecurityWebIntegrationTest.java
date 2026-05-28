package com.podsadowski.dynamicpricemanager.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminReservations_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void adminReservations_forbiddenForClient() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminReservations_okForAdmin() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}

