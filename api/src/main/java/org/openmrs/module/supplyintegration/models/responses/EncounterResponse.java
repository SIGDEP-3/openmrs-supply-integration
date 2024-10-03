package org.openmrs.module.supplyintegration.models.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EncounterResponse {
	
	Date encounterDatetime;
	
	String patientIdentifier;
	
	Integer treatmentDuration;
}
