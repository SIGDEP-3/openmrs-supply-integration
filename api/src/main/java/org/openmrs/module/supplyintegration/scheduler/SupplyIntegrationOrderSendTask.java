package org.openmrs.module.supplyintegration.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.module.supplyintegration.*;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.models.SupplyIntegrationOrder;
import org.openmrs.module.supplyintegration.models.requests.*;
import org.openmrs.module.supplyintegration.models.responses.OrderDataResponse;
import org.openmrs.module.supplyintegration.models.responses.OrderResponse;
import org.openmrs.module.supplyintegration.utils.Json;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.Security;
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
	
	@Autowired
	private SupplyIntegrationConfig config;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
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
	
	private  void sendMedicationRequest(List<SupplyIntegrationOrder> integrationOrders) throws Exception {
		List<RequestInformation> requestInformations = new ArrayList<>();
		
		for (SupplyIntegrationOrder integrationOrder: integrationOrders) {
			Order order = integrationOrder.getOrder();
			RequestInformation information = new RequestInformation();
			information.setPatient(new RequestPatient(order.getPatient().getPatientIdentifier().getIdentifier(), order.getPatient().getUuid()));
			information.setLocation(new RequestLocation(order.getEncounter().getLocation().getPostalCode(), order.getEncounter().getLocation().getName(), order.getEncounter().getLocation().getUuid()));
			
			information.setPractitioner(new RequestPractitioner(order.getOrderer().getName(), order.getOrderer().getUuid()));
			information.setRequestDate(order.getDateActivated());
			
			Obs regimeObs = order.getEncounter().getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("162240")).findFirst().orElse(null);
			Obs treatmentDurationObs = order.getEncounter().getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("164590")).findFirst().orElse(null);
			
			if (treatmentDurationObs != null) {
				information.setDuration(treatmentDurationObs.getValueNumeric().intValue());
			}
			
			if (order.getAction().equals(Order.Action.NEW)) {
				information.setRequestType("NEW");
			} else if (order.getAction().equals(Order.Action.RENEW)) {
				information.setRequestType("RENEW");
			} else if (order.getAction().equals(Order.Action.REVISE)) {
				information.setRequestType("REVISE");
			}
			
			if (regimeObs != null) {
				information.setMedication(new RequestMedication(order.getConcept().getDisplayString(), order.getConcept().getUuid()));
			}
			
			information.setUuid(order.getUuid());
			
			requestInformations.add(information);
		}
		
		String response = postRequest(
				config.getLisUrl() + config.getGpSupplySendEndpoint(),
				config.getUsername(),
				Security.decrypt(config.getPassword()),
				config.objectToString(requestInformations)
		);
		
//		System.out.println("----------------------------------------> Server response For order = " + response);
		
		if (response != null) {
			JsonNode resultNode = Json.parse(response);
			OrderDataResponse result = Json.fromJson(resultNode, OrderDataResponse.class);
//			System.out.println("----------------------------------------> Server response For order = " + result.getData());
			if (result != null && result.getData() != null && !result.getData().isEmpty()) {
				for (OrderResponse orderResponse : result.getData()) {
					if (orderResponse.getImported()) {
//						System.out.println("-------------------------------------> : Imported " + orderResponse.getUuid());
						Order orderToUpdate = orderService.getOrderByUuid(orderResponse.getUuid());
						if (orderToUpdate != null) {
							SupplyIntegrationOrder supplyIntegrationOrder = supplyIntegrationService.getSupplyIntegrationOrderByOrder(
									orderToUpdate);
							if (supplyIntegrationOrder != null) {
								supplyIntegrationOrder.setStatus(SupplyIntegrationConfig.ORDER_SEND_STATUS_SENT);
								supplyIntegrationService.saveSupplyIntegrationOrder(supplyIntegrationOrder);
							}
						}
					}
				}
			}
		}
		
	}
	
	//	public static DrugOrder convertToDrugOrder(Encounter encounter) {
	//		DrugOrder drugOrder = (DrugOrder) encounter.getOrders().stream().findFirst().orElse(null);
	//		if (drugOrder == null)
	//			return null;
	//
	//		Obs regimeObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("162240")).findFirst().orElse(null);
	//		Obs treatmentTypeObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("1255")).findFirst().orElse(null);
	//		Obs treatmentDurationObs = encounter.getObs().stream().filter(o -> o.getConcept().getUuid().startsWith("333")).findFirst().orElse(null);
	//		if (treatmentTypeObs != null && treatmentDurationObs != null && regimeObs != null) {
	//			drugOrder.setDuration(Integer.parseInt(treatmentDurationObs.getValueNumeric().toString()));
	//
	//		}
	//		return drugOrder;
	//	}
	
	//	public static MedicationRequest convertToFhir(DrugOrder order) {
	//		MedicationRequest request = new MedicationRequest();
	//
	//		// Mapping de l'ID
	//		request.setId(order.getUuid());
	//
	//		// Mapping du patient
	//		Patient patient = new Patient();
	//		patient.setId(order.getPatient().getUuid());
	//		request.setSubject(new Reference(patient));
	//
	//		// Médicament (concept dans OpenMRS)
	//		request.setMedication(new CodeableConcept().setText(order.getConcept().getDisplayString()));
	//
	//		// Instruction de dosage
	//		Dosage dosage = new Dosage();
	//		Timing timing = new Timing();
	//		timing.getRepeat().setFrequency(1).setPeriod(1).setPeriodUnit(Timing.UnitsOfTime.D);
	//		dosage.setTiming(timing);
	//		dosage.setText("Take " + order.getDose() + " " + order.getDoseUnits().getDisplayString());
	//		request.addDosageInstruction(dosage);
	//
	//		// Durée du traitement
	//		request.getDispenseRequest().getValidityPeriod().setStart(order.getDateActivated());
	//		request.getDispenseRequest().getQuantity().setValue(order.getQuantity());
	////		request.getDispenseRequest().getValidityPeriod().setEnd(addDays(order.getDateActivated(), order.getDuration()));
	//
	//		// Mapping du prescripteur
	//		Practitioner practitioner = new Practitioner();
	//		practitioner.setId(order.getOrderer().getUuid());
	//		request.setRequester(new Reference(practitioner));
	//
	//		return request;
	//	}
	
	public String postRequest(String url, String user, String pass, String data) throws Exception {
		
		String payload = config.sendRequest(url, user, pass, data, "POST");
		;
		
		return payload;
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
