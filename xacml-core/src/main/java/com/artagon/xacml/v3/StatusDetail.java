package com.artagon.xacml.v3;

import java.util.LinkedList;
import java.util.List;

public class StatusDetail extends XacmlObject
{
	private List<Object> content;
	
	public StatusDetail(){
		this.content = new LinkedList<Object>();
	}
	
	public void addStatusDetail(Object o){
		content.add(o);
	}
}
