package com.douglas.carepathwayexecution.query;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import QueryMetamodel.Age;
import QueryMetamodel.CarePathway;
import QueryMetamodel.Date;
import QueryMetamodel.EAttribute;
import QueryMetamodel.ECarePathway;
import QueryMetamodel.EConduct;
import QueryMetamodel.EMethod;
import QueryMetamodel.EQuery;
import QueryMetamodel.EStep;
import QueryMetamodel.Gender;
import QueryMetamodel.Message;
import QueryMetamodel.Method;
import QueryMetamodel.Order;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Range;
import QueryMetamodel.Sex;
import QueryMetamodel.Status;

public class QueryStructure {			
	public EQuery create( String methodStr, 
						String[] pathways, 
						String[] steps, 
						String[] conducts,
						String[] statusArr,
						String[] ages, 
						String sexStr, 
						String[] dates, 
						String[] ranges){
		
		EQuery query = Query_metamodelFactory.eINSTANCE.createEQuery();
		
		EMethod method = Query_metamodelFactory.eINSTANCE.createEMethod();
		EAttribute attribute = Query_metamodelFactory.eINSTANCE.createEAttribute();	
		Sex sex = Query_metamodelFactory.eINSTANCE.createSex();
		Age age = Query_metamodelFactory.eINSTANCE.createAge();
		Range range = Query_metamodelFactory.eINSTANCE.createRange();
		ECarePathway eCarePathway = Query_metamodelFactory.eINSTANCE.createECarePathway();
		Date date = Query_metamodelFactory.eINSTANCE.createDate();
		Status status = Query_metamodelFactory.eINSTANCE.createStatus();
		
		sex.setSex( Gender.getByName( sexStr));
				
		if (ages != null) {
			age.setFrom( Integer.parseInt(ages[0]));
			age.setTo( Integer.parseInt(ages[1]));
		}
		else {
			age.setFrom( 0);
			age.setTo( 0);
		}
		
		if (ranges != null) {
			range.setQuantity( Integer.parseInt(ranges[0]));	
			range.setOrder( Order.getByName( ranges[1]));			
		}
		else {
			range.setQuantity(0);	
			range.setOrder(Order.RANDOM);
		}
		
		if ( steps != null) {
			for (String step : steps) {
				eCarePathway.getSteps().add( EStep.get( Integer.parseInt(step)));
			}
		}
		
		if ( conducts != null) {
			for (String conduct : conducts) {
				eCarePathway.getConducts().add( EConduct.get( Integer.parseInt(conduct)));
			}
		}
				
		if ( pathways != null) {
			for (String pathway : pathways) {
				eCarePathway.getCarePathways().add(CarePathway.get( Integer.parseInt(pathway)));
			}
		}
		
		if (dates != null) {
			/////////////////////
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());	
			
			if (dates[0] != null) {
				try {
					date.setFrom(dateFormat.parse("2018-05-29T18:36:25.013818-03:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else {
				date.setFrom(null);
			}
			
			if (dates[1] != null) {
				try {
					date.setTo(dateFormat.parse("2018-05-29T18:36:25.013818-03:00"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else {
				date.setTo(null);
			}
			
			/////////////////////
		}
		else {
			date.setFrom(null);
			date.setTo(null);
		}		
		
		if (statusArr != null) {
			if (statusArr[0] != null) {
				status.setMessage(Message.getByName(statusArr[0]));
			}
			else {
				status.setMessage(null);
			}
			
			status.setValue(Boolean.valueOf(statusArr[1]));	
			
		}		
		
		attribute.setRange( range);
		attribute.setSex( sex);
		attribute.setStatus( status);
		attribute.setAge( age);
		attribute.setDate( date);
		attribute.setCarePathway( eCarePathway);
		
		method.setName(Method.getByName(methodStr));
		method.setEAttribute(attribute);
		
		query.setEMethod(method);
			
		return query;
	}
	
	public List<Entry<String, Double>> call(EQuery eQuery) {			
		//new QueryMethod(eQuery).conducts();		
		
		java.lang.reflect.Method method;
		Object results = null;
		
		try {
			System.out.println(eQuery.getEMethod().getName());
			method = QueryMethod.class.getMethod(eQuery.getEMethod().getName() + "");
			method.setAccessible(true);
			try {
				results = method.invoke(new QueryMethod(eQuery));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return (List<Entry<String, Double>>) results;
	}
		
	public void runExample() throws ParseException {		
		EQuery query = Query_metamodelFactory.eINSTANCE.createEQuery();
		EMethod method = Query_metamodelFactory.eINSTANCE.createEMethod();
		EAttribute attribute = Query_metamodelFactory.eINSTANCE.createEAttribute();	
		Sex sex = Query_metamodelFactory.eINSTANCE.createSex();
		Age age = Query_metamodelFactory.eINSTANCE.createAge();
		Range range = Query_metamodelFactory.eINSTANCE.createRange();
		ECarePathway eCarePathway = Query_metamodelFactory.eINSTANCE.createECarePathway();
		Date date = Query_metamodelFactory.eINSTANCE.createDate();
		Status status = Query_metamodelFactory.eINSTANCE.createStatus();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());	
		
		date.setFrom(dateFormat.parse("2018-05-29T18:36:25.013818-03:00"));
		date.setFrom(dateFormat.parse("2018-10-03T18:36:25.013818-03:00"));
		sex.setSex(null);
		age.setFrom(0);
		age.setTo(0);
		range.setQuantity(5);	
		range.setOrder(Order.TOP);
		eCarePathway.getSteps().add(null);
		eCarePathway.getConducts().add(null);
		eCarePathway.getCarePathways().add(CarePathway.PNEUMONIA_INFLUENZA);
		date.setFrom(null);
		date.setTo(null);
		status.setMessage(null);
		status.setValue(true);
		attribute.setRange( range);
		attribute.setSex( sex);
		attribute.setStatus( status);
		attribute.setAge( age);
		attribute.setDate( date);
		attribute.setCarePathway( eCarePathway);
		method.setName(Method.CONDUCTS);
		method.setEAttribute(attribute);
		query.setEMethod(method);
		
		call(query);
	}	
}
