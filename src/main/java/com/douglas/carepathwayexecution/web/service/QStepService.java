package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QStep;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;
import QueryMetamodel.Version;

@Service
public class QStepService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> stepsMap;
	private int count;
	
	public EQuery getRecurrentSteps(EQuery eQuery, String stepStr, int version) {
		long start = System.currentTimeMillis();
		//System.out.println(start);
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					this.stepsMap = new HashMap<>();
					this.count = 0;
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j); //finding all the documents
							QStep qStep = getData(docs, carePathway, i, stepStr, j);
							if (qStep.getPathway() != null) {
								eQuery.getEMethod().add(qStep);
							}
						}
					}
				}
			}	
		}
		else if (version == 0){
			this.stepsMap = new HashMap<>();
			this.count = 0;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
				QStep qStep = getData(docs, carePathway, i, stepStr, 99);
				docs.clear();
				if (qStep.getPathway() != null) {
					eQuery.getEMethod().add(qStep);
				}
			}		
		}
		else {
			this.stepsMap = new HashMap<>();
			this.count = 0;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
			QStep qStep = getData(docs, carePathway, version, stepStr, 99);
			docs.clear();
			if (qStep.getPathway() != null) {
				eQuery.getEMethod().add(qStep);
			}					
		}
		System.out.println("Total: "+(System.currentTimeMillis() - start));
		return eQuery;
	}
	
	private QStep getData(List<Document> docs,
							CarePathway carePathway,
							int version,
							String stepStr,
							int page) {
		QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
		getSteps(docs, stepStr, version);
		if (page == 99) {		
			for (String key : stepsMap.keySet()) {
				Step step = Query_metamodelFactory.eINSTANCE.createStep();
				String[] stepArr = key.split("-");
				step.setId(stepArr[1]);
				step.setType(stepArr[2]);
				step.setName(stepArr[3]);
				step.setQuantity(stepsMap.get(key));
				double percentage = service.rate(stepsMap.get(key), count);
				step.setPercentage(service.decimalFormat(percentage) + "%");
				if (stepArr.length > 4) {
					step.setDescription(stepArr[4]);
				}
				else {
					step.setDescription("");
				}
				qStep.getStep().add(step);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(stepsMap.size());
			pathway.setVersion(version);
			pathway.setId(carePathway.getValue() + "");
			qStep.setPathway(pathway);
		}		
		return qStep;
	}
	
	private void getSteps(List<Document> docs, String stepStr, int number) {
		for (Document doc : docs) {
			int version = doc.get("pathway", new Document()).getInteger("version");
			if (version == number) {
				List<Document> stepsDoc = doc.get( "executedSteps", new ArrayList<Document>());
				if (!stepsDoc.isEmpty()) {
					addSteps(stepsDoc, stepStr, version);
				}
			}			
		}		
	}

	private void addSteps(List<Document> stepsDoc, String stepStr, int version) {
		for (Document document : stepsDoc) {
			Document step = document.get( "step", new Document());
			String key = version + "-" +
						step.getInteger("_id") + "-" +
						step.getString("type") + "-" +
						step.getString("name") + "-" +
						step.getString("description");
			String type = step.getString("type");
			if (stepsMap.containsKey(key)) {
				int value = stepsMap.get(key) + 1; 
				stepsMap.replace(key, value);
				count++;
			}
			else {
				if (stepStr == null) {
					stepsMap.put(key, 1);
					count++;
				}
				else{
					if (type.toLowerCase().matches(".*" + stepStr.toLowerCase() + ".*")) {
						stepsMap.put(key, 1);
						count++;
					}
				}
			}
		}
	}
}
