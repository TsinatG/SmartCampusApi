Smart Campus:Sensor & Room Management API
University of Westminster | Client-Server Architectures (5COSC022W)
Lead Architect: Tsinat Mulatu

1. API Design Overview
This "Smart Campus" API is a high-performance web service built using JAX-RS (Jersey). The architecture follows a logical resource hierarchy:
•	Rooms: The top-level container for campus locations.
•	Sensors: Devices linked to specific rooms via a foreign key (roomId).
•	Readings: A nested sub-resource that maintains historical data for each sensor.
The design implements a Sub-Resource Locator pattern for readings, ensuring a deep nesting structure (/sensors/{id}/readings). It also features Error-Handling Strategy that maps specific business logic failures (like deleting a room that isn't empty) to HTTP status codes (403, 409, 422) using Exception Mappers.

2. Setup & Installation Instructions
Prerequisites
•	Java JDK 11 or higher
•	Apache Maven
•	Apache Tomcat 9.0.x
Step-by-Step Build and Launch
1.	Clone the Repo:
codeBash
git clone https://github.com/TsinatG/SmartCampusApi
2.	Build with Maven:
Open your terminal in the project folder and run:
codeBash
mvn clean install
3.	Deploy to Tomcat:
o	In NetBeans: Right-click the project -> Clean and Build, then click Run.
o	The service will be hosted at: http://localhost:8080/SmartCampusApi/api/v1/

3. Sample curl Commands
Note: Replace SmartCampusApi with your actual .war file name if different.
1.	Discovery Endpoint:
curl -X GET http://localhost:8080/SmartCampusApi/api/v1/
2.	Create a Room:
curl -X POST http://localhost:8080/SmartCampusApi/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\": \"LIB-301\", \"name\": \"Library Quiet Study\", \"capacity\": 50}"
3.	Get Sensors by Type:
curl -X GET http://localhost:8080/SmartCampusApi/api/v1/sensors?type=Temperature
4.	Add a Reading (Sub-Resource):
curl -X POST http://localhost:8080/SmartCampusApi/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\": 22.5, \"timestamp\": 1713520000000}"
5.	Attempt Illegal Room Deletion (Triggers 409):
curl -X DELETE http://localhost:8080/SmartCampusApi/api/v1/rooms/LIB-301

4. Conceptual Report (Answers to tasks)
Part 1: Service Architecture & Setup
Q1: JAX-RS Resource Lifecycle & Data Synchronization
In JAX-RS, the default behavior is that a new instance of the resource class is created for every single request. This is good for thread safety because requests don't share the same object, but it’s a problem for my project since I’m not using a database. Because the objects are recreated every time, any data stored in regular variables would be lost.
To fix this, I made my data structures (Maps and Lists) static so they stay in memory across different requests. I also used ConcurrentHashMap to make sure that if two people try to add or delete a sensor at the exact same time, the data doesn't get corrupted or lost.
Q2: The Importance of Hypermedia (HATEOAS)
HATEOAS makes an API "self-descriptive" by including links in the response. Instead of the developer having to memorize every URL, the server provides links that show what you can do next. It’s a bit like a website where you follow buttons to navigate. This is better than just having a static PDF for documentation because if I ever change a URL path in the future, the client’s code won't necessarily break—they just follow the new link provided by the server.

Part 2: Room Management
Q1: Returning IDs vs. Full Objects
This really comes down to a choice between saving data and making things faster for the user.
•	Returning only IDs: This makes the response very small and fast to send over the network. But, the downside is that the client then has to make a separate "GET" request for every single room to see its name or capacity. 
•	Returning full objects: This makes the first response bigger, but the client gets all the info they need in one go. For a campus manager who needs to see everything at once, sending full objects is usually better because it reduces the number of trips to the server.
Q2: Idempotency of the DELETE Operation
Yes, my DELETE implementation is idempotent. This means that if a client accidentally clicks "delete" twice, the result on the server is exactly the same.
The first time they call it, the room is removed. The second time they call it, the room is already gone, so the server just returns a 404 Not Found. Even though the error code is different the second time, the server's state hasn't changed—the room is still deleted and no new errors were created. This prevents accidental duplicate actions from causing issues.

Part 3: Sensor Operations & Linking
Q1: Technical Consequences of @Consumes Mismatch
By using @Consumes(MediaType.APPLICATION_JSON), I’m telling the server exactly what format to expect. If a client tries to send something else, like XML or plain text, JAX-RS stops the request immediately and sends back an HTTP 415 Unsupported Media Type error. This is a great safety feature because it means my code doesn't have to deal with weird data formats that might cause the JSON parser to crash.
Q2: Query Parameters vs. Path Parameters for Filtering
I used path parameters to identify a specific "thing" (like a specific sensor ID), but I used query parameters for filtering the list. Using @QueryParam is much better for searching because it's flexible. For example, a user can easily search for /sensors?type=CO2, and I can easily add more filters later (like status) without changing the URL structure. If I put the type in the URL path itself, the API would become very rigid and hard to use if I wanted to filter by multiple things at once.

Part 4: Deep Nesting with Sub-Resources
Q1: Architectural Benefits of Sub-Resource Locators
Using a Sub-Resource Locator for the readings (/sensors/{id}/readings) helps keep the project organized and modular.
Instead of having one giant SensorResource file with hundreds of lines of code, I can move all the reading-specific logic into its own class. This makes the code much easier to read and maintain. Also, it’s more efficient because the parent resource handles the sensor ID check once and then hands off the work to the sub-resource, so I don't have to keep re-writing the same ID validation logic in every method.

Part 5: Advanced Error Handling & Logging
Q1: Semantic Accuracy: 422 vs. 404
A 404 error usually means the "address" is wrong (the URL doesn't exist). But in my case, the URL for adding a sensor is correct. The problem is the data inside the request—specifically a Room ID that isn't in the system. Using 422 Unprocessable Entity is more accurate because it tells the developer: "I found the endpoint and your JSON is fine, but I can't do what you asked because the Room ID is invalid." It makes it much easier for the developer to debug their mistake.
Q2: Cybersecurity Risks of Stack Traces
Exposing a stack trace is basically giving a roadmap to a hacker. It’s called "Information Leakage" and it’s a big risk. A hacker can see:
•	Version Numbers: Like Tomcat or Jersey versions, which they can use to find known bugs to exploit.
•	File Paths: They can see exactly how my folders are set up on the server.
•	Code Structure: They can see my class and package names, which helps them guess how the rest of the app works.
By using an Exception Mapper, I hide all this "tech talk" and just give the user a clean JSON error message.
Q3: Advantages of JAX-RS Filters for Logging
Logging is what’s known as a "cross-cutting concern" because it affects the whole app. Using a filter is much better than manually adding LOG.info to every single method because:
1.	It saves time: I only had to write the logging code once, and it automatically covers every endpoint in the API.
2.	It’s consistent: Every request and response looks exactly the same in the logs.
3.	It’s cleaner: My resource classes stay focused on the campus logic and aren't cluttered with logging code.
4.	It catches everything: A filter can log requests that fail even before they reach my methods, which manual logging would miss.

