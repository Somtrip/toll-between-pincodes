# Toll Plaza Route API

A Spring Boot application that calculates toll plazas between two Indian pincodes using Google Maps APIs and geometric calculations.

## Features

- Calculate distance between two Indian pincodes using Google Distance Matrix API
- Find toll plazas along the route using geometric corridor detection
- Response caching for frequently queried routes
- Input validation for Indian pincodes
- Automatic loading of toll plaza data from CSV file

## Prerequisites

- Java 21
- MySQL 8.0+
- Maven 3.9+
- Google Maps API key with:
  - Distance Matrix API enabled
  - Geocoding API enabled

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Somtrip/toll-between-pincodes.git
   cd toll-between-pincodes/
   ```

2. **Database Setup**
   ```sql
   CREATE DATABASE toll_plaza_route;
   ```

3. **Configure Environment Variables**
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.password=your_mysql_password
   google.api.key=your_google_maps_api_key
   ```

4. **Build the Application**
   ```bash
   ./mvnw clean package
   ```

## Running the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Usage

### Get Toll Plazas Between Pincodes

**Endpoint:** `POST /api/v1/toll-plazas`

**Request Body:**
```json
{
  "sourcePincode": "560064",
  "destinationPincode": "411045"
}
```

**Example using Postman:**
1. Set request method to POST
2. Set URL: `http://localhost:8080/api/v1/toll-plazas`
3. Set Headers:
   - `Content-Type: application/json`
4. Set Body (raw JSON):
   ```json
   {
     "sourcePincode": "560064",
     "destinationPincode": "411045"
   }
   ```

**Example Response:**
```json
{
  "route": {
    "sourcePincode": "560064",
    "destinationPincode": "411045",
    "distanceInKm": 855.8
  },
  "tollPlazas": [
    {
      "name": "Devanahalli Toll Plaza",
      "latitude": 13.1936004,
      "longitude": 77.6472356,
      "distanceFromSource": 35.2,
      "geoState": "Karnataka"
    },
    {
      "name": "Hirekodige Toll Plaza",
      "latitude": 13.5175,
      "longitude": 77.495,
      "distanceFromSource": 62.1,
      "geoState": "Karnataka"
    }
  ]
}
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/som/toll/
│   │       ├── client/          # Google Maps API client
│   │       ├── controller/      # REST controllers
│   │       ├── dto/            # Data transfer objects
│   │       ├── entity/         # JPA entities
│   │       ├── loader/         # CSV data loader
│   │       ├── repository/     # Data repositories
│   │       ├── service/        # Business logic
│   │       └── util/           # Utility classes
│   └── resources/
│       ├── application.properties
│       └── toll_plaza_india.csv  # Toll plaza database
└── test/                       # Unit tests
```


## Troubleshooting

1. **Invalid Pincode Errors**: Ensure pincodes are valid 6-digit Indian pincodes
2. **API Key Errors**: Verify Google Maps API key has correct permissions
3. **Database Connection**: Check MySQL is running and credentials are correct
4. **CORS Issues**: Configure CORS if accessing from different domains

## Technologies Used

- Java 21
- Spring Boot 3.5.4
- MySQL Database
- Google Maps APIs
- Maven
- Lombok
- JPA/Hibernate
