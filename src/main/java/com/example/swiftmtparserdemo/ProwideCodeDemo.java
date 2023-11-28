package com.example.swiftmtparserdemo;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import com.prowidesoftware.swift.io.ConversionService;
import com.example.swiftmtparserdemo.exception.SwiftExceptions;
import com.example.swiftmtparserdemo.util.SwiftUtils;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.prowidesoftware.swift.model.MtSwiftMessage;
import com.prowidesoftware.swift.utils.Lib;
import com.swift.commons.jwt.NrSigner;
import com.swift.sdk.common.entity.ConnectionInfo;
import com.swift.sdk.common.entity.SecurityFootprintType;
import com.swift.sdk.common.handler.config.SDKConfigLoader;
import com.swift.sdk.management.util.Util;

public class ProwideCodeDemo {
    private static String configPath = "config/config-swift-connect.yaml";
    private static String block4Pattern = "\\{4:([^}]*)\\}";
    private static JsonObject configJson = null;
    private static ConnectionInfo connectionInfo = null;
    private static String mtPayload = null;

    public static void main(String[] args) throws IOException, SwiftExceptions, UnirestException {
        // Docs at https://dev.prowidesoftware.com/SRU2022/open-source/core/mt-parser/
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:/docs/**");

        // Read configuration with SWIFT credentials
        configJson = SwiftUtils.readConfigurationPropertiesYaml(configPath);
        connectionInfo = Util.createConnectionInfo(configJson);
        connectionInfo.setSecurityFootprintType(SecurityFootprintType.SOFT);
        SDKConfigLoader.load(connectionInfo);

        System.out.println("Configurations read from " + configPath);

        for (Resource resource : resources) {
            System.out.println("Parsing file " + resource.getFilename());

            MtSwiftMessage msg = MtSwiftMessage.parse(Lib.readResource("docs/" + resource.getFilename(), null));

            // AbstractMT msg = AbstractMT.parse(Lib.readResource("docs/" +
            // resource.getFilename(), null));
            System.out.println("Sender reference: " + msg.getReference());
            System.out.println("Sender: " + msg.getSender());
            System.out.println("Receiver: " + msg.getReceiver());
            System.out.println("Message ID: " + msg.getMtId());


            Pattern pattern = Pattern.compile(block4Pattern);
            Matcher matcher = pattern.matcher(msg.message());

            if (matcher.find()) {
                String block4Data = matcher.group(1);
                System.out.println("Block 4" + block4Data);
                mtPayload = Base64.getEncoder()
                        .encodeToString(block4Data.getBytes());

            } else {
                System.out.println("Block 4 not found");
            }

            ConversionService srv = new ConversionService();
            String xml = srv.getXml(Lib.readResource("docs/" + resource.getFilename(), null));
            // System.out.println(xml);

            // Unirest.setTimeouts(0, 0);
            // HttpResponse<String> response =
            // Unirest.post("https://sandbox.swift.com/oauth2/v1/token")
            // .header("Content-Type", "application/x-www-form-urlencoded")
            // .header("Accept", "application/json")
            // .header("Authorization",
            // "Basic Q3J4WEVJbjd2YlF1S1VtSDFSVjZWZFdHT29DOURPRFY6QVRueTI1eFl2Z2NaQ0daNg==")
            // .field("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            // .field("assertion",
            // "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1YyI6WyJNSUlFTmpDQ0F4NmdBd0lCQWdJQkFUQU5CZ2txaGtpRzl3MEJBUVVGQURCdk1Rc3dDUVlEVlFRR0V3SlRSVEVVTUJJR0ExVUVDaE1MUVdSa1ZISjFjM1FnUVVJeEpqQWtCZ05WQkFzVEhVRmtaRlJ5ZFhOMElFVjRkR1Z5Ym1Gc0lGUlVVQ0JPWlhSM2IzSnJNU0l3SUFZRFZRUURFeGxCWkdSVWNuVnpkQ0JGZUhSbGNtNWhiQ0JEUVNCU2IyOTBNQjRYRFRBd01EVXpNREV3TkRnek9Gb1hEVEl3TURVek1ERXdORGd6T0Zvd2J6RUxNQWtHQTFVRUJoTUNVMFV4RkRBU0JnTlZCQW9UQzBGa1pGUnlkWE4wSUVGQ01TWXdKQVlEVlFRTEV4MUJaR1JVY25WemRDQkZlSFJsY201aGJDQlVWRkFnVG1WMGQyOXlhekVpTUNBR0ExVUVBeE1aUVdSa1ZISjFjM1FnUlhoMFpYSnVZV3dnUTBFZ1VtOXZkRENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFMZjNHalBtOGdBRUxUbmdUbHZ0SDd4c0Q4MjEraU8yenQ2YkVUT1hwQ2xNZlpPZnZVcThrKzBER3VPUHorVnRVRnJXbHltVVdvQ3dTWHJiTHBYOXVNcS9Oemd0SGo2UlFhMXdWc2Z3VHovb01wNTB5c2lRVk9uR1h3OTRuWnBBUEE2c1lhcGVGSStlaDZGcVVOelhtazZ2QmJPbWNaU2NjYk5RWUFySEU1MDRCNFlDcU9tb2FTWVlrS3RNc0U4anF6cFBoTmpmenAvaGFXKzcxMExYYTBUa3g2M3ViVUZmY2xweENEZXplV1drV2FDVU4vY0FMdzNDa25MYTBEaHkyeFNvUmNSZEtuMjN0TmJFN3F6TkUwUzN5U3ZkUXdBbCttRzVhV3BZSXhHM3B6T1BWblZaOWMwcDEwYTNDaXRsdHROQ2J4V3l1SHY3NytsZFU5VTBXaWNDQXdFQUFhT0IzRENCMlRBZEJnTlZIUTRFRmdRVXJiMlllalMwSnZmNnhDWlU3d085NENUTFZCb3dDd1lEVlIwUEJBUURBZ0VHTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3Z1prR0ExVWRJd1NCa1RDQmpvQVVyYjJZZWpTMEp2ZjZ4Q1pVN3dPOTRDVExWQnFoYzZSeE1HOHhDekFKQmdOVkJBWVRBbE5GTVJRd0VnWURWUVFLRXd0QlpHUlVjblZ6ZENCQlFqRW1NQ1FHQTFVRUN4TWRRV1JrVkhKMWMzUWdSWGgwWlhKdVlXd2dWRlJRSUU1bGRIZHZjbXN4SWpBZ0JnTlZCQU1UR1VGa1pGUnlkWE4wSUVWNGRHVnlibUZzSUVOQklGSnZiM1NDQVFFd0RRWUpLb1pJaHZjTkFRRUZCUUFEZ2dFQkFMQ2I0SVVsd3RZajRnK1dCcEtkUVppYzJZUjVnZGtlV3hRSEl6WmxqN0RZZDd1c1FXeEhZSU5Sc1BreVBlZjg5aVlUeDRBV3BiOWEvSWZQZUhtSklacmlUQWNLaGpXODh0NVJ4TktXdDl4K1R1NXcvUnc1Nnd3Q1VSUXRqcjBXNE1IZlJuWG5KSzNzOUVLMGhaTndFR2U2blFZMVNoalRLM3JNVVVLaGVtUFI1cnVoeFN2Q05yNFREZWE5WTM1NWU2Y0pEVUNyYXQyUGlzUDI5b3dhUWdWUjFFWDFuNmRpSVdnVklFTThtZWQ4dlNUWXFaRVhjNGcvVmhzeE9CaTBjUSthemNnT25vNHVHK0dNbUlQTEh6SHhSRXpHQkhOSmRtQVB4L2k5RjRCckx1bk1UQTVhbW5rUElBb3UxWjVqSmg1VmtwVFlnaGRhZTlDOHg0OU9oZ1E9Il19.eyJpc3MiOiI8cmVwbGFjZSB3aXRoIGNvbnN1bWVyLWtleSBmcm9tIHRoZSBhcHAgeW91IGNyZWF0ZWQ-IiwiYXVkIjoic2FuZGJveC5zd2lmdC5jb20vb2F1dGgyL3YxL3Rva2VuIiwic3ViIjoiQ049ZGVza3RvcCwgTz1zYW5kYm94LCBPPXN3aWZ0IiwianRpIjoiMW5FNi1xZDRqRzVjWjh1ZzJfdkNrIiwiZXhwIjoxNjk4MDM3MTEyLCJpYXQiOjE2OTgwMzY1MDcsIm5iZiI6MTY5ODAzNjUwN30.z94XxosY52ZFnFxJL9q72_JfAaYgZ07oCcEt2vymqtMljIKDV9Ls32E0v8uFmMfcd7gKCoNzfkVwsakLHAgFllucofRbS-SDeZikaMXo6EElrxjLyzK62Z06zLQMa22z4aMKRclo7Chmju_Xh5uZS79vwEU17F7YIgLKCVYxiKrxKytTnKh3PseVCZLGou9obge--XCPuXf3J885ppKuV_v6q_l1Y7pcitw8SRnz9c1dq1tnb1ivR-Oi3oe4DyAK_xE6fZP8upjIqfoIhVHST6BquaRwJJuqR8YCr0IMFNLe3_mIxbFRc5Eri73ERJdU2yybk_XP9Ochq4kCFPXAqQ")
            // .field("scope", "swift.alliancecloud.api")
            // .asString();

            // System.out.println(response.getBody());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sender_reference", msg.getReference());
            jsonObject.put("message_type", msg.getMtId());
            jsonObject.put("sender", "SWLLUS33XXXX");
            jsonObject.put("receiver", "SWHQBEBBXXXX");
            jsonObject.put("payload", mtPayload);

            String sig = nrSign(connectionInfo.getGatewayHost() +
            "/alliancecloud/v1/fin/messages", jsonObject);

            // System.out.println(
            // "Signature: " + sig);
            // System.out.println("Payload: " + jsonObject.toString());

            // Unirest.setTimeouts(0, 0);
            // HttpResponse<String> response2 =
            // Unirest.post("https://sandbox.swift.com/alliancecloud/v1/fin/messages")
            // .header("X-SWIFT-Signature",
            // sig)
            // .header("Content-Type", "application/json")
            // .header("Accept", "application/json")
            // .header("Authorization", "Bearer 8x15hrPVkGDW5oHJBlwPZJ4XzNjE")
            // .body(jsonObject.toString())
            // .asString();

            // System.out.println(response2.getBody());

            // Unirest.setTimeouts(0, 0);
            // HttpResponse<String> response3 =
            // Unirest.get("https://sandbox.swift.com/alliancecloud/v1/distributions")
            // .header("Accept", "application/json")
            // .header("Accept", "application/json")
            // .header("Authorization", "Bearer 8x15hrPVkGDW5oHJBlwPZJ4XzNjE")
            // .asString();

            // System.out.println(response3.getBody());

            System.out.println("---------------------------------------------------------------");
        }
    }

    private static boolean isValidString(String string) {
        return string != null && (string == null || !string.replace(" ", "").isEmpty());
    }

    public static String nrSign(String audience, JSONObject jsonObject) {
        if (isValidString(audience) && jsonObject != null) {
            String jsonBody = jsonObject.toString();
            Map<String, String> claim = new HashMap();
            claim.put("audience", audience);
            claim.put("payload", jsonBody);
            NrSigner nrSigner = new NrSigner();
            return nrSigner.sign(claim);
        } else {
            throw new IllegalArgumentException(
                    "Cant NR sign a null body or empty audience. Please review your audience and Body settings");
        }
    }
}
