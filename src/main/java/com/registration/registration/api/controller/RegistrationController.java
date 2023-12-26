package com.registration.registration.api.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import com.registration.utils.Constants;
import com.registration.entity.UserInfo;
import com.registration.entity.UserNumber;
import com.registration.registration.api.external.WhatsAppClient;
import com.registration.registration.utils.BasicUtils;
import com.registration.repository.UserInfoRepositoryImpl;

public class RegistrationController {

	private static final Logger logger = Logger.getLogger(RegistrationController.class.getName());

	private UserInfoRepositoryImpl userInfoRepository = UserInfoRepositoryImpl.getInstance();
	static {
		logger.setLevel(BasicUtils.logLevel());
		ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        logger.addHandler(handler);
	}
	public Map<String, String> home(Map<String, String> jwt,Map<String, String> map) {
		logger.log(Level.INFO, "home info "+userInfoRepository);
		
		Optional<UserInfo> optUser = userInfoRepository.findByPrincipal(jwt.get("principal"));
		if (optUser.isEmpty()) {
			logger.log(Level.INFO, "User doesnt exist hence creating profile");
			saveUser(jwt,map);
			optUser = userInfoRepository.findByPrincipal(jwt.get("principal"));
		}
		UserInfo user = optUser.get();
//		Map<String, String> map = new HashMap<>();
		populateUserInfo(user, map); // TODO what if this fails
		populateUserNumber(user, map); // TODO what if this fails
		map.put("STATUS", "SUCCESS");
		map.put("STATUS_CODE", "SUCCESS");
		map.put("STATUS_DESCRIPTION", "User data populated");
		return map;
	}

	public Map<String, String> saveUser(Map<String, String> jwt, Map<String, String> map) { 
		logger.log(Level.INFO, "Save User");
		Optional<UserInfo> optUser = userInfoRepository.findByPrincipal(jwt.get("principal"));

		UserInfo user = null;
		if (optUser.isPresent()) {
			logger.log(Level.INFO, "User Already exist");
			user = optUser.get();
		} else {
			user = UserInfo.builder().principal(jwt.get("principal")).email(jwt.get("email")).name(jwt.get("name"))
					.picture(jwt.get("picture")).role("NORMAL").accountExpired(false).accountLocked(false).enabled(true)
					.credentialsExpired(false).build();
			logger.log(Level.INFO, "User saved in db");
			userInfoRepository.saveUserInfo(user);
		}

//		Map<String, String> map = new HashMap<>();
		map.put("STATUS", "SUCCESS");
		map.put("STATUS_CODE", "SUCCESS");
		map.put("STATUS_DESCRIPTION", "User created");
		map.put("HTTP_CODE", HttpStatus.SC_OK + "");
		populateUserInfo(user, map);

		return map;
	}
	
