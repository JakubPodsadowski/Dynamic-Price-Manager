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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminReservationWebIntegrationTest {

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

    private Long pendingId;

    @BeforeEach
    void seed() {
        var service = TestEntities.saveService(saloonServicesRepository, "Test", 50, 60);
        var employee = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "A", "B", service);
        var client = TestEntities.saveClient(userRepository, passwordEncoder, "filter-client@test.local");

        var inRange = TestEntities.saveReservation(reservationRepository, client, employee, service,
                LocalDate.of(2026, 6, 10), LocalTime.of(10, 0), ReservationStatus.PENDING);
        pendingId = inRange.getId();
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                LocalDate.of(2026, 8, 1), LocalTime.of(11, 0), ReservationStatus.CONFIRMED);
    }

    @Test
    @WithMockUser(username = "admin@system.com", roles = "ADMIN")
    void list_filtersByDateRange() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                        .param("dateFrom", "2026-06-10")
                        .param("dateTo", "2026-06-10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-06-10")))
                .andExpect(content().string(not(containsString("2026-08-01"))));
    }

    @Test
    @WithMockUser(username = "admin@system.com", roles = "ADMIN")
    void list_showsCancelForPendingAndConfirmed() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                        .param("dateFrom", "2026-06-10")
                        .param("dateTo", "2026-08-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fas fa-times")));
    }

    @Test
    @WithMockUser(username = "admin@system.com", roles = "ADMIN")
    void cancel_marksReservationCancelled() throws Exception {
        mockMvc.perform(post("/admin/reservations/{id}/cancel", pendingId)
                        .param("dateFrom", "2026-06-10")
                        .param("dateTo", "2026-06-10")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());

        var updated = reservationRepository.findById(pendingId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @WithMockUser(username = "admin@system.com", roles = "ADMIN")
    void confirm_preservesDateFiltersOnRedirect() throws Exception {
        mockMvc.perform(post("/admin/reservations/{id}/confirm", pendingId)
                        .param("dateFrom", "2026-06-10")
                        .param("dateTo", "2026-06-10")
                        .param("status", "ALL")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrlPattern("/admin/reservations?dateFrom=2026-06-10&dateTo=2026-06-10"));
    }
}
