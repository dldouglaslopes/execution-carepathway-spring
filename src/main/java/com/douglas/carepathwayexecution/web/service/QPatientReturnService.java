package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.Patient;
import QueryMetamodel.QPatientReturn;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QPatientReturnService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, List<Document>> patientsMap;

	public EQuery getReturnPatient(EQuery eQuery, String patient, int version, int hours) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					List<Document> docs = new ArrayList<>();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						docs.addAll(service.filterDocuments(eQuery));
					}
					QPatientReturn qPatientReturn = getData(docs, 
							patient, 
							eQuery.getEAttribute().getRange(), 
							carePathway);
					docs.clear();
					if (qPatientReturn.getPatient() != null) {
						eQuery.getEMethod().add(qPatientReturn);
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			List<Document> docs = new ArrayList<>();
			for (int i = 1; i < numVersion + 1; i++) {
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				docs.addAll(service.filterDocuments(eQuery));
			}
			QPatientReturn qPatientReturn = getData(docs, 
					patient, 
					eQuery.getEAttribute().getRange(), 
					carePathway);
			docs.clear();
			if (qPatientReturn.getPatient() != null) {
				eQuery.getEMethod().add(qPatientReturn);
			}	
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			QPatientReturn qPatientReturn = getData(docs, 
					patient, 
					eQuery.getEAttribute().getRange(), 
					carePathway);
			docs.clear();
			if (qPatientReturn.getPatient() != null) {
				eQuery.getEMethod().add(qPatientReturn);
			}									
		}		
		return eQuery;
	}
	
	private QPatientReturn getData(List<Document> docs, 
								String code, 
								ARange aRange, 
								CarePathway carePathway) {
		QPatientReturn qPatientReturn = Query_metamodelFactory.eINSTANCE.createQPatientReturn();
		if (!docs.isEmpty()) {	
			this.patientsMap = new HashMap<>();
			getPatient(docs, code);
			for (String key : patientsMap.keySet()) {
				if (patientsMap.get(key).size() > 1) {
					Patient patient = Query_metamodelFactory.eINSTANCE.createPatient();
					String[] keyArr = key.split("%"); 
					patient.setAge(null);
					patient.setCode(keyArr[0]);
					patient.setQuantity(patientsMap.get(key).size());
					patient.setSex(null);					
					for (Document document : patientsMap.get(key)) {
						Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
						pathway.setId(document.getInteger("idPathway") + "");
						pathway.setName(document.getString("name"));
						pathway.setQuantity(0);
						pathway.setVersion(document.getInteger("version"));
						patient.setPathway(pathway);
					}
					qPatientReturn.getPatient().add(patient);
				}
				else {
					patientsMap.remove(key);
				}
			}	
		}
		return qPatientReturn;
	}

	private void getPatient(List<Document> docs, String patient) {
		for (Document document : docs) {
			Document pathway = document.get("pathway", new Document());
			String name = pathway.getString("name");
			Document attendance = document.get("attendance", new Document());
			String code = attendance.getInteger("patientRecord") + "";
			String key = code + "%" + name;
			add( key, pathway);
		}
	}
	
	private void add(String key, Document value) {
		if (this.patientsMap.containsKey(key)) {
			List<Document> values = patientsMap.get(key);
			values.add(value);
			this.patientsMap.replace(key, values);
		}
		else {
			List<Document> values = new ArrayList<>();
			values.add(value);
			this.patientsMap.put(key, values);
		}
	}
}
