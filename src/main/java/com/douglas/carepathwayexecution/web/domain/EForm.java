package com.douglas.carepathwayexecution.web.domain;

public class EForm {
	private String method;
	private String carePathway;
	private String conduct;
	private String step;
	private String age;
	private String sex;
	private String range;
	private String status;
	private String date;
	 
    public String getMethod() {
        return method;
    }
 
    public void setMethod(String method) {
        this.method = method;
    }

	public String getCarePathway() {
		return carePathway;
	}

	public void setCarePathway(String carePathway) {
		this.carePathway = carePathway;
	}

	public String getConduct() {
		return conduct;
	}

	public void setConduct(String conduct) {
		this.conduct = conduct;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
