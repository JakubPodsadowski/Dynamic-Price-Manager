package com.podsadowski.dynamicpricemanager.config;

import com.podsadowski.dynamicpricemanager.entity.DynamicPricingSettings;
import com.podsadowski.dynamicpricemanager.entity.PricingRule;
import com.podsadowski.dynamicpricemanager.entity.PricingRuleKind;
import com.podsadowski.dynamicpricemanager.repository.DynamicPricingSettingsRepository;
import com.podsadowski.dynamicpricemanager.repository.PricingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Configuration
public class PricingDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PricingDataInitializer.class);

    @Bean
    public CommandLineRunner initDynamicPricing(
            DynamicPricingSettingsRepository settingsRepository,
            PricingRuleRepository pricingRuleRepository) {
        return args -> {
            if (settingsRepository.findById(DynamicPricingSettings.SINGLETON_ID).isEmpty()) {
                DynamicPricingSettings s = new DynamicPricingSettings();
                s.setId(DynamicPricingSettings.SINGLETON_ID);
                settingsRepository.save(s);
                log.info("Initialized dynamic pricing settings (singleton id=1).");
            }
            if (pricingRuleRepository.count() == 0) {
                PricingRule fridayBusy = new PricingRule();
                fridayBusy.setName("Friday 14–17 — busy surcharge");
                fridayBusy.setDayOfWeek(DayOfWeek.FRIDAY);
                fridayBusy.setWindowStart(LocalTime.of(14, 0));
                fridayBusy.setWindowEnd(LocalTime.of(17, 0));
                fridayBusy.setKind(PricingRuleKind.BUSY_SURCHARGE);
                fridayBusy.setPercent(15);
                fridayBusy.setEnabled(true);
                pricingRuleRepository.save(fridayBusy);

                PricingRule tueQuiet = new PricingRule();
                tueQuiet.setName("Tuesday 9–12 — quiet discount");
                tueQuiet.setDayOfWeek(DayOfWeek.TUESDAY);
                tueQuiet.setWindowStart(LocalTime.of(9, 0));
                tueQuiet.setWindowEnd(LocalTime.of(12, 0));
                tueQuiet.setKind(PricingRuleKind.QUIET_DISCOUNT);
                tueQuiet.setPercent(12);
                tueQuiet.setEnabled(true);
                pricingRuleRepository.save(tueQuiet);

                log.info("Seeded example pricing rules (Friday busy window, Tuesday quiet window).");
            }
        };
    }
}
