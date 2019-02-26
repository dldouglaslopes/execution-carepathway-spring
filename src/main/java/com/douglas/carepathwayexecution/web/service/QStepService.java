package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QStep;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;

@Service
public class QStepService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery getRecurrentSteps(EQuery eQuery, String stepStr) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);
				if (!docs.isEmpty()) {		
					QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
					List<Step> steps = getSteps(docs);
					for (Step step : steps) {
						qStep.getStep().add(step);
					}
					Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
					pathway.setName(carePathway.getName());
					pathway.setPercentage("");
					pathway.setQuantity(0);
					qStep.setPathway(pathway);
					eQuery.getEMethod().add(qStep);
				}			
			}	
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
			List<Step> steps = getSteps(docs);
			for (Step step : steps) {
				qStep.getStep().add(step);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setPercentage("");
			pathway.setQuantity(0);
			qStep.setPathway(pathway);
			eQuery.getEMethod().add(qStep);			
		}
		return eQuery;
	}
	
	private List<Step> getSteps(List<Document> docs) {
		List<Step> steps = new ArrayList<>();
		
		return steps;
	}
}
