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

import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.ECarePathwayService;
import com.douglas.carepathwayexecution.web.service.EPrescribedMedicationService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Controller
public class EPrescribedMedicationResource {
	@Autowired
	private ECarePathwayService service;
	@Autowired
	private EPrescribedMedicationService medicationService;
	
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/prescribedMedication" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> prescribedMedicationToOnePathway(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = medicationService.prescribedMedication(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}

	@RequestMapping(value = { "/medcare/execution/pathways/prescribedMedication" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> prescribedMedicationToAllPathways(
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = medicationService.prescribedMedication(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
}