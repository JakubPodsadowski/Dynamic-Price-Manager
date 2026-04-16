package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SaloonServiceManager {
    private final SaloonServicesRepository saloonServicesRepository;

    public SaloonService addService(SaloonService service) {
        return saloonServicesRepository.save(service);
    }

    public List<SaloonService> getAllServices() {
        return saloonServicesRepository.findAll();
    }
}
