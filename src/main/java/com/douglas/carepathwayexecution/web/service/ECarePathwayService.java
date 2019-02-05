package com.douglas.carepathwayexecution.web.service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.douglas.carepathwayexecution.query.DBConfig;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

import QueryMetamodel.Age;
import QueryMetamodel.CarePathway;
import QueryMetamodel.Date;
import QueryMetamodel.EAttribute;
import QueryMetamodel.ECarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Gender;
import QueryMetamodel.Message;
import QueryMetamodel.Order;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Range;
import QueryMetamodel.Sex;
import QueryMetamodel.Status;

@Service
public class ECarePathwayService {
	private DBConfig dbConfig;	
		
	public FindIterable<Document> getService(EQuery eQuery) {
		dbConfig = new DBConfig();
		return filterDocuments(eQuery);
	}
			
	private FindIterable<Document> filterDocuments(EQuery eQuery) {
		ECarePathway carePathway = eQuery.getEAttribute().getCarePathway();
		Age age = eQuery.getEAttribute().getAge(); ; 
		//Date date = eQuery.getEAttribute().getDate();;
		Sex sex = eQuery.getEAttribute().getSex();;
		Status status = eQuery.getEAttribute().getStatus();;
		
		FindIterable<Document> docs = dbConfig.getCollection().find();		
			
		if(carePathway.getName() != CarePathway.NONE) {
			
			docs = docs.filter( Filters.eq( "name", 
											carePathway.getName().getLiteral()));
		}			
		
//		if (carePathway.getName() != CarePathway.NONE) {
//			docs = docs.filter( Filters.exists( "complementaryConducts", 
//												carePathway.isConduct()));
//		}
				
		if (sex.getSex() != Gender.ALL) {
			docs = docs.filter( Filters.eq( "medicalcare.sex", sex.getSex()));
		}												
		
		if (status.getMessage() != Message.ALL) {
			System.out.println(status.getMessage().getName());
			docs = docs.filter( Filters.eq( status.getMessage().getName(), status.isValue()));
		}		 
	
		if (age.getFrom() > 0 && age.getTo() == 0) {
			docs = docs.filter( Filters.gte( "medicalcare.age", 
										age.getFrom()));
		}		
		else if (age.getFrom() >= 0 && age.getTo() > 0 && age.getTo() >= age.getFrom()) {
			docs = docs.filter( Filters.and( Filters.gte( "medicalcare.age", 
											age.getFrom()),
									Filters.lte( "medicalcare.age", 
											age.getTo())));
		}
		
		
//		List<Document> docList = new ArrayList<>();
//		
//		for (Document document : docs) {
//			
//			if(document.getDate("creation").after(date.getFrom()) &&
//					document.getDate("conclusion").before(date.getTo())) {
//				
//				docList.add(document);
//			}	
//		}	
//		for (Document document : docList) {
//			System.err.println(docList.get(0).get("creation"));
//		} 
	
		return docs;
	}		
	
	public EQuery setAtribbutte( int idPathway, 
								String[] conductArr,
								String[] statusArr,
								String[] ages, 
								String sexStr, 
								String[] dates, 
								String[] ranges){

		EQuery query = Query_metamodelFactory.eINSTANCE.createEQuery();
		
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
		}
		
		if ( idPathway > 0) {
			eCarePathway.setName(CarePathway.get( idPathway));
		}		
		
		if (statusArr != null) {
			if (statusArr[0] != null && statusArr[1] != null) {
				status.setMessage( Message.get( Integer.parseInt(statusArr[0])));
				status.setValue(Boolean.valueOf(statusArr[1]));	
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
		
		attribute.setRange( range);
		attribute.setSex( sex);
		attribute.setStatus( status);
		attribute.setAge( age);
		attribute.setDate( date);
		attribute.setCarePathway( eCarePathway);
		
		query.setEAttribute(attribute);
		
		return query;
	}
	
	public List<Entry<String, Double>> select(int quantity, List<Entry<String, Double>> list) {
		if( list.size() < quantity || quantity == 0) {
			return list;
		}		
		
		return list.subList( 0, quantity);
	}
	
	public void sort( List<Entry<String, Double>> list, Order order) {
		if (order.equals(Order.TOP)) {
			descending(list);
		}
		if (order.equals(Order.BOTTOM)) {
			ascending(list);
		}
	}
	
	public double rate( double dividend, double divider) {
		return ( dividend/ divider) * 100;
	}
	
	public int count( String field, String name, FindIterable<Document> iterable) {
		int cont = 0;
						
		for (Document document : iterable) {
			if (name == CarePathway.NONE.getLiteral()) {
				cont ++;
			}
			else if (document.get(field).equals(name)) {
				cont ++; 
			}				
		}		
		
		return cont;
	}
	
	public void descending(final List<Entry<String, Double>> list) {
		//sorting the list with a comparator
		Collections.sort( list, new Comparator<Entry<String, Double>>() {
			public int compare( final Map.Entry<String, Double> o1, final Map.Entry<String, Double> o2) {
				return ( o2.getValue()).compareTo( o1.getValue());
			}
		});
	}
	
	public void ascending(final List<Entry<String, Double>> list) {
		//sorting the list with a comparator
		Collections.sort( list, new Comparator<Entry<String, Double>>() {
			public int compare( final Map.Entry<String, Double> o1, final Map.Entry<String, Double> o2) {
				return ( o1.getValue()).compareTo( o2.getValue());
			}
		});
	}
	
	public String[] splitBy( String str, String symbol) {		
		if (!str.isEmpty()) {
			return str.split(symbol);
		}
		
		return null;
	}
	
 	public String decimalFormat( double number) {
		return new DecimalFormat("####0.00").format( number);
	}
}

