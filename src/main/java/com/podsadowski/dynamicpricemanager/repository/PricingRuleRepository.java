package com.podsadowski.dynamicpricemanager.repository;

import com.podsadowski.dynamicpricemanager.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findAllByOrderByIdAsc();
}
