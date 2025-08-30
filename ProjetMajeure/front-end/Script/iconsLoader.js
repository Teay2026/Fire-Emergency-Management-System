// Define the fire icon
var fireIcon = L.icon({
    iconUrl: '../icons/fire.png', 
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -14]
});

// Define the vehicle icons

// Mapping des types de véhicules aux chemins des fichiers SVG originaux et modifiés
var vehicleIconPaths = {
    'CAR': '../icons/car.png',
    'FIRE_ENGINE': '../icons/fire-engine.png',
    'PUMPER_TRUCK': '../icons/pumper-truck.png',
    'WATER_TENDERS': '../icons/water-tender.png',
    'TURNTABLE_LADDER_TRUCK': '../icons/turntable-ladder-truck.png',
    'TRUCK': '../icons/truck.png'
};

// Objet pour stocker les icônes par type
var icons = {};


for (const [type, path] of Object.entries(vehicleIconPaths)) {
    // Icône par défaut
    icons[type] = L.icon({
        iconUrl: path,
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -18]
    });

    // Icône pour les véhicules de la facility 102 avec une couleur différente
    icons[type + '_facility102'] = L.icon({
        iconUrl: path.replace('../icons/', '../icons/iconizer-'), // Chemin vers la version colorée
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -18],
    });
}
// const carIcon = L.icon({
//     iconUrl: '../icons/car.png', 
//     iconSize: [28, 28],
//     iconAnchor: [14, 14],
//     popupAnchor: [0, -16] 
// });
// const fireEngineIcon = L.icon({
//     iconUrl: '../icons/fire-engine.png', 
//     iconSize: [28, 28],
//     iconAnchor: [14, 14],
//     popupAnchor: [0, -16] 
// });
// const pumperTruckIcon = L.icon({
//     iconUrl: '../icons/pumper-truck.png', 
//     iconSize: [34, 34],
//     iconAnchor: [18, 18],
//     popupAnchor: [0, -20] 
// });
// const truckIcon = L.icon({
//     iconUrl: '../icons/truck.png', 
//     iconSize: [32, 32],
//     iconAnchor: [16, 16],
//     popupAnchor: [0, -18] 
// });
// const turntableLadderTruckIcon = L.icon({
//     iconUrl: '../icons/turntable-ladder-truck.png', 
//     iconSize: [32, 32],
//     iconAnchor: [16, 16],
//     popupAnchor: [0, -18] 
// });
// const waterTenderIcon = L.icon({
//     iconUrl: '../icons/water-tender.png', 
//     iconSize: [30, 30],
//     iconAnchor: [15, 15],
//     popupAnchor: [0, -17] 
// });

var facilityIcon = L.icon({
    iconUrl: '../icons/facility.png', 
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -18]
});
var littleFacilityIcon = L.icon({
    iconUrl: '../icons/facility-little.png', 
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -14]
});