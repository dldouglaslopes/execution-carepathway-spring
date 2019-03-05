package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QAverageTime;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QAverageTimeService {
	@Autowired
	private QCarePathwayService service;
	
	private double times;
	private int quantity;
	private int numVersion;
	private String idPathway = "";
	
	public EQuery getAverageByTime(EQuery eQuery, int version) {	//querying the average time
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					this.numVersion = 1;
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);
					if (!docs.isEmpty()) {
						for (int i = 1; i < numVersion + 1; i++) {
							this.times = 0;
							this.quantity = 0;
							getTime(docs, i);
							QAverageTime qAverageTime = getData(carePathway, i);
							if (qAverageTime.getPathway() != null) {
								eQuery.getEMethod().add(qAverageTime);
							}
						}
					}
				}
			}
		}
		else if(version == 0) {
			this.numVersion = 1;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			if (!docs.isEmpty()) {
				for (int i = 1; i < numVersion + 1; i++) {
					this.times = 0;
					this.quantity = 0;
					getTime(docs, i);
					QAverageTime qAverageTime = getData(carePathway, i);
					if (qAverageTime.getPathway() != null) {
						eQuery.getEMethod().add(qAverageTime);
					}
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			this.times = 0;
			this.quantity = 0;	
			List<Document> docs = service.filterDocuments(eQuery);
			if (!docs.isEmpty()) {
				getTime(docs, version);
				QAverageTime qAverageTime = getData(carePathway, version);
				if (qAverageTime.getPathway() != null) {
					eQuery.getEMethod().add(qAverageTime);
				}			
			}
		}		
		return eQuery;
	}
	
	private QAverageTime getData(CarePathway carePathway, int number) {		
		QAverageTime qAverageTime = Query_metamodelFactory.eINSTANCE.createQAverageTime();		
		Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();	
		qAverageTime.setAverage(times / 60);
		pathway.setQuantity(quantity);
		pathway.setName(carePathway.getName());	
		pathway.setId(this.idPathway);
		pathway.setVersion(number);
		qAverageTime.setPathway(pathway);
		return qAverageTime;
	}
	
	private void getTime(List<Document> docs, int number) {
		for (Document document : docs) {
			Document pathway = document.get("pathway", new Document());
			this.idPathway = pathway.getInteger("_id") + "";
			int version = pathway.getInteger("version");			
			if (number == version) {
				times =+ document.getDouble("timeExecution");
				this.quantity++;
			}
			if (this.numVersion < version) {
				this.numVersion = version;
			}			
		}
	}
}
