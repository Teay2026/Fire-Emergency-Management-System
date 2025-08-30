package com.ProjetMajeure.Services.Vehicle;

import com.ProjetMajeure.Services.Facility.Facility;
import com.ProjetMajeure.Services.Facility.FacilityService;
import com.ProjetMajeure.Services.Fire.Fire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VehicleService {

    @Value("${api.url.api}")
    private String apiUrl;

    @Value("${api.credentials.teamuuid}")
    private String teamuuid;

    @Value("${osrm.url}")
    private String osrmUrl;

    @Autowired
    private FacilityService facilityService;

    private final RestTemplate restTemplate;

    public VehicleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Méthode pour créer les en-têtes HTTP avec l'UUID de l'équipe et le type de contenu JSON
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("teamuuid", teamuuid);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Méthode pour obtenir tous les véhicules
    public List<Vehicle> getAllVehicles() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicles")
                .toUriString();
        ResponseEntity<Vehicle[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Vehicle[].class);
        return Arrays.asList(response.getBody());
    }

    // Méthode pour obtenir un véhicule par son ID
    public Vehicle getVehicleById(Long id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicle", "{id}")
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<Vehicle> response = restTemplate.exchange(url, HttpMethod.GET, entity, Vehicle.class);
        return response.getBody();
    }

    // Méthode pour sauvegarder un véhicule
    public Vehicle saveVehicle(Vehicle vehicle) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Vehicle> entity = new HttpEntity<>(vehicle, headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicle", teamuuid)
                .toUriString();
        ResponseEntity<Vehicle> response = restTemplate.exchange(url, HttpMethod.POST, entity, Vehicle.class);
        return response.getBody();
    }

    // Méthode pour créer un véhicule
    public Vehicle createVehicle(String type, String liquidType, int liquidQuantity, double fuel, int crewMember, Long facilityRefID) {
        Vehicle newVehicle = new Vehicle();
        newVehicle.setType(type);
        newVehicle.setLiquidType(liquidType);
        newVehicle.setLiquidQuantity(liquidQuantity);
        newVehicle.setFuel(fuel);
        newVehicle.setCrewMember(crewMember);
        newVehicle.setFacilityRefID(facilityRefID);
        newVehicle.setOnMission(false);

        return saveVehicle(newVehicle);
    }

    // Méthode pour mettre à jour un véhicule
    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Vehicle> entity = new HttpEntity<>(vehicle, headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicle", teamuuid, "{id}")
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<Vehicle> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Vehicle.class);
        return response.getBody();
    }

    // Méthode pour supprimer un véhicule
    public void deleteVehicle(Long id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicle", teamuuid, "{id}")
                .buildAndExpand(id)
                .toUriString();
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    // Méthode pour obtenir les véhicules par UUID d'équipe
    public List<Vehicle> getVehiclesByTeamUUID(String teamUUID) {
        String url = apiUrl + "/vehiclebyteam/" + teamUUID;
        Vehicle[] response = restTemplate.getForObject(url, Vehicle[].class);
        return response != null ? Arrays.asList(response) : new ArrayList<>();
    }


//    public void moveBackToBase(Vehicle vehicle) {
//        Facility facility = facilityService.getFacilityById(vehicle.getFacilityRefID().intValue());
//        moveVehicle(vehicle.getId(),
//                facility.getLon(),
//                facility.getLat(),
//                "EPSG:4326",10, vehicle.getVehicleType().getMaxSpeed()*100, vehicle.getVehicleType().getFuelConsumption() / 100);
//
//        System.out.println("Vehicle : "+vehicle.getId()+" Moved or kept in base because it's fuel is not enough");
//
//    }

    // Méthode pour déplacer un véhicule à la base
    public void moveBackToBase(Vehicle vehicle) {
        Facility facility = facilityService.getFacilityById(vehicle.getFacilityRefID().intValue());
        moveVehicle(vehicle.getId(),
                facility.getLon(),
                facility.getLat(),
                "EPSG:4326");

        System.out.println("Vehicle : " + vehicle.getId() + " moved to base because its fuel is not enough");
    }


//    public void refillVehicle(Vehicle vehicle) {
//        new Thread(() -> {
//            int maxFuel = vehicle.getMaxFuel();
//            int maxLiquid = vehicle.getMaxLiquidQuantity();
//
//            while (getFuelLevel(vehicle.getId()) < maxFuel-10 || getLiquidQuantity(vehicle.getId()) < maxLiquid/2) {
//                try {
//                    Thread.sleep(1000); // Wait for 1 second
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//
//                System.out.println("Vehicle " + vehicle.getId() + ": Fuel=" + getFuelLevel(vehicle.getId()) + "/" + maxFuel + ", Liquid=" + getLiquidQuantity(vehicle.getId())+ "/" + maxLiquid);
//            }
//
//            vehicle.setOnMission(false);
////            updateVehicle(vehicle.getId(), vehicle); // Ensure vehicle state is updated in the system
//        }).start();
//    }


    // Méthode pour obtenir l'itinéraire à partir d'OSRM
    private List<List<Double>> getRouteFromOSRM(double startLon, double startLat, double endLon, double endLat) {
        String trimmedOsrmUrl = osrmUrl.trim();
        String coordinates = startLon + "," + startLat + ";" + endLon + "," + endLat;
        String url = UriComponentsBuilder.fromHttpUrl(trimmedOsrmUrl)
                .pathSegment("route", "v1", "driving", coordinates)
                .queryParam("alternatives", "false")
                .queryParam("steps", "true")
                .queryParam("geometries", "geojson")
                .queryParam("overview", "full")
                .queryParam("annotations", "true")
                .toUriString();

        ResponseEntity<OSRMResponse> response = restTemplate.getForEntity(url, OSRMResponse.class);
        if (response.getBody() != null && response.getBody().routes != null && !response.getBody().routes.isEmpty()) {
            return response.getBody().routes.get(0).geometry.coordinates;
        }
        return new ArrayList<>();
    }

// Utilisation de points intermédiaires ( waypoints )
//    public Vehicle moveVehicle(Long id, double lon, double lat, String projection, int numIntermediatePoints, double speed, double fuelConsumptionPerKm) {
//        Vehicle vehicle = getVehicleById(id);
//        if (vehicle == null) {
//            return null;
//        }
//
//        // Marquer le véhicule comme en mission
//        vehicle.setOnMission(true);
//
//        double remainingFuel = getFuelLevel(vehicle.getId());
//        System.out.println("Fuel avant déplacement du véhicle "+ vehicle.getId() +" : "+ remainingFuel);
//        List<double[]> route = getRouteFromOSRM(vehicle.getLon(), vehicle.getLat(), lon, lat);
//
//        int totalPoints = route.size();
//        if (totalPoints > 1) {
//            int step = totalPoints / (numIntermediatePoints + 1);
//
//            for (int i = 1; i <= numIntermediatePoints; i++) { // modifié
//                double[] point = route.get(i*step);
//                moveVehicleStep(id, point[0], point[1], projection);
//                System.out.println("Vehicle " + id + " moved to (" + point[1] + ", " + point[0] + ")");
//
//                double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), point[1], point[0]);
//                double travelTime = (distance / (speed/3.6)) * 1000; // *1000 pour avoir valeur en ms
//                remainingFuel = getFuelLevel(getVehicleById(id).getId());
//
//                System.out.println("Remaining fuel: " + remainingFuel);
//
//
//
//                try {
//                    if (Double.isNaN(travelTime)) {
//                        System.out.println("Distance < 1metre" );
//                        System.out.println("Travel time très petit , waiting for 500 ms");
//                        Thread.sleep((long)(1/speed));// si la distance est très petite on prend le temps correspondant au parcours de 1m avec une vitesse de maxSpeed
//                    } else {
//                        System.out.println("Travel time: " + travelTime + " ms");
//                        System.out.println("Speed: " + speed + "Km/h");
//                        System.out.println("Distance: " + distance + " metres" );
//                        Thread.sleep((long) travelTime);
//                    }
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//
//                vehicle.setLat(point[1]);
//                vehicle.setLon(point[0]);
//            }
//        }
//
//        moveVehicleStep(id, lon, lat, projection);
//
//        return getVehicleById(id);
//    }

    // Méthode pour déplacer un véhicule
    public Vehicle moveVehicle(Long id, double lon, double lat, String projection) {
        Vehicle vehicle = getVehicleById(id);
        if (vehicle == null) {
            return null;
        }

        // Marquer le véhicule comme en mission
        vehicle.setOnMission(true);

        double remainingFuel = getFuelLevel(vehicle.getId());
        System.out.println("Fuel avant déplacement du véhicule " + vehicle.getId() + " : " + remainingFuel);
        List<List<Double>> route = getRouteFromOSRM(vehicle.getLon(), vehicle.getLat(), lon, lat);

        int totalPoints = route.size();
        System.out.println("Total points: " + totalPoints);
        System.out.println(route);

        for (List<Double> point : route) {
            moveVehicleStep(id, point.get(0), point.get(1), projection);
            System.out.println("Vehicle " + id + " moved to (" + point.get(1) + ", " + point.get(0) + ")");

            double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), point.get(1), point.get(0));
            int speedMultiplier = 10;
            double travelTime = (distance / ((vehicle.getVehicleType().getMaxSpeed()*speedMultiplier)/3.6)) ;
            double travelTimeMs = travelTime*1000;// Conversion de la vitesse en m/s et calcul du temps de trajet en ms

            remainingFuel = getFuelLevel(vehicle.getId());

            System.out.println("Remaining fuel: " + remainingFuel);

            try {
                if (distance < 0.0000001) {
                    System.out.println("Distance < 1 metre : " + distance);
                    System.out.println("Travel time très petit");
                    Thread.sleep((long)travelTimeMs);
                } else if (Double.isNaN(travelTime)) {
                    System.out.println("Travel time NaN, waiting for 50 ms");
                    System.out.println("Distance : " + distance);
                    System.out.println("Speed: " + distance/travelTime);
                    Thread.sleep((long)(3600/vehicle.getVehicleType().getMaxSpeed()));// attente du temps pour qu'un vehicle allant à 70 km/h parcourt 1metre
                } else {
                    System.out.println("Travel time: " + travelTime * 1000 + " ms");
                    System.out.println("Speed: " + (distance/travelTime)*3.6 +" Km/h");
                    System.out.println("Distance: " + distance + " metres");
                    Thread.sleep((long) travelTimeMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            vehicle.setLat(point.get(1));
            vehicle.setLon(point.get(0));
        }

        // Move to final destination point
        moveVehicleStep(id, lon, lat, projection);

        return getVehicleById(id);
    }




    // Méthode pour déplacer un véhicule étape par étape
    private void moveVehicleStep(Long id, double lon, double lat, String projection) {
        MoveVehicleRequest moveRequest = new MoveVehicleRequest(lon, lat, projection);
        HttpHeaders headers = createHeaders();
        HttpEntity<MoveVehicleRequest> entity = new HttpEntity<>(moveRequest, headers);
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("vehicle", "move", teamuuid, "{id}")
                .buildAndExpand(id)
                .toUriString();
        restTemplate.exchange(url, HttpMethod.PUT, entity, Vehicle.class);
    }

    // Méthode pour obtenir les véhicules par l'ID de l'installation
    public List<Vehicle> getVehiclesByFacilityId(Long facilityId) {
        List<Vehicle> allVehicles = getAllVehicles();
        List<Vehicle> vehiclesByFacility = new ArrayList<>();

        for (Vehicle vehicle : allVehicles) {
            if (facilityId.equals(vehicle.getFacilityRefID())) {
                vehiclesByFacility.add(vehicle);
            }
        }

        return vehiclesByFacility;
    }

    // Méthode pour calculer la distance entre deux points géographiques
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371e3;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLon / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    // Méthode pour éteindre un incendie ( pas utilisé )
    public boolean extinguishFire(Vehicle vehicle, Fire fire, double agentConsumptionPerSecond) {
        while (fire.isExtinguished() == false) {
            if (vehicle.getLiquidQuantity() <= 0) {
                System.out.println("Vehicle " + vehicle.getId() + " needs to refill extinguishing agent.");
                moveBackToBase(vehicle);
                return false;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            vehicle.setLiquidQuantity(vehicle.getLiquidQuantity() - (int) agentConsumptionPerSecond);
            fire.setIntensity(fire.getIntensity() - agentConsumptionPerSecond);

            if (fire.getIntensity() <= 0) {
                fire.setExtinguished(true);
                System.out.println("Fire extinguished by vehicle " + vehicle.getId());
                return true;
            }
        }
        return false;
    }

    // Méthode pour supprimer tous les véhicules ( à déplacer dans EmergencyManagerService !! )
    public void deleteAllVehicles() {
        List<Vehicle> allVehicles = getVehiclesByTeamUUID("17951cd8-eae6-4f67-be27-d7500039556e");
        for (Vehicle vehicle : allVehicles) {
            deleteVehicle(vehicle.getId());
        }
        System.out.println("All vehicles have been deleted.");
    }

//    public void refillAllVehicles() {
//        List<Vehicle> allVehicles = getVehiclesByTeamUUID("17951cd8-eae6-4f67-be27-d7500039556e");
//        for (Vehicle vehicle : allVehicles) {
//            refillVehicle(vehicle);
//        }
//        System.out.println("All vehicles have been refilled.");
//    }

    // Méthode pour créer des véhicules stratégiques ( A DEPLACER DANS EMERGENCY MANAGER SERVICE )
    public void createStrategicVehiclesA() {
        // Facility 102
        createVehicle("PUMPER_TRUCK", "SPECIAL_POWDER", 1000, 500, 6, 102L);
        createVehicle("PUMPER_TRUCK", "CARBON_DIOXIDE", 1000, 500, 6, 102L);
        createVehicle("FIRE_ENGINE", "FOAM", 50, 60, 4, 102L);

        // Facility 156
        createVehicle("PUMPER_TRUCK", "POWDER", 1000, 500, 6, 156L);
        createVehicle("PUMPER_TRUCK", "WATER", 1000, 500, 6, 156L);
        createVehicle("FIRE_ENGINE", "FOAM", 50, 60, 4, 156L);
    }

    // Méthode pour obtenir le niveau de carburant d'un véhicule
    public double getFuelLevel(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        return vehicle.getFuel();
    }

    // Méthode pour obtenir la quantité de liquide d'un véhicule
    public int getLiquidQuantity(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        return vehicle.getLiquidQuantity();
    }



    // Classe interne pour gérer les réponses OSRM
    private static class OSRMResponse {
        public List<Route> routes;

        public static class Route {
            public double distance;
            public double duration;
            public Geometry geometry;
            public List<Leg> legs;

            public static class Geometry {
                public String type;
                public List<List<Double>> coordinates;
            }

            public static class Leg {
                public double distance;
                public double duration;
                public List<Object> steps;
            }
        }
    }

}
