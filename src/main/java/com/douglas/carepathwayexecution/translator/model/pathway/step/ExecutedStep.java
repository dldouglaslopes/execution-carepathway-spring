package com.douglas.carepathwayexecution.translator.model.pathway.step;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import MetamodelExecution.Answer;
import MetamodelExecution.Audit;
import MetamodelExecution.Choice;
import MetamodelExecution.Complement;
import MetamodelExecution.EAuxiliaryConduct;
import MetamodelExecution.EDischarge;
import MetamodelExecution.EInformation;
import MetamodelExecution.EPrescription;
import MetamodelExecution.EReferral;
import MetamodelExecution.EStep;
import MetamodelExecution.ETreatment;
import MetamodelExecution.Examination;
import MetamodelExecution.Execution_metamodelFactory;
import MetamodelExecution.Internment;
import MetamodelExecution.Justification;
import MetamodelExecution.Medication;
import MetamodelExecution.Numeric;
import MetamodelExecution.Option;
import MetamodelExecution.PExamination;
import MetamodelExecution.PInternment;
import MetamodelExecution.PMedication;
import MetamodelExecution.PPrescription;
import MetamodelExecution.PProcedure;
import MetamodelExecution.Prescription;
import MetamodelExecution.PrescriptionResult;
import MetamodelExecution.Procedure;
import MetamodelExecution.Question;
import MetamodelExecution.Result;
import MetamodelExecution.Step;
import MetamodelExecution.Variable;
import MetamodelExecution.YesOrNo;

public class ExecutedStep {
	public EStep createEElement(JSONObject json, EStep eElement) throws ParseException, JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		
		//set step
		JSONObject stepJson = json.getJSONObject("passo");
		Step step = Execution_metamodelFactory.eINSTANCE.createStep();
		step.setId(stepJson.getInt("id"));
		step.setType(stepJson.getString("type"));
		step.setName(stepJson.getString("nome"));
		step.setDescription(stepJson.getString("descricao"));
		step.setIsInitial(stepJson.getBoolean("is_initial"));
		step.setIsTerminal(stepJson.getBoolean("is_terminal"));
		step.setMandatory(stepJson.getBoolean("obrigatoriedade"));
				
		//set audit
		if (!stepJson.isNull("ultima_auditoria")) {
			Audit audit = Execution_metamodelFactory.eINSTANCE.createAudit();	
			JSONObject auditJson = stepJson.getJSONObject("ultima_auditoria");
			String dateStr = auditJson.getString("data");
			Date date = dateFormat.parse(dateStr);	
			
			audit.setDate(date);
			
			step.setAudit(audit);
		}			
		
		//set dates
		String creationStr = json.getString("data_criacao");
		Date creationDate = dateFormat.parse(creationStr);
		String modificationStr = json.getString("data_modificacao");
		Date modificationDate = dateFormat.parse(modificationStr);
		
		//Set executed element/step
		eElement.setId(json.getInt("id"));
		eElement.setIsCurrent(json.getBoolean("is_current"));
		eElement.setReworked(json.getBoolean("reworked"));
		eElement.setExecuted(json.getBoolean("executado"));		
		eElement.setCreatedById(json.getInt("criado_por_id"));		
		eElement.setStep(step);
		eElement.setName(eElement.getStep().getName());		
		eElement.setCreationDate(creationDate);	
		eElement.setModificationDate(modificationDate);
		
		if(!json.isNull("previous")) {
			JSONObject previousJson = json.getJSONObject("previous");
			eElement.setPrevious(previousJson.getString("url"));
		}
		if(!json.isNull("next")) {
			JSONObject nextJson = json.getJSONObject("next");
			eElement.setNext(nextJson.getString("url"));
		}
		
		//set justification
		if (!json.isNull("justificativa")) {	
			Justification justification = Execution_metamodelFactory.eINSTANCE.createJustification();
			JSONObject justificationJson = json.getJSONObject("justificativa");
			justification.setId(justificationJson.getInt("id"));
			justification.setReason(justificationJson.getString("razao"));
			justification.setDescription(justificationJson.getString("descricao"));
			justification.setJustifiedById(justificationJson.getInt("justificado_por_id"));
		
			eElement.setJustification(justification);
		}		
		if (!json.isNull("executado_por_id")) {
			eElement.setExecutedById(json.getInt("executado_por_id"));
		}		
		if (!json.isNull("data_execucao")) {
			String executionStr = json.getString("data_execucao");
			Date executionDate = dateFormat.parse(executionStr);
			
			eElement.setExecutionDate(executionDate);
		}		
		
