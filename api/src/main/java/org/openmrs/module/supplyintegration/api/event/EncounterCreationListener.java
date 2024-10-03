package org.openmrs.module.supplyintegration.api.event;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.supplyintegration.SupplyIntegrationConfig;
import org.openmrs.module.supplyintegration.models.SupplyIntegrationOrder;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Date;
import java.util.UUID;

@Component("EncounterCreationListener")
public class EncounterCreationListener implements EventListener {
	
	private static final Logger log = LoggerFactory.getLogger(EncounterCreationListener.class);
	
	@Setter
	private DaemonToken daemonToken;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SupplyIntegrationConfig config;
	
	@Autowired
	private SupplyIntegrationService supplyIntegrationService;
	
	@Override
	public void onMessage(Message message) {
		log.trace("Received message {}", message);
		
		Daemon.runInDaemonThread(() -> {
			try {
				System.out.println("---------------------------> Received message");
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
			
			System.out.println("---------------------------> Received UUID = " + uuid);

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
			
			System.out.println("---------------------------> Received Encounter = " + encounter);

			if (encounter != null && encounter.getEncounterType().getUuid().equals(config.getOpenmrsEncounterTypeUuid())) {
				log.trace("Is the right encounter {}", encounter);
				try {
					Obs regimeObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("162240")).findFirst().orElse(null);
					Obs treatmentTypeObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("1255")).findFirst().orElse(null);
					Obs treatmentDurationObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("164590")).findFirst().orElse(null);
					if (regimeObs != null && treatmentTypeObs != null && treatmentDurationObs != null) {
						DrugOrder order;
						
						if (!encounter.getOrders().isEmpty()) {
							for (Order current : encounter.getOrders()) {
								DrugOrder orderToVoid = (DrugOrder) current;
								if (!orderToVoid.getVoided()) {
									orderToVoid.setAction(Order.Action.DISCONTINUE);
									orderService.voidOrder(orderToVoid, "Deleted because created new");
								}
								SupplyIntegrationOrder supplyIntegrationOrder = supplyIntegrationService.getSupplyIntegrationOrderByOrder(orderToVoid);
								if (supplyIntegrationOrder != null && supplyIntegrationOrder.getStatus().equals(SupplyIntegrationConfig.ORDER_SEND_STATUS_SENT)) {
									supplyIntegrationOrder.setStatus(SupplyIntegrationConfig.ORDER_STATUS_TO_DELETE);
									supplyIntegrationService.saveSupplyIntegrationOrder(supplyIntegrationOrder);
								}
							}
						}
						
//						if (encounter.getOrders().isEmpty()) {
							log.trace("No orders found for encounter {}", encounter);
							order = new DrugOrder();
							
							EncounterProvider encounterProvider = encounter.getEncounterProviders().stream().findFirst().orElse(null);
							order.setEncounter(encounter);
							
							if (encounterProvider != null) {
								order.setOrderer(encounterProvider.getProvider());
							}
							
							order.setPatient(encounter.getPatient());
							
							OrderType orderType = orderService.getOrderTypeByUuid(config.getOpenmrsOrderTypeUuid());
//							System.out.println("--------------------------------> Order Type = " + orderType);
							if (orderType != null) {
								order.setOrderType(orderType);
							}
							order.setDateActivated(encounter.getEncounterDatetime());
							
							CareSetting careSetting = orderService.getCareSettingByUuid("c365e560-c3ec-11e3-9c1a-0800200c9a66");
							if (careSetting != null) {
								order.setCareSetting(careSetting);
							}
							getTreatmentStatus(regimeObs, treatmentTypeObs, order);
							order.setUrgency(Order.Urgency.ROUTINE);
							order.setDose(1.);
							Concept doseUnit = Context.getConceptService().getConceptByUuid("162381AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
							if (doseUnit != null) {
								order.setDoseUnits(doseUnit);
							}
							OrderFrequency orderFrequency = orderService.getOrderFrequencyByUuid("b3cbccb0-d47c-4b9f-94d0-10b83c7e6cd1");
							order.setFrequency(orderFrequency);

							Concept route = Context.getConceptService().getConceptByUuid("160240AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
							if (route != null) {
								order.setRoute(route);
							}
							
							Concept durationUnit = Context.getConceptService().getConceptByUuid("1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
							if (durationUnit != null) {
								order.setDurationUnits(durationUnit);
							}
							
							order.setDuration(treatmentDurationObs.getValueNumeric().intValue());
							
//						} else {
//							log.trace("Orders found for encounter {}", encounter);
//							order = (DrugOrder) encounter.getOrders().stream().findFirst().orElse(null);
//
//							if (order != null) {
//								order.setDuration(treatmentDurationObs.getValueNumeric().intValue());
//								order.setConcept(treatmentDurationObs.getConcept());
//								order.setDateActivated(encounter.getEncounterDatetime());
//								getTreatmentStatus(regimeObs, treatmentTypeObs, order);
//							}
//						}
						createOrder(order);
						
					}
					
				} catch (Exception e) {
					log.error("An exception occurred while trying to create the order for encounter {}", encounter, e);
				}
			} else {
				log.trace("Is not the right encounter {}", encounter);
			}
		}
	}
	
	private void getTreatmentStatus(Obs regimeObs, Obs treatmentTypeObs, Order order) {
		Concept treatmentStatus = treatmentTypeObs.getValueCoded();
		if (treatmentStatus.getConceptId().equals(1256)) {
			order.setAction(Order.Action.NEW);
		} else if (!treatmentStatus.getConceptId().equals(1107)) {
			order.setAction(Order.Action.RENEW);
		}
		order.setConcept(regimeObs.getValueCoded());
	}
	
	public void createOrder(Order order) {
		Order savedOrder = orderService.saveOrder(order, new OrderContext());
		createSupplyIntegrationOrder(savedOrder);
	}
	
	public void createSupplyIntegrationOrder(Order order) {
		SupplyIntegrationOrder supplyIntegrationOrder = supplyIntegrationService.getSupplyIntegrationOrderByOrder(order);
		if (supplyIntegrationOrder != null) {
			if (supplyIntegrationOrder.getStatus().equals(SupplyIntegrationConfig.ORDER_SEND_STATUS_SENT)) {
				supplyIntegrationOrder.setStatus(SupplyIntegrationConfig.ORDER_SEND_STATUS_AWAITING_FOR_RESEND);
			}
		} else {
			supplyIntegrationOrder = new SupplyIntegrationOrder();
			supplyIntegrationOrder.setStatus(SupplyIntegrationConfig.ORDER_SEND_STATUS_AWAITING_FOR_SEND);
			supplyIntegrationOrder.setOrder(order);
			supplyIntegrationOrder.setUuid(UUID.randomUUID().toString());
			supplyIntegrationOrder.setDateCreated(new Date());
		}
		supplyIntegrationService.saveSupplyIntegrationOrder(supplyIntegrationOrder);
	}
}
