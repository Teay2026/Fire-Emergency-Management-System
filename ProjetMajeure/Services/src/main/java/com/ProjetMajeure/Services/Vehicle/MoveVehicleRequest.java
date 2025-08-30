package com.ProjetMajeure.Services.Vehicle;

public class MoveVehicleRequest {
    // Déclaration des champs privés
    private double lon; // Longitude de la nouvelle position du véhicule
    private double lat; // Latitude de la nouvelle position du véhicule
    private String projection; // Système de projection utilisé pour les coordonnées

    public MoveVehicleRequest(double lon, double lat, String projection) {
        this.lon = lon;
        this.lat = lat;
        this.projection = projection;
    }

    // Getters and Setters
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

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

}
