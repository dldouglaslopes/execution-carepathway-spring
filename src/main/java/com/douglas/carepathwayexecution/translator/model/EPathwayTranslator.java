package com.douglas.carepathwayexecution.translator.model;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.douglas.carepathwayexecution.translator.model.pathway.ExecutedPathway;
import com.douglas.carepathwayexecution.translator.model.pathway.complementaryconduct.ComplementaryConduct;
import com.douglas.carepathwayexecution.translator.model.pathway.step.ExecutedStep;

import MetamodelExecution.EPathway;
import MetamodelExecution.Execution_metamodelFactory;

public class EPathwayTranslator {
	private EPathway ePathway;
	
	public EPathway getePathway() {
		return ePathway;
	}

	//Constructor
	public EPathwayTranslator(){		
		this.ePathway = Execution_metamodelFactory.eINSTANCE.createEPathway();	
	}
	
	//convert JSON files in one XMI file
	public void toXMI(JSONObject json) throws ParseException, JSONException{	
		ExecutedStep executedStep = new ExecutedStep();
		ComplementaryConduct complementaryConduct = new ComplementaryConduct();
		ExecutedPathway executedPathway = new ExecutedPathway();
		ePathway = executedPathway.addEPathway(json, ePathway);
		
		//set executed steps
		JSONArray executedStepJsons = json.getJSONArray("passos_executados");	
	
		for (int i = 0; i < executedStepJsons.length(); i++) {
			JSONObject executedStepJson = executedStepJsons.getJSONObject(i);
			String type = executedStepJson.getJSONObject("passo").getString("type");
			selectEStep(type, executedStepJson, executedStep);
		}
		
		//set complementary conducts
		JSONArray complementaryConductsJson = json.getJSONArray("condutas_complementares");	
		
		for (int i = 0; i < complementaryConductsJson.length(); i++) {
			JSONObject complementaryConductJson = complementaryConductsJson.getJSONObject(i);
			String type = complementaryConductJson.getString("type");
			selectComplementaryConducts(type, complementaryConductJson, complementaryConduct);
		}
	}	

	private void selectEStep(
			String type, 
			JSONObject json, 
			ExecutedStep executedStep) throws ParseException, JSONException {
		
		switch (type) {
		case "AuxilioConduta":					
			ePathway.getEStep().add(executedStep.createEAuxiliaryConduct(json));
			break;			
		case "Tratamento":	
			ePathway.getEStep().add(executedStep.createETreatment(json));
			break;					
		case "Receita":	
			ePathway.getEStep().add(executedStep.createEPrescription(json));
			break;			
		case "Encaminhamento":	
			ePathway.getEStep().add(executedStep.createEReferral(json));
			break;			
		case "Informacao":
			ePathway.getEStep().add(executedStep.createEInformation(json));
			break;			
		case "Alta":
			ePathway.getEStep().add(executedStep.createEDischarge(json));
			break;			
		case "Pausa":
			System.out.println("UNKNOWN STEP TYPE! - pausa");
			//ePathway.getElement().add(executedStep.createEDischarge(json));
			break;			
		case "Acao":
			System.out.println("UNKNOWN STEP TYPE! - acao");
			//ePathway.getElement().add(executedStep.createEDischarge(json));
			break;			
		case "Processo":
			System.out.println("UNKNOWN STEP TYPE! - processo");
			//ePathway.getElement().add(executedStep.createEDischarge(json));
			break;
		default:
			System.out.println("UNKNOWN STEP TYPE!");
			break;			
		}
	}
	
	private void selectComplementaryConducts(
			String type, 
			JSONObject json,
			ComplementaryConduct complementaryConduct) throws ParseException, JSONException {
		
		switch (type) {
		case "MedicamentoComplementar":
			ePathway.getComplementaryconducts().add(complementaryConduct.createComplementaryMedicamention(json));
			break;
		case "ProcedimentoComplementar":
			ePathway.getComplementaryconducts().add(complementaryConduct.createComplementaryProcedure(json));
			break;
		case "ReceitaComplementar":
			System.out.println("UNKNOWN CONDUCT TYPE! - receita");
			//ePathway.getComplementaryconducts().add(complementaryConduct.createComplementaryItemPrescription(json));
			break;
		case "ExameComplementar":
			ePathway.getComplementaryconducts().add(complementaryConduct.createComplementaryExamination(json));
			break;
		default:
			System.out.println("UNKNOWN CONDUCT TYPE!");
			break;
		}		
	}
}
