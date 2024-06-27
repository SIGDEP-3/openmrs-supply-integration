package org.openmrs.module.supplyintegration.api.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ConnexionForm {
	
	String url;
	
	String username;
	
	String password;
	
}