	public Map<String, String> createSubscritpion(Map<String, String> jwt, Map<String, Object> payload, Map<String, String> map) {
		logger.log(Level.INFO, "Create Subscription for user " + jwt.get("email"));
//		Map<String, String> map = new HashMap<>();
		if (invalidInput(payload) || !"TRIAL".equals((String) payload.get("subscription"))) {
			logger.log(Level.WARNING, "Invalid Input " + jwt.get("email"));
			map.put("STATUS", "ERROR");
			map.put("STATUS_CODE", "INVALID_INPUT");
			map.put("STATUS_DESCRIPTION", "Invalid input");
			map.put("HTTP_CODE", HttpStatus.SC_BAD_REQUEST + "");
			return map;
		}
		Optional<UserInfo> optUser = userInfoRepository.findByPrincipal(jwt.get("principal"));
		if (optUser.isEmpty()) {
			logger.log(Level.INFO, "User doesn't exist int the system " + jwt.get("email"));
			map.put("STATUS", "ERROR");
			map.put("STATUS_CODE", "INVALID_INPUT");
			map.put("STATUS_DESCRIPTION", "Invalid input");
			map.put("HTTP_CODE", HttpStatus.SC_MOVED_PERMANENTLY + "");
			map.put("HTTP_REDIRECT", "/authnz/save_user");
			return map;
		}

		String phoneNumber = (String) payload.get("phoneNumber");
		String apiKey = (String) payload.get("apiKey");

		UserInfo user = optUser.get();
		String number = phoneNumber.replaceAll("-", "");
		number = number.replaceAll("\\+", "");
		number = number.trim();
		user.setPhoneNumber(number);

		
		Optional<UserNumber> findByPhoneNumber = userInfoRepository.findByPhoneNumber(phoneNumber);
		UserNumber userNumber = null;
		if (!findByPhoneNumber.isEmpty() && "ACTIVE".equals(findByPhoneNumber.get().getSubscriptionStatus())) {
			logger.log(Level.WARNING, "Already an active subscription " + jwt.get("email"));
			map.put("STATUS", "ERROR");
			map.put("STATUS_CODE", "SUBSCRIPTION_ALREADY_EXIST");
			map.put("STATUS_DESCRIPTION", "Subscription already exist");
			map.put("HTTP_CODE", HttpStatus.SC_BAD_REQUEST + "");
			return map;
		}
		if (findByPhoneNumber.isPresent()) {// so the subscription has expired ? then cant create

//			userNumber = findByPhoneNumber.get();
//			userNumber.setPhoneNumber(phoneNumber);
//			userNumber.setApiKey(apiKey);
		} else {
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(System.getenv("REGISTRATION_TRIAL_LENGTH")));
			Date endDate = calendar.getTime();
			logger.log(Level.INFO, "Building user_number object " + phoneNumber);
			userNumber = UserNumber.builder().phoneNumber(phoneNumber).picture(jwt.get("picture"))
					.principal(jwt.get("principal")).name(jwt.get("name")).apiKey(apiKey)
					.subscriptionStatus(Constants.SUBSCRIPTION_ACTIVE).subscriptionType(Constants.SUBSCRIPTION_TRIAL)
					.subscriptionStartDate(currentDate).subscriptionEndDate(endDate).build();
		}
		// TODO problem if phone number saved in user Info but not in user_number
		userInfoRepository.saveUserInfo(user);
		logger.log(Level.INFO, "User details updated in the user_info " + jwt.get("email"));
		userInfoRepository.saveUserNumber(userNumber);
		logger.log(Level.INFO, "User details updated in the user_number " + userNumber.getPhoneNumber());
		whatsAppUser(userNumber.getPhoneNumber());
		
