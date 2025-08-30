package com.ProjetMajeure.Services.EmergencyManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Indique que cette classe est un contrôleur REST
@RequestMapping("/emergency-manager") // Définit la racine des URL pour les requêtes de ce contrôleur
@CrossOrigin(origins = "*") // Permet les requêtes cross-origin de n'importe quelle origine
public class EmergencyManagerController {

    @Autowired // Injecte automatiquement une instance de EmergencyManagerService
    private EmergencyManagerService emergencyManagerService;

    @PostMapping("/deploy/all-vehicles") // Mappage pour les requêtes POST à "/deploy/all-vehicles"
    public void deployAllVehiclesToFires() {
        emergencyManagerService.deployVehiclesToFires(); // Appelle le service pour déployer tous les véhicules sur les incendies
    }


    // public void checkAndUpdateFireStatus() {
    //     emergencyManagerService.checkAndUpdateFireStatus();
    // }


    // public void deployOneVehicleToFire() {
    //     emergencyManagerService.deployOneVehicleToFire();
    // }

    @PostMapping("/moveAllBackToBase") // Mappage pour les requêtes POST à "/moveAllBackToBase"
    public void moveAllBackToBase() {
        emergencyManagerService.moveAllBackToBase(); // Appelle le service pour rappeler tous les véhicules à la base
        System.out.println("All vehicles have been recalled to base."); // Affiche un message de confirmation dans la console
    }

    @PostMapping("/start") // Mappage pour les requêtes POST à "/start"
    public ResponseEntity<String> startDeployingVehicles() {
        new Thread(() -> emergencyManagerService.startDeployingVehiclesInLoop()).start(); // Lance un nouveau thread pour déployer les véhicules en boucle
        return ResponseEntity.ok("Started deploying vehicles."); // Retourne une réponse HTTP 200 avec un message de confirmation
    }

    @PostMapping("/stop") // Mappage pour les requêtes POST à "/stop"
    public ResponseEntity<String> stopDeployingVehicles() {
        emergencyManagerService.stopDeployingVehicles(); // Appelle le service pour arrêter le déploiement des véhicules
        return ResponseEntity.ok("Stopped deploying vehicles."); // Retourne une réponse HTTP 200 avec un message de confirmation
    }
}