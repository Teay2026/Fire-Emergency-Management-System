package com.ProjetMajeure.Services.Vehicle;

public class VehicleDto {
    // Déclaration des champs privés
    private Long id; // Identifiant unique du véhicule
    private double lon; // Longitude de la position du véhicule
    private double lat; // Latitude de la position du véhicule
    private String type; // Type de véhicule (e.g., "Pumper Truck", "Fire Engine")
    private String liquidType; // Type de liquide utilisé par le véhicule (e.g., "Water", "Foam")
    private int liquidQuantity; // Quantité de liquide dans le véhicule
    private int fuel; // Niveau de carburant dans le véhicule
    private int crewMember; // Nombre de membres d'équipage
    private Long facilityRefID; // Référence de l'identifiant de l'installation associée au véhicule

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

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
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
}
