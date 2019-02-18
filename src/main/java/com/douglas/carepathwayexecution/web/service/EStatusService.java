package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;
import QueryMetamodel.QStatus;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EStatusService {
	@Autowired
	private ECarePathwayService service;
	
	public EQuery countStatus(EQuery eQuery) {
		
		//finding all the documents
		List<Document> status = service.getService(eQuery);		

		QStatus eStatus = Query_metamodelFactory.eINSTANCE.createQStatus();		
		int aborted = 0;
		int completed = 0;
		int inProgress = 0;
		
		//counting the occurrences of each status types
		for (Document doc : status) {
			if (doc.getBoolean("aborted")) {
				aborted++;
			}
			else if (doc.getBoolean( "completed")) {
				completed++;
			}
			else{
				inProgress++;
			}
		}
		
//		eStatus.setAborted(aborted);
//		eStatus.setCompleted(completed);
//		eStatus.setInProgress(inProgress);
		eQuery.setEMethod(eStatus);
		
		return eQuery;				
	}
}
