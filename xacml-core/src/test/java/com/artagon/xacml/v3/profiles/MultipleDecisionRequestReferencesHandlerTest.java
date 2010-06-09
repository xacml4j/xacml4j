package com.artagon.xacml.v3.profiles;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.policy.AttributesReference;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;

public class MultipleDecisionRequestReferencesHandlerTest 
{
	private PolicyDecisionCallback pdp;
	private RequestProfileHandler profile;
	
	@Before
	public void init()
	{
		this.pdp = createStrictMock(PolicyDecisionCallback.class);
		this.profile = new MultipleDecisionRequestReferencesHandler();
	}
	
	@Test
	public void testResolveRequestsWithValidReferences() throws RequestSyntaxException
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new Attribute("testId1", XacmlDataTypes.STRING.create("value0")));
		attributes0.add(new Attribute("testId2", XacmlDataTypes.STRING.create("value1")));
		Attributes attr0 = new Attributes("resourceAttr0",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new Attribute("testId3", XacmlDataTypes.STRING.create("value0")));
		attributes1.add(new Attribute("testId4", XacmlDataTypes.STRING.create("value1")));
		Attributes attr1 = new Attributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes1);
		
		Collection<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(new Attribute("testId3", XacmlDataTypes.STRING.create("value0")));
		attributes2.add(new Attribute("testId4", XacmlDataTypes.STRING.create("value1")));
		Attributes attr2 = new Attributes("actionAttr1",  AttributeCategoryId.ACTION, attributes1);
		
		Collection<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(new Attribute("testId5", XacmlDataTypes.STRING.create("value0")));
		attributes3.add(new Attribute("testId6", XacmlDataTypes.STRING.create("value1")));
		Attributes attr3 = new Attributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes2);
		
		Collection<Attribute> attributes4 = new LinkedList<Attribute>();
		attributes4.add(new Attribute("testId7", XacmlDataTypes.STRING.create("value0")));
		attributes4.add(new Attribute("testId8", XacmlDataTypes.STRING.create("value1")));
		Attributes attr4 = new Attributes("subjectAttr1",  AttributeCategoryId.SUBJECT_ACCESS, attributes3);
		
		
		Collection<AttributesReference> ref0 = new LinkedList<AttributesReference>();
		ref0.add(new AttributesReference("resourceAttr0"));
		ref0.add(new AttributesReference("subjectAttr0"));	
		RequestReference reference0 = new RequestReference(ref0);
		
		Collection<AttributesReference> ref1 = new LinkedList<AttributesReference>();
		ref1.add(new AttributesReference("resourceAttr1"));
		ref1.add(new AttributesReference("subjectAttr1"));	
		
		RequestReference reference1 = new RequestReference(ref1);
		
			
		Request context = new Request(false, 
				Arrays.asList(attr0, attr1, attr2, attr3, attr4), 
				Arrays.asList(reference0, reference1));
		
		Capture<Request> c0 = new Capture<Request>();
		Capture<Request> c1 = new Capture<Request>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		profile.handle(context, pdp).iterator();
		Request context0 = c0.getValue();
		Request context1 = c0.getValue();
		
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId5"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId6"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId1"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId2"));
		
		assertEquals(2, context0.getAttributes().size());
		assertEquals(1, context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context0.getAttributes(AttributeCategoryId.RESOURCE).size());
		
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId7"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId8"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId3"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId4"));
		assertEquals(2, context1.getAttributes().size());
		assertEquals(1, context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context1.getAttributes(AttributeCategoryId.RESOURCE).size());
		verify(pdp);
	}
	
	@Test
	public void testWithNoReferences()
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new Attribute("testId3", XacmlDataTypes.STRING.create("value0")));
		attributes0.add(new Attribute("testId4", XacmlDataTypes.STRING.create("value1")));
		Attributes attr0 = new Attributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new Attribute("testId5", XacmlDataTypes.STRING.create("value0")));
		attributes1.add(new Attribute("testId6", XacmlDataTypes.STRING.create("value1")));
		Attributes attr1 = new Attributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes1);
		
		Request request = new Request(false, 
				Arrays.asList(attr0, attr1));
		
		expect(pdp.requestDecision(request)).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(new Result(new Status(StatusCode.createProcessingError())), results.iterator().next());
		verify(pdp);
	}
	
	@Test
	public void testWithEmptyRequest()
	{
		Request context = new Request(false, 
				Collections.<Attributes>emptyList());
		
		Capture<Request> c0 = new Capture<Request>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}
