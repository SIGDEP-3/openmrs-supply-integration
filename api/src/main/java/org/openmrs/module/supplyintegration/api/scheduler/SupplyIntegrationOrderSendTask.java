package org.openmrs.module.supplyintegration.api.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.module.supplyintegration.SupplyIntegrationConfig;
import org.openmrs.module.supplyintegration.SupplyIntegrationOrder;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SupplyIntegrationOrderSendTask extends AbstractTask implements ApplicationContextAware {
	
	private static Log log = LogFactory.getLog(SupplyIntegrationOrderSendTask.class);
	
	private static ApplicationContext applicationContext;
	
	private SupplyIntegrationConfig config;
	
	@Autowired
	private OrderService orderService;
	
	private SupplyIntegrationService supplyIntegrationService;
	
	@Override
	public void execute() {
		try {
			applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
		} catch (Exception e) {
			// return;
		}
		
		if (!config.isLisEnabled()) {
			return;
		}
		
		List<SupplyIntegrationOrder> integrationOrders = new ArrayList<>(Collections.emptyList());
		
		try {
			
			List<SupplyIntegrationOrder> awaitingForSendOrders = supplyIntegrationService.getSupplyOrderByStatus(SupplyIntegrationConfig.ORDER_SEND_STATUS_AWAITING_FOR_SEND);
			List<SupplyIntegrationOrder> awaitingForResendOrders = supplyIntegrationService.getSupplyOrderByStatus(SupplyIntegrationConfig.ORDER_SEND_STATUS_AWAITING_FOR_RESEND);
			
			if (awaitingForSendOrders != null && !awaitingForSendOrders.isEmpty()) {
				integrationOrders.addAll(awaitingForSendOrders);
			}
			if (awaitingForResendOrders != null && !awaitingForResendOrders.isEmpty()) {
				integrationOrders.addAll(awaitingForResendOrders);
			}
			
			if (integrationOrders.isEmpty()) {
				return;
			} else {
				sendMedicationRequest(integrationOrders);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		super.startExecuting();
	}
	
	private  void sendMedicationRequest(List<SupplyIntegrationOrder> integrationOrders) {
		List<MedicationRequest> medicationRequests = new ArrayList<>();
		
		for (SupplyIntegrationOrder integrationOrder: integrationOrders) {
			MedicationRequest medicationRequest = new MedicationRequest();
			Encounter encounter = integrationOrder.getOrder().getEncounter();
			medicationRequest.setRequester(new Reference(integrationOrder.getOrder().getOrderer().getUuid()));
		}
	}
	
	@Override
	public void shutdown() {
		log.debug("shutting down SupplyIntegrationOrderSendTask Task");
		
		this.stopExecuting();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
