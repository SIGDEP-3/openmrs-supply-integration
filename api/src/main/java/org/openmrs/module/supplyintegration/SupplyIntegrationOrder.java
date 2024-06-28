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

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Order;

import javax.persistence.*;
import java.util.Date;

/**
 * Please note that a corresponding table schema must be created in liquibase.xml.
 */
//Uncomment 2 lines below if you want to make the Item class persistable, see also MSupplyIntegrationDaoTest and liquibase.xml
@Entity(name = "supplyintegration.SupplyIntegrationOrder")
@Table(name = "supply_integration_order")
@Setter
@Getter
public class SupplyIntegrationOrder extends BaseOpenmrsObject {
	@Id
	@GeneratedValue
	@Column(name = "supply_integration_order_id")
	private Integer supplyIntegrationOrderId;
	
	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
	
	@Basic
	@Column(name = "status", length = 255)
	private String status;
	
	@Column(name = "last_transfer_attempt_date")
	private Date lastTransferAttemptDate;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@Override
	public Integer getId() {
		return getSupplyIntegrationId();
	}
	
	@Override
	public void setId(Integer id) {
		setSupplyIntegrationId(id);
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
}
