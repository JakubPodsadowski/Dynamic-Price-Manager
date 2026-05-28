package com.podsadowski.dynamicpricemanager.repository;

import com.podsadowski.dynamicpricemanager.entity.DynamicPricingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicPricingSettingsRepository extends JpaRepository<DynamicPricingSettings, Long> {
}
