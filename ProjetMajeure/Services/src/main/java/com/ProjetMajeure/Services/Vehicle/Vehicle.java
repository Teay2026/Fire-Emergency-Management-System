package com.ProjetMajeure.Services.Vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double lon; // Longitude de la position du véhicule
    private double lat; // Latitude de la position du véhicule
    private String type; // Type de véhicule
    private String liquidType; // Type de liquide utilisé par le véhicule
    private int liquidQuantity; // Quantité de liquide disponible dans le véhicule
    private double fuel; // Quantité de carburant disponible dans le véhicule
    private int crewMember; // Nombre de membres d'équipage
    private Long facilityRefID; // Référence à l'ID de l'installation associée au véhicule
    private boolean onMission; // Indique si le véhicule est en mission
    private boolean isRefueling; // Indique si le véhicule est en train de se ravitailler

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLiquidType() {
        return liquidType;
    }

    public void setLiquidType(String liquidType) {
        this.liquidType = liquidType;
    }

    public int getLiquidQuantity() {
        return liquidQuantity;
    }

    public void setLiquidQuantity(int liquidQuantity) {
        this.liquidQuantity = liquidQuantity;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public int getCrewMember() {
        return crewMember;
    }

    public void setCrewMember(int crewMember) {
        this.crewMember = crewMember;
    }

    public Long getFacilityRefID() {
        return facilityRefID;
    }

    public void setFacilityRefID(Long facilityRefID) {
        this.facilityRefID = facilityRefID;
    }

    public boolean isOnMission() {
        return onMission;
    }

    public void setOnMission(boolean onMission) {
        this.onMission = onMission;
    }

    public int getMaxFuel() {
        return (int) getVehicleType().getFuelCapacity();
    }

    public double getFuelConsumptionPerKm() {
        return getVehicleType().getFuelConsumption();
    }

    public int getMaxLiquidQuantity() {
        return (int) getVehicleType().getLiquidCapacity();
    }

    public double getLiquidConsumptionPerSecond() {
        return getVehicleType().getLiquidConsumption();
    }

    public VehicleType getVehicleType() {
        return VehicleType.valueOf(type);
    }

    public boolean isRefueling() {
        return isRefueling;
    }

    public void setRefueling(boolean refueling) {
        isRefueling = refueling;
    }
}
