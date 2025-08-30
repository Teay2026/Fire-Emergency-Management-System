package com.ProjetMajeure.Services.Facility;

import java.util.List;

public class Facility {
    // Déclaration des champs privés
    private int id; // Identifiant de l'installation
    private double lon; // Longitude de l'installation
    private double lat; // Latitude de l'installation
    private String name; // Nom de l'installation
    private int maxVehicleSpace; // Capacité maximale de véhicules
    private int peopleCapacity; // Capacité maximale de personnes
    private List<Long> vehicleIdSet; // Liste des identifiants des véhicules
    private List<Long> peopleIdSet; // Liste des identifiants des personnes
    private String teamUuid; // UUID de l'équipe

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxVehicleSpace() {
        return maxVehicleSpace;
    }

    public void setMaxVehicleSpace(int maxVehicleSpace) {
        this.maxVehicleSpace = maxVehicleSpace;
    }

    public int getPeopleCapacity() {
        return peopleCapacity;
    }

    public void setPeopleCapacity(int peopleCapacity) {
        this.peopleCapacity = peopleCapacity;
    }

    public List<Long> getVehicleIdSet() {
        return vehicleIdSet;
    }

    public void setVehicleIdSet(List<Long> vehicleIdSet) {
        this.vehicleIdSet = vehicleIdSet;
    }

    public List<Long> getPeopleIdSet() {
        return peopleIdSet;
    }

    public void setPeopleIdSet(List<Long> peopleIdSet) {
        this.peopleIdSet = peopleIdSet;
    }

    public String getTeamUuid() {
        return teamUuid;
    }

    public void setTeamUuid(String teamUuid) {
        this.teamUuid = teamUuid;
    }


}