		map.put("STATUS", "SUCCESS");
		map.put("STATUS_CODE", "SUCCESS");
		map.put("STATUS_DESCRIPTION", "Subscription created");
		return map;

	}

	public Map<String, String> updateUser(Map<String, String> jwt, Map<String, Object> payload ,Map<String, String> map) {

		// TODO people can keep changing the phone number to continue the trial.
		// Which is INFO for the begining.we will put the restriction later.
		logger.log(Level.INFO, "updateUser called" + jwt.get("email"));
//		Map<String, String> map = new HashMap<>();
		if (invalidInput(payload)) {
			logger.log(Level.WARNING, "invalid input " + jwt.get("email")+"::"+payload);
			map.put("STATUS", "ERROR");
			map.put("STATUS_CODE", "INVALID_INPUT");
			map.put("STATUS_DESCRIPTION", "Invalid input");
			map.put("HTTP_CODE", HttpStatus.SC_BAD_REQUEST + "");
			return map;
		}
		Optional<UserInfo> optUser = userInfoRepository.findByPrincipal(jwt.get("principal"));
		if (optUser.isEmpty()) {
			logger.log(Level.WARNING, "User doesn't exist ..redirecting to save_user " + jwt.get("email"));
			map.put("HTTP_CODE", HttpStatus.SC_MOVED_PERMANENTLY + "");
			map.put("HTTP_REDIRECT", "/authnz/save_user");
			return map;
		}
		UserInfo userInfo = optUser.get();
		String oldPhoneNumber = userInfo.getPhoneNumber();
		String newPhoneNumber = (String) payload.get("phoneNumber");
		String apiKey = (String) payload.get("apiKey");

		UserNumber userNumber = null;
		Optional<UserNumber> findByPhoneNumber = userInfoRepository.findByPhoneNumber(oldPhoneNumber);
		if (findByPhoneNumber.isEmpty()) {
			logger.log(Level.WARNING, "User doesn't exist in user_number " + jwt.get("email"));
			map.put("STATUS", "ERROR");
			map.put("STATUS_CODE", "USER_DOES_NOT_EXIST");
			map.put("STATUS_DESCRIPTION", "User does not exist");
			map.put("HTTP_CODE", HttpStatus.SC_BAD_REQUEST + "");
			return map;
		} else {
			userNumber = findByPhoneNumber.get();
		}

		if (!apiKey.isBlank()) {
			userNumber.setApiKey(apiKey);
		}
		if (!newPhoneNumber.isBlank()) {
			String number = newPhoneNumber.replaceAll("-", "");
			number = number.replaceAll("\\+", "");
			number = number.trim();
			userNumber.setPhoneNumber(number);
			userInfo.setPhoneNumber(number);
		}

		// TODO what if api key is invalid, people can send request to us and we will
		// ask openai
		// there can be repeated such calls.

		// TODO problem if phone number saved in user Info but not in user_number
		userInfoRepository.deleteUserNumber(oldPhoneNumber);
		userInfoRepository.saveUserInfo(userInfo);
		userInfoRepository.saveUserNumber(userNumber);
		logger.log(Level.INFO, "User updated by deleting older number and creating new number in user_info and user_number table m" + jwt.get("email"));
//		populateUserInfo(user, map);
//		populateUserNumber(user, map);
		map.put("STATUS", "SUCCESS");
		map.put("STATUS_CODE", "SUCCESS");
		map.put("STATUS_DESCRIPTION", "User updated");
		map.put("HTTP_CODE", HttpStatus.SC_OK + "");
		whatsAppUser(userNumber.getPhoneNumber());
		return map;
	}

	private boolean invalidInput(Map<String, Object> payload) {
		// TODO
		String phoneNumber = (String) payload.get("phoneNumber");
		String apiKey = (String) payload.get("apiKey");

		if (apiKey.startsWith("sk-") && !apiKey.substring(3).matches("^[0-9a-zA-Z]+$")) {
			return true;
		}

		String number = phoneNumber.replaceAll("-", "");
		number = number.replaceAll("\\+", "");
		number = number.trim();
		if (!number.matches("^[0-9]+$")) {
			return true;
		}
		return false;
	}

	private void whatsAppUser(String to) {
		// TODO yes, this can be fixed
		String msg = "{ \"messaging_product\": \"whatsapp\"," + " \"to\": \"REPLACE_TO\", " + "\"type\": \"template\", "
				+ "\"template\": " + "{ \"name\": \"hello_world\"," + " \"language\": { \"code\": \"en_US\" }" + " }"
				+ " }";
		String msg1 = msg.replace("REPLACE_TO", to);
		WhatsAppClient.getInstance().send(System.getenv("WHATSAPP_HIVE_TOKEN"), System.getenv("WHATSAPP_HIVE_URL"),
				msg1);

	}

	private void populateUserInfo(UserInfo user, Map<String, String> map) {
		logger.log(Level.INFO, "populateUserInfo ");
		map.put("email", user.getEmail());
		map.put("phoneNumber", user.getPhoneNumber());
		map.put("name", user.getName());
		map.put("picture", user.getPicture());
	}

	private void populateUserNumber(UserInfo user, Map<String, String> map) {
		if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
			Optional<UserNumber> optUserNumber = userInfoRepository.findByPhoneNumber(user.getPhoneNumber());
			if (optUserNumber.isPresent()) {
				UserNumber userNumber = optUserNumber.get();
				map.put("email", userNumber.getEmail());
				map.put("phoneNumber", userNumber.getPhoneNumber());
				map.put("name", userNumber.getName());
				map.put("picture", userNumber.getPicture());

				map.put("calls", String.valueOf(userNumber.getCalls()));
				map.put("totalCalls", String.valueOf(userNumber.getTotalCalls()));

				String subscriptionEndDate = userNumber.getSubscriptionEndDate() == null ? ""
						: userNumber.getSubscriptionEndDate().toString();
				map.put("subscriptionEndDate", subscriptionEndDate);
				String subscriptionStartDate = userNumber.getSubscriptionStartDate() == null ? ""
						: userNumber.getSubscriptionStartDate().toString();
				map.put("subscriptionStartDate", subscriptionStartDate);
				map.put("subscriptionType", userNumber.getSubscriptionType());
				// TODO make sure to update subscription to expired once today > endDate
				if (userNumber.getSubscriptionEndDate().before(new Date())) {
					userNumber.setSubscriptionStatus(Constants.SUBSCRIPTION_EXPIRED);
					userInfoRepository.saveUserNumber(userNumber);
					map.put("subscriptionStatus", userNumber.getSubscriptionStatus());
				}
			}

		}
	}
	
}
/**
 * user has 1 phone number , and 1 subscription associated with 1 phone number.
 * may want to increase it
 */