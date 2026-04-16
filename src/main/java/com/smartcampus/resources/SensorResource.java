package com.smartcampus.resources;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Storage for sensors
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    static {
        Sensor s1 = new Sensor("TEMP001", "Temperature", "ACTIVE", 22.5, 301);
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, 101);
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
    }

    /**
     * SUB-RESOURCE LOCATOR
     * This method handles the path /sensors/{sensorId}/readings 
     * and hands it over to the SensorReadingResource class.
     * @param sensorId
     * @return 
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            // Throwing this exception immediately returns a 404 to the user
            throw new WebApplicationException("Sensor not found", Response.Status.NOT_FOUND);
        }
        // Return the sub-resource class, passing the sensor object
        return new SensorReadingResource(sensorId, sensor);
    }

    // --- EXISTING SENSOR CRUD METHODS ---

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isEmpty()) {
            return sensors.values();
        }
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = sensors.get(id);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found").build();
        }
        return Response.ok(sensor).build();
    }

    @GET
    @Path("/room/{roomId}")
    public Collection<Sensor> getSensorsByRoom(@PathParam("roomId") int roomId) {
        return sensors.values().stream()
                .filter(s -> s.getRoomId() == roomId)
                .collect(Collectors.toList());
    }

    @POST
    public Response addSensor(Sensor sensor) {
        if (sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT).entity("Sensor ID already exists").build();
        }

        Room room = RoomResource.rooms.get(sensor.getRoomId());
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room does not exist").build();
        }

        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateSensor(@PathParam("id") String id, Sensor updatedSensor) {
        Sensor existing = sensors.get(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found").build();
        }
        existing.setType(updatedSensor.getType());
        existing.setStatus(updatedSensor.getStatus());
        existing.setValue(updatedSensor.getValue());
        return Response.ok(existing).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSensor(@PathParam("id") String id) {
        Sensor removed = sensors.remove(id);
        if (removed == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Room room = RoomResource.rooms.get(removed.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(id);
        }
        return Response.noContent().build();
    }
}