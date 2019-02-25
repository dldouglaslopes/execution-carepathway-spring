package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Occurrence;
import QueryMetamodel.QOccurrence;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QOccurrenceService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery countOccurrences(EQuery eQuery) {
		//finding all the documents
		List<Document> occurrencesDocs = service.filterDocuments(eQuery);	
		
		QOccurrence qOccurrence = Query_metamodelFactory.eINSTANCE.createQOccurrence();
		if (!eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			String field = "name";
			String name = eQuery.getEAttribute().getCarePathway().getName().getName(); 
			String key = eQuery.getEAttribute().getCarePathway().getName().getLiteral();
			int size = service.count( field, key, occurrencesDocs);

			Occurrence occurrence = Query_metamodelFactory.eINSTANCE.createOccurrence();
			occurrence.setValue(size);
			occurrence.setName(name);
			qOccurrence.getOccurrence().add(occurrence);
		}
		else {
			for (CarePathway key : CarePathway.VALUES) {	
				if (!key.equals(CarePathway.NONE)) {
					String field = "name";
					int size = service.count( field, key.getLiteral(), occurrencesDocs);

					if (size > 0) {
						Occurrence occurrence = Query_metamodelFactory.eINSTANCE.createOccurrence();
						occurrence.setValue(size);
						occurrence.setName(key.getName());
						qOccurrence.getOccurrence().add(occurrence);		
					}
				}
			}				
		}
				
		eQuery.getEMethod().add(qOccurrence);
		
		return eQuery;
	}
}
