package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final EmployeeService employeeService;

    public AdminReservationController(ReservationService reservationService, EmployeeService employeeService) {
        this.reservationService = reservationService;
        this.employeeService = employeeService;
    }

    @InitBinder
    public void initDateBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else {
                    setValue(LocalDate.parse(text));
                }
            }
        });
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {
        Long employeeIdLong = parseLongParam(employeeId);
        String statusFilter = status == null || status.isBlank() ? "ALL" : status;
        model.addAttribute("activeTab", "reservations");
        model.addAttribute("employees", employeeService.getAllEmployeeDtos());
        model.addAttribute("selectedEmployeeId", employeeIdLong);
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("reservations", reservationService.listForAdmin(
                employeeIdLong,
                parseStatus(statusFilter),
                dateFrom,
                dateTo
        ));
        return "admin-reservations";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(
            @PathVariable Long id,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.confirmByAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Reservation confirmed.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + buildListUrl(employeeId, status, dateFrom, dateTo);
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelByAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + buildListUrl(employeeId, status, dateFrom, dateTo);
    }

    static String buildListUrl(String employeeId, String status, LocalDate dateFrom, LocalDate dateTo) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/reservations");
        if (employeeId != null && !employeeId.isBlank()) {
            builder.queryParam("employeeId", employeeId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            builder.queryParam("status", status);
        }
        if (dateFrom != null) {
            builder.queryParam("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            builder.queryParam("dateTo", dateTo);
        }
        return builder.build().toUriString();
    }

    private static Long parseLongParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static ReservationStatus parseStatus(String s) {
        if (s == null || s.isBlank() || "ALL".equalsIgnoreCase(s)) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
