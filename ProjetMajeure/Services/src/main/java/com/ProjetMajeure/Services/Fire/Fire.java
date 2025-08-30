package com.ProjetMajeure.Services.Fire;

public class Fire {
    // Déclaration des champs privés
    private int id; // Identifiant du feu
    private FireEnum type; // Type de feu (enum)
    private double lat; // Latitude de la position du feu
    private double lon; // Longitude de la position du feu
    private double intensity; // Intensité du feu
    private double range; // Portée du feu
    private boolean extinguished; // Statut indiquant si le feu est éteint

    public Fire(int id, FireEnum type, double lat, double lon, double intensity, double range) {
        this.id = id;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.intensity = intensity;
        this.range = range;
        this.extinguished = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FireEnum getType() {
        return type;
    }

    public void setType(FireEnum type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public boolean isExtinguished() {
        return extinguished;
    }

    public void setExtinguished(boolean extinguished) {
        this.extinguished = extinguished;
    }

    @Override
    public String toString() {
        return "Fire{" +
                "id=" + id +
                ", type=" + type +
                ", lat=" + lat +
                ", lon=" + lon +
                ", intensity=" + intensity +
                ", range=" + range +
                ", extinguished=" + extinguished +
                '}';
    }
}