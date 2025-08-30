// Function to send vehicle creation data
function createVehicle() {
    console.log("Create vehicule lancé");
    // Event handler for the vehicle creation form
    document.getElementById('newVehicleForm').addEventListener('submit', function(event) {
        event.preventDefault(); 

        // Retrieve values from the form fields
        const newVehicleType = document.getElementById('newVehicleType').value;
        const newVehicleLiquidType = document.getElementById('newVehicleLiquidType').value;
        const newVehicleFacility = document.getElementById('newVehicleFacility').value;
        const newVehicleCrewMembers = document.getElementById('newVehicleCrewMembers').value;

        // Log the complete request data before sending
        console.log('Complete request data:', newVehicleType);

        // Retrieve data of the selected facility
        const facility = facilityData[newVehicleFacility];
        if (!facility) {
            alert('Error: Facility not found.');
            return;
        }

        // Data to send in the POST request
        const newData = {
            "type": newVehicleType,
            "liquidType": newVehicleLiquidType,
            "liquidQuantity": 0,
            "fuel": 0,
            "crewMember": newVehicleCrewMembers,
            "facilityRefID": newVehicleFacility,
            "lat": facility.lat,
            "lon": facility.lon
        };

        // Log the complete request data before sending
        console.log('Complete request data:', newVehicleType);

        // Send POST request with the data
        fetch(`http://localhost:8080/vehicles/${teamuuid}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newData)
        })
        .then(response => response.json())
        .then(data => {
            console.log('New vehicle created:', data); 
            // Return the ID of the created vehicle
            if (data && data.id) {
                console.log('New vehicle ID:', data.id);
                alert(`Vehicle created successfully! ID: ${data.id}`);
                // Optional: refresh vehicle data to display the new vehicle
                fetchVehicleData();
            } else {
                console.log('No vehicle ID received');
                alert('Error: No vehicle ID received.');
            }
        })
    });
}


// Event listener for the delete vehicle button
document.getElementById('addVehicleButton').addEventListener('click', function() {
    createVehicle();
});

// Function to send vehicle deletion request
function deleteVehicle() {
    // Retrieve the selected vehicle ID
    const vehicleId = document.getElementById('deleteVehicleChoice').value;

    // Send DELETE request for the selected vehicle ID
    fetch(`http://localhost:8080/vehicles/${teamuuid}/${vehicleId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert(`Vehicle with ID ${vehicleId} deleted successfully.`);
            // Optional: refresh vehicle data after deletion
            fetchVehicleData();
        } else {
            alert(`Error deleting vehicle with ID ${vehicleId}.`);
        }
    })
}

// Event listener for the delete vehicle button
document.getElementById('deleteVehicleButton').addEventListener('click', function() {
    deleteVehicle();
});


// Fonction pour déployer tous les véhicules vers la base
function MoveAllBackToBase() {
    fetch('http://localhost:8080/emergency-manager/moveAllBackToBase', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert('All vehicles are moved back to base.');
        } else {
            alert('Error to move back to base.');
        }
    })
}


// Event listener for the delete vehicle button
document.getElementById('moveAllToBaseButton').addEventListener('click', function() {
    MoveAllBackToBase();
});

// Fonction pour déployer tous les véhicules vers la base
function deployAllVehicles() {
    fetch('http://localhost:8080/emergency-manager/deploy/all-vehicles', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert('All vehicles deployed successfully.');
        } else {
            alert('Error deploying vehicles.');
        }
    })
    .catch(error => {
        console.error('Error deploying vehicles:', error);
        alert('Error deploying vehicles.');
    });
}


// Event listener for the delete vehicle button
document.getElementById('deployButton').addEventListener('click', function() {
    deployAllVehicles();
});