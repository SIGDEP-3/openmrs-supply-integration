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

import lombok.Setter;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.supplyintegration.models.SupplyIntegrationOrder;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.api.dao.SupplyIntegrationDao;

import java.io.IOException;
import java.util.List;

@Setter
public class SupplyIntegrationServiceImpl extends BaseOpenmrsService implements SupplyIntegrationService {
	
	/**
	 * -- SETTER -- Injected in moduleApplicationContext.xml
	 */
	SupplyIntegrationDao dao;
	
	/**
	 * -- SETTER -- Injected in moduleApplicationContext.xml
	 */
	UserService userService;
	
	@Override
	public SupplyIntegrationOrder getSupplyIntegrationOrderByUuid(String uuid) throws APIException {
		return dao.getSupplyIntegrationOrderByUuid(uuid);
	}
	
	@Override
	public SupplyIntegrationOrder saveSupplyIntegrationOrder(SupplyIntegrationOrder supplyIntegrationOrder)
	        throws APIException {
		
		return dao.saveSupplyIntegrationOrder(supplyIntegrationOrder);
	}
	
	@Override
	public boolean testServer(String url, String user, String pass) throws IOException {
		return dao.testServer(url, user, pass);
	}
	
	@Override
	public SupplyIntegrationOrder getSupplyIntegrationOrderByOrder(Order order) {
		return dao.getSupplyIntegrationOrderByOrder(order);
	}
	
	@Override
	public List<SupplyIntegrationOrder> getSupplyOrderByStatus(String status) {
		return dao.getSupplyOrderByStatus(status);
	}
	
	@Override
	public Encounter findPatientLatestEncounter(String identifier, EncounterType encounterType) {
		return dao.findPatientLatestEncounter(identifier, encounterType);
	}
	
	@Override
	public Patient findPatientByIdentifier(String identifier) {
		return dao.findPatientByIdentifier(identifier);
	}
	
	@Override
	public Obs findPatientLatestObs(Person person, Concept concept) {
		return dao.findPatientLatestObs(person, concept);
	}
}
