package com.artagon.xacml.v3.spi.pip;

import org.junit.Ignore;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeIssuer;
import com.artagon.xacml.v3.sdk.XacmlAttributeKey;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringValue;

@XacmlAttributeResolverDescriptor(name="Test Resolver")
@XacmlAttributeIssuer("testIssuer")
@Ignore
public class TestAnnotatedResolver 
{
	@XacmlAttributeDescriptor(id="testId1", typeId="http://www.w3.org/2001/XMLSchema#string")
	@XacmlAttributeCategory({"urn:oasis:names:tc:xacml:3.0:attribute-category:resource", 
		"urn:oasis:names:tc:xacml:3.0:attribute-category:action"})
	public BagOfAttributeValues getTestId1Attribute(
			PolicyInformationPointContext context)
	{
		return StringType.STRING.emptyBag();
	}
	
	@XacmlAttributeDescriptor(id="testId2", typeId="http://www.w3.org/2001/XMLSchema#integer")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject")
	public BagOfAttributeValues getTestId2Attribute(
			PolicyInformationPointContext context)
	{
		return IntegerType.INTEGER.bagOf(IntegerType.INTEGER.create(1));
	}
	
	
	@XacmlAttributeDescriptor(id="testId3", typeId="http://www.w3.org/2001/XMLSchema#integer")
	@XacmlAttributeCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject")
	public BagOfAttributeValues getTestId3Attribute(
			PolicyInformationPointContext context, 
			@XacmlAttributeKey(id="username", 
					type="http://www.w3.org/2001/XMLSchema#integer")StringValue username)
	{
		return IntegerType.INTEGER.bagOf(IntegerType.INTEGER.create(1));
	}
}
