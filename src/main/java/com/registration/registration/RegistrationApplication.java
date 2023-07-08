package com.registration.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.registration.entity.UserNumber;
import com.registration.registration.api.controller.RegistrationController;
import com.registration.registration.api.external.WhatsAppClient;
import com.registration.repository.UserInfoRepositoryImpl;
import com.registration.registration.utils.BasicUtils;

public class RegistrationApplication implements RequestHandler<Map<String, Object>, Map<String, Object>> {
	private final String PREFIX = this.getClass().getName() + " ";
	private static final Logger logger = Logger.getLogger(RegistrationApplication.class.getName());
	static {
		logger.setLevel(BasicUtils.logLevel());
	}

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> requestParams, Context context) {
		Map<String, String> response = new HashMap<>();
		logger.log(Level.INFO, "New request");
		requestParams.entrySet().stream()
				.forEach(e -> logger.log(Level.INFO, PREFIX + e.getKey() + ": " + e.getValue()));
		Map<String, String> queryStringParameters = (Map<String, String>) requestParams.get("queryStringParameters");
		logger.log(Level.INFO, "queryStringParameters::" + queryStringParameters);
		Map<String, Object> requestContext = (Map<String, Object>) requestParams.get("requestContext");
		logger.log(Level.INFO, "requestContext::" + requestContext);
		Map<String, Object> httpMap = (Map<String, Object>) requestContext.get("http");
		logger.log(Level.INFO, "http::" + httpMap);
		Map<String, Object> headers = (Map<String, Object>) requestParams.get("headers");
		logger.log(Level.INFO, "headers::" + headers);
		headers.entrySet().stream()
				.forEach(e -> logger.log(Level.INFO, "headers::" + e.getKey() + ": " + e.getValue()));

		String method = (String) httpMap.get("method");
		String path = (String) httpMap.get("path");
		String jwt = (String) headers.get("authorization");
		Map<String, String> parsedJWT = BasicUtils.parseJWT(jwt);
		String collect = parsedJWT.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("; "));
		logger.log(Level.INFO, String.format("parsed JWT Token %s", collect));
		if (queryStringParameters != null) {
			collect = queryStringParameters.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
					.collect(Collectors.joining("; "));
			logger.log(Level.INFO, String.format("Method %s, Path %s, query parameters: %s", method, path, collect));
		}

		RegistrationController registrationController = new RegistrationController();
		if ("/authnz/home".equals(path)) {
			registrationController.home(parsedJWT,response);
		} else if ("/authnz/save_user".equals(path)) {
			registrationController.saveUser(parsedJWT,response);
		} else if ("/authnz/subscribe".equals(path)) {
			Object object = requestParams.get("body");
			if ( object!=null) {
				String encodedString = (String) object;
				String base64Decode = BasicUtils.base64Decode(encodedString);
				Map<String, Object> body = BasicUtils.jsonToMap(base64Decode);
				registrationController.createSubscritpion(parsedJWT, body,response);
			}
		} else if ("/authnz/update-user".equals(path)) {
			Object object = requestParams.get("body");
			if ( object!=null) {
				String encodedString = (String) object;
				String base64Decode = BasicUtils.base64Decode(encodedString);
				Map<String, Object> body = (Map<String, Object>) requestParams.get(base64Decode);
				registrationController.createSubscritpion(parsedJWT, body,response);
				registrationController.updateUser(parsedJWT, body,response);
			}
		}
		Map<String ,String > responseHeaders = new HashMap<>();
		responseHeaders.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		responseHeaders.put("Access-Control-Allow-Methods", "*");
		responseHeaders.put("Access-Control-Allow-Origin", "*");
		Map<String,Object> lambdaResponse = new HashMap<>();
		lambdaResponse.putAll(response);
		lambdaResponse.put("headers",responseHeaders);
		return lambdaResponse;
	}

	
}
