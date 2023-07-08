package com.registration.registration.api.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.registration.registration.utils.BasicUtils;

public class WhatsAppClient {
	private static final Logger logger = Logger.getLogger(WhatsAppClient.class.getName());
	private static WhatsAppClient instance;

	// Private constructor to prevent instantiation from outside the class
	private WhatsAppClient() {
		// Initialization code, if needed
	}

	// Static method to get the singleton instance
	public static WhatsAppClient getInstance() {
		if (instance == null) {
			synchronized (WhatsAppClient.class) {
				if (instance == null) {
					instance = new WhatsAppClient();
				}
			}
		}
		return instance;
	}

	public Map<String, Object> send(String authHeader, String url, String requestBody) {
		Map<String, Object> map = new HashMap<>();
		try {
			// Create an instance of HttpClient
			// ideally it should be singleton or pool of a few client.
			HttpClient client = HttpClient.newHttpClient();

			// Encode the Authorization token if needed
//	            String encodedToken = Base64.getEncoder().encodeToString(authHeader.getBytes(StandardCharsets.UTF_8));

			// Create a HttpRequest builder
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/json").header("Authorization", "Bearer " + authHeader)
					.POST(HttpRequest.BodyPublishers.ofString(requestBody));

			// Build the HttpRequest
			HttpRequest request = requestBuilder.build();

			// Send the request and get the response
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// Read the response
			String responseBody = response.body();
			logger.log(Level.INFO,"Whatsapp "+responseBody);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception = " + BasicUtils.exceptionTrace(e));
		}
		return map;
	}

	public static void main(String[] args) {
		WhatsAppClient w = getInstance();
		// TODO yes, this can be fixed
		String msg = "{ \"messaging_product\": \"whatsapp\"," + " \"to\": \"REPLACE_TO\", " + "\"type\": \"template\", "
				+ "\"template\": " + "{ \"name\": \"hello_world\"," + " \"language\": { \"code\": \"en_US\" }" + " }"
				+ " }";
		String msg1 = msg.replace("REPLACE_TO", "14388229758");
		String token ="EAADQ8dAN2gYBAC2iprHCnz1zz29ooLbFAxxlHAmcqHwHJZBQLRG2ZAZB12k0B0ZCmeFv9cATBsGUzDvZBADRtZCtPZCWMGZAYqH2oeWdErPtAPwvFZAeW6tSQ1vZBmD9zxnqeZBIuMZBEhqUQRZBzna4BCDKXpkQR03tIiTcqkpdRgwhwGAZDZD";
		WhatsAppClient.getInstance().send(token,"https://graph.facebook.com/v16.0/111807638629397/messages",
				msg1);

	}
}