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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
@Component
public class SupplyIntegrationActivator extends BaseModuleActivator implements ApplicationContextAware, DaemonTokenAware {
	
	private static final Log log = LogFactory.getLog(SupplyIntegrationActivator.class);
	
	private static ApplicationContext applicationContext;
	
	private static DaemonToken daemonToken;
	
	@Autowired
	private SupplyIntegrationOrderManager orderManager;
	
	@Autowired
	private SupplyIntegrationConfig config;
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started SupplyIntegration");
		applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
		
		orderManager.setDaemonToken(daemonToken);
		
		if (config.isLisEnabled()) {
			orderManager.enableLisConnector();
		}
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		if (orderManager != null) {
			orderManager.disableLisConnector();
		}
		log.info("Shutdown SupplyIntegration");
	}
	
	@Override
	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
