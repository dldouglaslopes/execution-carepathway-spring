package com.douglas.carepathwayexecution.query;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
	public EQuery create( String methodStr){
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
		
		try {
			date.setFrom(dateFormat.parse("2018-05-29T18:36:25.013818-03:00"));
			date.setFrom(dateFormat.parse("2018-10-03T18:36:25.013818-03:00"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		sex.setSex(Gender.ALL);
		age.setFrom(0);
		age.setTo(0);
		range.setQuantity(5);
		range.setOrder(Order.TOP);
		eCarePathway.getSteps().add(EStep.ALL);
		eCarePathway.getConducts().add(EConduct.ALL);
		eCarePathway.getCarePathways().add(CarePathway.PNEUMONIA_INFLUENZA);
		date.setFrom(null);
		date.setTo(null);
		status.setMessage(Message.ALL);
		status.setValue(true);
		attribute.setRange( range);
		attribute.setSex( sex);
		attribute.setStatus( status);
		attribute.setAge( age);
		attribute.setDate( date);
		attribute.setCarePathway( eCarePathway);		
		method.setName( Method.getByName(methodStr));
		method.setEAttribute(attribute);
		query.setEMethod(method);
	
		return query;
	}
	
	public void call(EQuery eQuery) {			
		//new QueryMethod(eQuery).conducts();		
		
		java.lang.reflect.Method method;
		
		try {
			System.out.println(eQuery.getEMethod().getName());
			method = QueryMethod.class.getMethod(eQuery.getEMethod().getName() + "");
			method.setAccessible(true);
			try {
				method.invoke(new QueryMethod(eQuery));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
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
		sex.setSex(Gender.ALL);
		age.setFrom(0);
		age.setTo(0);
		range.setQuantity(5);	
		range.setOrder(Order.TOP);
		eCarePathway.getSteps().add(EStep.ALL);
		eCarePathway.getConducts().add(EConduct.ALL);
		eCarePathway.getCarePathways().add(CarePathway.PNEUMONIA_INFLUENZA);
		date.setFrom(null);
		date.setTo(null);
		status.setMessage(Message.ALL);
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
