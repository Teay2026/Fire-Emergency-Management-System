package com.ProjetMajeure.Services.Fire;

import java.util.HashMap;
import java.util.Map;

public enum LiquidType {
    //https://www.seton.fr/quel-extincteur-pour-quel-feu.html
    //TODO OPTIMIZE EFFICIENCY MATRIX
    ALL(0.1f, 0.1f, 0.1f, 0.1f, 0.1f), // Efficacité générale pour tous les types de feux
    WATER(0.8f, 0.8f, 0.0f, 0.0f, 0.0f), // Efficacité de l'eau
    POWDER(0.6f, 0.6f, 1.0f, 0.0f, 0.0f), // Efficacité de la poudre
    SPECIAL_POWDER(0.0f, 0.0f, 0.0f, 1.0f, 0.0f), // Efficacité de la poudre spéciale
    CARBON_DIOXIDE(0.0f, 0.7f, 0.0f, 0.0f, 1.0f), // Efficacité du dioxyde de carbone
    FOAM(0.7f, 1.0f, 0.0f, 0.0f, 0.0f); // Efficacité de la mousse


    private Map<String,Float> fireEfficiencyMap;

    // Constructeur de l'énumération LiquidType
    LiquidType(float a_Efficiency,float b_Efficiency,float c_Efficiency,float d_Efficiency,float e_Efficiency){
        fireEfficiencyMap=new HashMap<String, Float>();
        fireEfficiencyMap.put(FireEnum.A.toString(),a_Efficiency);
        fireEfficiencyMap.put(FireEnum.B_Alcohol.toString(),b_Efficiency);
        fireEfficiencyMap.put(FireEnum.B_Gasoline.toString(),b_Efficiency);
        fireEfficiencyMap.put(FireEnum.B_Plastics.toString(),b_Efficiency);
        fireEfficiencyMap.put(FireEnum.C_Flammable_Gases.toString(),c_Efficiency);
        fireEfficiencyMap.put(FireEnum.D_Metals.toString(),d_Efficiency);
        fireEfficiencyMap.put(FireEnum.E_Electric.toString(),e_Efficiency);
    }

    // Méthode pour obtenir l'efficacité pour un type de feu donné
    public float getEfficiency(String fireType) {
        return fireEfficiencyMap.get(fireType);
    }



}
