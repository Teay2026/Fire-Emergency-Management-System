package com.ProjetMajeure.Services.Facility;

import com.ProjetMajeure.Services.Vehicle.Vehicle;
import com.ProjetMajeure.Services.Vehicle.VehicleService;
import com.ProjetMajeure.Services.Vehicle.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
public class FacilityService {

    @Value("${api.url.api}")
    private String apiUrl;

    @Value("${api.credentials.teamuuid}")
    private String teamuuid;

//    @Autowired
//    private VehicleService vehicleService;

    // Constructeur pour initialiser le RestTemplate
    private final RestTemplate restTemplate;

    public FacilityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Méthode pour créer les en-têtes HTTP avec l'UUID de l'équipe
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("teamuuid", teamuuid);
        return headers;
    }


    // Méthode pour obtenir toutes les installations
    public List<Facility> getAllFacilities() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("facility")
                .toUriString();
        ResponseEntity<Facility[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Facility[].class);
        return Arrays.asList(response.getBody());
    }


    // Méthode pour obtenir une installation par son ID
    public Facility getFacilityById(int id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("facility", "{id}")
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<Facility> response = restTemplate.exchange(url, HttpMethod.GET, entity, Facility.class);
        return response.getBody();
    }


    // Méthode pour obtenir un objet Facility par son ID
    public Facility getFacilityObjectById(int id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("facility", "object", "{id}")
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<Facility> response = restTemplate.exchange(url, HttpMethod.GET, entity, Facility.class);
        return response.getBody();
    }




}
