package com.douglas.carepathwayexecution.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.douglas.carepathwayexecution.query.QueryStructure;
import com.douglas.carepathwayexecution.web.model.EForm;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Controller
@RequestMapping("/")
public class ExecutedCarePathwayController {
	
	//http://localhost:8080/medcare/execution/status?pathways=&steps=&conducts=&age=&sex=&date=&range=
	
	@RequestMapping(value = { "/medcare/execution/{method}" }, method = RequestMethod.GET)
	public String selectByParam(
			@PathVariable( value = "method", required=true) String method, 
			@RequestParam( value = "pathways", required=false) String pathwaysStr,
			@RequestParam( value = "steps", required=false) String stepsStr,
			@RequestParam( value = "conducts", required=false) String conductsStr,
			@RequestParam( value = "age", required=false) String ageStr,
			@RequestParam( value = "sex", required=false) String sexStr,
			@RequestParam( value = "date", required=false) String dateStr,
			@RequestParam( value = "range", required=false) String rangeStr,
			Model model){		
		
		EForm form = new EForm();
	    model.addAttribute("form", form);	
	    
	    QueryStructure queryStructure = new QueryStructure();
		
	    EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = queryStructure.create(method,
										splitBy( pathwaysStr, ","),
										splitBy( stepsStr, ","),
										splitBy( conductsStr, ","),
										splitBy( ageStr, ","),
										splitBy( sexStr, ","),
										splitBy( dateStr, ","),
										splitBy( rangeStr, ","));
		
		List<Entry<String, Double>> results = queryStructure.call(eQuery);
	    	    
		model.addAttribute("results", results);
		
		return "selectByParam";
	}
	
	@RequestMapping(value = { "/" }, method = RequestMethod.GET)
	public String structure(Model model) {		
		return "howToStructure";
	}
	
	public List<String> splitBy( String str, String symbol) {		
		if (!str.isEmpty()) {
			String[] arr = str.split(symbol);
		    List<String> list = new ArrayList<>(Arrays.asList(arr));
		
		    return list;
		}
		
		return null;
	}

}

/*
	@RequestMapping(value = { "/medcare/execution" }, method = RequestMethod.GET)
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
*/