package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Exam;
import QueryMetamodel.Pathway;
import QueryMetamodel.QExam;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;
import QueryMetamodel.Version;

@Service
public class QExamService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> examsMap;
	private Map<String, Map<String, Integer>> stepsMap;
	private List<Entry<String, Double>> data;
	private int qtdExams;
	
	public EQuery getRecurrentExam(EQuery eQuery, String exam, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						this.examsMap = new HashMap<>();
						this.stepsMap = new HashMap<>();
						this.data = new ArrayList<Map.Entry<String,Double>>();
						this.qtdExams = 0;
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QExam qExam = getData(docs, 
									exam, 
									eQuery.getEAttribute().getRange(), 
									carePathway, 
									i,
									j);
							if (qExam.getPathway() != null) {
								eQuery.getEMethod().add(qExam);
							}
						}
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				this.examsMap = new HashMap<>();
				this.stepsMap = new HashMap<>();
				this.data = new ArrayList<Map.Entry<String,Double>>();
				this.qtdExams = 0;
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery);
				QExam qExam = getData(docs, 
						exam, 
						eQuery.getEAttribute().getRange(), 
						carePathway, 
						i,
						99);
				docs.clear();
				if (qExam.getPathway() != null) {
					eQuery.getEMethod().add(qExam);
				}
			}		
		}
		else {
			this.examsMap = new HashMap<>();
			this.stepsMap = new HashMap<>();
			this.data = new ArrayList<Map.Entry<String,Double>>();
			this.qtdExams = 0;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			QExam qExam = getData(docs, 
					exam, 
					eQuery.getEAttribute().getRange(), 
					carePathway, 
					version,
					99);
			docs.clear();
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
						int version,
						int page) {
		QExam qExam = Query_metamodelFactory.eINSTANCE.createQExam();
		List<Entry<String, Double>> list = getExams(docs, examStr, version, range, page);
		if (list.size() > 0) {
			data.addAll(list);
				
			for (Entry<String, Double> entry : data) {
				Exam exam = Query_metamodelFactory.eINSTANCE.createExam();
				String key = entry.getKey();
				String[] examArr = key.split("-");
				exam.setId(examArr[0]);
				exam.setName(examArr[1]);
				exam.setQuantity(examsMap.get(key));
				exam.setPercentage(service.decimalFormat(entry.getValue()) + "%");
				for (String stepStr : stepsMap.get(key).keySet()) {
					Step step = Query_metamodelFactory.eINSTANCE.createStep();
					String[] stepArr = stepStr.split("%");
					step.setId(stepArr[0]);
					step.setType(stepArr[2]);
					step.setName(stepArr[1]);
					if (stepArr[2].equals("ExameComplementar")) {
						exam.setName(examArr[1].split(":")[1]);
					}
					double percentage = service.rate(stepsMap.get(key).get(stepStr), examsMap.get(key));
					step.setPercentage(service.decimalFormat(percentage) + "%");
					if (stepArr.length > 3) {
						step.setDescription(stepArr[3]);
					}
					step.setQuantity(stepsMap.get(key).get(stepStr));
					exam.getStep().add(step);
				}
				qExam.getExam().add(exam);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setId(carePathway.getValue() + "");
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.qtdExams);
			pathway.setVersion(version);
			qExam.setPathway(pathway);
		}		
		return qExam;
	}
	
	private List<Entry<String, Double>> getExams(List<Document> docs, 
												String examStr, 
												int number, 
												ARange range, 
												int page) {
		for (Document doc : docs) {
			List<Document> eSteps = doc.get("executedSteps", new ArrayList<>());			
			getExamsInTreatement(eSteps, examStr);			
			List<Document> compConducts = doc.get("complementaryConducts", new ArrayList<>());			
			getExamsInComplementaryConducts(compConducts, examStr);
		}
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>();
		if (page == 99) {
			Map<String, Double> percentMap = new HashMap<>();
			for (String key : examsMap.keySet()) {
				double value = service.rate( examsMap.get(key), this.qtdExams);
				percentMap.put( key, value);
			}		
			list = new LinkedList<>( percentMap.entrySet());		
			service.sort(list, range.getOrder()); //sorting the list with a comparator		
			list = service.select( range.getQuantity(), list);	
		}					
		return list;
	}
	
	private void getExamsInTreatement(List<Document> eSteps, String name) {
		for (Document eStep : eSteps) { 
			Document step = eStep.get("step", new Document());			
			String typeStr = step.getString("type");			
			if (typeStr.equals("Tratamento")) {
				List<Document> pExaminations = eStep.get("pexamination", new ArrayList<Document>());
				for (Document pExamination : pExaminations) {
					Document examination = pExamination.get("examination", new Document());
					String key = examination.getInteger("idExamination") + "-" +
								examination.getString("name");
					String stepStr = step.getInteger("_id") + "%" +
							step.getString("name") + "%" + 
							step.getString("type") + "%" +
							step.getString("description");
					if (name == null) {
						add(key, stepStr);
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key, stepStr);
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
				Document conduct = compConduct.get("examinationprescribedresource", new Document());
				String key = conduct.getInteger("idExam") + "-" +
							conduct.getString("exam");
				String step = compConduct.getInteger("_id") + "%" +
						 	compConduct.getString("resource") + "%" + 
							type + "%" +
							compConduct.getString("justification");
				if (name == null) {
					add(key, step);
				}			
				else {
					if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
						add(key, step);
					}
				}			
			}
		}
	}

	private void add(String key, String step) {
		if (examsMap.containsKey(key)) {
			int value = examsMap.get(key) + 1;
			examsMap.replace(key, value);
			this.qtdExams++;
		}
		else {
			examsMap.put(key, 1);
			this.qtdExams++;
		}
		if (stepsMap.containsKey(key)) {
			if (stepsMap.get(key).containsKey(step)) {
				Map<String, Integer> value = stepsMap.get(key);
				int sum = value.get(step) + 1;
				value.replace(step, sum);
				stepsMap.put(key, value);
			}
			else {
				Map<String, Integer> value = stepsMap.get(key);
				value.put(step, 1);
				stepsMap.put(key, value);
			}
		}
		else {
			Map<String, Integer> value = new HashMap<>();
			value.put(step, 1);
			stepsMap.put(key, value);
		}
	}

	public EQuery getResults(JSONArray data2) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < data2.length(); i++) {
			JSONObject object = data2.getJSONObject(i);
			JSONArray exams = object.getJSONArray("exam");
			for (int j = 0; j < exams.length(); j++) {
				JSONObject exam = exams.getJSONObject(j);
				int quantity = exam.getInt("quantity");
				String name = exam.getString("name");
				if (map.containsKey(name)) {
					int value = quantity +
							map.get(name);
					map.replace( name, value);
				}
				else {
					map.put(name, quantity);
				}
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QExam qExam = Query_metamodelFactory.eINSTANCE.createQExam();
			Exam exam = Query_metamodelFactory.eINSTANCE.createExam();
			exam.setName(name);
			exam.setQuantity(map.get(name));
			qExam.getExam().add(exam);
			eQuery.getEMethod().add(qExam);
		}
		return eQuery;
	}
}
