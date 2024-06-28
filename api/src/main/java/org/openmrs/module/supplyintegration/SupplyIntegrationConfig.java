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
import org.openmrs.api.AdministrationService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
	
	public final static String GP_SUPPLY_OPENMRS_ENCOUNTER_UUIDS = "supplyintegration.openmrsEncounterType.uuid";
	
	private ApplicationContext applicationContext;
	
	@Autowired
	@Qualifier("adminService")
	AdministrationService administrationService;
	
	public String getLisUrl() {
		return administrationService.getGlobalProperty(GP_SUPPLY_URL);
	}
	
	public String getUsername() {
		return administrationService.getGlobalProperty(GP_SUPPLY_USERNAME);
	}
	
	public String getPassword() {
		return administrationService.getGlobalProperty(GP_SUPPLY_PASSWORD);
	}
	
	public String getOpenmrsEncounterType() {
		return administrationService.getGlobalProperty(GP_SUPPLY_OPENMRS_ENCOUNTER_UUIDS);
	}
	
	public boolean isLisEnabled() {
		return StringUtils.isNotBlank(getPassword());
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
