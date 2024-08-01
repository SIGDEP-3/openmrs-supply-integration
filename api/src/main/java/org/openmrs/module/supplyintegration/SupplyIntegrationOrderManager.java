package org.openmrs.module.supplyintegration;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.event.Event;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.supplyintegration.api.event.EncounterCreationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component("supplyIntegrationOrder")
public class SupplyIntegrationOrderManager implements GlobalPropertyListener {
	
	private static final Logger log = LoggerFactory.getLogger(SupplyIntegrationOrderManager.class);
	
	@Setter
	private DaemonToken daemonToken;
	
	//	private SupplyIntegrationConfig config;
	
	@Autowired
	private EncounterCreationListener encounterListener;
	
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return SupplyIntegrationConfig.GP_SUPPLY_URL.equals(propertyName);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		log.trace("Notified of change to property {}", SupplyIntegrationConfig.GP_SUPPLY_URL);
		
		if (StringUtils.isNotBlank((String) newValue.getValue())) {
			enableLisConnector();
		} else {
			disableLisConnector();
		}
	}
	
	@Override
	public void globalPropertyDeleted(String s) {
		disableLisConnector();
	}
	
	public void enableLisConnector() {
		log.info("Enabling Supply Integration Connector for Encounter");
		encounterListener.setDaemonToken(daemonToken);
		
		if (!isRunning.get()) {
			Event.subscribe(Encounter.class, Event.Action.CREATED.toString(), encounterListener);
			Event.subscribe(Encounter.class, Event.Action.UPDATED.toString(), encounterListener);
		}
		
		isRunning.set(true);
	}
	
	public void disableLisConnector() {
		log.info("Disabling Supply Integration Connector");
		Event.unsubscribe(Encounter.class, Event.Action.CREATED, encounterListener);
		Event.unsubscribe(Encounter.class, Event.Action.UPDATED, encounterListener);
		isRunning.set(false);
	}
}
