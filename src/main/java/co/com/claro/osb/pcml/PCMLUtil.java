package co.com.claro.osb.pcml;

import co.com.claro.osb.pcml.dto.ConsumePCML;
import co.com.claro.osb.pcml.dto.ConsumePCMLResponse;
import co.com.claro.osb.pcml.util.BinaryToString;
import co.com.claro.osb.pcml.util.PcmlException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;

public class PCMLUtil {

    public static String consumePCMLFromJSON(String requestJson) throws JsonProcessingException {
        return consumePCMLFromJSON(requestJson, Charset.defaultCharset().name());
    }

    public static String consumePCMLFromJSON(String requestJson, String charsetName) {
        ConsumePCMLResponse response = null;
        String jsonResponse = "";
        Manager manager = new Manager();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String normalizedJson = new String(requestJson.getBytes(charsetName));
            ConsumePCML pcmlRequest = mapper.readValue(normalizedJson, ConsumePCML.class);
            response = manager.consumePCML(pcmlRequest, Charset.forName(charsetName));
            jsonResponse = new ObjectMapper().writeValueAsString(response);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonResponse = "{\"code\": \"-4\",\"description\": \"" + jsonProcessingException + "\"}";
        } catch (IOException ioException) {
            response = new ConsumePCMLResponse("-1", ioException.toString());
            try {
                jsonResponse = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException jsonProcessingException) {
                jsonResponse = "{\"code\": \"-4\",\"description\": \"" + jsonProcessingException + "\"}";
            }
        } catch (PcmlException pcmlException) {
            response = new ConsumePCMLResponse("-2", pcmlException.toString());
            try {
                jsonResponse = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException jsonProcessingException) {
                jsonResponse = "{\"code\": \"-4\",\"description\": \"" + jsonProcessingException + "\"}";
            }
        } catch (Exception exception) {
            response = new ConsumePCMLResponse("-3", exception.toString());
            try {
                jsonResponse = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException jsonProcessingException) {
                jsonResponse = "{\"code\": \"-4\",\"description\": \"" + jsonProcessingException + "\"}";
            }
        }

        return jsonResponse;
    }

    public static String consumePCMLFromBinary(Object data, String charsetName) {
        String response = "";
        try {
            String payload = BinaryToString.getBinaryContentAsString(data);
            payload = payload.replace("null", "\"\"");
            response = consumePCMLFromJSON(payload, charsetName);
        } catch (Exception exception) {
            response = "{\"code\": \"-4\",\"description\": \"" + exception + "\"}";
        }
        return response;
    }

    public static String consumePCMLFromBinary(Object data) {
        String response = "";
        try {
            String payload = BinaryToString.getBinaryContentAsString(data);
            payload = payload.replace("null", "\"\"");
            response = consumePCMLFromJSON(payload, Charset.defaultCharset().name());
        } catch (Exception exception) {
            response = "{\"code\": \"-4\",\"description\": \"" + exception + "\"}";
        }
        return response;
    }

    public static String getMockResponse(String ignored) {
        return "{\"code\":\"0\",\"description\":\"Proceso exitoso\",\"data\":[[{\"parameter\":\"@_INSER??\",\"value\":\"00235011060314534\"}],[{\"parameter\":\"@_SALIDA\",\"value\":\"30151902|MTA|TELEFONIA|U07|MTA UBEE UBC1307|105BADE9FFA2|AS|CMF|ROSA DEBADILLO MZH-C5 PISO1|I|Transacion Exitosa \"}]]}\n";
    }
}
