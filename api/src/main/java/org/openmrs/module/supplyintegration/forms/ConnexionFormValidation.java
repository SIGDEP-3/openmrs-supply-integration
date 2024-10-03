package org.openmrs.module.supplyintegration.forms;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ConnexionFormValidation implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ConnexionForm.class);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ConnexionForm form = (ConnexionForm) target;
		
		if (form == null) {
			errors.reject("integration", "general.error");
		} else {
			ValidationUtils.rejectIfEmpty(errors, "url", null, "Ce champ est requis");
			ValidationUtils.rejectIfEmpty(errors, "username", null, "Ce champ est requis");
			ValidationUtils.rejectIfEmpty(errors, "password", null, "Ce champ est requis");
		}
	}
}
