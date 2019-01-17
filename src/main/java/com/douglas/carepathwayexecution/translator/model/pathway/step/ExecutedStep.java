package com.douglas.carepathwayexecution.translator.model.pathway.step;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import MetamodelExecution.Access;
import MetamodelExecution.Answer;
import MetamodelExecution.Audit;
import MetamodelExecution.Complement;
import MetamodelExecution.EAuxiliaryConduct;
import MetamodelExecution.EDischarge;
import MetamodelExecution.EInformation;
import MetamodelExecution.EPrescription;
import MetamodelExecution.EReferral;
import MetamodelExecution.EStep;
import MetamodelExecution.ETreatment;
import MetamodelExecution.Exam;
import MetamodelExecution.Examination;
import MetamodelExecution.Execution_metamodelFactory;
import MetamodelExecution.Justification;
import MetamodelExecution.Medication;
import MetamodelExecution.Numeric;
import MetamodelExecution.PrescribedExamination;
import MetamodelExecution.PrescribedInternment;
import MetamodelExecution.PrescribedMedication;
import MetamodelExecution.PrescribedPrescriptionItem;
import MetamodelExecution.PrescribedProcedure;
import MetamodelExecution.Prescription;
import MetamodelExecution.Question;
import MetamodelExecution.Result;
import MetamodelExecution.Step;
import MetamodelExecution.Unit;
import MetamodelExecution.Variable;
import MetamodelExecution.YesOrNo;

public class ExecutedStep {
	public EStep createEElement(JSONObject json, EStep eElement) throws ParseException, JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
		
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
			
			String type = answerJson.getString("type");		
			
			if (type.equals("RespostaSimOuNao")) {
				//set yes or no
				YesOrNo yesOrNo = Execution_metamodelFactory.eINSTANCE.createYesOrNo();
				yesOrNo.setValue(answerJson.getBoolean("valor"));				
				//save value
				//answer.setValue(yesOrNo);
			}
			else if (type.equals("RespostaNumerica")) {
				//set numeric
				Numeric numeric = Execution_metamodelFactory.eINSTANCE.createNumeric();
				numeric.setValue(answerJson.getDouble("valor"));				
				//save value
				//answer.setValue(numeric);
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
			
			if (type.equals("RespostaSimOuNao")) {			
				//set yes or no
				YesOrNo yesOrNo = Execution_metamodelFactory.eINSTANCE.createYesOrNo();
				yesOrNo.setValue(variableJson.getBoolean("valor"));					
				//save yes or no
				//variable.setValue(yesOrNo);			
			}else if (type.equals("RespostaNumerica")) {
				//set numeric
				Numeric numeric = Execution_metamodelFactory.eINSTANCE.createNumeric();
				numeric.setValue(variableJson.getDouble("valor"));				
				//save numeric
				//variable.setValue(numeric);
			}				
			
//			Bond bond = Execution_metamodelFactory.eINSTANCE.createBond();
//			if (!variableJson.isNull("vinculo")) {
//				JSONObject bondJson = variableJson.getJSONObject("vinculo");
//			//	bond.setId(bondJson.getInt("id"));
//				bond.setType(bondJson.getString("type"));
//			}
//						
			//save comorbidity
			//bond.setComorbidity(comorbidity);			
			//save bond
//			variable.setBond(bond);
			//save variable
			question.setVariable(variable);			
			//save question
			answer.setQuestion(question);			
			//save answer
			eAuxiliaryConduct.getAnswer().add(answer);
		}		
		
