package com.douglas.carepathwayexecution.web.service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import QueryMetamodel.AAge;
import QueryMetamodel.ACarePathway;
import QueryMetamodel.ADate;
import QueryMetamodel.ARange;
import QueryMetamodel.ASex;
import QueryMetamodel.AStatus;
import QueryMetamodel.CarePathway;
import QueryMetamodel.Conduct;
import QueryMetamodel.EAttribute;
import QueryMetamodel.EQuery;
import QueryMetamodel.Gender;
import QueryMetamodel.Message;
import QueryMetamodel.Order;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QCarePathwayService {
	private DBConfig dbConfig = new DBConfig();	
	
	public List<Document> filterDocuments(EQuery eQuery) {
		List<Document> docList = new ArrayList<>();
		
		for (int i = 0; i < 100; i++) {
			docList.addAll(filterDocuments(eQuery, i));
		}
		
		return docList;
	}
		
	public List<Document> filterDocuments(EQuery eQuery, int page) {
		ACarePathway carePathway = eQuery.getEAttribute().getCarePathway();
		AAge age = eQuery.getEAttribute().getAge();
		ADate date = eQuery.getEAttribute().getDate();
		ASex sex = eQuery.getEAttribute().getSex();
		AStatus status = eQuery.getEAttribute().getStatus();
		
		FindIterable<Document> docs = dbConfig.getCollection()
												.find()
												.skip(page*1000)
												.limit(1000)
												.filter(
														Filters.and(
																Filters.eq( "name", carePathway.getName().getLiteral()),
																Filters.eq( "pathway.version", carePathway.getVersion())));
		List<Document> docList = new ArrayList<>();
		
		if (docs.first() != null) {
			if (carePathway.getConduct() == Conduct.TRUE) {
				docs = docs.filter( Filters.or( Filters.exists( "complementaryConducts.prescribedresource"), 
												Filters.exists( "complementaryConducts.procedureprescribedresource"), 
												Filters.exists( "complementaryConducts.examinationprescribedresource")));
			}
			else if (carePathway.getConduct() == Conduct.FALSE) {
				docs = docs.filter( Filters.nor( Filters.exists( "complementaryConducts.prescribedresource"), 
												Filters.exists( "complementaryConducts.procedureprescribedresource"), 
												Filters.exists( "complementaryConducts.examinationprescribedresource")));
			}					
					
			if (sex.getSex() != Gender.ALL) {
				docs = docs.filter( Filters.eq( "medicalcare.sex", sex.getSex()));
			}
			if (status.getMessage() != Message.ALL) {
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
					
			if (date.getFrom() != null && date.getTo() != null) {
				for (Document document : docs) {
					if(document.getDate("creation").after(date.getFrom())) {
						if (document.getDate("conclusion") != null) {
							if (document.getDate("conclusion").before(date.getTo())) {
								docList.add(document);
							}						
						}
					}
				}								
			}	
			else if (date.getFrom() != null && date.getTo() == null) {
				for (Document document : docs) {
					if(document.getDate("creation").after(date.getFrom())) {
						docList.add(document);
					}
				}				
			}
			else if (date.getFrom() == null && date.getTo() != null) {
				for (Document document : docs) {
					if (document.getDate("conclusion") != null) {
						if (document.getDate("conclusion").before(date.getTo())) {
							docList.add(document);
						}						
					}
				}								
			}
			else {	
				for (Document document : docs) {					
					docList.add(document);
				}
			}
		}
		docs = null;
		return docList;
	}
	
	public EQuery setAtribbutte( int idPathway, 
								String conduct,
								String[] statusArr,
								String[] ages, 
								String sexStr, 
								String[] dates, 
								String[] ranges){

		EQuery query = Query_metamodelFactory.eINSTANCE.createEQuery();
		
		EAttribute attribute = Query_metamodelFactory.eINSTANCE.createEAttribute();	
		ASex sex = Query_metamodelFactory.eINSTANCE.createASex();
		AAge age = Query_metamodelFactory.eINSTANCE.createAAge();
		ARange range = Query_metamodelFactory.eINSTANCE.createARange();
		ACarePathway eCarePathway = Query_metamodelFactory.eINSTANCE.createACarePathway();
		ADate date = Query_metamodelFactory.eINSTANCE.createADate();
		AStatus status = Query_metamodelFactory.eINSTANCE.createAStatus();
		
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
		
		if (conduct != null && !conduct.isEmpty()) {
			if (Integer.parseInt(conduct) == 0) {
				eCarePathway.setConduct(Conduct.FALSE);
			}
			if (Integer.parseInt(conduct) == 1) {
				eCarePathway.setConduct(Conduct.TRUE);
			}
		}
		
		if (statusArr != null) {
			if (statusArr[0] != null && statusArr[1] != null) {
				status.setMessage( Message.get( Integer.parseInt(statusArr[0])));
				
				if (Integer.parseInt(statusArr[1]) == 1 && !statusArr[1].isEmpty()) {
					status.setValue(true);
				}
			}
		}
		
		if (dates != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());	//yyyy-MM-dd'T'HH:mm:ss.SSSSS
			
			if (dates[0] != null) {
				try {
					date.setFrom(dateFormat.parse(dates[0]));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else {
				date.setFrom(null);
			}
		
			if (dates[1] != null) {
				try {
					date.setTo(dateFormat.parse(dates[1]));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else {
				date.setTo(null);
			}
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
	
	public double rate( double dividend, double divider) {
		return ( dividend/ divider) * 100;
	}
	
	public int count( String field, String name, List<Document> iterable) {
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

