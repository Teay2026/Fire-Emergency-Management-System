// Tab functionality
function openTab(evt, tabName) {
    var i, tabcontent, tablinks;

    // Hide all tab contents
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Remove the active class from all tablinks
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab and add an "active" class to the button that opened the tab
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
}


// Event listener for delete vehicle button
document.getElementById('deleteVehicleButton').addEventListener('click', function () {
    var deleteVehicleChoice = document.getElementById('deleteVehicleChoice').value;
    if (deleteVehicleChoice !== 'none') {
        var vehicleSelect = document.getElementById('deleteVehicleChoice');
        vehicleSelect.remove(vehicleSelect.selectedIndex);

        var updateSelect = document.getElementById('updateVehicleChoice');
        for (var i = 0; i < updateSelect.length; i++) {
            if (updateSelect.options[i].value === deleteVehicleChoice) {
                updateSelect.remove(i);
                break;
            }
        }

        var moveSelect = document.getElementById('moveVehicle');
        for (var i = 0; i < moveSelect.length; i++) {
            if (moveSelect.options[i].value === deleteVehicleChoice) {
                moveSelect.remove(i);
                break;
            }
        }
    }
});

// Event listener for update vehicle selection
document.getElementById('updateVehicleChoice').addEventListener('change', function (event) {
    var updateVehicleChoice = event.target.value;
    if (updateVehicleChoice !== 'none') {
        document.getElementById('updateVehicleForm').style.display = 'block';
    } else {
        document.getElementById('updateVehicleForm').style.display = 'none';
    }
});

// Event listener for update vehicle form submission
document.getElementById('updateVehicleForm').addEventListener('submit', function (event) {
    event.preventDefault();
    var updateVehicleChoice = document.getElementById('updateVehicleChoice').value;
    var updateVehicleName = document.getElementById('updateVehicleName').value;
    if (updateVehicleChoice !== 'none' && updateVehicleName) {
        var vehicleSelect = document.getElementById('updateVehicleChoice');
        vehicleSelect.options[vehicleSelect.selectedIndex].text = updateVehicleName;
        vehicleSelect.options[vehicleSelect.selectedIndex].value = updateVehicleName;

        var deleteSelect = document.getElementById('deleteVehicleChoice');
        for (var i = 0; i < deleteSelect.length; i++) {
            if (deleteSelect.options[i].value === updateVehicleChoice) {
                deleteSelect.options[i].text = updateVehicleName;
                deleteSelect.options[i].value = updateVehicleName;
                break;
            }
        }

        var moveSelect = document.getElementById('moveVehicle');
        for (var i = 0; i < moveSelect.length; i++) {
            if (moveSelect.options[i].value === updateVehicleChoice) {
                moveSelect.options[i].text = updateVehicleName;
                moveSelect.options[i].value = updateVehicleName;
                break;
            }
        }

        document.getElementById('updateVehicleForm').style.display = 'none';
        document.getElementById('updateVehicleName').value = ''; // Clear the input field
    }
});

// Event listener for move vehicle selection
document.getElementById('moveVehicle').addEventListener('change', function (event) {
    var moveVehicleChoice = event.target.value;
    if (moveVehicleChoice !== 'none') {
        document.getElementById('moveVehicleForm').style.display = 'block';
    } else {
        document.getElementById('moveVehicleForm').style.display = 'none';
    }
});

// Event listener for move vehicle form submission
document.getElementById('moveVehicleForm').addEventListener('submit', function (event) {
    event.preventDefault();
    var moveVehicleChoice = document.getElementById('moveVehicle').value;
    var newLongitude = parseFloat(document.getElementById('NewLongitude').value);
    var newLatitude = parseFloat(document.getElementById('NewLatitude').value);
    if (moveVehicleChoice !== 'none' && !isNaN(newLongitude) && !isNaN(newLatitude)) {
        // Move the selected vehicle to the new coordinates
        moveVehicle(moveVehicleChoice, newLongitude, newLatitude);

        document.getElementById('moveVehicleForm').reset();
        document.getElementById('moveVehicle').selectedIndex = 0;
        document.getElementById('moveVehicleForm').style.display = 'none';
    }
});

// Mock function for moving the vehicle
function moveVehicle(vehicleId, longitude, latitude) {
    console.log(`Moving vehicle ${vehicleId} to coordinates (${longitude}, ${latitude})`);
}