		return eAuxiliaryConduct;
	}
	
	public ETreatment createETreatment(JSONObject json) throws ParseException, JSONException{
		//set executed treatment
		ETreatment eTreatment = Execution_metamodelFactory.eINSTANCE.createETreatment();		
		eTreatment = (ETreatment) createEElement(json, eTreatment);	
		
		//set prescribed examination
		JSONArray idsPrescribedExaminationJson = json.getJSONArray("exames_prescritos_ids");		
		for (int i = 0; i < idsPrescribedExaminationJson.length(); i++) {
			//save prescribed examination
			eTreatment.getIdsPrescribedExamination().add(idsPrescribedExaminationJson.optInt(i));
		}
		
		//set prescribed  procedure
		JSONArray idsPrescribedProceduresJson = json.getJSONArray("procedimentos_prescritos_ids");		
		for (int i = 0; i < idsPrescribedProceduresJson.length(); i++) {
			//save prescribed  procedure
			eTreatment.getIdsPrescribedProcedure().add(idsPrescribedProceduresJson.optInt(i));
		}
		
		//set prescribed internment
		JSONArray idsPrescribedInternmentJson = json.getJSONArray("internamentos_prescritos_ids");		
		for (int i = 0; i < idsPrescribedInternmentJson.length(); i++) {
			//save prescribed internment
			eTreatment.getIdsPrescribedInternment().add(idsPrescribedInternmentJson.optInt(i));
		}
		
		//set prescribed medication 
		JSONArray idsPrescribedMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPrescribedMedicationJson.length(); i++) {
			//save prescribed medication 
			eTreatment.getIdsPrescribedMedication().add(idsPrescribedMedicationJson.optInt(i));
		}	
		
		//save prescribed medication
		eTreatment.getPrescribedmedication().addAll(createPrescribedMedication(json));
		
		//set prescribed examination
		JSONArray prescribedExaminations = json.getJSONArray("exames_prescritos");			
		for (int i = 0; i < prescribedExaminations.length(); i++) {			
			PrescribedExamination prescribedExamination = Execution_metamodelFactory.eINSTANCE.createPrescribedExamination();
			JSONObject prescribedExaminationJson = prescribedExaminations.getJSONObject(i);
			prescribedExamination.setId(prescribedExaminationJson.getInt("id"));
			prescribedExamination.setReport(prescribedExaminationJson.getString("laudo"));
			
			if (!prescribedExaminationJson.isNull("numero_guia")) {
				prescribedExamination.setNumberGuide(prescribedExaminationJson.getInt("numero_guia"));
			}			
			
			if (!prescribedExaminationJson.isNull("resultado")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
				JSONObject resultJson = prescribedExaminationJson.getJSONObject("resultado");			
				
				Result result = Execution_metamodelFactory.eINSTANCE.createResult();
				
				result.setId(resultJson.getInt("id"));
				result.setSuccess(resultJson.getBoolean("sucesso"));
				result.setMessage(resultJson.getString("mensagem"));			
				
				String resultStr = resultJson.getString("data_solicitacao");
				Date resultDate = dateFormat.parse(resultStr);			
				
				result.setRequestDate(resultDate);
			
				prescribedExamination.setResult(result);
				
			}						
			
			//save prescription 
			prescribedExamination.setPrescription(createPrescription(prescribedExaminationJson));
			
			//set complement
			Complement complement = Execution_metamodelFactory.eINSTANCE.createComplement();			
			if (!prescribedExaminationJson.isNull("complemento")) {				
				JSONObject complementJson = prescribedExaminationJson.getJSONObject("complemento");
				complement.setId(complementJson.getInt("id"));
				complement.setSideLimb(complementJson.getString("lado_membro"));
				complement.setJustification(complementJson.getString("justificativa"));
				complement.setClinicalIndication(complementJson.getString("indicacao_clinica"));
				
				if (!complementJson.isNull("quantidade")) {
					complement.setQuantity(complementJson.getInt("quantidade"));
				}			
			}
			//save complement	
			prescribedExamination.setComplement(complement);
			
			//set examination
			Examination examination = Execution_metamodelFactory.eINSTANCE.createExamination();
			JSONObject examinationJson = prescribedExaminationJson.getJSONObject("exame");
			examination.setId(examinationJson.getInt("id"));
			examination.setSideLimb(examinationJson.getString("lado_membro"));
			examination.setQuantity(examinationJson.getInt("quantidade"));
			examination.setJustification(examinationJson.getString("justificativa"));
			examination.setClinicalIndication(examinationJson.getString("indicacao_clinica"));
			
			//set exam
			Exam exam = Execution_metamodelFactory.eINSTANCE.createExam();
			JSONObject examJson = examinationJson.getJSONObject("exame");
			exam.setId(examJson.getInt("id"));
			exam.setCode(examJson.getInt("codigo"));
			exam.setName(examJson.getString("nome"));
			exam.setDescription(examJson.getString("descricao"));
			exam.setOnlyEmergency(examJson.getBoolean("somente_emergencia"));
			exam.setMemberPeers(examJson.getBoolean("membros_pares"));
			
			//save exam
			examination.setExam(exam);
			
			//save examination
			prescribedExamination.setExamination(examination);
			
			//save prescribed examination
			eTreatment.getPrescribedexam().add(prescribedExamination);
		}	
		
		//set prescribed procedure
		JSONArray prescribedProcedures = json.getJSONArray("procedimentos_prescritos");
		for (int i = 0; i < prescribedProcedures.length(); i++) {			
			PrescribedProcedure prescribedProcedure = Execution_metamodelFactory.eINSTANCE.createPrescribedProcedure();
			//save prescribed procedure
			eTreatment.getPrescribedprocedure().add(prescribedProcedure);
		}
		
		//set prescribed internment
		JSONArray prescribedInternments = json.getJSONArray("internamentos_prescritos");
		for (int i = 0; i < prescribedInternments.length(); i++) {			
			PrescribedInternment prescribedInternment = Execution_metamodelFactory.eINSTANCE.createPrescribedInternment();
			//save prescribed internment
			eTreatment.getPrescribedinternment().add(prescribedInternment);
		}		
		
		return eTreatment;
	}

	//set executed precription
	public EPrescription createEPrescription(JSONObject json) throws ParseException, JSONException{		
		EPrescription ePrescription = Execution_metamodelFactory.eINSTANCE.createEPrescription();
		ePrescription = (EPrescription) createEElement(json, ePrescription);
		
		ePrescription.setText(json.getString("texto"));
		
		//set ids prescribed medication 
		JSONArray idsPrescribedMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPrescribedMedicationJson.length(); i++) {
			//save prescribed medication 
			ePrescription.getIdsPrescribedMedication().add(idsPrescribedMedicationJson.optInt(i));
		}
		
		//set ids prescribed prescription item
		JSONArray idsPrescribedPrescriptionItemJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPrescribedPrescriptionItemJson.length(); i++) {
			//save prescribed PrescriptionItem 
			ePrescription.getIdsPrescribedPrescriptionItem().add(idsPrescribedPrescriptionItemJson.optInt(i));
		}
		
		//save prescription info
		//ePrescription.getPrescription().add(createPrescription(json));
		
		//set prescription prescription item
		JSONArray prescribedPrescriptionItens = json.getJSONArray("itens_receita_prescritos");
		for (int i = 0; i < prescribedPrescriptionItens.length(); i++) {			
			PrescribedPrescriptionItem prescribedPrescriptionItem = Execution_metamodelFactory.eINSTANCE.createPrescribedPrescriptionItem();
			//save prescription prescription item
			ePrescription.getPrescribedprescriptionitem().add(prescribedPrescriptionItem);
		}
		
		//save prescription medication
		List<PrescribedMedication> prescribedMedications = createPrescribedMedication(json);		
		for (int i = 0; i < prescribedMedications.size(); i++) {
			ePrescription.getPrescribedmedication().add(prescribedMedications.get(i));
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
		
		return eDischarge;
	}
	
	//set prescribed medication
	private List<PrescribedMedication> createPrescribedMedication(JSONObject json) throws ParseException, JSONException {
		JSONArray prescribedMedicationsJson = json.getJSONArray("medicamentos_prescritos");
		List<PrescribedMedication> prescribedMedications = new ArrayList<PrescribedMedication>();
		
		for (int i = 0; i < prescribedMedicationsJson.length(); i++) {
			PrescribedMedication prescribedMedication = Execution_metamodelFactory.eINSTANCE.createPrescribedMedication();
			JSONObject prescribedMedicationJson = prescribedMedicationsJson.getJSONObject(i);
			
			prescribedMedication.setId(prescribedMedicationJson.getInt("id"));
			if (!prescribedMedicationJson.isNull("resultado")) {
				prescribedMedication.setResult(prescribedMedicationJson.getString("resultado"));
			}
			
			//save prescription
			prescribedMedication.setPrescription(createPrescription(prescribedMedicationJson));
			
			//set medicament
			Medication medicament = Execution_metamodelFactory.eINSTANCE.createMedication();
			JSONObject medicamentJson = prescribedMedicationJson.getJSONObject("medicamento");
			medicament.setId(medicamentJson.getInt("id"));
			medicament.setName(medicamentJson.getString("nome"));
			medicament.setCode(medicamentJson.getString("codigo"));
			medicament.setDescription(medicamentJson.getString("descricao"));
			medicament.setBrand(medicamentJson.getString("marca"));
			medicament.setDailyDosage(medicamentJson.getInt("dose_diaria"));
			medicament.setCycles(medicamentJson.getInt("ciclos"));
			medicament.setFrequency(medicamentJson.getInt("frequencia"));
			medicament.setTimeInterval(medicamentJson.getInt("dias_intervalo"));
			medicament.setTimeTreatement(medicamentJson.getInt("dias_tratamento"));
			
			if (!medicamentJson.isNull("ambulatorial")) {
				medicament.setOutpatient(medicamentJson.getBoolean("ambulatorial"));
			}
			
			if (!medicamentJson.isNull("padrao")) {
				medicament.setStandard(medicamentJson.getString("padrao"));
			}	
			
			//set unit
			Unit unit = Execution_metamodelFactory.eINSTANCE.createUnit();
			JSONObject unitJson = medicamentJson.getJSONObject("unidade");
			unit.setId(unitJson.getInt("id"));
			unit.setName(unitJson.getString("nome"));
			unit.setCode(unitJson.getString("codigo"));
			unit.setUnit(unitJson.getString("unidade"));
			
			//save unit
			//medicament.getUnit().add(unit);
			
			//set access
			Access access = Execution_metamodelFactory.eINSTANCE.createAccess();
			JSONObject accessJson = medicamentJson.getJSONObject("via_acesso");
			access.setId(accessJson.getInt("id"));
			access.setName(accessJson.getString("nome"));
			access.setCode(accessJson.getInt("codigo"));
			
			//save access
			//medicament.getAccess().add(access);
			
			//prescribedMedication.setMedicament(medicament);
			
			//add prescribedMedication
			prescribedMedications.add(prescribedMedication);
		}		
		
		return prescribedMedications;
	}
	
	//set prescription exam
	private Prescription createPrescription(JSONObject json) throws ParseException, JSONException {
		Prescription prescription = Execution_metamodelFactory.eINSTANCE.createPrescription();
		
		if (!json.isNull("prescricao")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
			JSONObject prescriptionJson = json.getJSONObject("prescricao");			
			
			prescription.setId(prescriptionJson.getInt("id"));
			prescription.setSuccess(prescriptionJson.getBoolean("sucesso"));
			prescription.setMessage(prescriptionJson.getString("mensagem"));			
			
			String requestStr = prescriptionJson.getString("data_solicitacao");
			Date requestDate = dateFormat.parse(requestStr);			
			
			prescription.setRequestDate(requestDate);
		}		
		
		return prescription;
	}
}
