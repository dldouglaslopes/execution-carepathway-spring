package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;
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
			@PathVariable( value = "method", required=true) String methodStr, 
			@RequestParam( value = "pathways", required=false) String pathwaysStr,
			@RequestParam( value = "steps", required=false) String stepsStr,
			@RequestParam( value = "conducts", required=false) String conductsStr,
			@RequestParam( value = "status", required=false) String statusStr,
			@RequestParam( value = "age", required=false) String ageStr,
			@RequestParam( value = "sex", required=false) String sexStr,
			@RequestParam( value = "date", required=false) String dateStr,
			@RequestParam( value = "range", required=false) String rangeStr,
			Model model) throws ParseException{		
		
		EForm form = new EForm();
	    model.addAttribute("form", form);	
	    
	    QueryStructure queryStructure = new QueryStructure();
		
	    EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = queryStructure.create(methodStr,
										splitBy( pathwaysStr, ","),
										splitBy( stepsStr, ","),
										splitBy( conductsStr, ","),
										splitBy( statusStr, ","),
										splitBy( ageStr, ","),
										sexStr, 
										splitBy( dateStr, ","),
										splitBy( rangeStr, ","));
		
		List<Entry<String, Double>> results = queryStructure.call(eQuery);
	    	    
		model.addAttribute("results", results);
		
		return "selectByParam";
	}
	
	@RequestMapping(value = { "/medcare" }, method = RequestMethod.GET)
	public String structure(Model model) {		
		return "howToStructure";
	}
	
	public String[] splitBy( String str, String symbol) {		
		if (!str.isEmpty()) {
			return str.split(symbol);
		}
		
		return null;
	}
}