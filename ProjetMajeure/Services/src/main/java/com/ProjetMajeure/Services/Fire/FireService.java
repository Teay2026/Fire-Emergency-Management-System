package com.ProjetMajeure.Services.Fire;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
public class FireService {

    @Value("${api.url.api}")
    private String apiUrl;

    @Value("${api.credentials.teamuuid}")
    private String teamuuid;

    private final RestTemplate restTemplate;// Déclaration du RestTemplate pour les appels API

    // Constructeur pour initialiser le RestTemplate
    public FireService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Méthode pour créer les en-têtes HTTP avec l'UUID de l'équipe
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("teamuuid", teamuuid);
        return headers;
    }


    // Méthode pour obtenir la liste de tous les feux
    public List<Fire> getFires() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("fires")
                .toUriString();
        ResponseEntity<Fire[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Fire[].class);
        return Arrays.asList(response.getBody());
    }

    // Méthode pour obtenir un feu par son identifiant
    public Fire getFireById(int id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("fire", "{id}") // Correct the path segment to "fire"
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<Fire> response = restTemplate.exchange(url, HttpMethod.GET, entity, Fire.class);
        return response.getBody();
    }

    // Méthode pour obtenir l'intensité d'un feu par son identifiant
    public double getFireIntensity(int id) {
        Fire fire = getFireById(id);
        return fire.getIntensity();
    }

    // Méthode pour obtenir la liste des types de feux
    public FireEnum[] getFireTypes() {
        return FireEnum.values();
    }
}
