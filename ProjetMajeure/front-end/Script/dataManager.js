// Fetch facility data from the API
let facilityData = {};
function fetchFacilityData() {
    var markersToKeep  = [];
    fetch('http://localhost:8080/facility')
        .then(response => response.json())
        .then(data => {
            //console.log('Facility data:', data); // Debugging: log the fetched data
            facilityData = {};
        
            data.forEach(facility => {
                facilityData[facility.id] = facility;

                var oldMarkerArray = facilityMarkers.filter(item => item.id == facility.id);
                if (oldMarkerArray.length > 0){
                    oldMarker = oldMarkerArray[0];
                    oldMarker.marker.setLatLng([facility.lat, facility.lon]);
                    oldMarker.marker.getPopup().setContent(
                        `Team: ${facility.name}
                        <br>id: ${facility.id}`
                    );   
                    markersToKeep.push(oldMarker);
                }
                else if (facility.lat && facility.lon) {
                    console.log("New Facility: ", facility)
                    
                    var icon;
                    if(facility.name == "FA_Flambis" || facility.name == "FA_FLAMBIS_2"){icon = facilityIcon}
                    else{icon = littleFacilityIcon}
                    
                    var marker = L.marker(
                        [facility.lat, facility.lon], 
                        {icon: icon})
                    marker.bindPopup(
                        `Team: ${facility.name}
                        <br>id: ${facility.id}`
                    );
                    markersToKeep.push({ id: facility.id, category: "facility", marker: marker});
                    marker.addTo(map);
                } else {
                    console.warn('Invalid coordinates for facility:', facility); // Debugging: log invalid data
                }
            });
            facilityMarkers.filter(item => !(item.id in facilityData)).forEach(item => {
                map.removeLayer(item.marker);
                console.log("Removed facility: ", item)
            });
            facilityMarkers = markersToKeep;
        })
        .catch(error => {
            console.error('Error fetching facility data:', error);
        });
}


// Fetch fire data from the API
function fetchFireData() {
    fireIds = [];
    var markersToKeep  = [];
    fetch('http://localhost:8080/fires')
        .then(response => response.json())
        .then(data => {
            //console.log('Fire data:', data); // Debugging: log the fetched data
            data.forEach(fire => {
                fireIds.push(fire.id);
                var fireRadius = 10*Math.sqrt(fire.range/Math.PI)

                var oldMarkerArray = fireMarkers.filter(item => item.id == fire.id);
                if (oldMarkerArray.length > 0){
                    oldMarker = oldMarkerArray[0];
                    oldMarker.marker.setLatLng([fire.lat, fire.lon]);
                    oldMarker.marker.getPopup().setContent(
                        `Fire class: ${fire.type}
                        <br>id: ${fire.id}
                        <br>Intensity: ${fire.intensity}
                        <br>Range: ${fire.range}m²`
                    );   
                    markersToKeep.push(oldMarker);
                }
                else if (fire.lat && fire.lon) {
                    console.log("New Fire: ", fire)
                    var fireCircle = L.circle(
                        [fire.lat, fire.lon], 
                        {color: 'red',     
                        fillColor: '#f03', 
                        fillOpacity: 0.5,  
                        radius: fireRadius
                    })
                    var marker = L.marker(
                        [fire.lat, fire.lon], 
                        {icon: fireIcon})
                    marker.bindPopup(
                        `Fire class: ${fire.type}
                        <br>id: ${fire.id}
                        <br>Intensity: ${fire.intensity}
                        <br>Range: ${fire.range}m²`
                    );
                    marker.setZIndexOffset(500);
                    markersToKeep.push({ id: fire.id, category: "fire", marker: marker, circle: fireCircle, type: fire.type,
                                        intensity: fire.intensity, range: fire.range});

                    fireCircle.addTo(map)
                    marker.addTo(map);
                } else {
                    console.warn('Invalid coordinates for fire:', fire); // Debugging: log invalid data
                }
            });
            fireMarkers.filter(item => !(fireIds.filter(id => id == item.id))).forEach(item => {
                map.removeLayer(item.marker);
                map.removeLayer(item.circle);
                console.log("Removed fire: ", item)
            });
            fireMarkers = markersToKeep;
        updateVehicleDropdowns(data); 
        })
        .catch(error => {
            console.error('Error fetching fire data:', error);
        });
}

