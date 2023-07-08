package lambda;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.registration.registration.RegistrationApplication;

public class TestSubscription {
	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Read JSON file and convert it to a Map<String, Object>
//			System.setenv("REGISTRATION_TRIAL_LENGTH","30");
//
//			System.setenv("WHATSAPP_HIVE_TOKEN","EAADQ8dAN2gYBAC2iprHCnz1zz29ooLbFAxxlHAmcqHwHJZBQLRG2ZAZB12k0B0ZCmeFv9cATBsGUzDvZBADRtZCtPZCWMGZAYqH2oeWdErPtAPwvFZAeW6tSQ1vZBmD9zxnqeZBIuMZBEhqUQRZBzna4BCDKXpkQR03tIiTcqkpdRgwhwGAZDZD");
//			 System.setenv("WHATSAPP_HIVE_URL","https://graph.facebook.com/v16.0/11180763862939/messages");
//			 System.setenv("GOOGLE_CLIENT_ID","721312221625-2qr8k4kghj2oet7dsctq5up9sbrha2gg.apps.googleusercontent.com");
			File jsonFile = new File("src/test/resources/SubscriptionLambdaRequest.json");
			
			Map<String, Object> readValue = objectMapper.readValue(jsonFile, Map.class);
			RegistrationApplication ra = new RegistrationApplication();
			Map<String, Object> handleRequest = ra.handleRequest(readValue, null);
			System.out.println(handleRequest);
		} catch (IOException e) {
			e.printStackTrace();
			// Handle exception if the file cannot be read or JSON parsing fails
		}

	}
}
