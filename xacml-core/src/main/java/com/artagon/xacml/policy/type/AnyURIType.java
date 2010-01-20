package com.artagon.xacml.policy.type;

import java.net.URI;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface AnyURIType extends AttributeDataType
{	
	AnyURIValue create(Object value);
	AnyURIValue fromXacmlString(String v);
	
	final class AnyURIValue extends BaseAttributeValue<URI> 
	{
		public AnyURIValue(AnyURIType type, URI value) {
			super(type, value);
		}
	}
}