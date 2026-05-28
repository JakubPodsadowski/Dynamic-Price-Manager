package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.DynamicPricingSettings;
import com.podsadowski.dynamicpricemanager.entity.PricingRule;
import com.podsadowski.dynamicpricemanager.service.DynamicPricingService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/pricing")
public class AdminDynamicPricingController {

    private final DynamicPricingService dynamicPricingService;
    private final SaloonServiceManager saloonServiceManager;

    public AdminDynamicPricingController(DynamicPricingService dynamicPricingService, SaloonServiceManager saloonServiceManager) {
        this.dynamicPricingService = dynamicPricingService;
        this.saloonServiceManager = saloonServiceManager;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeTab", "pricing");
        model.addAttribute("settings", dynamicPricingService.getSettings());
        model.addAttribute("rules", dynamicPricingService.listRules());
        model.addAttribute("allServices", saloonServiceManager.listAllDtos());
        model.addAttribute("ruleForm", new PricingRule());
        return "admin-pricing";
    }

    @PostMapping("/settings")
    public String saveSettings(
            @RequestParam int historyLookbackWeeks,
            @RequestParam int busyIfTotalReservationsGte,
            @RequestParam int quietIfTotalReservationsLte,
            @RequestParam int lastMinuteWithinHours,
            @RequestParam double lastMinuteDiscountPercent,
            RedirectAttributes redirectAttributes) {
        try {
            DynamicPricingSettings s = dynamicPricingService.getSettings();
            s.setHistoryLookbackWeeks(historyLookbackWeeks);
            s.setBusyIfTotalReservationsGte(busyIfTotalReservationsGte);
            s.setQuietIfTotalReservationsLte(quietIfTotalReservationsLte);
            s.setLastMinuteWithinHours(lastMinuteWithinHours);
            s.setLastMinuteDiscountPercent(lastMinuteDiscountPercent);
            dynamicPricingService.saveSettings(s);
            redirectAttributes.addFlashAttribute("success", "Dynamic pricing settings saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Could not save settings: " + ex.getMessage());
        }
        return "redirect:/admin/pricing";
    }

    @PostMapping("/rules")
    public String addRule(@ModelAttribute PricingRule ruleForm, RedirectAttributes redirectAttributes) {
        try {
            ruleForm.setId(null);
            dynamicPricingService.addRule(ruleForm);
            redirectAttributes.addFlashAttribute("success", "Rule added.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pricing";
    }

    @PostMapping("/rules/{id}/delete")
    public String deleteRule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        dynamicPricingService.deleteRule(id);
        redirectAttributes.addFlashAttribute("success", "Rule deleted.");
        return "redirect:/admin/pricing";
    }

    @PostMapping("/rules/{id}")
    public String updateRule(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam java.time.DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime windowStart,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime windowEnd,
            @RequestParam com.podsadowski.dynamicpricemanager.entity.PricingRuleKind kind,
            @RequestParam double percent,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes) {
        try {
            PricingRule patch = new PricingRule();
            patch.setEnabled(enabled);
            patch.setName(name);
            patch.setDayOfWeek(dayOfWeek);
            patch.setWindowStart(windowStart);
            patch.setWindowEnd(windowEnd);
            patch.setKind(kind);
            patch.setPercent(percent);
            dynamicPricingService.updateRule(id, patch);
            redirectAttributes.addFlashAttribute("success", "Rule updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pricing";
    }

    @GetMapping(value = "/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> quoteJson(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            return ResponseEntity.ok(dynamicPricingService.quote(serviceId, date, time));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
