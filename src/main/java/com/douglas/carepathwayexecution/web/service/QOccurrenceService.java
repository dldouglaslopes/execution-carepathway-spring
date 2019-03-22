package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
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
	
	public EQuery getOccurrences(EQuery eQuery, int version) {	
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {	
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
						QOccurrence qOccurrence = getData(docs, carePathway, i);
						docs.clear();
						if (qOccurrence.getPathway() != null) {
							eQuery.getEMethod().add(qOccurrence);
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
				QOccurrence qOccurrence = getData(docs, carePathway, i);
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
			QOccurrence qOccurrence = getData(docs, carePathway, version);
			docs.clear();
			if (qOccurrence.getPathway() != null) {
				eQuery.getEMethod().add(qOccurrence);
			}							
		}
		return eQuery;
	}
	
	private QOccurrence getData(List<Document> docs, CarePathway carePathway, int version) {
		QOccurrence qOccurrence = Query_metamodelFactory.eINSTANCE.createQOccurrence();
		if (docs.size() > 0) {
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(docs.size());
			pathway.setId(carePathway.getValue() + "");
			pathway.setVersion(version);
			qOccurrence.setPathway(pathway);
		}		
		return qOccurrence;
	}
}
