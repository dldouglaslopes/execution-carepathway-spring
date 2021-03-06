package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.QCarePathwayService;
import com.douglas.carepathwayexecution.web.service.QPatientReturnService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "PatientReturn", 
	description = "Show the return patient of the care pathway execution",
	produces ="application/json")
@Controller
public class QPatientReturnResource {
	@Autowired
	private QCarePathwayService service;
	@Autowired
	private QPatientReturnService returnService;
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathway(
		@PathVariable( value = "id", required=true) int idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, 0, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id by pathway version")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/return/patients" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathwayByVersion(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "version", required=true) int version,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, version, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id +"
			+ "by pathway version and patient code")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/return/patients/{code}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathwayByVersionAndPatientCode(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "version", required=true) int version,
		@PathVariable( value = "code", required=true) String code,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, code, version, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToAllPathways(
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, 0, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients/{code}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToOnePathway(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "code", required=true) String codePatient,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, codePatient, 0, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients/{code}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToAllPathways(
		@PathVariable( value = "code", required=true) String codePatient,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, codePatient, 0, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id by time interval")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathwayByHours(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, 0, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id "
			+ "by pathway version and time interval")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/return/patients/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathwayByVersionAndHours(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "version", required=false) int version,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, version, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id +"
			+ "by pathway version, time interval and patient code")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/return/patients/{code}/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathwayByVersionAndPatientCodeAndHours(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "version", required=true) int version,
		@PathVariable( value = "code", required=true) String code,
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, code, version, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patients that return of each care pathway by time interval")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToAllPathwaysByHours(
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, null, 0, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of a specified care pathway id by time interval")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients/{code}/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToOnePathwayByHours(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "code", required=true) String codePatient,
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, codePatient, 0, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of each care pathway by time interval")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients/{code}/hours/{time}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToAllPathwaysByHours(
		@PathVariable( value = "code", required=true) String codePatient,
		@PathVariable( value = "time", required=false) int hours,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = returnService.getReturnPatient(eQuery, codePatient, 0, hours);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
}
