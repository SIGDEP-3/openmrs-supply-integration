package org.openmrs.module.supplyintegration.models.responses;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderResponse {
	
	String uuid;
	
	Boolean imported;
}
