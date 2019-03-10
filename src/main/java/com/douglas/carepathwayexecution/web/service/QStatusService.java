package com.douglas.carepathwayexecution.web.service;

import java.util.List;

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
	
	private int numVersion;
	private int aborted;
	private int completed;
	private int inProgress;
	
	public EQuery getStatus(EQuery eQuery, int version) {		
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery); //finding all the documents		
					this.numVersion = 1;
					if (!docs.isEmpty()) {
						for (int i = 1; i < numVersion + 1; i++) {
							this.aborted = 0;
							this.completed = 0;
							this.inProgress = 0;
							QStatus qStatus = getData(docs, carePathway, i);
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
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents		
			this.numVersion = 1;
			if (!docs.isEmpty()) {
				for (int i = 1; i < numVersion + 1; i++) {
					this.aborted = 0;
					this.completed = 0;
					this.inProgress = 0;
					QStatus qStatus = getData(docs, carePathway, i);
					if (qStatus.getPathway() != null) {
						eQuery.getEMethod().add(qStatus);
					}
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents		
			this.numVersion = 1;
			if (!docs.isEmpty()) {
				this.aborted = 0;
				this.completed = 0;
				this.inProgress = 0;
				QStatus qStatus = getData(docs, carePathway, version);
				if (qStatus.getPathway() != null) {
					eQuery.getEMethod().add(qStatus);
				}
			}
		}		
		return eQuery;				
	}
	
	private QStatus getData(List<Document> docs, 
							CarePathway carePathway, 
							int number) {
		QStatus qStatus = Query_metamodelFactory.eINSTANCE.createQStatus();
		int idPathway = 0;
		for (Document document : docs) { //counting the occurrences of each status types
			Document pathway = document.get("pathway", new Document());
			idPathway = pathway.getInteger("_id");
			String key = pathway.getString("name");
			int version = pathway.getInteger("version");
			if (version == number) {
				add(document, key);
			}
			if (this.numVersion < version) {
				this.numVersion = version;
			}
		}
		qStatus.setAborted(aborted);
		qStatus.setCompleted(completed);
		qStatus.setInProgress(inProgress);		
		Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
		pathway.setName(carePathway.getName());
		pathway.setQuantity(this.aborted + this.completed + this.inProgress);
		pathway.setId(idPathway + "");
		pathway.setVersion(number);
		qStatus.setPathway(pathway);
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
	