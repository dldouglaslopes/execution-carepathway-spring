package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.douglas.carepathwayexecution.web.domain.EForm;
import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.ExecutedCarePathwayService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Controller
@RequestMapping("/")
public class ExecutedCarePathwayController {
	@Autowired
	private ExecutedCarePathwayService service;
	
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/status" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> statusToOnePathway(
			@PathVariable( value = "id", required=true) String idPathway,
			@RequestParam( value = "status", required=false) String statusStr,
			@RequestParam( value = "age", required=false) String ageStr,
			@RequestParam( value = "sex", required=false) String sexStr,
			@RequestParam( value = "date", required=false) String dateStr,
			@RequestParam( value = "range", required=false) String rangeStr,
			Model model) throws ParseException{			
	   
	    EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										splitBy( statusStr, ","),
										splitBy( ageStr, ","),
										sexStr, 
										splitBy( dateStr, ","),
										splitBy( rangeStr, ","));
		
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = service.countStatus(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@RequestMapping(value = { "/medcare/execution/pathways/{method}" }, 
					method = RequestMethod.GET)
	public String methodToAllPathways(
			@PathVariable( value = "method", required=true) String methodStr,
			@RequestParam( value = "status", required=false) String statusStr,
			@RequestParam( value = "age", required=false) String ageStr,
			@RequestParam( value = "sex", required=false) String sexStr,
			@RequestParam( value = "date", required=false) String dateStr,
			@RequestParam( value = "range", required=false) String rangeStr,
			Model model) throws ParseException{		
		
		EForm form = new EForm();
	    model.addAttribute("form", form);	
	    
//	    QueryStructure queryStructure = new QueryStructure();
//		
//	    EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
//		eQuery = queryStructure.create(methodStr,
//										0,
//										splitBy( statusStr, ","),
//										splitBy( ageStr, ","),
//										sexStr, 
//										splitBy( dateStr, ","),
//										splitBy( rangeStr, ","));
//		
		//List<Entry<String, Double>> results = queryStructure.call(eQuery);
	    	    
		//System.out.println(results.size());
		//model.addAttribute("results", results);
		
		return "selectByParam";
	}
	
	public String[] splitBy( String str, String symbol) {		
		if (!str.isEmpty()) {
			return str.split(symbol);
		}
		
		return null;
	}
}