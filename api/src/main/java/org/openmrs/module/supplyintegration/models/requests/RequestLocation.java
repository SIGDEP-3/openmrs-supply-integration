package org.openmrs.module.supplyintegration.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestLocation {
	
	String code;
	
	String name;
	
	String uuid;
}
