package com.artagon.xacml.v20;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.ResponseType;

import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.BaseJAXBUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseUnmarshaller;

public class Xacml20ResponseUnmarshaller 
	extends BaseJAXBUnmarshaller<ResponseContext> 
implements ResponseUnmarshaller
{
	private Xacml20ContextMapper mapper;
	
	public Xacml20ResponseUnmarshaller(JAXBContext context){
		super(context);
		this.mapper = new Xacml20ContextMapper();
	}
	
	public Xacml20ResponseUnmarshaller(){
		this(JAXBContextUtil.getInstance());
	}

	@Override
	protected ResponseContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(
				(ResponseType)jaxbInstance.getValue());
	}
}
