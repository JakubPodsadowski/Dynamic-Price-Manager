package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.service.EmployeeService;
import com.podsadowski.dynamicpricemanager.service.ReservationService;
import com.podsadowski.dynamicpricemanager.service.SaloonServiceManager;
import com.podsadowski.dynamicpricemanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;

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
        model.addAttribute("services", saloonServiceManager.getAllServices());
        return "client";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        AppUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        return "client-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String phoneNumber,
                                Principal principal) {
        userService.updateUserProfile(principal.getName(), firstName, lastName, phoneNumber);
        return "redirect:/client/profile?success";
    }

    @GetMapping("/book")
    public String bookingForm(@RequestParam Long serviceId, Model model) {
        model.addAttribute("activeTab", "reservation");
        model.addAttribute("selectedService", saloonServiceManager.getServiceById(serviceId));
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "client-book";
    }

    @PostMapping("/reserve")
    public String processReservation(@RequestParam Long serviceId,
                                     @RequestParam Long employeeId,
                                     @RequestParam String date,
                                     @RequestParam String time,
                                     Principal principal) {

        AppUser client = userService.getUserByEmail(principal.getName());
        SaloonService service = saloonServiceManager.getServiceById(serviceId);
        Employee employee = employeeService.getEmployeeById(employeeId);

        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setService(service);
        reservation.setEmployee(employee);
        reservation.setReservationDate(LocalDate.parse(date));
        reservation.setReservationTime(LocalTime.parse(time));

        reservationService.addReservation(reservation);
        return "redirect:/client?success";
    }
}