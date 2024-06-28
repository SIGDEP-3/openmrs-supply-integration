/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supplyintegration.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.supplyintegration.SupplyIntegrationOrder;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.api.dao.SupplyIntegrationDao;

import java.io.IOException;

public class SupplyIntegrationServiceImpl extends BaseOpenmrsService implements SupplyIntegrationService {
	
	SupplyIntegrationDao dao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(SupplyIntegrationDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public SupplyIntegrationOrder getItemByUuid(String uuid) throws APIException {
		return dao.getItemByUuid(uuid);
	}
	
	@Override
	public SupplyIntegrationOrder saveItem(SupplyIntegrationOrder supplyIntegrationOrder) throws APIException {
		if (supplyIntegrationOrder.getOwner() == null) {
			supplyIntegrationOrder.setOwner(userService.getUser(1));
		}
		
		return dao.saveItem(supplyIntegrationOrder);
	}
	
	@Override
	public boolean testServer(String url, String user, String pass) throws IOException {
		return dao.testServer(url, user, pass);
	}
}
