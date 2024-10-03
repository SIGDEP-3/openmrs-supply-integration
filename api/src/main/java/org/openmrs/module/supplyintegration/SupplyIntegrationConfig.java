/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supplyintegration;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.supplyintegration.models.requests.RequestInformation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Contains module's config.
 */
@Component("supplyintegration.SupplyIntegrationConfig")
@Configuration
public class SupplyIntegrationConfig implements ApplicationContextAware {
	
	public final static String MODULE_PRIVILEGE = "SupplyIntegration Privilege";
	
	public final static String GP_SUPPLY_URL = "supplyintegration.URL";
	
	public final static String GP_SUPPLY_USERNAME = "supplyintegration.userName";
	
	public final static String GP_SUPPLY_PASSWORD = "supplyintegration.password";
	
	public final static String GP_SUPPLY_OPENMRS_ENCOUNTER_TYPE_UUID = "supplyintegration.openmrsEncounterType.uuid";
	
	public final static String GP_SUPPLY_OPENMRS_OTHER_ENCOUNTER_TYPE_UUID = "supplyintegration.openmrsOtherEncounterType.uuid";
	
	public final static String GP_SUPPLY_OPENMRS_ORDER_TYPE_UUID = "supplyintegration.openmrsOrderType.uuid";
	
	public final static String GP_SUPPLY_SEND_ENDPOINT = "supplyintegration.sendOrder.endpoint";
	
	public final static String GP_SUPPLY_RECEIVE_ENDPOINT = "supplyintegration.receiveDispensation.endpoint";
	
	public final static String ORDER_SEND_STATUS_SENT = "SENT";
	
	public final static String ORDER_SEND_STATUS_AWAITING_FOR_SEND = "AWAITING_FOR_SEND";
	
	public final static String ORDER_SEND_STATUS_AWAITING_FOR_RESEND = "AWAITING_FOR_RESEND";
	
	public final static String ORDER_SEND_STATUS_ERROR = "ERROR";
	
	public final static String DISPENSATION_TYPE = "DISPENSATIONEEEEEEEEEEEEEEEEEEEEEEEE";
	
	public final static String FOLLOWUP_TYPE = "ERROR";
	
	public static final String ORDER_STATUS_TO_DELETE = "TO_DELETE";
	
	public static final String ORDER_STATUS_DELETE = "DELETED";
	
	private ApplicationContext applicationContext;
	
	@Autowired
	@Qualifier("adminService")
	AdministrationService administrationService;
	
	public String getLisUrl() {
		return administrationService.getGlobalProperty(GP_SUPPLY_URL);
	}
	
	public String getGpSupplySendEndpoint() {
		return administrationService.getGlobalProperty(GP_SUPPLY_SEND_ENDPOINT);
	}
	
	public String getGpSupplyReceiveEndpoint() {
		return administrationService.getGlobalProperty(GP_SUPPLY_RECEIVE_ENDPOINT);
	}
	
	public String getUsername() {
		return administrationService.getGlobalProperty(GP_SUPPLY_USERNAME);
	}
	
	public String getPassword() {
		//		System.out.println("---------------------------------------------> "
		//		        + administrationService.getGlobalProperty(GP_SUPPLY_PASSWORD));
		return administrationService.getGlobalProperty(GP_SUPPLY_PASSWORD);
	}
	
	public String getOpenmrsOrderTypeUuid() {
		return administrationService.getGlobalProperty(GP_SUPPLY_OPENMRS_ORDER_TYPE_UUID);
	}
	
	public String getOpenmrsEncounterTypeUuid() {
		return administrationService.getGlobalProperty(GP_SUPPLY_OPENMRS_ENCOUNTER_TYPE_UUID);
	}
	
	public String getOpenmrsOtherEncounterTypeUuid() {
		return administrationService.getGlobalProperty(GP_SUPPLY_OPENMRS_OTHER_ENCOUNTER_TYPE_UUID);
	}
	
	public boolean isLisEnabled() {
		return StringUtils.isNotBlank(getPassword());
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public boolean netIsAvailable() {
		try {
			final URL url = new URL(getLisUrl());
			final URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream().close();
			return true;
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Crunchify's isAlive Utility
	 * 
	 * @param hostName
	 * @param port
	 * @return boolean - true/false
	 */
	public boolean isSocketAlive(String hostName, int port) {
		boolean isAlive = false;
		
		// Creates a socket address from a hostname and a port number
		SocketAddress socketAddress = new InetSocketAddress(hostName, port);
		Socket socket = new Socket();
		
		// Timeout required - it's in milliseconds
		int timeout = 2000;
		
		//		log("hostName: " + hostName + ", port: " + port);
		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			isAlive = true;
			
		}
		catch (SocketTimeoutException exception) {
			System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
		}
		catch (IOException exception) {
			System.out
			        .println("IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
		}
		return isAlive;
	}
	
	public RequestInformation createObjectFromByte(byte[] yourBytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
		ObjectInput in = null;
		RequestInformation dataTransferModel = new RequestInformation();
		try {
			in = new ObjectInputStream(bis);
			dataTransferModel = (RequestInformation) in.readObject();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			}
			catch (IOException ex) {
				// ignore close exception
			}
		}
		
		return dataTransferModel;
	}
	
	public String objectToString(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(object);
			// System.out.println("ResultingJSONString = " + json);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public String sendRequest(String targetUrl, String username, String password, String data, String requestMethod) throws Exception {
		// Créer l'URL de la requête
		URL url = new URL(targetUrl);
		
		// Ouvrir une connexion HTTP
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		// Configurer la méthode de requête (POST)
		connection.setRequestMethod(requestMethod);
		
		// Encoder le nom d'utilisateur et le mot de passe en Base64
		String auth = username + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		String authHeaderValue = "Basic " + encodedAuth;
		
		// Ajouter l'en-tête d'authentification
		connection.setRequestProperty("Authorization", authHeaderValue);
		
		// Ajouter les en-têtes nécessaires pour la requête
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setDoOutput(true);  // Permet d'envoyer des données dans le corps de la requête
		
		// Écrire les données dans le flux de sortie de la connexion
		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = data.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}
		
		// Lire la réponse du serveur (si nécessaire)
		StringBuilder response = new StringBuilder();
		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // Si la réponse est OK (code 200)
			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}
		} else {
			throw new Exception("Erreur lors de la requête, code de réponse: " + responseCode);
		}
//		if (responseCode == HttpURLConnection.HTTP_OK) {
//			return connection.get();
//		} else {
//			return "Failed to send data, response code: " + responseCode;
//		}
		
		return response.toString();
	}
}
