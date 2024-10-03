package org.openmrs.module.supplyintegration.models.responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDataResponse {
	
	List<OrderResponse> data;
}
