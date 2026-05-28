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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientBookingWebIntegrationTest {

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

    private Long serviceId;
    private Long employeeId;

    @BeforeEach
    void seed() {
        var service = TestEntities.saveService(saloonServicesRepository, "Haircut", 80, 60);
        serviceId = service.getId();
        var employee = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "Anna", "Nowak", service);
        employeeId = employee.getId();
        TestEntities.saveClient(userRepository, passwordEncoder, "booking-client@test.local");
    }

    @Test
    @WithMockUser(username = "booking-client@test.local", roles = "CLIENT")
    void book_unknownService_redirectsWithError() throws Exception {
        mockMvc.perform(get("/client/book").param("serviceId", "999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client"));
    }

    @Test
    @WithMockUser(username = "booking-client@test.local", roles = "CLIENT")
    void clientPanel_listsServices() throws Exception {
        mockMvc.perform(get("/client"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Haircut")));
    }

    @Test
    @WithMockUser(username = "booking-client@test.local", roles = "CLIENT")
    void bookingApi_returnsAvailableSlots() throws Exception {
        LocalDate date = LocalDate.now().plusDays(7);
        mockMvc.perform(get("/api/booking/available-slots")
                        .param("employeeId", employeeId.toString())
                        .param("serviceId", serviceId.toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("slots")));
    }

    @Test
    @WithMockUser(username = "booking-client@test.local", roles = "CLIENT")
    void reserve_createsPendingReservation() throws Exception {
        LocalDate date = LocalDate.now().plusDays(7);
        mockMvc.perform(post("/client/reserve")
                        .with(csrf())
                        .param("serviceId", serviceId.toString())
                        .param("employeeId", employeeId.toString())
                        .param("date", date.toString())
                        .param("startTime", "09:00")
                        .param("contactFirstName", "Jan")
                        .param("contactLastName", "Kowalski")
                        .param("contactEmail", "booking-client@test.local")
                        .param("contactPhone", "500600700"))
                .andExpect(status().is3xxRedirection());

        var list = reservationRepository.findAll();
        assertThat(list).anyMatch(r ->
                r.getClient().getEmail().equals("booking-client@test.local")
                        && r.getStatus() == ReservationStatus.PENDING
                        && r.getReservationDate().equals(date));
    }
}
