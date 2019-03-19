package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.QAbortedStep;

@Service
public class QStopStepService {
	@Autowired
	private QCarePathwayService service;
	
	private int numVersion;
	
	public EQuery getRecurrentAbortedStep(EQuery eQuery, String step, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					this.numVersion = 1;
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);
					for (int i = 1; i < numVersion + 1; i++) {
						QAbortedStep qAbortedStep = getData(docs, carePathway, i, step);
						if (qAbortedStep.getPathway() != null) {
							eQuery.getEMethod().add(qAbortedStep);
						}
					}
				}
			}	
		}
		else if (version == 0){
			this.numVersion = 1;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			for (int i = 1; i < numVersion + 1; i++) {
				QAbortedStep qAbortedStep = getData(docs, carePathway, i, step);
				if (qAbortedStep.getPathway() != null) {
					eQuery.getEMethod().add(qAbortedStep);
				}
			}		
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			QAbortedStep qAbortedStep = getData(docs, carePathway, version, step);
			if (qAbortedStep.getPathway() != null) {
				eQuery.getEMethod().add(qAbortedStep);
			}					
		}

		return eQuery;
	}

	private QAbortedStep getData(List<Document> docs, 
						CarePathway carePathway, 
						int version, 
						String stepStr) {
		
		return null;
	}

}
