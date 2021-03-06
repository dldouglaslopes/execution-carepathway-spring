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
import QueryMetamodel.QOccurrence;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QOccurrenceService {
	@Autowired
	private QCarePathwayService service;
	
	private int size = 0;
	
	public EQuery getOccurrences(EQuery eQuery, int version) {	
		long start = System.currentTimeMillis();
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {	
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>(); //finding all the documents
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QOccurrence qOccurrence = getData(docs, carePathway, i, j);
							if (qOccurrence.getPathway() != null) {
								eQuery.getEMethod().add(qOccurrence);
							}
						}						
					}
				}
			}
		}
		else if (version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
				QOccurrence qOccurrence = getData(docs, carePathway, i, 99);
				docs.clear();
				if (qOccurrence.getPathway() != null) {
					eQuery.getEMethod().add(qOccurrence);
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
			QOccurrence qOccurrence = getData(docs, carePathway, version, 99);
			docs.clear();
			if (qOccurrence.getPathway() != null) {
				eQuery.getEMethod().add(qOccurrence);
			}							
		}
		System.out.println((System.currentTimeMillis() - start ));

		return eQuery;
	}
	
	private QOccurrence getData(List<Document> docs, CarePathway carePathway, int version, int page) {
		QOccurrence qOccurrence = Query_metamodelFactory.eINSTANCE.createQOccurrence();
		if (docs.size() > 0) {
			this.size += docs.size();
		}	
		if (page == 99) {
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(size);
			pathway.setId(carePathway.getValue() + "");
			pathway.setVersion(version);
			qOccurrence.setPathway(pathway);
			this.size = 0;
		}		
		return qOccurrence;
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			String pathway = object.getJSONObject("pathway").getString("name");
			int value = object.getJSONObject("pathway").getInt("quantity");
			if (map.containsKey(pathway)) {
				int sum = value +
						map.get(pathway);
				map.replace(pathway, sum);
			}
			else {
				map.put(pathway, value);
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QOccurrence qOccurrence = Query_metamodelFactory.eINSTANCE.createQOccurrence();
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(name);
			pathway.setQuantity(map.get(name));
			pathway.setId("");
			pathway.setVersion(0);
			qOccurrence.setPathway(pathway);
			eQuery.getEMethod().add(qOccurrence);
		}
		return eQuery;
	}
}









