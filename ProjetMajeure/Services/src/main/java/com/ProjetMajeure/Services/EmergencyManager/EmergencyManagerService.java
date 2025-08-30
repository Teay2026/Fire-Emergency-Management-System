package com.ProjetMajeure.Services.EmergencyManager;

import com.ProjetMajeure.Services.Facility.Facility;
import com.ProjetMajeure.Services.Facility.FacilityService;
import com.ProjetMajeure.Services.Fire.Fire;
import com.ProjetMajeure.Services.Fire.FireEnum;
import com.ProjetMajeure.Services.Fire.FireService;
import com.ProjetMajeure.Services.Fire.LiquidType;
import com.ProjetMajeure.Services.Vehicle.Vehicle;
import com.ProjetMajeure.Services.Vehicle.VehicleService;
import com.ProjetMajeure.Services.Vehicle.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;



import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyManagerService {

    @Autowired // Injection automatique du service FireService
    private FireService fireService;

    @Autowired // Injection automatique du service VehicleService
    private VehicleService vehicleService;

    @Autowired // Injection automatique du service FacilityService
    private FacilityService facilityService;

    private AtomicBoolean running = new AtomicBoolean(false); // Indicateur atomique pour vérifier si le service est en cours d'exécution
    private Thread deploymentThread; // Thread pour le déploiement des véhicules

    public void manageFacilities() {
        // Gestion des installations
        List<Facility> facilities = Arrays.asList(
                facilityService.getFacilityById(102),
                facilityService.getFacilityById(156)
        );

        for (Facility facility : facilities) {
            manageFacilityVehicles(facility);
        }
    }

    private void manageFacilityVehicles(Facility facility) {
        // Gestion des véhicules dans une installation spécifique
        List<Vehicle> vehicles = vehicleService.getVehiclesByFacilityId((long) facility.getId());
        Map<String, List<Vehicle>> vehiclesByType = classifyVehiclesByType(vehicles);

        ensureVehicleTypes(facility, vehiclesByType, VehicleType.PUMPER_TRUCK, 2);
        ensureVehicleTypes(facility, vehiclesByType, VehicleType.FIRE_ENGINE, 1);

        List<Fire> fires = fireService.getFires();
        List<Thread> threads = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            if (!vehicle.isOnMission()) {
                List<Fire> suitableFires = filterFiresByAssignedType(vehicle, fires, facility);
                suitableFires.sort(Comparator.comparingDouble(fire -> calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon())));

                if (!suitableFires.isEmpty()) {
                    Fire targetFire = suitableFires.get(0);
                    Thread vehicleThread = new Thread(() -> {
                        if (moveVehicleToFire(vehicle, targetFire)) {
                            extinguishFire(vehicle, targetFire);
                        }
                    });
                    threads.add(vehicleThread);
                    vehicleThread.start();
                }
            }
        }

        // Attendre la fin de tous les threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private List<Fire> filterFiresByAssignedType(Vehicle vehicle, List<Fire> fires, Facility facility) {
        // Filtrage des incendies en fonction du type assigné au véhicule
        FireEnum assignedFireType = getAssignedFireType(vehicle, facility);
        List<Fire> suitableFires = new ArrayList<>();
        for (Fire fire : fires) {
            if (fire.getType() == assignedFireType && !fire.isExtinguished()) {
                suitableFires.add(fire);
            }
        }
        return suitableFires;
    }

    private void deployVehicleToFire(Vehicle vehicle, Fire fire) {
        // Déploiement d'un véhicule vers un incendie
        List<Fire> firesInRange = getFiresInRange(vehicle, fireService.getFires(), 5);

        if (firesInRange.isEmpty()) {
            System.out.println("No fires in range for vehicle " + vehicle.getId() + ". Returning to base.");
            vehicleService.moveBackToBase(vehicle);

            Fire nearestFire = findNearestFire(vehicle, fireService.getFires(), facilityService.getFacilityById(vehicle.getFacilityRefID().intValue()));
            if (nearestFire != null) {
                moveVehicleToFire(vehicle, nearestFire);
                extinguishFire(vehicle, nearestFire);
            }
        } else {
            for (Fire fireInRange : firesInRange) {
                if (isSuitableVehicle(vehicle, fireInRange)) {
                    if (!moveVehicleToFire(vehicle, fireInRange)) {
                        vehicleService.moveBackToBase(vehicle);

                        Fire nearestFire = findNearestFire(vehicle, fireService.getFires(), facilityService.getFacilityById(vehicle.getFacilityRefID().intValue()));
                        if (nearestFire != null) {
                            moveVehicleToFire(vehicle, nearestFire);
                            extinguishFire(vehicle, nearestFire);
                        }
                        break;
                    } else {
                        extinguishFire(vehicle, fireInRange);
                    }
                }
            }
        }
    }



    private void ensureVehicleTypes(Facility facility, Map<String, List<Vehicle>> vehiclesByType, VehicleType type, int requiredCount) {
        // Assurer qu'il y a un certain nombre de véhicules de chaque type dans une installation
        List<Vehicle> vehicles = vehiclesByType.getOrDefault(type.name(), new ArrayList<>());
        if (vehicles.size() < requiredCount) {
            for (int i = vehicles.size(); i < requiredCount; i++) {
                Vehicle newVehicle = vehicleService.createVehicle(
                        type.name(),
                        LiquidType.ALL.toString(),
                        0, 0, 0, (long) facility.getId()
                );
                vehicles.add(newVehicle);
                System.out.println("Created " + type.name() + " with ID " + newVehicle.getId() + " in facility " + facility.getId());
            }
        }
    }

    private Map<String, List<Vehicle>> classifyVehiclesByType(List<Vehicle> vehicles) {
        // Classification des véhicules par type
        Map<String, List<Vehicle>> vehiclesByType = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            vehiclesByType
                    .computeIfAbsent(vehicle.getType(), k -> new ArrayList<>())
                    .add(vehicle);
        }
        return vehiclesByType;
    }

    private Fire findNearestFire(Vehicle vehicle, List<Fire> fires, Facility facility) {
        // Trouver le feu le plus proche pour un véhicule donné
        Fire nearestFire = null;
        double minDistance = Double.MAX_VALUE;
        FireEnum assignedFireType = getAssignedFireType(vehicle, facility);

        for (Fire fire : fires) {
            if (!fire.isExtinguished() && fire.getType() == assignedFireType) {
                double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestFire = fire;
                }
            }
        }
        return nearestFire;
    }

    private FireEnum getAssignedFireType(Vehicle vehicle, Facility facility) {
        // Déterminer le type de feu assigné à un véhicule dans une installation
        if (facility.getId() == 156) {
            if (vehicle.getType().equals("PUMPER_TRUCK")) {
                if (vehicle.getLiquidType().equals("WATER")) {
                    return FireEnum.A;
                } else if (vehicle.getLiquidType().equals("POWDER")) {
                    return FireEnum.C_Flammable_Gases;
                }
            } else if (vehicle.getType().equals("FIRE_ENGINE")) {
                return FireEnum.B_Gasoline;
            }
        } else if (facility.getId() == 102) {
            if (vehicle.getType().equals("PUMPER_TRUCK")) {
                if (vehicle.getLiquidType().equals("CARBON_DIOXIDE")) {
                    return FireEnum.E_Electric;
                } else if (vehicle.getLiquidType().equals("SPECIAL_POWDER")){
                    return FireEnum.D_Metals;
                }
            } else if (vehicle.getType().equals("FIRE_ENGINE")) {
                return FireEnum.B_Alcohol;
            }
        }
        return null;
    }


    private boolean isSuitableVehicle(Vehicle vehicle, Fire fire) {
        // Vérifier si un véhicule est approprié pour un certain type de feu
        LiquidType liquidType = LiquidType.valueOf(vehicle.getLiquidType());
        return liquidType.getEfficiency(fire.getType().toString()) > 0;
    }



    public void startDeployingVehiclesInLoop() {
        // Démarrer le déploiement des véhicules en boucle
        running.set(true);
        deploymentThread = new Thread(() -> {
            while (running.get()) {
                deployVehiclesToFires();
                try {
                    Thread.sleep(10000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        deploymentThread.start();
    }

    public void stopDeployingVehicles()
    {   // Arrêter le déploiement des véhicules
        running.set(false);
    }


    public void deployVehiclesToFires() {
        // Déploiement des véhicules vers les incendies
        List<Facility> facilities = Arrays.asList(
                facilityService.getFacilityById(102),
                facilityService.getFacilityById(156)
        );

        for (Facility facility : facilities) {
            List<Vehicle> vehicles = vehicleService.getVehiclesByFacilityId((long) facility.getId());
            ensureRequiredVehicles(facility, vehicles);

            for (Vehicle vehicle : vehicles) {
                if (!isVehicleOnFire(vehicle)) {  // Vérifier si le véhicule n'est pas en mission
                    Thread vehicleThread = new Thread(() -> {
                        vehicle.setOnMission(true);  // Marquer le véhicule comme en mission


                        List<Fire> suitableFires = filterFiresByAssignedType(vehicle, fireService.getFires(), facility);
                        suitableFires.sort(Comparator.comparingDouble(fire -> calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon())));
                        for (Fire fire : suitableFires) {
                            if (moveVehicleToFire(vehicle, fire)) {
                                extinguishFire(vehicle, fire);
                                break;
                            }
                        }

                        vehicle.setOnMission(false);  // Marquer le véhicule comme non en mission après avoir éteint le feu
                        System.out.println(vehicle.getId() + "n'est plus en mission");
                    });
                    vehicleThread.start();
                }


            }
        }
    }

    public boolean isVehicleOnFire(Vehicle vehicle) {
        // Vérifier si un véhicule est sur un incendie
        List<Fire> fires = fireService.getFires();
        for (Fire fire : fires) {
            if (fire.getLat() == vehicle.getLat() && fire.getLon() == vehicle.getLon()) {
                return true;
            }
        }
        return false;
    }



    //latest update
//    public void deployVehiclesToFires() {
//        List<Facility> facilities = Arrays.asList(
//                facilityService.getFacilityById(102),
//                facilityService.getFacilityById(156)
//        );
//
//        List<Thread> threads = new ArrayList<>();
//
//        for (Facility facility : facilities) {
//            List<Vehicle> vehicles = vehicleService.getVehiclesByFacilityId((long) facility.getId());
//            ensureRequiredVehicles(facility, vehicles);
//
//            for (Vehicle vehicle : vehicles) {
//                Thread vehicleThread = new Thread(() -> {
//                    if (!vehicle.isOnMission() && !stop) {
//                        List<Fire> suitableFires = filterFiresByAssignedType(vehicle, fireService.getFires(), facility);
//                        suitableFires.sort(Comparator.comparingDouble(fire -> calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon())));
//                        for (Fire fire : suitableFires) {
//                            if (moveVehicleToFire(vehicle, fire)) {
//                                extinguishFire(vehicle, fire);
//                                break;
//                            }
//                        }
//                    }
//                });
//                threads.add(vehicleThread);
//                vehicleThread.start();
//            }
//        }
//
//        // Wait for all threads to complete
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }


    private Vehicle createVehicleForFire(Fire fire, Facility facility) {
        // Créer un véhicule pour un incendie
        LiquidType mostEfficientLiquid = getMostEfficientLiquid(fire.getType());
        VehicleType vehicleType = VehicleType.TRUCK; // Choisissez le type de véhicule approprié
        if (getAvailableSpace(facility.getId()) >= vehicleType.getSpaceUsedInFacility()) {
            return vehicleService.createVehicle(vehicleType.name(), mostEfficientLiquid.toString(), (int) vehicleType.getLiquidCapacity(), vehicleType.getFuelCapacity(), vehicleType.getVehicleCrewCapacity(), (long) facility.getId());
        }
        return null;
    }




    private boolean moveVehicleToFire(Vehicle vehicle, Fire fire) {
        // Déplacer un véhicule vers un incendie
        VehicleType vehicleType = vehicle.getVehicleType();
        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
        Facility nearestFacility = findNearestFacility(vehicle.getLat(), vehicle.getLon());
        System.out.println("nearestFacility: " + nearestFacility.getId() + " pour véhicule " + vehicle.getId());
        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), nearestFacility.getLat(), nearestFacility.getLon());

        // +10 mesure de sécurité pour le carburant
        double requiredFuel = (distanceToFire + distanceToFacility) *( vehicleType.getFuelConsumption()/100000) + 10;

        double currentFuelLevel = vehicleService.getFuelLevel(vehicle.getId());
        System.out.println("Current fuel level of vehicle " + vehicle.getId() + ": " + currentFuelLevel);
        System.out.println("Fuel required to reach the fire and return to base: " + requiredFuel);

        if (currentFuelLevel < requiredFuel) {
            System.out.println("Not enough fuel to reach the fire and return to base. Returning to base for refueling.");
            vehicleService.moveBackToBase(vehicle);
            return false;
        }

        System.out.println("Vehicle " + vehicle.getId() + " is moving to fire");
        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326");

        System.out.println("Vehicle " + vehicle.getId() + " with liquid type " + vehicleService.getLiquidQuantity(vehicle.getId()) +
                " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ") which is of type " + fire.getType());

        currentFuelLevel = vehicleService.getFuelLevel(vehicle.getId());
        System.out.println("Current fuel level of vehicle " + vehicle.getId() + " after reaching the fire: " + currentFuelLevel);

        nearestFacility = findNearestFacility(vehicle.getLat(), vehicle.getLon());
        distanceToFacility = calculateDistance(vehicle.getLat(), vehicle.getLon(), nearestFacility.getLat(), nearestFacility.getLon());
        requiredFuel = distanceToFacility * (vehicleType.getFuelConsumption() / 100000 )+ 20; // +20 mesure de sécurité pour le retour à la base

        System.out.println("Fuel required to return to base from fire location: " + requiredFuel);

        if (currentFuelLevel < requiredFuel) {
            System.out.println("Not enough fuel to return to base after extinguishing the fire. Returning to base for refueling.");
            vehicleService.moveBackToBase(vehicle);
            return false;
        }

        return true;
    }





    private void extinguishFire(Vehicle vehicle, Fire fire) {
        // Extinction d'un incendie
        VehicleType vehicleType = vehicle.getVehicleType();
        double liquidConsumptionPerSecond = vehicleType.getLiquidConsumption() / 100;
        double remainingLiquid = vehicleService.getLiquidQuantity(vehicle.getId());

        double attenuationFactor = 1;

        while (true) {
            try {
                Fire currentFire = fireService.getFireById(fire.getId());
                if (currentFire == null || currentFire.getIntensity() <= 0) {
                    break;
                }

                if (remainingLiquid < liquidConsumptionPerSecond) {
                    System.out.println("Not enough extinguishing agent. Returning to base for refilling.");
                    vehicleService.moveBackToBase(vehicle);

                    List<Fire> suitableFires = filterFiresByAssignedType(vehicle, fireService.getFires(), facilityService.getFacilityById(vehicle.getFacilityRefID().intValue()));
                    suitableFires.sort(Comparator.comparingDouble(f -> calculateDistance(vehicle.getLat(), vehicle.getLon(), f.getLat(), f.getLon())));
                    for (Fire nearestFire : suitableFires) {
                        if (moveVehicleToFire(vehicle, nearestFire)) {
                            extinguishFire(vehicle, nearestFire);
                            break;
                        }
                    }
                    vehicle.setOnMission(false);
                    return;
                }

                Thread.sleep(1000); // Sleep for 1 second

                double efficiency = vehicleType.getEfficiency() * LiquidType.valueOf(vehicle.getLiquidType()).getEfficiency(currentFire.getType().toString());
                double attenuation = vehicleType.getEfficiency() * efficiency * attenuationFactor;
                remainingLiquid -= liquidConsumptionPerSecond;

                System.out.println("Vehicle " + vehicle.getId() + " with liquid type " + vehicle.getLiquidType() + " extinguishing fire of type " + currentFire.getType() + ". Remaining Liquid: " + remainingLiquid + ". Fire intensity: " + currentFire.getIntensity());
            } catch (Exception e) {
                System.out.println("Error occurred: " + e.getMessage());
                vehicle.setOnMission(false);
                break;
            }
        }

        vehicle.setLiquidQuantity((int) remainingLiquid);
        System.out.println("Fire at (" + fire.getLat() + ", " + fire.getLon() + ") extinguished by vehicle " + vehicle.getId());
        System.out.println("Remaining Liquid quantity: " + vehicle.getLiquidQuantity());

        vehicle.setOnMission(false);
    }




    //latest
//private void extinguishFire(Vehicle vehicle, Fire fire) {
//    VehicleType vehicleType = vehicle.getVehicleType();
//    double liquidConsumptionPerSecond = vehicleType.getLiquidConsumption() / 100;
//    double remainingLiquid = vehicleService.getLiquidQuantity(vehicle.getId());
//
//    double attenuationFactor = 1;
//
//    while (fireService.getFireIntensity(fire.getId()) > 0) {
//        if (remainingLiquid < liquidConsumptionPerSecond) {
//            System.out.println("Not enough extinguishing agent. Returning to base for refilling.");
//            vehicleService.moveBackToBase(vehicle);
//
//            List<Fire> suitableFires = filterFiresByAssignedType(vehicle, fireService.getFires(), facilityService.getFacilityById(vehicle.getFacilityRefID().intValue()));
//            suitableFires.sort(Comparator.comparingDouble(f -> calculateDistance(vehicle.getLat(), vehicle.getLon(), f.getLat(), f.getLon())));
//            for (Fire nearestFire : suitableFires) {
//                if (moveVehicleToFire(vehicle, nearestFire)) {
//                    extinguishFire(vehicle, nearestFire);
//                    break;
//                }
//            }
//            vehicle.setOnMission(false);
//            return;
//        }
//
//        try {
//            Thread.sleep(1000); // Sleep for 1 second
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        double efficiency = vehicleType.getEfficiency() * LiquidType.valueOf(vehicle.getLiquidType()).getEfficiency(fire.getType().toString());
//        double attenuation = vehicleType.getEfficiency() * efficiency * attenuationFactor;
//        fire.setIntensity(fireService.getFireIntensity(fire.getId())); // Update fire intensity
//        remainingLiquid -= liquidConsumptionPerSecond;
//
//        System.out.println("Vehicle " + vehicle.getId() + " with liquid type " + vehicle.getLiquidType() + " extinguishing fire of type " + fire.getType() + ". Remaining Liquid: " + remainingLiquid + ". Fire intensity: " + fire.getIntensity());
//
//    }
//
//    vehicle.setLiquidQuantity((int) remainingLiquid);
//
//    System.out.println("Fire at (" + fire.getLat() + ", " + fire.getLon() + ") extinguished by vehicle " + vehicle.getId());
//    System.out.println("Remaining Liquid quantity: " + vehicle.getLiquidQuantity());
//
//    vehicle.setOnMission(false);
//}





//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        double earthRadius = 6371;
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLon / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return earthRadius * c;
//    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Calcul de la distance entre deux points géographiques
        double earthRadius = 6371e3; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInMeters = earthRadius * c;
        return distanceInMeters; // Return in meters
    }




    private LiquidType getMostEfficientLiquid(FireEnum fireType) {
        // Obtenir le liquide le plus efficace pour un type de feu donné
        LiquidType mostEfficient = LiquidType.ALL;
        float maxEfficiency = -1;

        for (LiquidType liquidType : LiquidType.values()) {
            float efficiency = liquidType.getEfficiency(fireType.toString());
            if (efficiency > maxEfficiency) {
                maxEfficiency = efficiency;
                mostEfficient = liquidType;
            }
        }
        return mostEfficient;
    }

    private List<Fire> getFiresInRange(Vehicle vehicle, List<Fire> fires, double radiusKm) {
        // Obtenir les incendies dans un rayon donné autour d'un véhicule
        List<Fire> firesInRange = new ArrayList<>();
        for (Fire fire : fires) {
            if (calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon()) <= radiusKm) {
                firesInRange.add(fire);
            }
        }
        return firesInRange;
    }

    private void ensureRequiredVehicles(Facility facility, List<Vehicle> vehicles) {
        // Assurer la présence de véhicules nécessaires dans une installation
        Map<String, Integer> vehicleCounts = new HashMap<>();
        vehicleCounts.put("PUMPER_TRUCK", 0);
        vehicleCounts.put("FIRE_ENGINE", 0);

        for (Vehicle vehicle : vehicles) {
            vehicleCounts.put(vehicle.getType(), vehicleCounts.getOrDefault(vehicle.getType(), 0) + 1);
        }

        if (facility.getId() == 156) {
            // Ensure required PUMPER_TRUCK vehicles with WATER and POWDER
            if (vehicleCounts.get("PUMPER_TRUCK") < 2) {
                int neededWaterTrucks = 1;
                int neededPowderTrucks = 1;

                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getType().equals("PUMPER_TRUCK") && vehicle.getLiquidType().equals("WATER")) {
                        neededWaterTrucks--;
                    } else if (vehicle.getType().equals("PUMPER_TRUCK") && vehicle.getLiquidType().equals("POWDER")) {
                        neededPowderTrucks--;
                    }
                }

                for (int i = 0; i < neededWaterTrucks; i++) {
                    Vehicle newVehicle = vehicleService.createVehicle("PUMPER_TRUCK", LiquidType.WATER.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }

                for (int i = 0; i < neededPowderTrucks; i++) {
                    Vehicle newVehicle = vehicleService.createVehicle("PUMPER_TRUCK", LiquidType.POWDER.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }
            }

            // Ensure required FIRE_ENGINE vehicle with GASOLINE
            if (vehicleCounts.get("FIRE_ENGINE") < 1) {
                boolean needGasolineEngine = true;

                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getType().equals("FIRE_ENGINE") && vehicle.getLiquidType().equals("GASOLINE")) {
                        needGasolineEngine = false;
                        break;
                    }
                }

                if (needGasolineEngine) {
                    Vehicle newVehicle = vehicleService.createVehicle("FIRE_ENGINE", LiquidType.FOAM.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }
            }
        } else if (facility.getId() == 102) {
            // Ensure required PUMPER_TRUCK vehicles with CARBON_DIOXIDE and SPECIAL_POWDER
            if (vehicleCounts.get("PUMPER_TRUCK") < 2) {
                int neededCarbonDioxideTrucks = 1;
                int neededSpecialPowderTrucks = 1;

                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getType().equals("PUMPER_TRUCK") && vehicle.getLiquidType().equals("CARBON_DIOXIDE")) {
                        neededCarbonDioxideTrucks--;
                    } else if (vehicle.getType().equals("PUMPER_TRUCK") && vehicle.getLiquidType().equals("SPECIAL_POWDER")) {
                        neededSpecialPowderTrucks--;
                    }
                }

                for (int i = 0; i < neededCarbonDioxideTrucks; i++) {
                    Vehicle newVehicle = vehicleService.createVehicle("PUMPER_TRUCK", LiquidType.CARBON_DIOXIDE.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }

                for (int i = 0; i < neededSpecialPowderTrucks; i++) {
                    Vehicle newVehicle = vehicleService.createVehicle("PUMPER_TRUCK", LiquidType.SPECIAL_POWDER.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }
            }

            // Ensure required FIRE_ENGINE vehicle with ALCOHOL
            if (vehicleCounts.get("FIRE_ENGINE") < 1) {
                boolean needAlcoholEngine = true;

                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getType().equals("FIRE_ENGINE") && vehicle.getLiquidType().equals("ALCOHOL")) {
                        needAlcoholEngine = false;
                        break;
                    }
                }

                if (needAlcoholEngine) {
                    Vehicle newVehicle = vehicleService.createVehicle("FIRE_ENGINE", LiquidType.FOAM.toString(), 0, 0, 0, (long) facility.getId());
//                    vehicleService.refillVehicle(newVehicle);
                    vehicles.add(newVehicle);
                }
            }
        }
    }

    private Facility findNearestFacility(double lat, double lon) {
        // Trouver l'installation la plus proche
        List<Facility> facilities = Arrays.asList(
                facilityService.getFacilityById(102),
                facilityService.getFacilityById(156)
        );

        Facility nearestFacility = null;
        double minDistance = Double.MAX_VALUE;

        for (Facility facility : facilities) {
            double distance = calculateDistance(lat, lon, facility.getLat(), facility.getLon());
            if (distance < minDistance) {
                minDistance = distance;
                nearestFacility = facility;
            }
        }

        return nearestFacility;
    }

    private int getAvailableSpace(int facilityId) {
        Facility facility = facilityService.getFacilityById(facilityId);
        int currentVehicleCount = facility.getVehicleIdSet().size();
        return facility.getMaxVehicleSpace() - currentVehicleCount;
    }


    public void moveAllBackToBase() {
        List<Vehicle> allVehicles = vehicleService.getVehiclesByTeamUUID("17951cd8-eae6-4f67-be27-d7500039556e");

        // Créer un pool de threads avec un nombre de threads égal au nombre de véhicules
        ExecutorService executorService = Executors.newFixedThreadPool(allVehicles.size());

        for (Vehicle vehicle : allVehicles) {
            executorService.submit(() -> {
                vehicleService.moveBackToBase(vehicle);
                // vehicleService.refillVehicle(vehicle);
            });
        }

        // Arrêter l'executor service de manière ordonnée
        executorService.shutdown();
        try {
            // Attendre que toutes les tâches se terminent
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("All vehicles have been moved back to base.");
    }


}








