package org.openmrs.module.supplyintegration.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestInformation {
	
	RequestPatient patient;
	
	RequestLocation location;
	
	RequestPractitioner practitioner;
	
	RequestMedication medication;
	
	Date requestDate;
	
	Integer duration;
	
	String requestType;
	
	String uuid;
}
