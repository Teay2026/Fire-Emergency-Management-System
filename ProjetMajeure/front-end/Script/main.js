const teamuuid = '17951cd8-eae6-4f67-be27-d7500039556e';

//Initializing the markers array
var facilityMarkers = [];
var fireMarkers = [];
var vehicleMarkers = [];

// Select elements variables Initialisation
const fireSelectElement = document.getElementById('fireTypeFilter');
const vehicleSelectElement = document.getElementById('vehicleTypeFilter');

// Initialize the map
var map = L.map('map').setView([45.75, 4.85], 13); // Coordinates of Lyon



// Add OpenStreetMap tiles
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);


// Function to update vehicles on the map
async function update() {
    fetchFacilityData();
    fetchFireData();
    fetchVehicleData();
    //updateVehicleDropdowns(data);
}


// Call the function initially to populate the map
update();

// Refresh map data every 5 seconds (5000 milliseconds)
setInterval(update, 500);