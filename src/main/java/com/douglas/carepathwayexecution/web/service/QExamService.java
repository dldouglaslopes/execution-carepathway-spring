package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Exam;
import QueryMetamodel.Pathway;
import QueryMetamodel.QExam;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QExamService {
	@Autowired
	private QCarePathwayService service;

	private int numVersion;
	private int idPathway = 0;
	private Map<String, Integer> examsMap;
	private int qtdExams;
	
	public EQuery getRecurrentExam(EQuery eQuery, String exam, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);
					this.numVersion = 1;
					for (int i = 1; i < numVersion + 1; i++) {
						QExam qExam = getData(docs, 
								exam, 
								eQuery.getEAttribute().getRange(), 
								carePathway, 
								i);
						if (qExam.getPathway() != null) {
							eQuery.getEMethod().add(qExam);
						}
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			List<Document> docs = service.filterDocuments(eQuery);
			numVersion = 1;
			for (int i = 1; i < numVersion + 1; i++) {
				QExam qExam = getData(docs, 
						exam, 
						eQuery.getEAttribute().getRange(), 
						carePathway, 
						i);
				if (qExam.getPathway() != null) {
					eQuery.getEMethod().add(qExam);
				}
			}		
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			List<Document> docs = service.filterDocuments(eQuery);
			QExam qExam = getData(docs, 
					exam, 
					eQuery.getEAttribute().getRange(), 
					carePathway, 
					version);
			if (qExam.getPathway() != null) {
				eQuery.getEMethod().add(qExam);
			}										
		}		
		return eQuery;
	}

	private QExam getData(List<Document> docs, 
						String examStr, 
						ARange range, 
						CarePathway carePathway, 
						int version) {
		QExam qExam = Query_metamodelFactory.eINSTANCE.createQExam();
		this.examsMap = new HashMap<>();
		this.qtdExams = 0;
		if (!docs.isEmpty()) {			
			List<Entry<String, Double>> list = getExams(docs, examStr, version, range);
			for (Entry<String, Double> entry : list) {
				Exam exam = Query_metamodelFactory.eINSTANCE.createExam();
				String key = entry.getKey();
				String[] examArr = key.split("-");
				exam.setId(examArr[0]);
				exam.setName(examArr[1]);
				exam.setQuantity(examsMap.get(key));
				exam.setPercentage(entry.getValue() + "%");
				qExam.getExam().add(exam);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setId(this.idPathway + "");
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.qtdExams);
			pathway.setVersion(version);
			qExam.setPathway(pathway);
		}		
		return qExam;
	}
	
	private List<Entry<String, Double>> getExams(List<Document> docs, String examStr, int number, ARange range) {
		for (Document doc : docs) {
			int version = doc.get("pathway", new Document()).getInteger("version");
			this.idPathway = doc.get("pathway", new Document()).getInteger("_id");
			if (version == number) {
				List<Document> eSteps = doc.get("executedSteps", new ArrayList<>());			
				getExamsInTreatement(eSteps, examStr);			
				List<Document> compConducts = doc.get("complementaryConducts", new ArrayList<>());			
				getExamsInComplementaryConducts(compConducts, examStr);
			}
			if (this.numVersion < version) {
				this.numVersion = version;
			}
		}
		Map<String, Double> percentMap = new HashMap<>();
		for (String key : examsMap.keySet()) {
			double value = service.rate( examsMap.get(key), this.qtdExams);
			percentMap.put( key, value);
		}		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());		
		service.sort(list, range.getOrder()); //sorting the list with a comparator		
		list = service.select( range.getQuantity(), list);						
		return list;
	}
	
	private void getExamsInTreatement(List<Document> eSteps, String name) {
		for (Document eStep : eSteps) { 
			Document step = eStep.get("step", new Document());
			String typeStr = step.getString("type");				
			if (typeStr.equals("Tratamento")) {
				List<Document> pExaminations = step.get("pexamination", new ArrayList<>());
				for (Document pExamination : pExaminations) {
					Document examination = pExamination.get("examination", new Document());
					String key = examination.getInteger("_id") + "-" +
								examination.getString("name");
					if (name == null) {
						add(key);
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key);
						}
					}
				}
			}
		}
	}
	
	private void getExamsInComplementaryConducts(List<Document> compConducts, String name) {
		for (Document compConduct : compConducts) { 
			String type = compConduct.getString("type");
			if (type.equals("ExameComplementar")) {
				Document conduct = compConduct.get("prescribedresource", new Document());
				String key = conduct.getInteger("_id") + "-" +
							conduct.getString("name");
				if (name == null) {
					add(key);
				}			
				else {
					if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
						add(key);
					}
				}			
			}
		}
	}

	private void add(String key) {
		if (examsMap.containsKey(key)) {
			int value = examsMap.get(key) + 1;
			examsMap.replace(key, value);
			this.qtdExams++;
		}
		else {
			examsMap.put(key, 1);
			this.qtdExams++;
		}
	}
}
