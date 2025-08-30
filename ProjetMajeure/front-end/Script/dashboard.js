async function fillTable(facilityId, tableId) {
    try {
        // Fetch facility data
        const response = await fetch(`http://localhost:8080/facility/${facilityId}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        // Log the full response to inspect its structure
        console.log(`Réponse complète pour la caserne ${facilityId}:`, data);

        // Extract vehicleIdSet from the response
        const vehicleIdSet = data.vehicleIdSet;

        // Log the vehicle IDs for the facility
        console.log(`Véhicules pour la caserne ${facilityId}:`, vehicleIdSet);

        const tableBody = document.querySelector(`#${tableId} tbody`);
        tableBody.innerHTML = ''; // Clear previous table content

        // Ensure vehicleIdSet exists and is an array
        if (Array.isArray(vehicleIdSet)) {
            // Array to store all vehicle detail fetch promises
            const fetchPromises = [];

            // Add each vehicle detail fetch promise to the array
            vehicleIdSet.forEach(vehicleId => {
                const fetchPromise = fetch(`http://localhost:8080/vehicles/${vehicleId}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`HTTP error! status: ${response.status}`);
                        }
                        return response.json();
                    })
                    .catch(error => console.error('Error fetching vehicle details:', error));
                fetchPromises.push(fetchPromise);
            });

            // Wait for all promises to resolve
            const vehicleDetails = await Promise.all(fetchPromises);

            // Add each vehicle detail to the table
            vehicleDetails.forEach(vehicleData => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${vehicleData.id}</td>
                    <td>${vehicleData.type}</td>
                    <td>${vehicleData.liquidType}</td>
                    <td>${vehicleData.onMission ? 'On Mission' : 'Not on Mission'}</td>
                `;
                tableBody.appendChild(row);
            });
        } else {
            // Log an error and add a row indicating no vehicles are associated
            console.error(`vehicleIdSet is not a valid array for facility ${facilityId}`);
            const row = document.createElement('tr');
            row.innerHTML = `<td>No vehicles associated</td>`;
            tableBody.appendChild(row);
        }
    } catch (error) {
        console.error('Error fetching facility data:', error);
    }
}

fillTable(102, 'dashboard102');
fillTable(156, 'dashboard156');
