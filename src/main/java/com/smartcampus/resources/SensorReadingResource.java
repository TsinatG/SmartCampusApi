/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.smartcampus.resources;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.exceptions.SensorUnavailableException; // Import your custom exception
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
        // --- PART 5 TASK 3: Check for MAINTENANCE status ---
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is currently under maintenance and cannot accept readings.");
        }

        readingsHistory.putIfAbsent(sensorId, new ArrayList<>());
        reading.setTimestamp(new Date());
        readingsHistory.get(sensorId).add(reading);

        // SIDE EFFECT: Update the parent sensor object
        parentSensor.setValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}