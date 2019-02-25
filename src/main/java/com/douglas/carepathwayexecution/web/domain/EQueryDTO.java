package com.douglas.carepathwayexecution.web.domain;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import QueryMetamodel.EAttribute;
import QueryMetamodel.EMethod;

@Document
public class EQueryDTO {
	private List<EMethod> methods;
	private EAttribute attribute;
	
	public EQueryDTO() {}
	
	public EQueryDTO(List<EMethod> methods, EAttribute attribute) {
		super();
		this.attribute = attribute;
		this.methods = methods;
	}	

	public List<EMethod> getMethod() {
		return methods;
	}

	public void setMethod(List<EMethod> methods) {
		this.methods = methods;
	}
	
	public EAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(EAttribute attribute) {
		this.attribute = attribute;
	}
}
