package org.openmrs.module.supplyintegration.models.responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DispensationResponse {
	
	String location;
	
	List<EncounterResponse> encounterResponses;
}
