package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
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
}
	