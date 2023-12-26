package com.registration.registration.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class BasicUtils {
	private static final String PREFIX = "utils.BasicUtils ";
	private static final Logger logger = Logger.getLogger(BasicUtils.class.getName());
	static {
		logger.setLevel(BasicUtils.logLevel());
	}

	public static Map<String, Object> jsonToMap(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<>();
        try {
        	 map = objectMapper.readValue(json, Map.class);

        } catch (IOException e) {
            logger.log(Level.WARNING,exceptionTrace(e));
        }
        return map;
	}

	public static Map<String, Object> getRequestParams(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String json = reader.lines().collect(Collectors.joining());
		logger.log(Level.INFO, "incoming json = " + json);
		Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
		logger.log(Level.INFO, "map representation = ");
//		flattenJson.entrySet().stream().forEach(e -> logger.log(Level.INFO ,  e.getKey() + ": " + e.getValue()));
		return flattenJson;
	}

	public static Map<String, Object> flattenAsMap(String json) {
		return JsonFlattener.flattenAsMap(json);
	}

	public static Level logLevel() {
		String logLevel = System.getenv("LOGGING_LEVEL") == null ? "FINEST" : System.getenv("LOGGING_LEVEL");
		Level level = Level.INFO;
		switch (logLevel) {
		case "INFO":
			level = Level.INFO;
			break;
		case "FINE":
			level = Level.FINE;
			break;
		case "DEBUG":
			level = Level.INFO;
			break;
		case "FINEST":
			level = Level.FINEST;
			break;
		case "WARNING":
			level = Level.WARNING;
			break;
		case "ERROR":
			level = Level.SEVERE;
			break;
		default:
			level = Level.INFO;
		}
		return level;
	}

	public static Map<String, String> getEnvVariable() {
		Map<String, String> map = new HashMap<>();

		map.put("WHATSAPP_TOKEN", System.getenv("WHATSAPP_TOKEN"));
		map.put("VERIFY_TOKEN", System.getenv("VERIFY_TOKEN"));
		String getenv = System.getenv("LOGGING_LEVEL");

		map.put("LOGGING_LEVEL",
				System.getenv("LOGGING_LEVEL") == null || System.getenv("LOGGING_LEVEL").isBlank() ? "INFO"
						: System.getenv("LOGGING_LEVEL"));
		return map;
	}

	public static Map<String, String> parseJWT(String idTokenString) {
		String AUTH_PREFIX = "Bearer ";
		if (idTokenString.startsWith(AUTH_PREFIX)) {
			idTokenString = idTokenString.substring(AUTH_PREFIX.length());
		}
		logger.info(idTokenString);
		Map<String, String> response = new HashMap<>();
		String YOUR_CLIENT_ID = "721312221625-2qr8k4kghj2oet7dsctq5up9sbrha2gg.apps.googleusercontent.com";
//		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
//				.setAudience(Collections.singletonList(System.getenv("GOOGLE_CLIENT_ID"))).build();
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
				.setAudience(Collections.singletonList(YOUR_CLIENT_ID)).build();

		try {
			GoogleIdToken idToken = verifier.verify(idTokenString);
			if (idToken != null) {
				Payload payload = idToken.getPayload();
				// Perform necessary validation checks
				String userId = payload.getSubject();
				String email = payload.getEmail();
				response.put("principal", payload.getSubject());
				response.put("email", payload.getEmail());
				response.put("picture", (String) payload.get("picture"));
				response.put("name", (String) payload.get("name"));
			} else {
				logger.log(Level.WARNING, "unable to parse id_token ( possibly expired):" + idTokenString);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception occured " + exceptionTrace(e));
		}
		return response;
	}

	public static String exceptionTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String base64Decode(String encodedString) {
		if (encodedString == null)
			return "";
		byte[] decode = Base64.getDecoder().decode(encodedString);
		return new String(decode);
	}

	public static void main(String[] args) {
		Level logLevel = logLevel();
		System.out.println(logLevel.getName());
		Map<String, String> parseJWT = parseJWT(
				"A");
		parseJWT.entrySet().forEach(e-> System.out.println(e.getKey()+":"+e.getValue()));
	}

}
