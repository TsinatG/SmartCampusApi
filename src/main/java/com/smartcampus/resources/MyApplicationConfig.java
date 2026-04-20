package com.smartcampus.resources;


import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.smartcampus.mappers.RoomNotEmptyMapper; // 
import com.smartcampus.mappers.LinkedResourceNotFoundMapper;
import com.smartcampus.mappers.SensorUnavailableMapper;
import com.smartcampus.mappers.GlobalExceptionMapper;

@ApplicationPath("api/v1")
public class MyApplicationConfig extends ResourceConfig {

    public MyApplicationConfig() {
        
        // 1. Manually register the Resources
        register(RoomResource.class); 
        register(SensorResource.class);
        // Register the Filter
    register(com.smartcampus.filters.LoggingFilter.class);
        
        // 2. Manually register the Mapper (The most important part)
        register(RoomNotEmptyMapper.class);  //409
        register(LinkedResourceNotFoundMapper.class); //422
        register(SensorUnavailableMapper.class); //403
        register(GlobalExceptionMapper.class); //500
        // 3. Force Jackson for JSON
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
        
        System.out.println("DEBUG: MyApplicationConfig has loaded!");
        
 
       
        
        
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}
        
    
