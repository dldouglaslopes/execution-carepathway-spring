package com.douglas.carepathwayexecution.web.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Hospital;
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
	private Map<String, List<String>> hospitalsMap;
	private Map<String, Date[]> datesMap;
	
	public EQuery getReturnPatient(EQuery eQuery, String patient, int version, int hours) throws ParseException {
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
							carePathway,
							hours);
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
					carePathway,
					hours);
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
					carePathway,
					hours);
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
								CarePathway carePathway,
								int hours) throws ParseException {
		QPatientReturn qPatientReturn = Query_metamodelFactory.eINSTANCE.createQPatientReturn();
		if (!docs.isEmpty()) {	
			this.patientsMap = new HashMap<>();
			this.hospitalsMap = new HashMap<>();
			this.datesMap = new HashMap<>();
			getPatient(docs, code);
			for (String key : this.patientsMap.keySet()) {
				if (this.patientsMap.get(key).size() > 1) {
					Patient patient = Query_metamodelFactory.eINSTANCE.createPatient();
					String[] keyArr = key.split("%");
					patient.setAge(null);
					patient.setCode(keyArr[0]);
					patient.setQuantity(patientsMap.get(key).size());
					patient.setSex(null);					
					for (String hospitalStr : hospitalsMap.get(key)) {
						Date[] dates = this.datesMap.get(hospitalStr);
						Date creation = dates[0];
						Date conclusion = dates[1];
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
						if (hours == 0) {
							Hospital hospital = Query_metamodelFactory.eINSTANCE.createHospital();
							String[] hospitalArr = hospitalStr.split("%");
							hospital.setName(hospitalArr[0]);
							hospital.setMedicalCare(hospitalArr[1]);
							hospital.setCreation(dateFormat.format(creation));
							hospital.setConclusion(dateFormat.format(conclusion));
							for (Document document : patientsMap.get(key)) {
								Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
								pathway.setId(document.getInteger("idPathway") + "");
								pathway.setName(document.getString("name"));
								pathway.setQuantity(0);
								pathway.setVersion(document.getInteger("version"));
								hospital.setPathway(pathway);
							}
							patient.getHospital().add(hospital);
						}
						else {
							long hoursDiff = conclusion.getTime() - creation.getTime();
							if (hoursDiff <= hours) {
								String[] hospitalArr = hospitalStr.split("%");
								Hospital hospital = Query_metamodelFactory.eINSTANCE.createHospital();
								hospital.setName(hospitalArr[0]);
								hospital.setMedicalCare(hospitalArr[1]);
								hospital.setCreation(dateFormat.format(conclusion));
								hospital.setConclusion(dateFormat.format(creation));
								for (Document document : patientsMap.get(key)) {
									Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
									pathway.setId(document.getInteger("idPathway") + "");
									pathway.setName(document.getString("name"));
									pathway.setQuantity(0);
									pathway.setVersion(document.getInteger("version"));
									hospital.setPathway(pathway);
								}
								patient.getHospital().add(hospital);
							}
						}
					}
					if (!patient.getHospital().isEmpty()) {
						qPatientReturn.getPatient().add(patient);
					}
				}
			}	
		}
		return qPatientReturn;
	}

	private void getPatient(List<Document> docs, String patient) {
		for (Document document : docs) {			
			Date creation = document.getDate("creation");
			Date conclusion = document.getDate("conclusion");			
			Document pathway = document.get("pathway", new Document());
			String name = pathway.getString("name");
			Document attendance = document.get("attendance", new Document());
			String code = attendance.getString("patientRecord");
			String key = code + "%" + name;
			int medicalCare = attendance.getInteger("codeMedicalCare");
			String hospital = attendance.getString("hospitalUnit");
			add( key, 
				pathway, 
				hospital+"%"+medicalCare+"%"+creation+"%"+conclusion,
				creation,
				conclusion);
		}
	}
	
	private void add(String key, Document value, String data, Date creation, Date conclusion) {
		if (this.patientsMap.containsKey(key)) {
			List<Document> values = this.patientsMap.get(key);
			values.add(value);
			this.patientsMap.replace(key, values);
		}
		else {
			List<Document> values = new ArrayList<>();
			values.add(value);
			this.patientsMap.put(key, values);
		}
		if (this.hospitalsMap.containsKey(key)) {
			List<String> values = this.hospitalsMap.get(key);
			values.add(data);
			this.hospitalsMap.replace(key, values);
		}
		else {
			List<String> values = new ArrayList<>();
			values.add(data);
			this.hospitalsMap.put(key, values);
		}
		Date[] dates = new Date[]{creation, conclusion};
		this.datesMap.put(data, dates);
	}

	public EQuery getResults(JSONArray data) {
		// TODO Auto-generated method stub
		return null;
	}
}
