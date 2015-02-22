/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.services.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.StringWriter;

/**
 *
 * @author Mac
 */
public class JsonConverter {
    
    public static String toJsonString(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        //configure Object mapper for pretty print
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        //writing to console, can write to any output stream such as file
        StringWriter jsonString = new StringWriter();
        objectMapper.writeValue(jsonString, obj);
        return jsonString.toString();
    }

    public static Object fromJsonString(String json, Class<? extends Object> type) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        //convert json string to object
        Object obj = objectMapper.readValue(json, type);
        return type.cast(obj);
    }
}
