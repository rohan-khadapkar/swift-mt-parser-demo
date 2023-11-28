package com.example.swiftmtparserdemo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.*;
import com.example.swiftmtparserdemo.exception.SwiftExceptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SwiftUtils {

    public static JsonObject readConfigurationPropertiesYaml(String configPath) throws SwiftExceptions {
        byte[] fileContent = null;
        JsonObject jsonObject = null;

        try {
            fileContent = Files.readAllBytes((new File(configPath)).toPath());
            String tmpStr = new String(fileContent, StandardCharsets.UTF_8);
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(tmpStr, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();

            JsonParser jsonParser = new JsonParser();
            jsonObject = (JsonObject) jsonParser.parse(new StringReader(jsonWriter.writeValueAsString(obj)));
        } catch (IOException | JsonIOException | JsonSyntaxException ex) {
            throw new SwiftExceptions(ex.getMessage(), ex);
        }

        return jsonObject;
    }
}
