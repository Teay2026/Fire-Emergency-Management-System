package com.ProjetMajeure.Services.Vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;


    // Endpoint pour obtenir tous les véhicules
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    // Endpoint pour obtenir un véhicule par son ID
    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }


    // Endpoint pour sauvegarder un véhicule
    @PostMapping("/{teamuuid}")
    public Vehicle saveVehicle(@PathVariable String teamuuid, @RequestBody Vehicle vehicle) {
        return vehicleService.saveVehicle(vehicle);
    }


    // Endpoint pour mettre à jour un véhicule
    @PutMapping("/{teamuuid}/{id}")
    public Vehicle updateVehicle(@PathVariable String teamuuid, @PathVariable Long id, @RequestBody Vehicle vehicle) {
        return vehicleService.updateVehicle(id, vehicle);
    }


    // Endpoint pour déplacer un véhicule
    @PostMapping("/move/{teamuuid}/{id}")
    public Vehicle moveVehicle(@PathVariable String teamuuid, @PathVariable Long id, @RequestBody MoveVehicleRequest request) {
        return vehicleService.moveVehicle(id, request.getLon(), request.getLat(), request.getProjection()); ///


    }

    // Endpoint pour supprimer un véhicule
    @DeleteMapping("/{teamuuid}/{id}")
    public void deleteVehicle(@PathVariable String teamuuid, @PathVariable Long id) {
        vehicleService.deleteVehicle(id);
    }

    // Endpoint pour déplacer un véhicule à la base
    @PostMapping("/moveBackToBase/{vehicleId}")
    public void moveBackToBase(@PathVariable Long vehicleId) {
        vehicleService.moveBackToBase(getVehicleById(vehicleId));
        System.out.println("Vehicle " + vehicleId + " has been recalled to base.");
    }

    // Endpoint pour obtenir les véhicules d'une équipe
    @GetMapping("/ourvehicles/{teamUUID}")
    public List<Vehicle> getOurVehicles(@PathVariable String teamUUID) {
        return vehicleService.getVehiclesByTeamUUID(teamUUID);
    }

    // Endpoint pour supprimer tous les véhicules
    @DeleteMapping("/deleteAll")
    public void deleteAllVehicles() {
        vehicleService.deleteAllVehicles();
        System.out.println("All vehicles have been deleted.");
    }

//    @PostMapping("/refillAll")
//    public void refillAllVehicles() {
//        vehicleService.refillAllVehicles();
//    }

    // Endpoint pour créer des véhicules stratégiques ( 2 PUMPER TRUCK ET 1 FIRE ENGINE , dans chaque caserne
    @PostMapping("/createStrategicVehiclesA")
    public void createStrategicVehiclesA() {
        vehicleService.createStrategicVehiclesA();
    }

    // Endpoint pour obtenir le niveau de carburant d'un véhicule
    @GetMapping("/{id}/fuel")
    public double getFuelLevel(@PathVariable Long id) {
        return vehicleService.getFuelLevel(id);
    }

    // Endpoint pour obtenir la quantité de liquide d'un véhicule
    @GetMapping("/{id}/liquid")
    public int getLiquidQuantity(@PathVariable Long id) {
        return vehicleService.getLiquidQuantity(id);
    }

}
