package lambda;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.registration.registration.RegistrationApplication;



public class TestComplete {
	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file and convert it to a Map<String, Object>
            File jsonFile = new File("src/test/resources/SampleLambdaRequest.json");
            Map<String,Object> readValue = objectMapper.readValue(jsonFile, Map.class);
            RegistrationApplication ra = new RegistrationApplication();
            Map<String, Object> handleRequest = ra.handleRequest(readValue, null);
            System.out.println(handleRequest);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception if the file cannot be read or JSON parsing fails
        }
        
}
}
