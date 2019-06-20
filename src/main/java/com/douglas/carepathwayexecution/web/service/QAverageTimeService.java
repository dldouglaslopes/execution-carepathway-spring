package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QAverageTime;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QAverageTimeService {
	@Autowired
	private QCarePathwayService service;
	
	private double times;
	
	public EQuery getAverageByTime(EQuery eQuery, int version) {	//querying the average time
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						this.times = 0;
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							getTime(docs);
							QAverageTime qAverageTime = getData(carePathway, docs.size(), i, j);
							if (qAverageTime.getPathway() != null) {
								eQuery.getEMethod().add(qAverageTime);
							}
						}
					}
				}
			}
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			if (!carePathway.equals(CarePathway.NONE)) {
				int numVersion = Version.getByName(carePathway.getName()).getValue();
				for (int i = 1; i < numVersion + 1; i++) {
					eQuery.getEAttribute().getCarePathway().setVersion(i);
					List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
					this.times = 0;
					getTime(docs);
					QAverageTime qAverageTime = getData(carePathway, docs.size(), i, 99);
					if (qAverageTime.getPathway() != null) {
						eQuery.getEMethod().add(qAverageTime);
					}
				}
			}
		}
		else {
			this.times = 0;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			getTime(docs);
			QAverageTime qAverageTime = getData(carePathway, docs.size(), version, 99);
			if (qAverageTime.getPathway() != null) {
				eQuery.getEMethod().add(qAverageTime);
			}
		}		
		return eQuery;
	}
	
	private QAverageTime getData(CarePathway carePathway, int size, int version, int page) {		
		QAverageTime qAverageTime = Query_metamodelFactory.eINSTANCE.createQAverageTime();		
		if (page == 99) {
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();	
			qAverageTime.setAverage(times / 60);
			pathway.setQuantity(size);
			pathway.setName(carePathway.getName());	
			pathway.setId(carePathway.getValue() + "");
			pathway.setVersion(version);
			qAverageTime.setPathway(pathway);
		}
		return qAverageTime;
	}
	
	private void getTime(List<Document> docs) {
		for (Document document : docs) {
			times =+ document.getDouble("timeExecution");		
		}
	}
}
