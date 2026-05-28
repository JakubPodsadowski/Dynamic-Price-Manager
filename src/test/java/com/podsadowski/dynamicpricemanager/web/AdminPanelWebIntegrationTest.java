package com.podsadowski.dynamicpricemanager.web;

import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminPanelWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void services_addValidationError() throws Exception {
        mockMvc.perform(post("/admin/services/add")
                        .with(csrf())
                        .param("name", "")
                        .param("price", "10")
                        .param("duration", "30")
                        .param("description", "x"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-services"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void services_listAddEditDelete() throws Exception {
        mockMvc.perform(get("/admin/services"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-services"));

        mockMvc.perform(post("/admin/services/add")
                        .with(csrf())
                        .param("name", "New service")
                        .param("price", "99.5")
                        .param("duration", "45")
                        .param("description", "Test description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services?success"));

        Long id = saloonServicesRepository.findAll().stream()
                .filter(s -> "New service".equals(s.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(post("/admin/services/edit")
                        .with(csrf())
                        .param("id", id.toString())
                        .param("name", "Updated service")
                        .param("price", "110")
                        .param("duration", "50")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/admin/services/delete/{id}", id).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void employees_validationError() throws Exception {
        mockMvc.perform(post("/admin/employees/save")
                        .with(csrf())
                        .param("firstName", "")
                        .param("lastName", "X")
                        .param("workDayStart", "09:00")
                        .param("workDayEnd", "17:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-employees"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void employees_saveAndList() throws Exception {
        var service = TestEntities.saveService(saloonServicesRepository, "For employee", 40, 30);

        mockMvc.perform(get("/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-employees"));

        mockMvc.perform(post("/admin/employees/save")
                        .with(csrf())
                        .param("firstName", "Kasia")
                        .param("lastName", "Test")
                        .param("specialization", "Stylist")
                        .param("workDayStart", "09:00")
                        .param("workDayEnd", "17:00")
                        .param("serviceIds", service.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/employees"));

        mockMvc.perform(get("/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kasia")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pricing_pageAndSettings() throws Exception {
        mockMvc.perform(get("/admin/pricing"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-pricing"));

        mockMvc.perform(post("/admin/pricing/settings")
                        .with(csrf())
                        .param("historyLookbackWeeks", "6")
                        .param("busyIfTotalReservationsGte", "5")
                        .param("quietIfTotalReservationsLte", "2")
                        .param("lastMinuteWithinHours", "12")
                        .param("lastMinuteDiscountPercent", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pricing"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pricing_addAndDeleteRule() throws Exception {
        mockMvc.perform(post("/admin/pricing/rules")
                        .with(csrf())
                        .param("enabled", "true")
                        .param("name", "Test rule")
                        .param("dayOfWeek", "MONDAY")
                        .param("windowStart", "10:00")
                        .param("windowEnd", "12:00")
                        .param("kind", "QUIET_DISCOUNT")
                        .param("percent", "5"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin/pricing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Test rule")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pricing_quoteJson() throws Exception {
        var service = TestEntities.saveService(saloonServicesRepository, "Pricing", 50, 60);
        mockMvc.perform(get("/admin/pricing/quote")
                        .param("serviceId", service.getId().toString())
                        .param("date", "2026-06-15")
                        .param("time", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("finalPrice")));
    }
}
