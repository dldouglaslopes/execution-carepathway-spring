package com.douglas.carepathwayexecution.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QStatus;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QStatusService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery countStatus(EQuery eQuery) {
		
		//finding all the documents
		List<Document> statusDoc = service.filterDocuments(eQuery);		
		Map<String, Integer> aborted = new HashMap<>();
		Map<String, Integer> completed = new HashMap<>();
		Map<String, Integer> inProgress = new HashMap<>();
		
		//counting the occurrences of each status types
		for (Document document : statusDoc) {
			Document pathway = document.get("pathway", new Document());
			String key = pathway.getString("name");
			
			if (document.getBoolean("aborted")) {
				if (aborted.containsKey(key)) {
					int value = aborted.get(key) + 1;
					aborted.replace(key, value);
				}
				else {
					aborted.put(key, 1);
					completed.put(key, 0);
					inProgress.put(key,	0);
				}
			}
			else if (document.getBoolean( "completed")) {
				if (completed.containsKey(key)) {
					int value = completed.get(key) + 1;
					completed.replace(key, value);
				}
				else {
					completed.put(key, 1);
					aborted.put(key, 0);
					inProgress.put(key, 0);
				}
			}
			else{
				if (inProgress.containsKey(key)) {
					int value = inProgress.get(key) + 1;
					inProgress.replace(key, value);
				}
				else {
					inProgress.put(key, 1);
					aborted.put(key, 0);
					completed.put(key, 0);
				}
			}
		}
		for (String key : aborted.keySet()) {
			QStatus qStatus = Query_metamodelFactory.eINSTANCE.createQStatus();
			qStatus.setAborted(aborted.get(key));
			qStatus.setCompleted(completed.get(key));
			qStatus.setInProgress(inProgress.get(key));
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(CarePathway.getByName(key).getName());
			pathway.setQuantity(0);
			qStatus.setPathway(pathway);
			eQuery.getEMethod().add(qStatus);
		}
		
		return eQuery;				
	}
}
