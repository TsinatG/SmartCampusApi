/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;
    private Sensor parentSensor;
    
    // Static storage for history: Map<SensorID, List of Readings>
    private static final Map<String, List<SensorReading>> readingsHistory = new ConcurrentHashMap<>();

    public SensorReadingResource(String sensorId, Sensor parentSensor) {
        this.sensorId = sensorId;
        this.parentSensor = parentSensor;
    }

    @GET
    public List<SensorReading> getHistory() {
        return readingsHistory.getOrDefault(sensorId, new ArrayList<>());
    }

    @POST
    public Response addReading(SensorReading reading) {
        // 1. Ensure the list exists for this sensor
        readingsHistory.putIfAbsent(sensorId, new ArrayList<>());
        
        // 2. Set the timestamp to now
        reading.setTimestamp(new Date());
        
        // 3. Add to history
        readingsHistory.get(sensorId).add(reading);

        // 4. SIDE EFFECT: Update the parent sensor's current value
        parentSensor.setValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}