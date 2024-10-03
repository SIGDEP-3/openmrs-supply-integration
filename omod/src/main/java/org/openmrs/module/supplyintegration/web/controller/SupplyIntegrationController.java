/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supplyintegration.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supplyintegration.SupplyIntegrationConfig;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.forms.ConnexionForm;
import org.openmrs.module.supplyintegration.forms.ConnexionFormValidation;
import org.openmrs.util.Security;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class configured as controller using annotation and mapped with the URL of
 * 'module/${rootArtifactid}/${rootArtifactid}Link.form'.
 */
@Controller("${rootrootArtifactid}.SupplyIntegrationController")
@RequestMapping(value = "module/supplyintegration/supplyintegration")
public class SupplyIntegrationController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private SupplyIntegrationService getService() {
		return Context.getService(SupplyIntegrationService.class);
	}
	
	//	@Autowired
	//	UserService userService;
	
	/** Success form view name */
	private final String VIEW = "/module/supplyintegration/supplyintegration";
	
	/**
	 * Initially called after the getUsers method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String onGet(ModelMap modelMap) throws IOException {
		List<GlobalProperty> globalProperties = Context.getAdministrationService().getGlobalPropertiesByPrefix(
		    "supplyintegration");
		ConnexionForm connexionForm = new ConnexionForm();
		String connexionMessage = "";
		boolean connexionStatus = false;
		GlobalProperty passwordGlobalProperty =
				globalProperties.stream().filter(g -> g.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_PASSWORD)).findFirst().orElse(null);
		if (passwordGlobalProperty != null &&
				passwordGlobalProperty.getPropertyValue() != null) {
			connexionForm.setPassword(Security.decrypt(passwordGlobalProperty.getPropertyValue()));
			for (GlobalProperty globalProperty : globalProperties) {
				if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_URL)) {
					connexionForm.setUrl(globalProperty.getPropertyValue());
				} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_SEND_ENDPOINT)) {
					connexionForm.setSendingEndpoint(globalProperty.getPropertyValue());
				}  else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_RECEIVE_ENDPOINT)) {
					connexionForm.setReceptionEndpoint(globalProperty.getPropertyValue());
				} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_USERNAME)) {
					connexionForm.setUsername(globalProperty.getPropertyValue());
				}
			}
			if (getService().testServer(connexionForm.getUrl() + connexionForm.getSendingEndpoint(), connexionForm.getUsername(), connexionForm.getPassword())) {
				connexionStatus = true;
				connexionMessage = "Vous êtes connecté pour échanger des données";
			} else {
				connexionMessage = "Vous n'êtes pas connecté pour échanger de données";
			}
			
		} else {
			connexionMessage = "Vous n'êtes pas connecté pour échanger de données";
		}
		
		
		
		modelMap.addAttribute("connexionForm", connexionForm);
		modelMap.addAttribute("connexionMessage", connexionMessage);
		modelMap.addAttribute("connexionStatus", connexionStatus);
		
		return VIEW;
	}
	
	/**
	 * All the parameters are optional based on the necessity
	 * 
	 * @param request
	 * @param connexionForm
	 * @param errors
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String onPost(ModelMap modelMap, HttpServletRequest request,
	        @ModelAttribute("connexionForm") ConnexionForm connexionForm, BindingResult errors) throws IOException {
		HttpSession httpSession = request.getSession();
		String connexionMessage = "";
		boolean connexionStatus = false;
		
		httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ARGS, "Formulaire !");
		
		new ConnexionFormValidation().validate(connexionForm, errors);
		
		if (errors.hasErrors()) {
			// return error view
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ARGS, "Vous avez un problème avec votre formulaire !");
		} else {
			//			System.out.println("Connexion URL : " + connexionForm.getUrl());
			//			System.out.println("Connexion User name : " + connexionForm.getUsername());
			//			System.out.println("Connexion Password : " + connexionForm.getPassword());
			
			if (getService().testServer(connexionForm.getUrl() + connexionForm.getSendingEndpoint(),
			    connexionForm.getUsername(), connexionForm.getPassword())) {
				List<GlobalProperty> globalProperties = Context.getAdministrationService().getGlobalPropertiesByPrefix(
				    "supplyintegration");
				for (GlobalProperty globalProperty : globalProperties) {
					if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_URL)) {
						globalProperty.setPropertyValue(connexionForm.getUrl());
					} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_PASSWORD)) {
						globalProperty.setPropertyValue(Security.encrypt(connexionForm.getPassword()));
					} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_USERNAME)) {
						globalProperty.setPropertyValue(connexionForm.getUsername());
					} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_RECEIVE_ENDPOINT)) {
						globalProperty.setPropertyValue(connexionForm.getReceptionEndpoint());
					} else if (globalProperty.getProperty().equals(SupplyIntegrationConfig.GP_SUPPLY_SEND_ENDPOINT)) {
						//						globalProperty.setPropertyValue(connexionForm.getSendingEndpoint());
					}
					Context.getAdministrationService().saveGlobalProperty(globalProperty);
				}
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ARGS,
				    "La connexion au serveur a été effectuée avec succès !");
				connexionMessage = "La connexion au serveur a été effectuée avec succès !";
				connexionStatus = true;
				
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ARGS,
				    "La connexion au serveur a échoué, veuillez vérifier vos paramètres de connexion");
				connexionMessage = "La connexion au serveur a échoué, veuillez vérifier vos paramètres de connexion";
			}
			
			modelMap.addAttribute("connexionMessage", connexionMessage);
			modelMap.addAttribute("connexionForm", connexionForm);
			modelMap.addAttribute("connexionStatus", connexionStatus);
		}
		
		return VIEW;
	}
	
	//	/**
	//	 * This class returns the form backing object. This can be a string, a boolean, or a normal java
	//	 * pojo. The bean name defined in the ModelAttribute annotation and the type can be just defined
	//	 * by the return type of this method
	//	 */
	//	@ModelAttribute("users")
	//	protected List<User> getUsers() throws Exception {
	//		List<User> users = userService.getAllUsers();
	//
	//		// this object will be made available to the jsp page under the variable name
	//		// that is defined in the @ModuleAttribute tag
	//		return users;
	//	}
	
}
