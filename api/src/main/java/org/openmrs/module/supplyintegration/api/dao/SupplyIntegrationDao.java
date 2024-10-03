/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supplyintegration.api.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.BasicHttpContext;
import org.hibernate.criterion.Restrictions;
import org.openmrs.*;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.supplyintegration.models.SupplyIntegrationOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Repository("supplyintegration.SupplyIntegrationDao")
public class SupplyIntegrationDao {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public SupplyIntegrationOrder getSupplyIntegrationOrderByUuid(String uuid) {
		return (SupplyIntegrationOrder) getSession().createCriteria(SupplyIntegrationOrder.class)
		        .add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public SupplyIntegrationOrder saveSupplyIntegrationOrder(SupplyIntegrationOrder supplyIntegrationOrder) {
		getSession().saveOrUpdate(supplyIntegrationOrder);
		return supplyIntegrationOrder;
	}
	
	public boolean testServer(String url, String user, String pass) throws IOException {
		URL testURL;
		boolean success = false;
		
		// Check if the URL makes sense
		try {
			testURL = new URL(url); // Add the root API
			// endpoint to the URL
		}
		catch (MalformedURLException e) {
			e.fillInStackTrace();
			return false;
		}
		
		//		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		//
		//		credentialsProvider.setCredentials(new AuthScope(url, 11223), new UsernamePasswordCredentials(user, pass));
		
		//				final HttpParams httpParams = new BasicHttpParams();
		//				HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
		//		//		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		//		HttpClient httpClient = new DefaultHttpClient();
		
		try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
			HttpHost targetHost = new HttpHost(testURL.getHost(), testURL.getPort(), testURL.getProtocol());
			BasicHttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(testURL.toURI());
			Credentials credentials = new UsernamePasswordCredentials(user, pass);
			Header bs = new BasicScheme().authenticate(credentials, httpGet, localContext);
			
			httpGet.addHeader("Authorization", bs.getValue());
			httpGet.addHeader("Content-Type", "application/json");
			httpGet.addHeader("Accept", "application/json");
			
			//execute the test query
			//			HttpResponse response = httpClient.execute(targetHost, httpGet, localContext);
			CloseableHttpResponse response = httpclient.execute(targetHost, httpGet, localContext);
			
			log.debug("----------------------------------------> : " + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
				success = true;
			}
		}
		catch (Exception ex) {
			ex.fillInStackTrace();
			//			success = false;
		}
		finally {
			log.debug("---------------------------------------> Connection closed");
		}
		
		return success;
	}
	
	public SupplyIntegrationOrder getSupplyIntegrationOrderByOrder(Order order) {
		return (SupplyIntegrationOrder) getSession().createCriteria(SupplyIntegrationOrder.class)
		        .add(Restrictions.eq("order", order)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<SupplyIntegrationOrder> getSupplyOrderByStatus(String status) {
		return getSession().createCriteria(SupplyIntegrationOrder.class).add(Restrictions.eq("status", status)).list();
	}
	
	public Encounter findPatientLatestEncounter(String identifier, EncounterType encounterType) {
		return (Encounter) getSession().createCriteria(Encounter.class, "e").createAlias("e.patient", "p")
		        .createAlias("p.identifiers", "i").add(Restrictions.eq("i.identifier", identifier))
		        .add(Restrictions.eq("e.encounterType", encounterType)).add(Restrictions.eq("e.voided", false))
		        .addOrder(org.hibernate.criterion.Order.desc("e.encounterDatetime")).setMaxResults(1).uniqueResult();
	}
	
	public Patient findPatientByIdentifier(String identifier) {
		return (Patient) getSession().createCriteria(Patient.class, "p").createAlias("p.identifiers", "i")
		        .add(Restrictions.eq("i.identifier", identifier)).uniqueResult();
	}
	
	public Obs findPatientLatestObs(Person person, Concept concept) {
		return (Obs) getSession().createCriteria(Obs.class).add(Restrictions.eq("person", person))
		        .add(Restrictions.eq("concept", concept)).addOrder(org.hibernate.criterion.Order.desc("obsDatetime"))
		        .setMaxResults(1).uniqueResult();
	}
}
