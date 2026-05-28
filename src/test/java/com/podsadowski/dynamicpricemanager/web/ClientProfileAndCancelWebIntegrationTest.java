package com.podsadowski.dynamicpricemanager.web;

import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientProfileAndCancelWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long reservationId;

    @BeforeEach
    void seed() {
        TestEntities.saveClient(userRepository, passwordEncoder, "profile-client@test.local");
        var service = TestEntities.saveService(saloonServicesRepository, "S", 50, 60);
        var employee = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "E", "M", service);
        var client = userRepository.findByEmail("profile-client@test.local").orElseThrow();
        reservationId = TestEntities.saveReservation(reservationRepository, client, employee, service,
                LocalDate.now().plusDays(3), LocalTime.of(11, 0), ReservationStatus.CONFIRMED).getId();
    }

    @Test
    @WithMockUser(username = "profile-client@test.local", roles = "CLIENT")
    void profile_viewAndUpdate() throws Exception {
        mockMvc.perform(get("/client/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("client-profile"));

        mockMvc.perform(post("/client/profile")
                        .with(csrf())
                        .param("firstName", "Ewa")
                        .param("lastName", "Nowak")
                        .param("phoneNumber", "600700800"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/profile?success"));
    }

    @Test
    @WithMockUser(username = "profile-client@test.local", roles = "CLIENT")
    void reservations_listAndCancel() throws Exception {
        mockMvc.perform(get("/client/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("client-reservations"))
                .andExpect(content().string(containsString("fas fa-times")));

        mockMvc.perform(post("/client/reservations/{id}/cancel", reservationId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/reservations"));
    }
}
