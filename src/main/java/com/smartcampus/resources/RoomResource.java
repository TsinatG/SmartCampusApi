package com.smartcampus.resources;

import com.smartcampus.model.Room;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    // 1. Change the Map key to Integer to match Room.getId()
    private static Map<Integer, Room> rooms = new HashMap<>();

    static {
        // IDs are now ints, so no quotes needed
        Room r1 = new Room(301, "Library Quiet Study", 50);
        Room r2 = new Room(101, "Engineering Lab", 30);

        // r1.getSensorIds() is a List<Integer>, so we add an int
        // Note: Use '1' instead of '001' (001 is octal notation in Java)
        r1.getSensorIds().add(1); 

        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
    }

    @GET
    public Collection<Room> getRooms() {
        return rooms.values();
    }

    // 2. Change PathParam type from String to int
    @GET
    @Path("/{id}")
    public Room getRoom(@PathParam("id") int id) {
        return rooms.get(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        // Check if room ID already exists
        if (rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room with ID " + room.getId() + " already exists.")
                    .build();
        }

        // Add to our "database"
        rooms.put(room.getId(), room);

        // Return 201 Created status with the room object
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }
    
    // 3. Change PathParam type from String to int
   @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") int id) {
        Room room = rooms.get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        if (!room.getSensorIds().isEmpty()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Cannot delete room: sensors still assigned")
                    .build();
        }

        rooms.remove(id);
        return Response.ok("Room deleted successfully").build();
    }
}