// Fetch vehicle data from the API
// Fonction pour récupérer et afficher les données des véhicules
function fetchVehicleData() {
    fetch('http://localhost:8080/vehicles')
        .then(response => response.json())
        .then(data => {
            //console.log('Vehicle data:', data); // Debugging: log the fetched data
            updateVehicleDropdowns(data);

            const markersToKeep = new Set();

            // Iterate over the fetched vehicle data
            data.forEach(vehicle => {
                if (vehicle.lat && vehicle.lon) {
                    let existingMarker = vehicleMarkers.find(item => item.id === vehicle.id && item.category === "vehicle");

                    if (existingMarker) {
                        // Update the existing marker's position and popup content
                        existingMarker.marker.setLatLng([vehicle.lat, vehicle.lon]);
                        existingMarker.marker.getPopup().setContent(
                            `Type: ${vehicle.type}
                            <br>ID: ${vehicle.id}
                            <br>Liquid Type: ${vehicle.liquidType}
                            <br>Liquid Quantity: ${vehicle.liquidQuantity}L
                            <br>Fuel: ${vehicle.fuel}L
                            <br>Crew Members: ${vehicle.crewMember}`
                        );
                        markersToKeep.add(existingMarker);
                    } else {
                        // Create a new marker for the new vehicle
                        const icon = vehicle.facilityRefID === 102 || vehicle.facilityRefID === 156 ? icons[vehicle.type + '_facility102'] : icons[vehicle.type];
                        const ours = vehicle.facilityRefID in [102, 156];

                        const marker = L.marker([vehicle.lat, vehicle.lon], { icon: icon });
                        marker.bindPopup(
                            `Type: ${vehicle.type}
                            <br>ID: ${vehicle.id}
                            <br>Liquid Type: ${vehicle.liquidType}
                            <br>Liquid Quantity: ${vehicle.liquidQuantity}L
                            <br>Fuel: ${vehicle.fuel}L
                            <br>Crew Members: ${vehicle.crewMember}`
                        );
                        if(!ours){marker.setOpacity(0.9)}
                        marker.setZIndexOffset(300); // Ensure vehicle markers are in front
                        marker.addTo(map);

                        const markerObj = { id: vehicle.id, category: "vehicle", marker: marker, type: vehicle.type , facilityRefID: vehicle.facilityRefID};
                        vehicleMarkers.push(markerObj);
                        markersToKeep.add(markerObj);
                    }
                } else {
                    console.warn('Invalid coordinates for vehicle:', vehicle);
                }
            });

            // Remove vehicle markers that are no longer in the data
            vehicleMarkers.filter(item =>!markersToKeep.has(item)).forEach(item => {
                map.removeLayer(item.marker);
            });

            // Update the markers list to only include the current markers
            vehicleMarkers = Array.from(markersToKeep);
        })
        .catch(error => {
            console.error('Error fetching vehicle data:', error);
        });
}

function updateVehicleDropdowns(data) {
    const deleteVehicleChoice = document.getElementById('deleteVehicleChoice');
    const updateVehicleChoice = document.getElementById('updateVehicleChoice');
    const moveVehicleChoice = document.getElementById('moveVehicle');

    // Clear existing options in the dropdowns
    deleteVehicleChoice.innerHTML = '<option value="none">Choose a vehicle</option>';
    updateVehicleChoice.innerHTML = '<option value="none">Choose a vehicle</option>';
    moveVehicleChoice.innerHTML = '<option value="none">Choose a vehicle</option>';

    // Track unique vehicle types for the filter
    const uniqueVehicleTypes = new Set();

    data.forEach(vehicle => {
        if(vehicle.facilityRefID == 102 || vehicle.facilityRefID == 156)
            {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.text = vehicle.id;
    
                deleteVehicleChoice.add(option.cloneNode(true));
                updateVehicleChoice.add(option.cloneNode(true));
                moveVehicleChoice.add(option.cloneNode(true));
    
                // Add the vehicle type to the set of unique types
                uniqueVehicleTypes.add(vehicle.type);
            }
    });

    // Populate vehicle type filter
    uniqueVehicleTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.text = type;
    });
}

// Function to filter markers
var fireType = "All";  var fireRange = 50; var fireIntensity = 50;
function filterFires() {
    fireMarkers.forEach(item => {
        if (fireType === 'All' || item.type == fireType) {
            item.marker.addTo(map);
            item.circle.addTo(map);
            if (item.range <= fireRange) {
                item.marker.addTo(map);
                item.circle.addTo(map);
                if (item.intensity <= fireIntensity) {
                    item.marker.addTo(map);
                    item.circle.addTo(map);
                } 
                else {
                    map.removeLayer(item.marker);
                    map.removeLayer(item.circle)
                }
            } 
            else {
                map.removeLayer(item.marker);
                map.removeLayer(item.circle)
            }
        } else {
            map.removeLayer(item.marker);
            map.removeLayer(item.circle)
        }

    });
}


var vehicleType = 'All'; var facilityFilter = "All";
// Function to filter vehicles
function filterVehicles() {
    vehicleMarkers.forEach(item => {
        if ((facilityFilter == "102" || facilityFilter == "OURS") && item.facilityRefID == 102){
            if (vehicleType === 'All' || item.type === vehicleType) {
                item.marker.addTo(map);
            } else {
                map.removeLayer(item.marker);
            }
        }
        else if ((facilityFilter == "156" || facilityFilter == "OURS") && item.facilityRefID == 156){
            if (vehicleType === 'All' || item.type === vehicleType) {
                item.marker.addTo(map);
            } else {
                map.removeLayer(item.marker);
            }
        }
        else if (facilityFilter == "OTHERS" && (item.facilityRefID != 102) && (item.facilityRefID != 156)){
            if (vehicleType === 'All' || item.type === vehicleType) {
                item.marker.addTo(map);
            } else {
                map.removeLayer(item.marker);
            }
        }
        else if(facilityFilter == "All"){
            if (vehicleType === 'All' || item.type === vehicleType) {
                item.marker.addTo(map);
            } else {
                map.removeLayer(item.marker);
            }
        }
        else{
            map.removeLayer(item.marker);
        }
    });
}



// Event listener for vehicle type filter selection
document.getElementById('vehicleTypeFilter').addEventListener('change', function (event) {
    vehicleType = event.target.value;
    filterVehicles();
});

// Event listener for vehicle type filter selection
document.getElementById('facilityTypeFilter').addEventListener('change', function (event) {
    facilityFilter = event.target.value;
    filterVehicles();
});



// Event listener for fire type filter selection
document.getElementById('fireTypeFilter').addEventListener('change', function (event) {
    fireType = event.target.value;
    filterFires();
});

// Event listener for fire range filter
document.getElementById('rangeSlider').addEventListener('change', function (event) {
    fireRange = event.target.value;
    document.getElementById('rangeText').innerHTML = fireRange.toString();
    filterFires();
});

// Event listener for fire intensity filter
document.getElementById('intensitySlider').addEventListener('change', function (event) {
    fireIntensity = event.target.value;
    document.getElementById('intensityText').innerHTML = fireIntensity.toString();
    filterFires();
});







