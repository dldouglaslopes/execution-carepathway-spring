package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QAbortedStep;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;
import QueryMetamodel.Version;

@Service
public class QAbortedStepService {
	@Autowired
	private QCarePathwayService service;
	
	private int sum = 0;
	private Map<Document, Integer> abortedMap = new HashMap<>();
	
	public EQuery getRecurrentAbortedStep(EQuery eQuery, String step, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						this.abortedMap = new HashMap<>();
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QAbortedStep qAbortedStep = getData(docs, 
																carePathway, 
																i, 
																step,
																j);
							if (qAbortedStep.getPathway() != null) {
								eQuery.getEMethod().add(qAbortedStep);
							}
						}
					}
				}
			}	
		}
		else if (version == 0){
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				this.abortedMap = new HashMap<>();
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery);
				QAbortedStep qAbortedStep = getData(docs, carePathway, i, step, 99);
				if (qAbortedStep.getPathway() != null) {
					eQuery.getEMethod().add(qAbortedStep);
				}
			}		
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			this.abortedMap = new HashMap<>();
			QAbortedStep qAbortedStep = getData(docs, carePathway, version, step, 99);
			docs.clear();
			if (qAbortedStep.getPathway() != null) {
				eQuery.getEMethod().add(qAbortedStep);
			}					
		}
		return eQuery;
	}

	private QAbortedStep getData(List<Document> docs, 
						CarePathway carePathway, 
						int version, 
						String stepStr,
						int page) {
		QAbortedStep qAbortedStep = Query_metamodelFactory.eINSTANCE.createQAbortedStep();
		getAbortedSteps(docs, version, stepStr);
		if (page == 99) {		
			for (Document key : abortedMap.keySet()) {
				Step step = Query_metamodelFactory.eINSTANCE.createStep();
				step.setDescription(key.getString("description"));
				step.setId(key.getInteger("_id") + "");
				step.setName(key.getString("name"));
				double percentage = service.rate(abortedMap.get(key), sum);
				step.setPercentage(service.decimalFormat(percentage) + "%");
				step.setQuantity(abortedMap.get(key));
				step.setType(key.getString("type"));
				qAbortedStep.getStep().add(step);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setId(carePathway.getValue() + "");
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.sum);
			pathway.setVersion(version);
			qAbortedStep.setPathway(pathway);
		}
		return qAbortedStep;
	}
	
	private void getAbortedSteps(List<Document> docs, 
								int version, 
								String stepStr) {
		for (Document document : docs) {
			List<Document> eSteps = document.get("executedSteps", new ArrayList<Document>());
			for (Document eStep : eSteps) {			
				boolean aborted = document.getBoolean("aborted");
				boolean completed = document.getBoolean("completed");
				if (!eStep.containsKey("next")) {
					if (aborted && completed) {
						Document step = eStep.get("step", new Document());
						add(step);						
					}						
				}
			}
		}
	}

	private void add(Document key) {
		if (abortedMap.containsKey(key)) {
			int value = abortedMap.get(key) + 1;
			abortedMap.replace(key, value);
			this.sum++;
		}		
		else {
			abortedMap.put(key, 1);
			this.sum++;
		}
	}

	public EQuery getResults(JSONArray data) {
		Map<String, JSONObject> map = new HashMap<>();
		Map<String, Integer> map2 = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			JSONArray steps = object.getJSONArray("step");
			for (int j = 0; j < steps.length(); j++) {
				JSONObject step = steps.getJSONObject(j);
				String id = step.getString("id");
				int quantity = step.getInt("quantity");
				if (map2.containsKey(id)) {
					int value = quantity +
							map2.get(id);
					map2.replace( id, value);
				}
				else {
					map.put(id, step);
					map2.put(id, quantity);
				}
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String id : map.keySet()) {
			QAbortedStep qAbortedStep = Query_metamodelFactory.eINSTANCE.createQAbortedStep();
			Step step = Query_metamodelFactory.eINSTANCE.createStep();
			JSONObject stepObj = map.get(id);
			step.setDescription(stepObj.getString("description"));
			step.setId(id);
			step.setName(stepObj.getString("name"));
			step.setQuantity(map2.get(id));
			step.setType(stepObj.getString("type"));
			step.setPercentage(null);
			qAbortedStep.getStep().add(step);
			qAbortedStep.setPathway(null);
			eQuery.getEMethod().add(qAbortedStep);
		}
		return eQuery;
	}
}
