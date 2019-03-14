package com.douglas.carepathwayexecution.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QOccurrence;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QOccurrenceService {
	@Autowired
	private QCarePathwayService service;
	
	private int numVersion;
	
	public EQuery getOccurrences(EQuery eQuery, int version) {	
		Set<String> names = new HashSet<>();
		for (Document doc : service.filterDocuments(eQuery)) {
			boolean verificado = false;
			String name = doc.get("pathway", new Document()).getString("name");
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (carePathway.getLiteral().equals(name)) {
					verificado = true;
				}
			}
			if (!verificado) {
				names.add(name);
			}
		}
		for (String string : names) {
			System.out.println(string);
		}
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {	
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				if (!carePathway.equals(CarePathway.NONE)) {
					this.numVersion = 1;		
					List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
					for (int i = 1; i < numVersion + 1; i++) {
						QOccurrence qOccurrence = getData(docs, carePathway, i);
						if (qOccurrence.getPathway() != null) {
							eQuery.getEMethod().add(qOccurrence);
						}
					}
				}
			}
		}
		else if (version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			if (!carePathway.equals(CarePathway.NONE)) {
				this.numVersion = 1;		
				List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
				for (int i = 1; i < numVersion + 1; i++) {
					QOccurrence qOccurrence = getData(docs, carePathway, i);
					if (qOccurrence.getPathway() != null) {
						eQuery.getEMethod().add(qOccurrence);
					}
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
			QOccurrence qOccurrence = getData(docs, carePathway, version);
			if (qOccurrence.getPathway() != null) {
				eQuery.getEMethod().add(qOccurrence);
			}							
		}
		return eQuery;
	}
	
	private QOccurrence getData(List<Document> docs, CarePathway carePathway, int version) {
		QOccurrence qOccurrence = Query_metamodelFactory.eINSTANCE.createQOccurrence();
		int id = 0;		
		int size = 0;
		for (Document document : docs) {
			Document pathway = document.get("pathway", new Document());
			id = pathway.getInteger("_id");
			int number = pathway.getInteger("version");
			if (number == version) {
				size++;
			}
			if (this.numVersion < number) {
				this.numVersion = number;
			}
		}
		if (size > 0) {
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(size);
			pathway.setId(id + "");
			pathway.setVersion(version);
			qOccurrence.setPathway(pathway);
		}		
		return qOccurrence;
	}
}
