/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supplyintegration.api;

import org.openmrs.*;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.supplyintegration.SupplyIntegrationConfig;
import org.openmrs.module.supplyintegration.models.SupplyIntegrationOrder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface SupplyIntegrationService extends OpenmrsService {
	
	/**
	 * Returns an item by uuid. It can be called by any authenticated user. It is fetched in read
	 * only transaction.
	 * 
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Authorized()
	@Transactional(readOnly = true)
	SupplyIntegrationOrder getSupplyIntegrationOrderByUuid(String uuid) throws APIException;
	
	/**
	 * Saves an item. Sets the owner to superuser, if it is not set. It can be called by users with
	 * this module's privilege. It is executed in a transaction.
	 * 
	 * @param supplyIntegrationOrder
	 * @return
	 * @throws APIException
	 */
	@Authorized(SupplyIntegrationConfig.MODULE_PRIVILEGE)
	@Transactional
	SupplyIntegrationOrder saveSupplyIntegrationOrder(SupplyIntegrationOrder supplyIntegrationOrder) throws APIException;
	
	boolean testServer(String url, String user, String pass) throws IOException;
	
	SupplyIntegrationOrder getSupplyIntegrationOrderByOrder(Order order);
	
	List<SupplyIntegrationOrder> getSupplyOrderByStatus(String status);
	
	Encounter findPatientLatestEncounter(String identifier, EncounterType encounterType);
	
	Patient findPatientByIdentifier(String identifier);
	
	Obs findPatientLatestObs(Person person, Concept concept);
}
