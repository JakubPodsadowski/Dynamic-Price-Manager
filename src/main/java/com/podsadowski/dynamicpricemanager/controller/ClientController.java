package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.dto.CreateReservationDto;
import com.podsadowski.dynamicpricemanager.dto.UserProfileDto;
import com.podsadowski.dynamicpricemanager.dto.UserProfileUpdateDto;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.ReservationService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import com.podsadowski.dynamicpricemanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final SaloonServiceManager saloonServiceManager;
    private final ReservationService reservationService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public ClientController(SaloonServiceManager saloonServiceManager, ReservationService reservationService, UserService userService, EmployeeService employeeService) {
        this.saloonServiceManager = saloonServiceManager;
        this.reservationService = reservationService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String clientPanel(Model model) {
        model.addAttribute("activeTab", "services");
        model.addAttribute("services", saloonServiceManager.listAllDtos());
        return "client";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        model.addAttribute("activeTab", "profile");
        UserProfileDto profile = userService.getUserProfileDto(principal.getName());
        model.addAttribute("userProfile", profile);
        UserProfileUpdateDto update = new UserProfileUpdateDto();
        update.setFirstName(profile.getFirstName() != null ? profile.getFirstName() : "");
        update.setLastName(profile.getLastName() != null ? profile.getLastName() : "");
        update.setPhoneNumber(profile.getPhoneNumber());
        model.addAttribute("profileUpdate", update);
        return "client-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileUpdate") UserProfileUpdateDto profileUpdate,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "profile");
            model.addAttribute("userProfile", userService.getUserProfileDto(principal.getName()));
            return "client-profile";
        }
        userService.updateUserProfile(principal.getName(), profileUpdate);
        return "redirect:/client/profile?success";
    }

    @GetMapping("/reservations")
    public String myReservations(Model model, Principal principal) {
        model.addAttribute("activeTab", "myReservations");
        model.addAttribute("reservations", reservationService.listForClient(principal.getName()));
        return "client-reservations";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelByClient(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/client/reservations";
    }

    @GetMapping("/book")
    public String bookingForm(@RequestParam Long serviceId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("selectedService", saloonServiceManager.getServiceDtoById(serviceId));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/client";
        }
        model.addAttribute("activeTab", "services");
        model.addAttribute("employees", employeeService.getAllEmployeeDtos());
        if (!model.containsAttribute("reservationForm")) {
            model.addAttribute("reservationForm", buildPrefilledReservationForm(serviceId, principal.getName()));
        }
        return "client-book";
    }

    private CreateReservationDto buildPrefilledReservationForm(Long serviceId, String email) {
        CreateReservationDto dto = new CreateReservationDto();
        dto.setServiceId(serviceId);
        UserProfileDto profile = userService.getUserProfileDto(email);
        dto.setContactFirstName(emptyToBlank(profile.getFirstName()));
        dto.setContactLastName(emptyToBlank(profile.getLastName()));
        dto.setContactEmail(emptyToBlank(profile.getEmail()));
        dto.setContactPhone(emptyToBlank(profile.getPhoneNumber()));
        return dto;
    }

    private static String emptyToBlank(String s) {
        if (s == null) {
            return "";
        }
        return s.trim();
    }

    @PostMapping("/reserve")
    public String processReservation(@Valid @ModelAttribute("reservationForm") CreateReservationDto reservationForm,
                                     BindingResult bindingResult,
                                     Model model,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "services");
            try {
                model.addAttribute("selectedService", saloonServiceManager.getServiceDtoById(reservationForm.getServiceId()));
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return "redirect:/client";
            }
            model.addAttribute("employees", employeeService.getAllEmployeeDtos());
            return "client-book";
        }
        try {
            reservationService.createReservation(reservationForm, principal.getName());
            return "redirect:/client?success=pending";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/client/book?serviceId=" + reservationForm.getServiceId();
        }
    }
}
