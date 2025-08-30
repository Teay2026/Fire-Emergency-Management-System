package com.ProjetMajeure.Services.Facility;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facility")
@CrossOrigin(origins = "*")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @GetMapping("")
    public List<Facility> getAll(){
        return facilityService.getAllFacilities();
    }

    @GetMapping("/{id}")
    public Facility getFacilityById(@PathVariable int id) {
        return facilityService.getFacilityById(id);
    }

    @GetMapping("/object/{id}")
    public Facility getFacilityObjectById(@PathVariable int id) {
        return facilityService.getFacilityObjectById(id);
    }

}
