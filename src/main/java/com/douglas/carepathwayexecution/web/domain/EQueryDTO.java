package com.douglas.carepathwayexecution.web.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import QueryMetamodel.EAttribute;
import QueryMetamodel.EMethod;

@Document
public class EQueryDTO {
	
	private EAttribute attribute;
	private EMethod method;
	
	public EQueryDTO() {}
	
	public EQueryDTO(EAttribute attribute, EMethod method) {
		super();
		this.attribute = attribute;
		this.method = method;
	}

	public EAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(EAttribute attribute) {
		this.attribute = attribute;
	}

	public EMethod getMethod() {
		return method;
	}

	public void setMethod(EMethod method) {
		this.method = method;
	}
	
	
	
	
/*	private CarePathway carePathway;
	private Status status;
	private Age age;
	private Sex sex;
	private Date date;
	private Range range;
	private int aborted;
	private int completed;
	private int inProgress;
	
	public EStatusDTO() {}	
	
	public EStatusDTO(CarePathway carePathway, Status status, Age age, Sex sex, Date date, Range range, int aborted,
			int completed, int inProgress) {
		super();
		this.carePathway = carePathway;
		this.status = status;
		this.age = age;
		this.sex = sex;
		this.date = date;
		this.range = range;
		this.aborted = aborted;
		this.completed = completed;
		this.inProgress = inProgress;
	}
	
	public CarePathway getCarePathway() {
		return carePathway;
	}
	public void setCarePathway(CarePathway carePathway) {
		this.carePathway = carePathway;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Age getAge() {
		return age;
	}
	public void setAge(Age age) {
		this.age = age;
	}
	public Sex getSex() {
		return sex;
	}
	public void setSex(Sex sex) {
		this.sex = sex;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Range getRange() {
		return range;
	}
	public void setRange(Range range) {
		this.range = range;
	}
	public int getAborted() {
		return aborted;
	}
	public void setAborted(int aborted) {
		this.aborted = aborted;
	}
	public int getCompleted() {
		return completed;
	}
	public void setCompleted(int completed) {
		this.completed = completed;
	}
	public int getInProgress() {
		return inProgress;
	}
	public void setInProgress(int inProgress) {
		this.inProgress = inProgress;
	}
	
*/	
}
