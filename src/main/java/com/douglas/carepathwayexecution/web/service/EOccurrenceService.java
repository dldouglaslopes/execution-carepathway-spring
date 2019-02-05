package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EOccurrence;
import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EOccurrenceService {
	@Autowired
	private ECarePathwayService service;
	
	public EQuery countOccurrences(EQuery eQuery) {
		//finding all the documents
		List<Document> occurrencesDocs = service.getService(eQuery);					
		
		int size = 0;
		String field = "name";
		String name = eQuery.getEAttribute().getCarePathway().getName().getName();				
		
		if (name != CarePathway.NONE.getName()) {
			String literal = eQuery.getEAttribute().getCarePathway().getName().getLiteral();
			size = service.count( field, literal, occurrencesDocs);		
		}
		else {
			String literal = eQuery.getEAttribute().getCarePathway().getName().getLiteral();
			size = service.count( field, literal, occurrencesDocs);		
		}
		
		EOccurrence occurrence = Query_metamodelFactory.eINSTANCE.createEOccurrence();
		occurrence.setValue(size);
		eQuery.setEMethod(occurrence);
		
		return eQuery;
	}
}
