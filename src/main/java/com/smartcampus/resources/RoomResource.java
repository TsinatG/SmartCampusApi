package com.smartcampus.resources;

import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    public static final Map<Integer, Room> rooms = new ConcurrentHashMap<>();

    static {
        Room r1 = new Room(301, "Library Quiet Study", 50);
        Room r2 = new Room(101, "Engineering Lab", 30);

        // Pre-fill so we have something to test
        r1.getSensorIds().add("TEMP001"); 
        r2.getSensorIds().add("CO2-001");

        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
    }

    @GET
    public Collection<Room> getRooms() {
        return rooms.values();
    }

    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") int id) {
        Room room = rooms.get(id);
        if (room == null) return Response.status(404).entity("Room not found").build();
        return Response.ok(room).build();
    }

    @POST
    public Response addRoom(Room room) {
        if (rooms.containsKey(room.getId())) {
            return Response.status(409).entity("Room ID exists").build();
        }
        rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") int id) {
        Room room = rooms.get(id);

        if (room == null) {
            return Response.status(404).entity("Room not found").build();
        }

        // Check if room has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            // This THROW is what triggers the 409 Mapper
            throw new RoomNotEmptyException("Room " + id + " is occupied by active hardware.");
        }

        rooms.remove(id);
        return Response.noContent().build();
    }
}