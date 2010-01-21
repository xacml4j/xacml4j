package com.artagon.xacml.policy.function;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.type.AnyURIType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.StringType;

public class ToFromStringFunctionFactoryTest extends DefaultFunctionFactoryTestCase
{
	private FunctionFactory factory;
	private DatatypeFactory dtf;
	
	@Before
	public void init() throws Exception{
		this.factory = new ToFromStringFunctionFactory();
		this.dtf = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testAnyURIType() throws PolicyEvaluationException
	{
		AnyURIType t1 = DataTypes.ANYURI.getType();
		StringType t2 = DataTypes.STRING.getType();
		assertEquals(t2.create("http://www.test.org"), factory.getFunction(Functions.STRING_FROM_ANYURI).invoke(context, t1.create(URI.create("http://www.test.org"))));
		assertEquals(t1.create("http://www.test.org"), factory.getFunction(Functions.ANYURI_FROM_STRING).invoke(context, t2.create("http://www.test.org")));
	}
}
