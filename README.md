# Smart Campus: Sensor & Room Management API
**University of Westminster | Client-Server Architectures (5COSC022W)**  
**Lead Architect:** Tsinat Mulatu

This "Smart Campus" API is a high-performance web service built using **JAX-RS (Jersey)**. The architecture follows a logical resource hierarchy designed for campus monitoring.

The design implements a **Sub-Resource Locator** pattern for readings, ensuring a deep nesting structure (`/sensors/{id}/readings`). It also features a custom **Error-Handling Strategy** that maps specific business logic failures to semantic HTTP status codes (403, 409, 422).

---

## 1. Setup & Installation Instructions

### Prerequisites
* **Java JDK 11** or higher
* **Apache Maven**
* **Apache Tomcat 9.0.x**

### Step-by-Step Build and Launch
1. **Clone the Repo:**
   ```bash
   git clone https://github.com/TsinatG/SmartCampusApi
   cd SmartCampusApi
Build with Maven:
Open your terminal in the project folder and run:
code
Bash
mvn clean install
Deploy to Tomcat:
In NetBeans: Right-click the project -> Clean and Build, then click Run.
Service URL: The service will be hosted at http://localhost:8080/SmartCampusApi/api/v1/

---
2. Sample curl Commands
Note: Replace SmartCampusApi with your actual .war file name if different.
1. Discovery Endpoint
code
Bash
curl -X GET http://localhost:8080/SmartCampusApi/api/v1/
2. Create a Room
code
Bash
curl -X POST http://localhost:8080/SmartCampusApi/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d "{\"id\": \"LIB-301\", \"name\": \"Library Quiet Study\", \"capacity\": 50}"
---
3. Get Sensors by Type
code
Bash
curl -X GET http://localhost:8080/SmartCampusApi/api/v1/sensors?type=Temperature
4. Add a Reading (Sub-Resource)
code
Bash
curl -X POST http://localhost:8080/SmartCampusApi/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d "{\"value\": 22.5, \"timestamp\": 1713520000000}"
5. Attempt Illegal Room Deletion (Triggers 409 Conflict)
code
Bash
curl -X DELETE http://localhost:8080/SmartCampusApi/api/v1/rooms/LIB-301
---
3. Conceptual Report
Part 1: Service Architecture & Setup
Q1: JAX-RS Resource Lifecycle & Data Synchronization: In JAX-RS, a new instance of the resource class is created for every request. Since I am not using a database, I made data structures static so they stay in memory. I utilized ConcurrentHashMap to ensure that simultaneous requests (adds/deletes) do not corrupt data.
Q2: The Importance of Hypermedia (HATEOAS): HATEOAS makes the API self-descriptive by including links in responses. This allows clients to navigate the API like a website rather than relying on a static PDF, ensuring client code doesn't break if URL paths change.
Part 2: Room Management
Q1: Returning IDs vs. Full Objects: Returning only IDs saves bandwidth but requires the client to make "N+1" separate requests for details. Returning full objects increases payload size but provides all info in one trip, which is generally better for campus managers.
Q2: Idempotency of DELETE: My DELETE implementation is idempotent. The first call removes the room; the second call returns a 404. Since the server's state (the room being gone) remains the same after the first call, it is idempotent.
Part 3: Sensor Operations & Linking
Q1: Technical Consequences of @Consumes Mismatch: Using @Consumes(MediaType.APPLICATION_JSON) ensures the server rejects invalid formats (like XML) with an HTTP 415 error, preventing the JSON parser from crashing.
Q2: Query vs. Path Parameters: I use Path Parameters to identify specific resources and Query Parameters for flexible filtering (e.g., /sensors?type=CO2). This allows for complex searches without making the URL structure rigid.
Part 4: Deep Nesting with Sub-Resources
Q1: Architectural Benefits: Using a Sub-Resource Locator for readings keeps the project modular. Reading-specific logic is moved to its own class, making the code cleaner and ensuring the parent resource handles ID validation first.
Part 5: Advanced Error Handling & Logging
Q1: Semantic Accuracy (422 vs 404): A 404 implies a broken URL. I use 422 Unprocessable Entity when the URL is correct but the logic (like an invalid Room ID) is wrong, making debugging easier for developers.
Q2: Cybersecurity Risks of Stack Traces: Stack traces cause "Information Leakage," exposing version numbers, file paths, and code structure to hackers. I use Exception Mappers to hide these details and return clean JSON error messages.
Q3: Advantages of JAX-RS Filters: Logging via filters is a "cross-cutting concern." It saves time by writing code once, ensuring consistency, keeping resource classes clean, and catching requests before they even reach the business logic.
---
5. Project Layout
code
Code
SmartCampusApi/
  pom.xml               # Maven configuration
  src/main/java/
    com/westminster/
      resources/        # Top-level resources (Rooms, Sensors)
      subresources/     # Sub-resource locators (Readings)
      models/           # Data Objects (POJOs)
      mappers/          # Exception Mappers (403, 409, 422)
      filters/          # JAX-RS Logging Filters
  src/main/webapp/
    WEB-INF/            # Deployment descriptor
---
7. Useful Endpoints
Method	Route	Description
GET	/api/v1/	API Discovery Endpoint
POST	/api/v1/rooms	Register a new campus location
GET	/api/v1/sensors	List all sensors (supports filtering)
POST	/api/v1/sensors/{id}/readings	Submit new sensor data
DELETE	/api/v1/rooms/{id}	Remove a room (checks if empty)
