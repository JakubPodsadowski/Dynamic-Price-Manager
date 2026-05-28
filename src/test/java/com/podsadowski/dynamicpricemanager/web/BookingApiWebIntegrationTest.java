package com.podsadowski.dynamicpricemanager.web;

import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private com.podsadowski.dynamicpricemanager.repository.EmployeeRepository employeeRepository;

    private Long serviceId;
    private Long employeeId;

    @BeforeEach
    void seed() {
        var service = TestEntities.saveService(saloonServicesRepository, "API", 70, 60);
        serviceId = service.getId();
        employeeId = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "X", "Y", service).getId();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void availableSlots_badDate_returns400() throws Exception {
        mockMvc.perform(get("/api/booking/available-slots")
                        .param("employeeId", employeeId.toString())
                        .param("serviceId", serviceId.toString())
                        .param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void priceQuote_badTime_returns400() throws Exception {
        mockMvc.perform(get("/api/booking/price-quote")
                        .param("serviceId", serviceId.toString())
                        .param("date", "2026-06-01")
                        .param("time", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void priceQuote_unknownService_returns400() throws Exception {
        mockMvc.perform(get("/api/booking/price-quote")
                        .param("serviceId", "99999")
                        .param("date", "2026-06-01")
                        .param("time", "10:00"))
                .andExpect(status().isBadRequest());
    }
}
