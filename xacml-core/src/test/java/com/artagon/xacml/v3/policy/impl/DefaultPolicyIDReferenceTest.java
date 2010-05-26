package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.XacmlException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

public class DefaultPolicyIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private Policy refPolicy;
	private Request request;
	private EvaluationContextFactory contextFactory;
	private PolicyReferenceResolver policyResolver;
	
	@Before
	public void init(){
		this.policyResolver = createStrictMock(PolicyReferenceResolver.class);
		this.policySet = createStrictMock(PolicySet.class);
		this.refPolicy = createStrictMock(Policy.class);
		this.request = createStrictMock(Request.class);
		this.contextFactory = new DefaultEvaluationContextFactory( 
				policyResolver,
				createStrictMock(XPathProvider.class));
		this.context = contextFactory.createContext(policySet, request);
	}
	
	@Test
	public void testNoReferencedPolicyFound() throws XacmlException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(policyResolver);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertNull(policyRefContext.getCurrentPolicy());
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(policyResolver);
		reset(policyResolver);
		
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		ref.evaluate(context);
	}
	
	@Test
	public void testEvaluatePolicyIDReference() throws XacmlException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicy);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicyDelegatingEvaluationContext(ctx, refPolicy);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		replay(policyResolver, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(policyResolver, refPolicy);
	}
	
	@Test
	public void testEvaluateIfApplicablePolicyIDReference() throws XacmlException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicy);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicyDelegatingEvaluationContext(ctx, refPolicy);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.evaluateIfApplicable(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		replay(policyResolver, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluateIfApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(policyResolver, refPolicy);
	}
	
	@Test
	public void testIsApplicableIDReference() throws XacmlException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicy);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicyDelegatingEvaluationContext(ctx, refPolicy);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.isApplicable(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);
		replay(policyResolver, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(policyResolver, refPolicy);
	}

	private void expectPolicyMatch(Policy p, String id, String v) throws PolicySyntaxException
	{
		expect(p.getId()).andReturn(id);
		expect(p.getVersion()).andReturn(Version.parse(v));
	}
}
