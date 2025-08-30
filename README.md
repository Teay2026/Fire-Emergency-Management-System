# ProjetMajeure - Fire Emergency Management System

![Fire Management System](icons/flan.png)

## 🔥 Overview

**ProjetMajeure** is an intelligent fire emergency management system developed by **"Les Flambis"** team. It simulates and optimizes the management of fire vehicles, fires, and fire stations with a real-time web supervision interface.

## 🏗️ System Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.3.0
- **Java Version**: 17
- **Database**: H2 (in-memory)
- **Architecture**: MVC with modular separation

### Frontend (Web Interface)
- **Technologies**: HTML5, CSS3, JavaScript ES6
- **Mapping**: Leaflet.js with OpenStreetMap
- **Interface**: Real-time dashboard with advanced controls

## 📁 Project Structure

```
ProjetMajeure/
├── Services/                          # Spring Boot Backend
│   ├── src/main/java/com/ProjetMajeure/Services/
│   │   ├── Vehicle/                   # Vehicle management
│   │   ├── Fire/                      # Fire management
│   │   ├── Facility/                  # Fire station management
│   │   ├── EmergencyManager/          # Emergency coordination
│   │   └── ServicesApplication.java   # Entry point
│   ├── src/main/resources/
│   │   └── application.properties     # Configuration
│   └── pom.xml                        # Maven dependencies
├── front-end/                         # Web interface
│   ├── Script/                        # JavaScript scripts
│   │   ├── main.js                    # Main logic
│   │   ├── dataManager.js             # Data management
│   │   ├── dashboard.js               # User interface
│   │   └── ...
│   ├── page.html                      # Main page
│   └── style.css                      # CSS styles
├── icons/                             # Icons and resources
├── Diagramme_archi.svg               # System architecture
└── README.md                          # Documentation
```

## 🚗 Data Models

### Vehicles
```java
// 6 available vehicle types
- CAR: Light vehicle (2 seats, 10L liquid)
- FIRE_ENGINE: Basic truck (4 seats, 50L liquid)
- PUMPER_TRUCK: Pump truck (6 seats, 1000L liquid)
- WATER_TENDERS: Water tender (3 seats, 1000L liquid)
- TURNTABLE_LADDER_TRUCK: Ladder truck (6 seats, 1000L liquid)
- TRUCK: Heavy truck (8 seats, 2000L liquid)
```

### Fire Types
```java
// 7 fire classes according to standard
- A: Ordinary solid materials
- B_Gasoline: Flammable liquids (gasoline)
- B_Alcohol: Flammable liquids (alcohol)
- B_Plastics: Plastic materials
- C_Flammable_Gases: Flammable gases
- D_Metals: Flammable metals
- E_Electric: Electrical equipment
```

## 🛠️ Installation and Configuration

### Prerequisites
- Java 17+
- Maven 3.6+
- Modern web browser

### Backend Installation
```bash
cd Services/
./mvnw clean install
./mvnw spring-boot:run
```
Server starts on `http://localhost:8080`

### Web Interface Configuration
```bash
cd front-end/
# Open page.html in a browser
# Or use a local server:
python -m http.server 3000
```
Interface accessible on `http://localhost:3000`

### API Configuration
Edit `Services/src/main/resources/application.properties`:
```properties
# External CPE API
api.url.api=http://tp.cpe.fr:8081
api.credentials.id=3
api.credentials.teamuuid=17951cd8-eae6-4f67-be27-d7500039556e

# OSRM routing service
osrm.url=http://router.project-osrm.org
```

## 🚀 Main Features

### 🚒 Vehicle Management
- ✅ Automatic creation and configuration
- ✅ Intelligent movement with OSRM route calculation
- ✅ Fuel and extinguishing liquid management
- ✅ Automatic return to nearest base
- ✅ Real-time mission tracking

### 🔥 Fire Management
- ✅ Automatic classification by type
- ✅ Real-time intensity monitoring
- ✅ Precise geolocation with impact zone
- ✅ Vehicle/fire efficiency calculation

### 🏢 Fire Station Management
- ✅ Vehicle/station association
- ✅ Capacity management (vehicles and personnel)
- ✅ Deployment optimization

### 🧠 Intelligent Algorithms
- **Algorithm 1**: Geographic proximity deployment
- **Algorithm 2**: Optimized deployment by fire/vehicle type compatibility
- **Configurable thresholds**: Liquid capacity (default: 100%)
- **Smart return**: Nearest base for refueling

### 🖥️ Supervision Interface
- 🗺️ **Interactive map** with real-time markers
- 🔍 **Advanced filters** (fire type, vehicle type, range, intensity)
- ⚡ **Automatic refresh** (500ms)
- 📊 **Dashboards** with metrics
- 🎛️ **Manual controls** for testing and demonstrations

## 📡 REST API

### Vehicles
```http
GET    /vehicles                    # List all vehicles
GET    /vehicles/{id}               # Get vehicle details
POST   /vehicles/{teamuuid}         # Create a vehicle
PUT    /vehicles/{teamuuid}/{id}    # Update vehicle
DELETE /vehicles/{teamuuid}/{id}    # Delete vehicle
POST   /vehicles/move/{teamuuid}/{id} # Move vehicle
POST   /vehicles/moveBackToBase/{id}  # Return to base
```

### Fires
```http
GET /fires                    # List all fires
GET /fires/{id}/intensity     # Get fire intensity
GET /firetypes               # Get available fire types
```

### Emergency Management
```http
POST /emergency/deploy/all-vehicles  # General deployment
POST /emergency/start               # Start simulation
POST /emergency/stop                # Stop simulation
POST /emergency/moveAllBackToBase   # General return to base
```

### Fire Stations
```http
GET /facility           # List fire stations
GET /facility/{id}      # Get station details
GET /facility/object/{id} # Get complete station object
```

## 🔧 Technologies Used

### Backend
- **Spring Boot 3.3.0** - Web framework
- **Spring Data JPA** - ORM and persistence
- **H2 Database** - In-memory database
- **RestTemplate** - HTTP client
- **Maven** - Dependency management

### Frontend
- **Leaflet.js** - Interactive mapping
- **OpenStreetMap** - Map tiles
- **JavaScript ES6** - Client logic
- **CSS3 & HTML5** - User interface

### External Services
- **OSRM** - Optimized route calculation
- **CPE API** - External system interface

## 🚦 Vehicle States

```javascript
// Possible statuses
- IDLE: Waiting at base
- ON_MISSION: On intervention mission  
- MOVING: Moving towards target
- REFUELING: Being refueled
- EXTINGUISHING: Extinguishing a fire
```

## 📊 Metrics and Monitoring

- **Real-time position** of all vehicles
- **Fuel and liquid levels** per vehicle
- **Fire intensity** with history
- **Intervention efficiency** by type
- **Average response time** by zone

## 🔒 Security and Configuration

- **CORS configured** for cross-origin access
- **Team UUID** for API authentication
- **Externalized configuration** for environments
- **Secured REST API** with parameter validation

## 📈 Future Developments

- [ ] Responsive mobile interface
- [ ] AI algorithms for fire prediction
- [ ] Real-time weather integration
- [ ] Intervention history with analytics
- [ ] GraphQL API for complex queries
- [ ] Real-time push notifications
- [ ] Ground team geolocation
