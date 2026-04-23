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
2.Build with Maven:
Open your terminal in the project folder and run:
code
Bash
mvn clean install
Deploy to Tomcat:
3. In NetBeans: Right-click the project -> Clean and Build, then click Run.
Service URL: The service will be hosted at http://localhost:8080/SmartCampusApi/api/v1/

---
## 2. Sample curl Commands
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
## 3. Conceptual Report

### Part 1: Service Architecture & Setup

*   **Question 1: JAX-RS Resource Lifecycle & Data Synchronization**  
    **Answer:** In JAX-RS, a new instance of the resource class is created for every single request. This is good for thread safety but problematic for this project since I am not using a database. Because objects are recreated every time, standard variables would be lost.  
    **Solution:** I made my data structures **static** so they persist in memory across requests. I also used `ConcurrentHashMap` to ensure that if multiple users add or delete sensors simultaneously, the data remains consistent and uncorrupted.

*   **Question 2: The Importance of Hypermedia (HATEOAS)**  
    **Answer:** HATEOAS makes an API "self-descriptive" by including links in the response. Instead of developers memorizing every URL, the server provides links for the next possible actions. This is superior to static documentation because if a URL path changes in the future, the client’s code won't break—they simply follow the new link provided by the server.

---

### Part 2: Room Management

*   **Question 1: Returning IDs vs. Full Objects**  
    **Answer:** This is a choice between data size and speed.  
    - **Returning only IDs:** Makes responses small and fast, but the client must make a separate "GET" request for every room to see its details.  
    - **Returning full objects:** Makes the first response larger, but the client gets all the info in one go. For a campus manager, full objects are better as they reduce the number of trips to the server.

*   **Question 2: Idempotency of the DELETE Operation**  
    **Answer:** Yes, my DELETE implementation is **idempotent**. The first time it is called, the room is removed. The second time, the room is already gone, so the server returns a 404. Even though the status code changes, the state of the server remains the same: the room stays deleted and no new errors are created.

---

### Part 3: Sensor Operations & Linking

*   **Question 1: Technical Consequences of @Consumes Mismatch**  
    **Answer:** By using `@Consumes(MediaType.APPLICATION_JSON)`, I tell the server exactly what format to expect. If a client sends XML or plain text, JAX-RS stops the request immediately and returns an **HTTP 415 Unsupported Media Type** error. This acts as a safety feature, ensuring the JSON parser doesn't crash on invalid data formats.

*   **Question 2: Query Parameters vs. Path Parameters for Filtering**  
    **Answer:** I use **Path Parameters** to identify a specific resource (like a Sensor ID) and **Query Parameters** for searching or filtering. Using `@QueryParam` is more flexible; a user can search for `/sensors?type=Temperature`, and I can add more filters later (like status) without changing the URL structure.
    
### Part 4: Deep Nesting with Sub-Resources
*   **Q1: Architectural Benefits of Sub-Resource Locators**  
    Using a Sub-Resource Locator for the readings (`/sensors/{id}/readings`) helps keep the project organized and modular. Instead of one giant resource file, I moved reading-specific logic into its own class. This makes the code easier to maintain and ensures the parent resource handles the sensor ID check once before handing off the work.

### Part 5: Advanced Error Handling & Logging
*   **Q1: Semantic Accuracy: 422 vs. 404**  
    A 404 error usually means the address is wrong. In my case, I use **422 Unprocessable Entity** when the URL is correct but the data inside (like a Room ID that doesn't exist) is invalid. This makes it much easier for developers to debug.

*   **Q2: Cybersecurity Risks of Stack Traces**  
    Exposing stack traces is "Information Leakage." Hackers can see version numbers, file paths, and code structures to find exploits. By using an **Exception Mapper**, I hide this technical data and return a clean JSON error message instead.

*   **Q3: Advantages of JAX-RS Filters for Logging**  
    Logging is a "cross-cutting concern." Using a filter is better than manual logging because:
    1. It saves time by writing the code once for the whole app.
    2. It ensures every log entry looks the same.
    3. It keeps resource classes clean from non-business logic.
    4. It catches requests that fail even before they reach my methods.

---

## 4. Project Layout
```text
SmartCampusApi/
├── pom.xml               # Maven configuration
└── src/main/java/
    └── com/westminster/
        ├── resources/    # Top-level (Rooms, Sensors)
        ├── subresources/ # Locators (Readings)
        ├── models/       # Data Objects (POJOs)
        ├── mappers/      # Error Mappers (403, 409, 422)
       └── filters/      # JAX-RS Logging Filters

---

 ## 5. Useful Endpoints

| Method | Route | Description |
| :--- | :--- | :--- |
| **GET** | `/api/v1/` | API Discovery Endpoint |
| **POST** | `/api/v1/rooms` | Register a new campus location |
| **GET** | `/api/v1/sensors` | List all sensors (supports filtering) |
| **POST** | `/api/v1/sensors/{id}/readings` | Submit new sensor data |
| **DELETE** | `/api/v1/rooms/{id}` | Remove a room (checks if empty) |
