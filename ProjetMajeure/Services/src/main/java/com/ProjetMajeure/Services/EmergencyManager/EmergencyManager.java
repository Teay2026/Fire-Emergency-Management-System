package com.ProjetMajeure.Services.EmergencyManager;

public class EmergencyManager {

    // Champs privés pour l'ID du véhicule, l'ID du feu, et le statut de la tâche d'urgence
    private Long vehicleId;
    private Long fireId;
    private String status; // Status of the emergency task, e.g., "assigned", "extinguishing", "completed"

    // Constructeur par défaut
    public EmergencyManager() {}

    // Constructeur avec paramètres pour initialiser tous les champs
    public EmergencyManager(Long vehicleId, Long fireId, String status) {
        this.vehicleId = vehicleId;
        this.fireId = fireId;
        this.status = status;
    }

    // Méthode getter pour vehicleId
    public Long getVehicleId() {
        return vehicleId;
    }

    // Méthode setter pour vehicleId
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    // Méthode getter pour fireId
    public Long getFireId() {
        return fireId;
    }

    // Méthode setter pour fireId
    public void setFireId(Long fireId) {
        this.fireId = fireId;
    }

    // Méthode getter pour status
    public String getStatus() {
        return status;
    }

    // Méthode setter pour status
    public void setStatus(String status) {
        this.status = status;
    }

    // Méthode toString redéfinie pour fournir une représentation sous forme de chaîne de l'objet EmergencyManager
    @Override
    public String toString() {
        return "EmergencyManager{" +
                "vehicleId=" + vehicleId +
                ", fireId=" + fireId +
                ", status='" + status + '\'' +
                '}';
    }
}