//@Service
//public class EmergencyManagerService {
//
//    @Autowired
//    private FireService fireService;
//
//    @Autowired
//    private VehicleService vehicleService;
//
//    @Autowired
//    private FacilityService facilityService;
//
////    public void deployVehiclesToFires() {
////        List<Facility> facilities = Arrays.asList(
////                facilityService.getFacilityById(102),
////                facilityService.getFacilityById(156)
////        );
////
////        List<Vehicle> availableVehicles = new ArrayList<>();
////        for (Facility facility : facilities) {
////            List<Long> vehicleIds = facility.getVehicleIdSet();
////            List<Vehicle> allVehicles = vehicleService.getAllVehicles();
////
////            // Filter available vehicles in each facility
////            for (Vehicle v : allVehicles) {
////                if (vehicleIds.contains(v.getId()) && !v.isOnMission()) {
////                    availableVehicles.add(v);
////                }
////            }
////        }
////
////        if (availableVehicles.isEmpty()) {
////            System.out.println("No available vehicles.");
////            return;
////        }
////
////        // Continuously move each vehicle to the nearest fire
////        while (true) {
////            List<Fire> fires = fireService.getFires();
////            List<Fire> activeFires = new ArrayList<>();
////            for (Fire fire : fires) {
////                if (!fire.isExtinguished()) {
////                    activeFires.add(fire);
////                }
////            }
////
////            if (activeFires.isEmpty()) {
////                System.out.println("No more active fires.");
////                break;
////            }
////
////            assignVehiclesToFires(availableVehicles, activeFires);
////        }
////    }
//
//    public void deployVehiclesToFires() {
//        List<Facility> facilities = Arrays.asList(
//                facilityService.getFacilityById(102),
//                facilityService.getFacilityById(156)
//        );
//
//        // Met à jour les véhicules dans les installations
//        updateVehiclesInFacilities(facilities);
//
//        List<Vehicle> availableVehicles = new ArrayList<>();
//        for (Facility facility : facilities) {
//            List<Long> vehicleIds = facility.getVehicleIdSet();
//            List<Vehicle> allVehicles = vehicleService.getAllVehicles();
//
//            // Filtrer les véhicules disponibles dans chaque facility
//            for (Vehicle v : allVehicles) {
//                if (vehicleIds.contains(v.getId()) && !v.isOnMission()) {
//                    availableVehicles.add(v);
//                }
//            }
//        }
//
//        if (availableVehicles.isEmpty()) {
//            System.out.println("No available vehicles.");
//            return;
//        }
//
//        // Déplace continuellement chaque véhicule vers le feu le plus proche
//        while (true) {
//            List<Fire> fires = fireService.getFires();
//            List<Fire> activeFires = new ArrayList<>();
//            for (Fire fire : fires) {
//                if (!fire.isExtinguished()) {
//                    activeFires.add(fire);
//                }
//            }
//
//            if (activeFires.isEmpty()) {
//                System.out.println("No more active fires.");
//                break;
//            }
//
//            assignVehiclesToFires(availableVehicles, activeFires);
//
//            // Break the loop after processing all active fires once
//            break;
//        }
//    }
//
//    private void assignVehiclesToFires(List<Vehicle> vehicles, List<Fire> fires) {
//        Set<Fire> assignedFires = new HashSet<>();
//        for (Fire fire : fires) {
//            LiquidType mostEfficientLiquid = getMostEfficientLiquid(fire.getType());
//
//            Vehicle bestVehicle = null;
//            double minDistance = Double.MAX_VALUE;
//
//            for (Vehicle vehicle : vehicles) {
//                if (!assignedFires.contains(fire) && vehicle.getLiquidType().equals(mostEfficientLiquid.toString())) {
//                    double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//                    if (distance < minDistance) {
//                        minDistance = distance;
//                        bestVehicle = vehicle;
//                    }
//                }
//            }
//
//            if (bestVehicle != null) {
//                assignedFires.add(fire);
//                moveVehicleToFire(bestVehicle, fire);
//            }
//        }
//    }
//
//    private void updateVehiclesInFacilities(List<Facility> facilities) {
//        for (Facility facility : facilities) {
//            List<Vehicle> vehicles = vehicleService.getVehiclesByFacilityId((long)facility.getId());
//            List<Fire> fires = fireService.getFires();
//
//            // Supprimez les véhicules inactifs uniquement lorsqu'ils sont dans une caserne
//            for (Vehicle vehicle : vehicles) {
//                if (!vehicle.isOnMission() && !isVehicleNeeded(vehicle, fires)) {
//                    if (vehicle.getLat() == facility.getLat() && vehicle.getLon() == facility.getLon()) {
//                        vehicleService.deleteVehicle(vehicle.getId());
//                        System.out.println("Vehicle " + vehicle.getId() + " deleted from facility " + facility.getId());
//                    } else {
//                        System.out.println("Vehicle " + vehicle.getId() + " cannot be deleted because it is not in the facility.");
//                    }
//                }
//            }
//
//            // Créez de nouveaux véhicules en fonction des besoins
//            for (Fire fire : fires) {
//                if (!fire.isExtinguished() && !isFireAssigned(fire, vehicles)) {
//                    Vehicle newVehicle = createVehicleForFire(fire, facility);
//                    if (newVehicle != null) {
//                        System.out.println("Vehicle " + newVehicle.getId() + " created in facility " + facility.getId());
//                    }
//                }
//            }
//        }
//    }
//
////    private void assignVehiclesToFires(List<Vehicle> vehicles, List<Fire> fires) {
////        Set<Fire> assignedFires = new HashSet<>();
////        for (Vehicle vehicle : vehicles) {
////            Fire nearestFire = findNearestFire(vehicle, fires, assignedFires);
////            if (nearestFire != null) {
////                assignedFires.add(nearestFire);
////                moveVehicleToFire(vehicle, nearestFire);
////            }
////        }
////    }
//
//    private boolean isVehicleNeeded(Vehicle vehicle, List<Fire> fires) {
//        // Définissez la logique pour déterminer si un véhicule est encore nécessaire
//        for (Fire fire : fires) {
//            if (!fire.isExtinguished()) {
//                LiquidType mostEfficientLiquid = getMostEfficientLiquid(fire.getType());
//                if (vehicle.getLiquidType().equals(mostEfficientLiquid.toString())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean isFireAssigned(Fire fire, List<Vehicle> vehicles) {
//        // Définissez la logique pour déterminer si un feu est déjà assigné à un véhicule
//        for (Vehicle vehicle : vehicles) {
//            if (vehicle.isOnMission() && vehicle.getLat() == fire.getLat() && vehicle.getLon() == fire.getLon()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private Vehicle createVehicleForFire(Fire fire, Facility facility) {
//        LiquidType mostEfficientLiquid = getMostEfficientLiquid(fire.getType());
//        VehicleType vehicleType = VehicleType.TRUCK; // Choisissez le type de véhicule approprié
//        if (facilityService.getAvailableSpace(facility.getId()) >= vehicleType.getSpaceUsedInFacility()) {
//            Vehicle newVehicle = vehicleService.createVehicle(
//                    "TRUCK",
//                    mostEfficientLiquid.toString(),
//                    0, // Commence avec 0
//                    0, // Commence avec 0
//                    0, // Crew member
//                    (long)facility.getId()
//            );
//            vehicleService.refillVehicle(newVehicle); // Remplissage du véhicule lors de la création
//            return newVehicle;
//        }
//        return null;
//    }
//
//
//
//    private Fire findNearestFire(Vehicle vehicle, List<Fire> fires, Set<Fire> assignedFires) {
//        Fire nearestFire = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Fire fire : fires) {
//            if (!assignedFires.contains(fire)) {
//                double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    nearestFire = fire;
//                }
//            }
//        }
//
//        return nearestFire;
//    }
//
//    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
//        VehicleType vehicleType = vehicle.getVehicleType();
//        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//        Facility nearestFacility = findNearestFacility(fire.getLat(), fire.getLon());
//        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), nearestFacility.getLat(), nearestFacility.getLon());
//        double requiredFuel = (distanceToFire + distanceToFacility) * vehicleType.getFuelConsumption();
//
//        if (vehicle.getFuel() < requiredFuel) {
//            System.out.println("Not enough fuel to reach the destination and return to base. Returning to base for refueling.");
//            vehicleService.moveBackToBase(vehicle);
//            return;
//        }
//
//        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326", 2, vehicleType.getMaxSpeed(), vehicleType.getFuelConsumption());
//        vehicle.setLat(fire.getLat());
//        vehicle.setLon(fire.getLon());
//        vehicle.setFuel(vehicle.getFuel() - (distanceToFire * vehicleType.getFuelConsumption()));
//        System.out.println("Vehicle " + vehicle.getId() + " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
//
//        extinguishFire(vehicle, fire);
//    }
//
//
////    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
////        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
////        Facility nearestFacility = findNearestFacility(fire.getLat(), fire.getLon());
////        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), nearestFacility.getLat(), nearestFacility.getLon());
////        double requiredFuel = (distanceToFire + distanceToFacility) * vehicle.getFuelConsumptionPerKm();
////
////        if (vehicle.getFuel() < requiredFuel) {
////            System.out.println("Not enough fuel to reach the destination and return to base. Returning to base for refueling.");
////            vehicleService.moveBackToBase(vehicle);
////            return;
////        }
////
////        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326", 2, 100, vehicle.getFuelConsumptionPerKm());
////        vehicle.setLat(fire.getLat());
////        vehicle.setLon(fire.getLon());
////        vehicle.setFuel(vehicle.getFuel() - (distanceToFire * vehicle.getFuelConsumptionPerKm()));
////        System.out.println("Vehicle " + vehicle.getId() + " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
////
////        // Extinguish the fire directly
////        extinguishFire(vehicle, fire);
////    }
//
//    // last updated
////    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
////        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
////        Facility nearestFacility = findNearestFacility(fire.getLat(), fire.getLon());
////        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), nearestFacility.getLat(), nearestFacility.getLon());
////        double requiredFuel = (distanceToFire + distanceToFacility) * vehicle.getFuelConsumptionPerKm();
////
////        if (vehicle.getFuel() < requiredFuel) {
////            System.out.println("Not enough fuel to reach the destination and return to base. Returning to base for refueling.");
////            vehicleService.moveBackToBase(vehicle);
////            return;
////        }
////
////        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326", 2, 100, vehicle.getFuelConsumptionPerKm());
////        vehicle.setLat(fire.getLat());
////        vehicle.setLon(fire.getLon());
////        vehicle.setFuel(vehicle.getFuel() - (distanceToFire * vehicle.getFuelConsumptionPerKm()));
////        System.out.println("Vehicle " + vehicle.getId() + " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
////
////        // Extinguish the fire directly
////        extinguishFire(vehicle, fire);
////    }
//
////    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
////        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
////        Facility nearestFacility = findNearestFacility(fire.getLat(), fire.getLon());
////        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), nearestFacility.getLat(), nearestFacility.getLon());
////        double requiredFuel = (distanceToFire + distanceToFacility) * vehicle.getFuelConsumptionPerKm();
////
////        if (vehicle.getFuel() < requiredFuel) {
////            System.out.println("Not enough fuel to reach the destination and return to base. Returning to base for refueling.");
////            vehicleService.moveBackToBase(vehicle);
////            return;
////        }
////
////        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326", 10, 500, vehicle.getFuelConsumptionPerKm());
////        vehicle.setLat(fire.getLat());
////        vehicle.setLon(fire.getLon());
////        vehicle.setFuel(vehicle.getFuel() - (distanceToFire * vehicle.getFuelConsumptionPerKm()));
////        System.out.println("Vehicle " + vehicle.getId() + " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
////        fire.setExtinguished(true);
////    }
//
//    private Facility findNearestFacility(double lat, double lon) {
//        List<Facility> facilities = Arrays.asList(
//                facilityService.getFacilityById(102),
//                facilityService.getFacilityById(156)
//        );
//
//        Facility nearestFacility = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Facility facility : facilities) {
//            double distance = calculateDistance(lat, lon, facility.getLat(), facility.getLon());
//            if (distance < minDistance) {
//                minDistance = distance;
//                nearestFacility = facility;
//            }
//        }
//
//        return nearestFacility;
//    }
//
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        double earthRadius = 6371; // km
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLon / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return earthRadius * c;
//    }
//
//    public void checkAndUpdateFireStatus() {
//        List<Fire> fires = fireService.getFires();
//        List<Vehicle> vehicles = vehicleService.getAllVehicles();
//
//        for (Vehicle vehicle : vehicles) {
//            if (vehicle.isOnMission()) {
//                for (Fire fire : fires) {
//                    if (!fire.isExtinguished() && vehicle.getLat() == fire.getLat() && vehicle.getLon() == fire.getLon()) {
//                        fire.setExtinguished(true);
//                        vehicle.setOnMission(false);
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    public void moveAllBackToBase() {
//        List<Vehicle> allVehicles = vehicleService.getVehiclesByTeamUUID("17951cd8-eae6-4f67-be27-d7500039556e");
//        for (Vehicle vehicle : allVehicles) {
//            vehicleService.moveBackToBase(vehicle);
//        }
//    }
//
//
//    private void extinguishFire(Vehicle vehicle, Fire fire) {
//        VehicleType vehicleType = vehicle.getVehicleType();
//        double liquidConsumptionPerSecond = vehicleType.getLiquidConsumption(); // Consommation de liquide par seconde
//        double totalLiquidNeeded = 10; // Suppose chaque feu nécessite 10 unités de liquide pour être éteint
//        double remainingLiquid = vehicle.getLiquidQuantity();
//
//        while (fire.getIntensity() > 0) {
//            if (remainingLiquid < liquidConsumptionPerSecond) {
//                System.out.println("Not enough extinguishing agent. Returning to base for refilling.");
//                vehicleService.moveBackToBase(vehicle);
//                return;
//            }
//
//            try {
//                Thread.sleep(1000); // Attendre 1 seconde
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            remainingLiquid -= liquidConsumptionPerSecond;
//            fire.setIntensity(fire.getIntensity() - liquidConsumptionPerSecond);
//
//            System.out.println("Vehicle " + vehicle.getId() + " extinguishing fire. Remaining Liquid: " + remainingLiquid + ". Fire intensity: " + fire.getIntensity());
//        }
//
//        vehicle.setLiquidQuantity((int) remainingLiquid);
//        fire.setExtinguished(true);
//        System.out.println("Fire at (" + fire.getLat() + ", " + fire.getLon() + ") extinguished by vehicle " + vehicle.getId());
//        System.out.println("Remaining Liquid quantity: " + vehicle.getLiquidQuantity());
//    }
//
//// last updated
////    private void extinguishFire(Vehicle vehicle, Fire fire) {
////        double liquidConsumptionPerSecond = 0.1; // Consommation de liquide par seconde
////        double totalLiquidNeeded = 10; // Suppose chaque feu nécessite 10 unités de liquide pour être éteint
////        double remainingLiquid = vehicle.getLiquidQuantity();
////
////        if (remainingLiquid < totalLiquidNeeded) {
////            System.out.println("Not enough extinguishing agent. Returning to base for refilling.");
////            vehicleService.moveBackToBase(vehicle);
////            return;
////        }
////
////        remainingLiquid -= totalLiquidNeeded;
////        fire.setExtinguished(true);
////
////        vehicle.setLiquidQuantity((int) remainingLiquid);
////        System.out.println("Fire at (" + fire.getLat() + ", " + fire.getLon() + ") extinguished by vehicle " + vehicle.getId());
////        System.out.println("Remaining Liquid quantity: " + vehicle.getLiquidQuantity());
////    }
//
////    private void extinguishFire(Vehicle vehicle, Fire fire) {
////        double liquidConsumptionPerSecond = 0.1; // Consommation de liquide par seconde
////        double totalLiquidNeeded = 10; // Suppose chaque feu nécessite 10 unités de liquide pour être éteint
////        double remainingLiquid = vehicle.getLiquidQuantity();
////
////        while (fire.getIntensity() > 0) {
////            if (remainingLiquid < liquidConsumptionPerSecond) {
////                System.out.println("Not enough extinguishing agent. Returning to base for refilling.");
////                vehicleService.moveBackToBase(vehicle);
////                return;
////            }
////
////            try {
////                Thread.sleep(10); // Attendre 1 seconde
////            } catch (InterruptedException e) {
////                Thread.currentThread().interrupt();
////            }
////
////            remainingLiquid -= liquidConsumptionPerSecond;
////            fire.setIntensity(fire.getIntensity() - liquidConsumptionPerSecond);
////
////            System.out.println("Vehicle " + vehicle.getId() + " extinguishing fire. Remaining Liquid: " + remainingLiquid + ". Fire intensity: " + fire.getIntensity());
////        }
////
////        vehicle.setLiquidQuantity((int) remainingLiquid);
////        fire.setExtinguished(true);
////        System.out.println("Fire at (" + fire.getLat() + ", " + fire.getLon() + ") extinguished by vehicle " + vehicle.getId());
////        System.out.println("Remaining Liquid quantity: " + vehicle.getLiquidQuantity());
////    }
//
//    private LiquidType getMostEfficientLiquid(FireEnum fireType) {
//        LiquidType mostEfficient = LiquidType.ALL;
//        float maxEfficiency = -1;
//
//        for (LiquidType liquidType : LiquidType.values()) {
//            float efficiency = liquidType.getEfficiency(fireType.toString());
//            if (efficiency > maxEfficiency) {
//                maxEfficiency = efficiency;
//                mostEfficient = liquidType;
//            }
//        }
//        return mostEfficient;
//    }
//
//    private void refillVehicle(Vehicle vehicle) {
//        int maxFuel = vehicle.getMaxFuel();
//        int maxLiquid = vehicle.getMaxLiquidQuantity();
//
//        while (vehicle.getFuel() < maxFuel || vehicle.getLiquidQuantity() < maxLiquid) {
//            try {
//                Thread.sleep(1000); // Attendre 1 seconde
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            if (vehicle.getFuel() < maxFuel) {
//                vehicle.setFuel(vehicle.getFuel() + 1);
//            }
//
//            if (vehicle.getLiquidQuantity() < maxLiquid) {
//                vehicle.setLiquidQuantity(vehicle.getLiquidQuantity() + 1);
//            }
//
//            System.out.println("Refueling vehicle " + vehicle.getId() + ": Fuel=" + vehicle.getFuel() + "/" + maxFuel + ", Liquid=" + vehicle.getLiquidQuantity() + "/" + maxLiquid);
//        }
//    }
//
//}
//
//
//
//














// 1 seule caserne
//package com.ProjetMajeure.Services.EmergencyManager;
//
//import com.ProjetMajeure.Services.Facility.Facility;
//import com.ProjetMajeure.Services.Facility.FacilityService;
//import com.ProjetMajeure.Services.Fire.Fire;
//import com.ProjetMajeure.Services.Fire.FireService;
//import com.ProjetMajeure.Services.Vehicle.Vehicle;
//import com.ProjetMajeure.Services.Vehicle.VehicleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class EmergencyManagerService {
//
//    @Autowired
//    private FireService fireService;
//
//    @Autowired
//    private VehicleService vehicleService;
//
//    @Autowired
//    private FacilityService facilityService;
//
//    public void deployVehiclesToFires() {
//        Facility facility = facilityService.getFacilityById(102);
//        List<Long> vehicleIds = facility.getVehicleIdSet();
//        List<Vehicle> allVehicles = vehicleService.getAllVehicles();
//
//        // Filter available vehicles in the facility
//        List<Vehicle> availableVehicles = new ArrayList<>();
//        for (Vehicle v : allVehicles) {
//            if (vehicleIds.contains(v.getId()) && !v.isOnMission()) {
//                availableVehicles.add(v);
//            }
//        }
//
//        if (availableVehicles.isEmpty()) {
//            System.out.println("No available vehicles.");
//            return;
//        }
//
//        // Continuously move each vehicle to the nearest fire
//        while (true) {
//            List<Fire> fires = fireService.getFires();
//            List<Fire> activeFires = new ArrayList<>();
//            for (Fire fire : fires) {
//                if (!fire.isExtinguished()) {
//                    activeFires.add(fire);
//                }
//            }
//
//            if (activeFires.isEmpty()) {
//                System.out.println("No more active fires.");
//                break;
//            }
//
//            assignVehiclesToFires(availableVehicles, activeFires);
//        }
//    }
//
//    private void assignVehiclesToFires(List<Vehicle> vehicles, List<Fire> fires) {
//        Set<Fire> assignedFires = new HashSet<>();
//        for (Vehicle vehicle : vehicles) {
//            Fire nearestFire = findNearestFire(vehicle, fires, assignedFires);
//            if (nearestFire != null) {
//                assignedFires.add(nearestFire);
//                moveVehicleToFire(vehicle, nearestFire);
//            }
//        }
//    }
//
//    private Fire findNearestFire(Vehicle vehicle, List<Fire> fires, Set<Fire> assignedFires) {
//        Fire nearestFire = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Fire fire : fires) {
//            if (!assignedFires.contains(fire)) {
//                double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    nearestFire = fire;
//                }
//            }
//        }
//
//        return nearestFire;
//    }
//
//    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
//        double distanceToFire = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//        double distanceToFacility = calculateDistance(fire.getLat(), fire.getLon(), facilityService.getFacilityById(102).getLat(), facilityService.getFacilityById(102).getLon());
//        double requiredFuel = (distanceToFire + distanceToFacility) * vehicle.getFuelConsumptionPerKm();
//
//        if (vehicle.getFuel() < requiredFuel) {
//            System.out.println("Not enough fuel to reach the destination and return to base. Returning to base for refueling.");
//            vehicleService.moveBackToBase(vehicle);
//            return;
//        }
//
//        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326", 10, 50, vehicle.getFuelConsumptionPerKm());
//        vehicle.setLat(fire.getLat());
//        vehicle.setLon(fire.getLon());
//        vehicle.setFuel(vehicle.getFuel() - (distanceToFire * vehicle.getFuelConsumptionPerKm()));
//        System.out.println("Vehicle " + vehicle.getId() + " moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
//        fire.setExtinguished(true);
//    }
//
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        double earthRadius = 6371; // km
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLon / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return earthRadius * c;
//    }
//
//    public void checkAndUpdateFireStatus() {
//        List<Fire> fires = fireService.getFires();
//        List<Vehicle> vehicles = vehicleService.getAllVehicles();
//
//        for (Vehicle vehicle : vehicles) {
//            if (vehicle.isOnMission()) {
//                for (Fire fire : fires) {
//                    if (!fire.isExtinguished() && vehicle.getLat() == fire.getLat() && vehicle.getLon() == fire.getLon()) {
//                        fire.setExtinguished(true);
//                        vehicle.setOnMission(false);
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    public void moveAllBackToBase() {
//        List<Vehicle> allVehicles = vehicleService.getVehiclesByTeamUUID("17951cd8-eae6-4f67-be27-d7500039556e");
//        for (Vehicle vehicle : allVehicles) {
//            vehicleService.moveBackToBase(vehicle);
//        }
//    }
//
//}






//package com.ProjetMajeure.Services.EmergencyManager;
//
//import com.ProjetMajeure.Services.Facility.Facility;
//import com.ProjetMajeure.Services.Facility.FacilityService;
//import com.ProjetMajeure.Services.Fire.Fire;
//import com.ProjetMajeure.Services.Fire.FireService;
//import com.ProjetMajeure.Services.Vehicle.Vehicle;
//import com.ProjetMajeure.Services.Vehicle.VehicleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class EmergencyManagerService {
//
//    @Autowired
//    private FireService fireService;
//
//    @Autowired
//    private VehicleService vehicleService;
//
//    @Autowired
//    private FacilityService facilityService;
//
//    public void deployOneVehicleToFires() {
//        Facility facility = facilityService.getFacilityById(102);
//        List<Long> vehicleIds = facility.getVehicleIdSet();
//        List<Vehicle> allVehicles = vehicleService.getAllVehicles();
//
//        // Filter available vehicles in the facility
//        Vehicle vehicle = null;
//        for (Vehicle v : allVehicles) {
//            if (vehicleIds.contains(v.getId()) && !v.isOnMission()) {
//                vehicle = v;
//                break;
//            }
//        }
//
//        if (vehicle == null) {
//            System.out.println("No available vehicles.");
//            return;
//        }
//
//        // Continuously move the vehicle to the nearest fire
//        while (true) {
//            List<Fire> fires = fireService.getFires();
//            Fire nearestFire = findNearestFire(vehicle, fires);
//
//            if (nearestFire == null) {
//                System.out.println("No more active fires.");
//                break;
//            }
//
//            moveVehicleToFire(vehicle, nearestFire);
//        }
//    }
//
//    private Fire findNearestFire(Vehicle vehicle, List<Fire> fires) {
//        Fire nearestFire = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Fire fire : fires) {
//            if (!fire.isExtinguished()) {
//                double distance = calculateDistance(vehicle.getLat(), vehicle.getLon(), fire.getLat(), fire.getLon());
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    nearestFire = fire;
//                }
//            }
//        }
//
//        return nearestFire;
//    }
//
//    private void moveVehicleToFire(Vehicle vehicle, Fire fire) {
//        vehicleService.moveVehicle(vehicle.getId(), fire.getLon(), fire.getLat(), "EPSG:4326");
//        vehicle.setLat(fire.getLat());
//        vehicle.setLon(fire.getLon());
//        System.out.println("Vehicle moved to fire at (" + fire.getLat() + ", " + fire.getLon() + ")");
//        fire.setExtinguished(true);
//    }
//
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        double earthRadius = 6371; // km
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLon / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return earthRadius * c;
//    }
//
//    public void checkAndUpdateFireStatus() {
//        List<Fire> fires = fireService.getFires();
//        List<Vehicle> vehicles = vehicleService.getAllVehicles();
//
//        for (Vehicle vehicle : vehicles) {
//            if (vehicle.isOnMission()) {
//                for (Fire fire : fires) {
//                    if (!fire.isExtinguished() && vehicle.getLat() == fire.getLat() && vehicle.getLon() == fire.getLon()) {
//                        fire.setExtinguished(true);
//                        vehicle.setOnMission(false);
//                        break;
//                    }
//                }
//            }
//        }
//    }
//}
