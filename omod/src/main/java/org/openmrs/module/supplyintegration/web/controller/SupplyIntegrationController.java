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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.api.forms.ConnexionForm;
import org.openmrs.module.supplyintegration.api.forms.ConnexionFormValidation;
import org.openmrs.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	
	@Autowired
	UserService userService;
	
	/** Success form view name */
	private final String VIEW = "/module/supplyintegration/supplyintegration";
	
	/**
	 * Initially called after the getUsers method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String onGet() {
		return VIEW;
	}
	
	/**
	 * All the parameters are optional based on the necessity
	 * 
	 * @param httpSession
	 * @param connexionForm
	 * @param errors
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String onPost(HttpSession httpSession, @ModelAttribute("connexionForm") ConnexionForm connexionForm,
	        BindingResult errors) {
		
		new ConnexionFormValidation().validate(connexionForm, errors);
		
		if (errors.hasErrors()) {
			// return error view
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ARGS, "Vous avez un problème avec votre formulaire !");
		} else {
			System.out.println("Connexion URL : " + connexionForm.getUrl());
			System.out.println("Connexion User name : " + connexionForm.getUsername());
			System.out.println("Connexion Password : " + connexionForm.getUrl());
			
			if (getService().testServer(connexionForm.getUrl(), connexionForm.getUsername(), connexionForm.getPassword())) {
				List<GlobalProperty> globalProperties = Context.getAdministrationService().getGlobalPropertiesByPrefix(
				    "supplyintegration");
				for (GlobalProperty globalProperty : globalProperties) {
					if (globalProperty.getProperty().equals("supplyintegration.url")) {
						globalProperty.setPropertyValue(connexionForm.getUrl());
					} else if (globalProperty.getProperty().equals("supplyintegration.password")) {
						globalProperty.setPropertyValue(connexionForm.getUrl());
					} else if (globalProperty.getProperty().equals("supplyintegration.username")) {
						globalProperty.setPropertyValue(connexionForm.getUrl());
					}
					Context.getAdministrationService().saveGlobalProperty(globalProperty);
				}
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ARGS,
				    "La connexion au serveur a été effectuée avec succès !");
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ARGS,
				    "La connexion au serveur a échouée, veuillez vérifier vos paramètres de connexion");
			}
			
		}
		
		return null;
	}
	
	/**
	 * This class returns the form backing object. This can be a string, a boolean, or a normal java
	 * pojo. The bean name defined in the ModelAttribute annotation and the type can be just defined
	 * by the return type of this method
	 */
	@ModelAttribute("users")
	protected List<User> getUsers() throws Exception {
		List<User> users = userService.getAllUsers();
		
		// this object will be made available to the jsp page under the variable name
		// that is defined in the @ModuleAttribute tag
		return users;
	}
	
}
