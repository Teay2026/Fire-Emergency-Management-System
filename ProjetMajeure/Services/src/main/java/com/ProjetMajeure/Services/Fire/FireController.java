package com.ProjetMajeure.Services.Fire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // Permet les requêtes cross-origin de n'importe quelle origine
public class FireController {

    private final FireService fireService; // Service de gestion des feux


    // Constructeur avec injection du service FireService
    @Autowired
    public FireController(FireService fireService) {
        this.fireService = fireService;
    }


    // Endpoint pour obtenir la liste de tous les feux
    @GetMapping("/fires")
    public List<Fire> getFires() {
        return fireService.getFires();
    }


    // Endpoint pour obtenir la liste des types de feux
    @GetMapping("/firetypes")
    public FireEnum[] getFireTypes() {
        return fireService.getFireTypes();
    }


    // Endpoint pour obtenir l'intensité d'un feu par son identifiant
    @GetMapping("fires/{id}/intensity")
    public double getFireIntensity(@PathVariable int id) {
        return fireService.getFireIntensity(id);
    }

}