		return eElement;
	}
	
	public EAuxiliaryConduct createEAuxiliaryConduct(JSONObject json) throws ParseException, JSONException{
		//Set executed auxiliary conduct
		EAuxiliaryConduct eAuxiliaryConduct = Execution_metamodelFactory.eINSTANCE.createEAuxiliaryConduct();
		eAuxiliaryConduct = (EAuxiliaryConduct) createEElement(json, eAuxiliaryConduct);
		
		//Set answers
		JSONArray answers = json.getJSONArray("respostas");
		
		for (int i = 0; i < answers.length(); i++) {
			//set answer
			JSONObject answerJson = answers.getJSONObject(i);
			Answer answer = Execution_metamodelFactory.eINSTANCE.createAnswer();			
			answer.setId(answerJson.getInt("id"));
			answer.setType(answerJson.getString("type"));
			
			if (!answerJson.isNull("justificativa")) {
				answer.setJustification(answerJson.getString("justificativa"));
			}
			
			String type = answerJson.getString("type");		
			
			if (type.equals("RespostaSimOuNao")) {				
				YesOrNo yesOrNo = Execution_metamodelFactory.eINSTANCE.createYesOrNo(); //set yes or no
				yesOrNo.setValue(answerJson.getBoolean("valor")); //save value				
				answer.setValue(yesOrNo); 
			}
			else if (type.equals("RespostaNumerica")) {				
				Numeric numeric = Execution_metamodelFactory.eINSTANCE.createNumeric(); //set numeric
				numeric.setValue(answerJson.getDouble("valor")); //save value				
				answer.setValue(numeric); 
			}
			else if (type.equals("RespostaEscolha")) {
				if (!answerJson.isNull("opcoes_escolhidas")) {
					Choice choice = Execution_metamodelFactory.eINSTANCE.createChoice();
					Object optionsJson = answerJson.get("opcoes_escolhidas");
					if (optionsJson.getClass().equals(Integer.class)) {
						choice.getOption().add(answerJson.getInt("opcoes_escolhidas"));
					}
					else {						
						JSONArray optionsArr = (JSONArray) optionsJson;
						for (int j = 0; j < optionsArr.length(); j++) {
							choice.getOption().add(optionsArr.optInt(j));
						}
					}				
										
					answer.setChoice(choice);
				}
			}
			
			//set question
			JSONObject questionJson = answerJson.getJSONObject("pergunta");
			Question question = Execution_metamodelFactory.eINSTANCE.createQuestion();			
			question.setId(questionJson.getInt("id"));
			question.setText(questionJson.getString("texto"));
			
			if (!questionJson.isNull("categoria")) {
				question.setCategory(questionJson.getString("categoria"));
			}  
			
			JSONObject variableJson = questionJson.getJSONObject("variavel");
			Variable variable = Execution_metamodelFactory.eINSTANCE.createVariable();
			variable.setId(variableJson.getInt("id"));
			variable.setType(variableJson.getString("type"));
			variable.setName(variableJson.getString("nome"));
			variable.setWeight(variableJson.getInt("peso"));			
			variable.setValue(variableJson.get("valor") + "");			
			
			if (!variableJson.isNull("opcoes")) {
				JSONArray optionsJson = variableJson.getJSONArray("opcoes");
				for (int j = 0; j < optionsJson.length(); j++) {
					Option option = Execution_metamodelFactory.eINSTANCE.createOption();
					JSONObject optionJson = optionsJson.getJSONObject(j);
					option.setId(optionJson.getInt("id"));
					option.setText(optionJson.getString("texto"));
					option.setWeight(optionJson.getDouble("peso"));
					variable.getOption().add(option);
				}
			}
			
			question.setVariable(variable); //save question
			answer.setQuestion(question);
			eAuxiliaryConduct.getAnswer().add(answer); //save answer
		}		
		
		return eAuxiliaryConduct;
	}
	
	public ETreatment createETreatment(JSONObject json) throws ParseException, JSONException{
		//set executed treatment
		ETreatment eTreatment = Execution_metamodelFactory.eINSTANCE.createETreatment();		
		eTreatment = (ETreatment) createEElement(json, eTreatment);	
		
		//set prescribed examination
		JSONArray idsPExaminationJson = json.getJSONArray("exames_prescritos_ids");		
		for (int i = 0; i < idsPExaminationJson.length(); i++) {
			eTreatment.getIdsPExamination().add(idsPExaminationJson.optInt(i));
		}
		
		//set prescribed  procedure
		JSONArray idsPProceduresJson = json.getJSONArray("procedimentos_prescritos_ids");		
		for (int i = 0; i < idsPProceduresJson.length(); i++) {
			eTreatment.getIdsPProcedure().add(idsPProceduresJson.optInt(i));
		}
		
		//set prescribed internment
		JSONArray idsPInternmentJson = json.getJSONArray("internamentos_prescritos_ids");		
		for (int i = 0; i < idsPInternmentJson.length(); i++) {
			eTreatment.getIdsPInternment().add(idsPInternmentJson.optInt(i));
		}
		
		//set prescribed medication 
		JSONArray idsPMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPMedicationJson.length(); i++) {
			eTreatment.getIdsPMedication().add(idsPMedicationJson.optInt(i));
		}
		
		//set prescribed paediatric medication 
		JSONArray idsPPaediatricMedicationJson = json.getJSONArray("medicamentos_pediatricos_prescritos_ids");		
		for (int i = 0; i < idsPPaediatricMedicationJson.length(); i++) {
			System.out.println("###################PEDIATRICO###############");
			//eTreatment.getIdsPPaediatricMedication().add(idsPPaediatricMedicationJson.optInt(i));
		}
		
		//save prescribed medication
		JSONArray pMedicationsJson = json.getJSONArray("medicamentos_prescritos");
		for (int j = 0; j < pMedicationsJson.length(); j++) {
			PMedication pMedication = Execution_metamodelFactory.eINSTANCE.createPMedication();
			eTreatment.getPmedication().add( setPMedication( pMedicationsJson.getJSONObject(j), pMedication));
		}
		
		//set prescribed examination
		JSONArray pExaminations = json.getJSONArray("exames_prescritos");			
		for (int i = 0; i < pExaminations.length(); i++) {			
			PExamination pExamination = Execution_metamodelFactory.eINSTANCE.createPExamination();
			JSONObject pExaminationJson = pExaminations.getJSONObject(i);
			pExamination.setId(pExaminationJson.getInt("id"));
			pExamination.setReport(pExaminationJson.getString("laudo"));
			
			if (!pExaminationJson.isNull("numero_guia")) {
				pExamination.setNumberGuide(pExaminationJson.getInt("numero_guia"));
			}			
			
			if (!pExaminationJson.isNull("resultado")) {			
				pExamination.setResult( createResult(pExaminationJson));			
			}						
			
			if (!pExaminationJson.isNull("prescricao")) {
				pExamination.setPrescriptionResult(createPrescriptionResult(pExaminationJson));
			}
			
			//set complement
			Complement complement = Execution_metamodelFactory.eINSTANCE.createComplement();			
			if (!pExaminationJson.isNull("complemento")) {				
				JSONObject complementJson = pExaminationJson.getJSONObject("complemento");
				complement.setId(complementJson.getInt("id"));
				complement.setSideLimb(complementJson.getString("lado_membro"));
				complement.setJustification(complementJson.getString("justificativa"));
				complement.setClinicalIndication(complementJson.getString("indicacao_clinica"));
				
				if (!complementJson.isNull("quantidade")) {
					complement.setQuantity(complementJson.getInt("quantidade"));
				}			
			}
			
			//save complement	
			pExamination.setComplement(complement);
			
			//set examination
			Examination examination = Execution_metamodelFactory.eINSTANCE.createExamination();
			JSONObject examinationJson = pExaminationJson.getJSONObject("exame");
			examination.setId(examinationJson.getInt("id"));
			examination.setSideLimb(examinationJson.getString("lado_membro"));
			examination.setQuantity(examinationJson.getInt("quantidade"));
			examination.setJustification(examinationJson.getString("justificativa"));
			examination.setClinicalIndication(examinationJson.getString("indicacao_clinica"));
			
			JSONObject examJson = examinationJson.getJSONObject("exame");
			examination.setIdExamination(examJson.getInt("id"));
			examination.setCode(examJson.getInt("codigo"));
			examination.setName(examJson.getString("nome"));
			examination.setDescription(examJson.getString("descricao"));
			examination.setOnlyEmergency(examJson.getBoolean("somente_emergencia"));
			examination.setMemberPeers(examJson.getBoolean("membros_pares"));
			
			//save examination
			pExamination.setExamination(examination);
			
			//save prescribed examination
			eTreatment.getPexamination().add(pExamination);
		}	
		
		//set prescribed procedure
		JSONArray pProceduresJson = json.getJSONArray("procedimentos_prescritos");
		for (int i = 0; i < pProceduresJson.length(); i++) {			
			JSONObject procedureJson = pProceduresJson.getJSONObject(i);			
			JSONObject itemProcedureJson = procedureJson.getJSONObject("procedimento");
			JSONObject itemJson = itemProcedureJson.getJSONObject("procedimento");
			
			Procedure procedure = Execution_metamodelFactory.eINSTANCE.createProcedure();
			procedure.setCode(itemJson.getString("codigo"));
			procedure.setDescription(itemJson.getString("descricao"));
			procedure.setFrequency(itemProcedureJson.getString("frequencia"));
			procedure.setId(itemProcedureJson.getInt("id"));
			procedure.setIdProcedure(itemJson.getInt("id"));
			procedure.setMemberPeers(itemJson.getBoolean("membros_pares"));
			procedure.setName(itemJson.getString("nome"));
			procedure.setOutpatient(itemJson.getBoolean("ambulatorial"));
			procedure.setQuantity(itemProcedureJson.getInt("quantidade"));
			procedure.setTypeName(itemJson.getString("nome_tipo"));
			
			if (!itemJson.isNull("codigo_tipo")) {
				procedure.setTypeCode(itemJson.getInt("codigo_tipo"));
			}
			
			if (!itemProcedureJson.isNull("categoria")) {
				procedure.setCategory(itemProcedureJson.getString("categoria"));
			}
			
			PProcedure pProcedure = Execution_metamodelFactory.eINSTANCE.createPProcedure();
			pProcedure.setId(procedureJson.getInt("id"));
			pProcedure.setProcedure(procedure);
			
			if (!procedureJson.isNull("prescricao")) {
				pProcedure.setPrescriptionResult(createPrescriptionResult(procedureJson));
			}
			
			if (!procedureJson.isNull("resultado")) {
				pProcedure.setResult(createResult(procedureJson));
			}
			
			eTreatment.getPprocedure().add(pProcedure);
		}
		
		//set prescribed internment
		JSONArray pInternmentsJson = json.getJSONArray("internamentos_prescritos");
		for (int i = 0; i < pInternmentsJson.length(); i++) {			
			JSONObject internmentJson = pInternmentsJson.getJSONObject(i);
			JSONObject itemInternmentJson = internmentJson.getJSONObject("internamento");
			JSONObject itemJson = itemInternmentJson.getJSONObject("internacao");
			
			Internment internment = Execution_metamodelFactory.eINSTANCE.createInternment();
			internment.setCode(itemJson.getString("codigo"));
			internment.setDescription(itemJson.getString("descricao"));
			internment.setClinicalIndication(itemInternmentJson.getString("indicacao_clinica"));
			internment.setId(itemInternmentJson.getInt("id"));
			internment.setIdInternment(itemJson.getInt("id"));
			internment.setMemberPeers(itemJson.getBoolean("membros_pares"));
			internment.setName(itemJson.getString("nome"));
			internment.setOutpatient(itemJson.getBoolean("ambulatorial"));
			internment.setQuantity(itemInternmentJson.getInt("quantidade"));
			internment.setTypeName(itemJson.getString("nome_tipo"));
			internment.setJustification(itemInternmentJson.getString("justificativa"));
			
			if (!itemInternmentJson.isNull("categoria")) {
				internment.setCategory(itemInternmentJson.getString("categoria"));
			}
			
			if (!itemJson.isNull("codigo_tipo")) {
				internment.setTypeCode(itemJson.getInt("codigo_tipo"));
			}
			
			PInternment pInternment = Execution_metamodelFactory.eINSTANCE.createPInternment();
			pInternment.setId(internmentJson.getInt("id"));
			pInternment.setInternment(internment);
			
			if (!internmentJson.isNull("numero_guia")) {
				pInternment.setNumberGuide(internmentJson.getInt("numero_guia"));
			}
			
			if (!internmentJson.isNull("prescricao")) {
				pInternment.setPrescriptionResult(createPrescriptionResult(internmentJson));
			}
			
			if (!internmentJson.isNull("resultado")) {
				pInternment.setResult(createResult(internmentJson));
			}
						
			eTreatment.getPinternment().add(pInternment);
		}		
		
		return eTreatment;
	}

	//set executed precription
	public EPrescription createEPrescription(JSONObject json) throws ParseException, JSONException{		
		EPrescription ePrescription = Execution_metamodelFactory.eINSTANCE.createEPrescription();
		ePrescription = (EPrescription) createEElement(json, ePrescription);
		
		ePrescription.setText(json.getString("texto"));
		
		//set ids prescribed medication 
		JSONArray idsPMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPMedicationJson.length(); i++) {
			//save prescribed medication 
			ePrescription.getIdsPMedication().add(idsPMedicationJson.optInt(i));
		}
		
		//set ids prescribed prescription item
		JSONArray idsPPrescriptionJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPPrescriptionJson.length(); i++) {
			//save prescribed Prescription 
			ePrescription.getIdsPPrescription().add(idsPPrescriptionJson.optInt(i));
		}
		
		//save prescribed medication
		JSONArray pMedicationsJson = json.getJSONArray("medicamentos_prescritos");
		for (int j = 0; j < pMedicationsJson.length(); j++) {
			PMedication pMedication = Execution_metamodelFactory.eINSTANCE.createPMedication();
			ePrescription.getPmedication().add( setPMedication( pMedicationsJson.getJSONObject(j), pMedication));
		}
		
		//set prescription prescription item
		JSONArray pPrescriptionJson = json.getJSONArray("itens_receita_prescritos");
		for (int i = 0; i < pPrescriptionJson.length(); i++) {			
			Prescription prescription = Execution_metamodelFactory.eINSTANCE.createPrescription();
			
			JSONObject prescriptionJson = pPrescriptionJson.getJSONObject(i);			
			JSONObject itemJson = prescriptionJson.getJSONObject("item_receita");
			prescription.setAccess( itemJson.getString("via_acesso"));
			prescription.setFrequency(itemJson.getInt("quantidade_frequencia_uso"));
			prescription.setId(itemJson.getInt("id"));
			prescription.setMedication(itemJson.getString("medicamento"));
			prescription.setName(itemJson.getString("nome"));
			prescription.setOrder(itemJson.getInt("ordem"));
			prescription.setPresentation(itemJson.getString("apresentacao"));
			prescription.setQtdDuration(itemJson.getInt("quantidade_duracao"));
			prescription.setQtdPrescription(itemJson.getInt("quantidade_receita"));
			prescription.setUnitDuration(itemJson.getString("unidade_duracao_display"));
			prescription.setUnitFrequency(itemJson.getString("unidade_frequencia_display"));
			prescription.setComplement(itemJson.getString("complemento"));
			
			if (!itemJson.isNull("medicamento_id")) {
				prescription.setIdMedication(itemJson.getInt("medicamento_id"));
			}
			
			PPrescription pPrescription = Execution_metamodelFactory.eINSTANCE.createPPrescription();
			pPrescription.setId(prescriptionJson.getInt("id"));
			pPrescription.setLastPrescriptionExecuted(prescriptionJson.getBoolean("prescrito_ultima_receita_executado"));
			pPrescription.setPrescription(prescription);
			
			if (!prescriptionJson.isNull("prescricao")) {
				pPrescription.setPrescriptionResult(createPrescriptionResult(prescriptionJson));
			}
			if (!prescriptionJson.isNull("resultado")) {
				pPrescription.setResult(createResult(prescriptionJson));
			}
			
			//save prescription prescription
			ePrescription.getPprescription().add(pPrescription);
		}
		
		return ePrescription;
	}

	//set executed information
	public EInformation createEInformation(JSONObject json) throws ParseException, JSONException{		
		EInformation eInformation = Execution_metamodelFactory.eINSTANCE.createEInformation();
		eInformation = (EInformation) createEElement(json, eInformation);
		
		return eInformation;
	}
	
	//set executed referral
	public EReferral createEReferral(JSONObject json) throws ParseException, JSONException{		
		EReferral eReferral = Execution_metamodelFactory.eINSTANCE.createEReferral();
		eReferral = (EReferral) createEElement(json, eReferral);
		
		return eReferral;
	}
	
	//set executed discharge
	public EDischarge createEDischarge(JSONObject json) throws ParseException, JSONException{
		EDischarge eDischarge = Execution_metamodelFactory.eINSTANCE.createEDischarge();
		eDischarge = (EDischarge) createEElement(json, eDischarge);
		
		if (!json.isNull("prescricao")) {
			eDischarge.setPrescriptionResult(createPrescriptionResult(json));
		}
		
		return eDischarge;
	}
	
	//set prescribed medication
	private PMedication setPMedication(JSONObject json, PMedication pMedication) throws ParseException, JSONException {
		JSONObject medicationJson = json.getJSONObject("medicamento");
		JSONObject medJson = medicationJson.getJSONObject("medicamento");
		JSONObject unitJson = medicationJson.getJSONObject("unidade");
		JSONObject accessJson = medicationJson.getJSONObject("via_acesso");

		//set medicament
		Medication medication = Execution_metamodelFactory.eINSTANCE.createMedication();
		medication.setId(medicationJson.getInt("id"));
		medication.setIdMedication(medJson.getInt("id"));
		medication.setName(medJson.getString("nome"));
		medication.setCode(medJson.getBigInteger("codigo") + "");
		medication.setDescription(medJson.getString("descricao"));
		medication.setBrand(medJson.getString("marca"));
		medication.setDailyDosage(medicationJson.getInt("dose_diaria"));
		medication.setCycles(medicationJson.getInt("ciclos"));
		medication.setFrequency(medicationJson.getInt("frequencia"));
		medication.setTimeInterval(medicationJson.getInt("dias_intervalo"));
		medication.setTimeTreatment(medicationJson.getInt("dias_tratamento"));
		medication.setCodeUnit(unitJson.getString("codigo"));
		medication.setUnit(unitJson.getString("unidade"));
		medication.setAccess(accessJson.getString("nome"));
		medication.setCodeAccess(accessJson.getInt("codigo"));
		
		if (!medicationJson.isNull("ambulatorial")) {
			medication.setOutpatient(medicationJson.getBoolean("ambulatorial"));
		}
		
		if (!medicationJson.isNull("padrao")) {
			//medication.setStandard(medicationJson.getString("padrao"));
		}			
		
		if (!medicationJson.isNull("categoria")) {
			medication.setCategory(medicationJson.getString("categoria"));
		}
		
		pMedication.setId(json.getInt("id"));
		pMedication.setMedication(medication);
		
		if (!json.isNull("resultado")) {
			pMedication.setResult(createResult(json));
		}
		
		if (!json.isNull("prescricao")) {
			pMedication.setPrescriptionResult(createPrescriptionResult(json));
		}			
		
		return pMedication;
	}
	
	//set prescription result
	private PrescriptionResult createPrescriptionResult(JSONObject json) throws ParseException, JSONException {
		PrescriptionResult prescription = Execution_metamodelFactory.eINSTANCE.createPrescriptionResult();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		JSONObject prescriptionJson = json.getJSONObject("prescricao");			
		prescription.setId(prescriptionJson.getInt("id"));
		prescription.setSuccess(prescriptionJson.getBoolean("sucesso"));
		prescription.setMessage(prescriptionJson.getString("mensagem"));			
		String requestStr = prescriptionJson.getString("data_solicitacao");
		Date requestDate = dateFormat.parse(requestStr);			
		prescription.setRequestDate(requestDate);		
		return prescription;
	}
	
	//set result
	private Result createResult(JSONObject json) throws ParseException {
		Result result = Execution_metamodelFactory.eINSTANCE.createResult();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		JSONObject resultJson = json.getJSONObject("resultado");				
		result.setId(resultJson.getInt("id"));
		result.setSuccess(resultJson.getBoolean("sucesso"));
		result.setMessage(resultJson.getString("mensagem"));			
		String resultStr = resultJson.getString("data_solicitacao");
		Date resultDate = dateFormat.parse(resultStr);					
		result.setRequestDate(resultDate);			
		return result; 
	}
}
