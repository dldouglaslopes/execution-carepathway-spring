package com.douglas.carepathwayexecution.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.douglas.carepathwayexecution.web.model.EForm;

import QueryMetamodel.CarePathway;
import QueryMetamodel.ECarePathway;
import QueryMetamodel.EConduct;
import QueryMetamodel.EMethod;
import QueryMetamodel.EStep;
import QueryMetamodel.Method;
import QueryMetamodel.Query_metamodelFactory;

@Controller
@RequestMapping("/")
public class ExecutedCarePathwayController {
	
	@RequestMapping(value = { "/medicalCare/execution/{method}" }, method = RequestMethod.GET)
	public String selectByParam(Model model) {		
		return "selectByParam";
	}
	
	@RequestMapping(value = { "/medicalCare/execution/{id}" }, method = RequestMethod.GET)
	public String selectById(Model model) {		
		return "selectById";
	}
	
	@RequestMapping(value = { "/medicalCare/execution" }, method = RequestMethod.GET)
	public String selectByOptions(Model model) {		
	    EForm form = new EForm();
	    model.addAttribute("form", form);
	
	    List<EMethod> methodNames = new ArrayList<>();
	    List<Method> methodEnums = Method.VALUES;
	    
	    for (Method method : methodEnums) {
			EMethod eMethod = Query_metamodelFactory.eINSTANCE.createEMethod();
	    	eMethod.setName(method);
			
			methodNames.add(eMethod);
		}
	    
	    model.addAttribute("methodNames", methodNames);
	    
	    ECarePathway eCarePathway = Query_metamodelFactory.eINSTANCE.createECarePathway();
	    List<EConduct> eConducts = EConduct.VALUES;
	    List<EStep> eSteps = EStep.VALUES;
	    List<CarePathway> carePathways = CarePathway.VALUES;
	    
	    for (EStep eStep : eSteps) {
			eCarePathway.getSteps().add(eStep);
		}
	    
	    for (EConduct eConduct : eConducts) {
	    	eCarePathway.getConducts().add(eConduct);
		}
	    
	    for (CarePathway carePathway : carePathways) {
	    	eCarePathway.getCarePathways().add(carePathway);
	    }
	    
	    model.addAttribute("eCarePathway", eCarePathway);
	    
	    return "selectByOptions";
	}
}