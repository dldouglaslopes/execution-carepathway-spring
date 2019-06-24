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
import QueryMetamodel.QStatus;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QStatusService {
	@Autowired
	private QCarePathwayService service;
	
	private int aborted;
	private int completed;
	private int inProgress;
	
	public EQuery getStatus(EQuery eQuery, int version) {		
		long start = System.currentTimeMillis();
		//System.out.println(start);
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>(); //finding all the documents
						this.aborted = 0;
						this.completed = 0;
						this.inProgress = 0;
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QStatus qStatus = getData(docs, carePathway, i, j);
							if (qStatus.getPathway() != null) {
								eQuery.getEMethod().add(qStatus);
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
				this.aborted = 0;
				this.completed = 0;
				this.inProgress = 0;
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery); //finding all the documents		
				QStatus qStatus = getData(docs, carePathway, i, 99);
				docs.clear();
				if (qStatus.getPathway() != null) {
					eQuery.getEMethod().add(qStatus);
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents		
			this.aborted = 0;
			this.completed = 0;
			this.inProgress = 0;
			QStatus qStatus = getData(docs, carePathway, version, 99);
			docs.clear();
			if (qStatus.getPathway() != null) {
				eQuery.getEMethod().add(qStatus);
			}
		}		
		System.out.println("Total: "+(System.currentTimeMillis() - start));
		return eQuery;				
	}
	
	private QStatus getData(List<Document> docs, 
							CarePathway carePathway, 
							int number,
							int page) {
		QStatus qStatus = Query_metamodelFactory.eINSTANCE.createQStatus();
		for (Document document : docs) { //counting the occurrences of each status types
			Document pathway = document.get("pathway", new Document());
			String key = pathway.getString("name");
			add(document, key);
		}
		if (page == 99) {
			qStatus.setAborted(aborted);
			qStatus.setCompleted(completed);
			qStatus.setInProgress(inProgress);		
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.aborted + this.completed + this.inProgress);
			pathway.setId(carePathway.getValue() + "");
			pathway.setVersion(number);
			qStatus.setPathway(pathway);
		}
		return qStatus;
	}	
	
	private void add(Document document, String key) {
		if (document.getBoolean("aborted")) {
			this.aborted++;
		}
		else if (document.getBoolean( "completed")) {
			this.completed++;
		}
		else{
			this.inProgress++;
		}
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Map<String, Integer>> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			String pathway = object.getJSONObject("pathway").getString("name");
			if (map.containsKey(pathway)) {
				int completed = object.getInt("completed") +
						map.get(pathway).get("completed");
				int inProgress = object.getInt("inProgress") + 
						map.get(pathway).get("inProgress");
				int aborted = object.getInt("aborted") +
						map.get(pathway).get("aborted");
				Map<String, Integer> map2 = map.get(pathway);
				map2.replace("completed", completed);
				map2.replace("aborted", aborted);
				map2.replace("inProgress", inProgress);
				map.replace(pathway, map2);
			}
			else {
				Map<String, Integer> map2 = new HashMap<String, Integer>();
				map2.put("completed", object.getInt("completed"));
				map2.put("inProgress", object.getInt("inProgress"));
				map2.put("aborted", object.getInt("aborted"));
				map.put(pathway, map2);
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QStatus qStatus = Query_metamodelFactory.eINSTANCE.createQStatus();
			qStatus.setAborted(map.get(name).get("aborted"));
			qStatus.setCompleted(map.get(name).get("completed"));
			qStatus.setInProgress(map.get(name).get("inProgress"));
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(name);
			pathway.setQuantity(this.aborted + this.completed + this.inProgress);
			pathway.setId("");
			pathway.setVersion(0);
			qStatus.setPathway(pathway);
			eQuery.getEMethod().add(qStatus);
		}
		return eQuery;
	}
}
	