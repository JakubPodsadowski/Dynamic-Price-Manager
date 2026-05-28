package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.repository.DynamicPricingSettingsRepository;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.PricingRuleRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DynamicPricingServiceIntegrationTest {

    @Autowired
    private DynamicPricingService dynamicPricingService;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PricingRuleRepository pricingRuleRepository;
    @Autowired
    private DynamicPricingSettingsRepository settingsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void ruleCrud_andQuoteWithSeededRules() {
        var service = TestEntities.saveService(saloonServicesRepository, "Kolor", 100, 60);
        var employee = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "A", "B", service);
        var client = TestEntities.saveClient(userRepository, passwordEncoder, "pricing-int@test.local");

        LocalDate friday = LocalDate.now().with(DayOfWeek.FRIDAY).plusWeeks(1);
        for (int i = 0; i < 5; i++) {
            TestEntities.saveReservation(reservationRepository, client, employee, service,
                    friday.minusWeeks(i), LocalTime.of(15, 0), ReservationStatus.CONFIRMED);
        }

        var quote = dynamicPricingService.quote(service.getId(), friday, LocalTime.of(15, 0));
        assertThat(quote.basePrice()).isEqualTo(100.0);

        var rules = dynamicPricingService.listRules();
        assertThat(rules).isNotEmpty();
        Long ruleId = rules.getFirst().getId();
        var patch = rules.getFirst();
        patch.setName("Zmieniona");
        dynamicPricingService.updateRule(ruleId, patch);
        assertThat(pricingRuleRepository.findById(ruleId).orElseThrow().getName()).isEqualTo("Zmieniona");

        dynamicPricingService.deleteRule(ruleId);
        assertThat(pricingRuleRepository.findById(ruleId)).isEmpty();

        var settings = dynamicPricingService.getSettings();
        settings.setHistoryLookbackWeeks(8);
        dynamicPricingService.saveSettings(settings);
        assertThat(settingsRepository.findById(settings.getId()).orElseThrow().getHistoryLookbackWeeks()).isEqualTo(8);
    }
}
