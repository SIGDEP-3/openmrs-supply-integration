package org.openmrs.module.supplyintegration.api.event;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.supplyintegration.SupplyIntegrationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

@Component("visitEncounterListener")
public class EncounterCreationListener implements EventListener {
	
	private static final Logger log = LoggerFactory.getLogger(EncounterCreationListener.class);
	
	@Setter
	@Getter
	private DaemonToken daemonToken;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SupplyIntegrationConfig config;
	
	@Override
	public void onMessage(Message message) {
		log.trace("Received message {}", message);
		
		Daemon.runInDaemonThread(() -> {
			try {
				processMessage(message);
			}
			catch (Exception e) {
				log.error("Failed to update the user's last viewed patients property", e);
			}
		}, daemonToken);
	}
	
	public void processMessage(Message message) {
		if (message instanceof MapMessage) {
			MapMessage mapMessage = (MapMessage) message;

			String uuid;
			try {
				uuid = mapMessage.getString("uuid");
				log.debug("Handling encounter {}", uuid);
			} catch (JMSException e) {
				log.error("Exception caught while trying to get encounter uuid for event", e);
				return;
			}

			if (uuid == null || StringUtils.isBlank(uuid)) {
				return;
			}

			Encounter encounter;
			try {
				encounter = encounterService.getEncounterByUuid(uuid);
//				if (encounter == null || !encounter.getEncounterType().getName().equals("Demande de charge Virale")) {
//					return;
//				}
				log.trace("Fetched encounter {}", encounter);
			} catch (APIException e) {
				log.error("Exception caught while trying to load encounter {}", uuid, e);
				return;
			}

			if (encounter != null && encounter.getEncounterType().getUuid().equals(config.getOpenmrsEncounterType())) {
				log.trace("Found order(s) for encounter {}", encounter);
				try {
					Obs regimeObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("162240")).findFirst().orElse(null);
					if (regimeObs != null) {
						Order order = new Order();
						if (encounter.getOrders().isEmpty()) {
							EncounterProvider encounterProvider = encounter.getEncounterProviders().stream().findFirst().orElse(null);
							
							order.setEncounter(encounter);
							
							if (encounterProvider != null) {
								order.setOrderer(encounterProvider.getProvider());
							}
							
							order.setPatient(encounter.getPatient());
							order.setOrderType(orderService.getOrderType(2));
							order.setDateActivated(encounter.getEncounterDatetime());
							
							CareSetting careSetting = new CareSetting();
							careSetting.setCareSettingType(CareSetting.CareSettingType.INPATIENT);
							order.setCareSetting(careSetting);
							order.setAction(Order.Action.RENEW);
							order.setConcept(regimeObs.getConcept());
							
							orderService.saveOrder(order, new OrderContext());
						} else {
							order = encounter.getOrders().stream().findFirst().orElse(null);
							if (order != null) {
								order.setDateActivated(encounter.getEncounterDatetime());
								order.setAction(Order.Action.RENEW);
								order.setConcept(regimeObs.getConcept());
							}
						}
						orderService.saveOrder(order, new OrderContext());
					}
					
				} catch (Exception e) {
					log.error("An exception occurred while trying to create the order for encounter {}", encounter, e);
				}
			} else {
				log.trace("No orders found for encounter {}", encounter);
			}
		}
	}
}
