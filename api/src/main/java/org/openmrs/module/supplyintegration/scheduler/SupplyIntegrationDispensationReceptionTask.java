package org.openmrs.module.supplyintegration.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.supplyintegration.*;
import org.openmrs.module.supplyintegration.api.SupplyIntegrationService;
import org.openmrs.module.supplyintegration.models.requests.RequestLocation;
import org.openmrs.module.supplyintegration.models.responses.DispensationResponse;
import org.openmrs.module.supplyintegration.models.responses.EncounterResponse;
import org.openmrs.module.supplyintegration.utils.Json;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.Security;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SupplyIntegrationDispensationReceptionTask extends AbstractTask implements ApplicationContextAware {
	
	private static Log log = LogFactory.getLog(SupplyIntegrationDispensationReceptionTask.class);
	
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
		}
		catch (Exception e) {
			// return;
		}
		
		if (!config.isLisEnabled()) {
			return;
		}
		
		Location currentLocation = Context.getLocationService().getDefaultLocation();
		
		try {
			//			System.out.println("----------------------------------------> " + currentLocation);
			if (currentLocation == null) {
				return;
			} else {
				getDispensation(currentLocation);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		super.startExecuting();
	}
	
	private void getDispensation(Location location) throws Exception {
		RequestLocation locationRequest = new RequestLocation(location.getPostalCode(), location.getName(),
		        location.getUuid());
		String response = postRequest(config.getLisUrl() + config.getGpSupplyReceiveEndpoint(), config.getUsername(),
		    Security.decrypt(config.getPassword()), config.objectToString(locationRequest));
		//		System.out.println("----------------------------------------> Response from dispensation = " + response);
		if (response != null) {
			JsonNode resultNode = Json.parse(response);
			DispensationResponse result = Json.fromJson(resultNode, DispensationResponse.class);
			//			System.out.println("----------------------------------------> Result object  = " + result);
			if (result != null && result.getEncounterResponses() != null && !result.getEncounterResponses().isEmpty()) {
				for (EncounterResponse encounterResponse : result.getEncounterResponses()) {
					createDispensation(encounterResponse);
				}
			}
		}
		
	}
	
	private void createDispensation(EncounterResponse encounterResponse) {
		EncounterType dispensationType = Context.getEncounterService().getEncounterTypeByUuid(
		    config.getOpenmrsOtherEncounterTypeUuid());
		EncounterType followupVisitType = Context.getEncounterService().getEncounterTypeByUuid(
		    config.getOpenmrsEncounterTypeUuid());
		if (dispensationType == null && followupVisitType == null) {
			return;
		}
		Encounter latestDispensation = supplyIntegrationService.findPatientLatestEncounter(
		    encounterResponse.getPatientIdentifier(), dispensationType);
		Encounter latestFollowupVisit = supplyIntegrationService.findPatientLatestEncounter(
		    encounterResponse.getPatientIdentifier(), followupVisitType);
		
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
		
		String payload = config.sendRequest(url, user, pass, data, "GET");
		;
		
		return payload;
	}
	
	public Map<String, Boolean> saveDispensation(String url, String user, String pass, String data) {
		Map<String, Boolean> payload = new HashMap<>();
		
		
		return payload;
	}
	
	@Override
	public void shutdown() {
		log.debug("shutting down SupplyIntegrationDispensationReceptionTask Task");
		
		this.stopExecuting();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
