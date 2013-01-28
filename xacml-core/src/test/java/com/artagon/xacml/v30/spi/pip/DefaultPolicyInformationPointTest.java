package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;

@Ignore
public class DefaultPolicyInformationPointTest 
{
	private PolicyInformationPoint pip;
	
	private ResolverRegistry registry;
	
	private AttributeResolver resolver1;
	private AttributeResolver resolver2;
	
	private PolicyInformationPointCacheProvider cache;
	private EvaluationContext context;
	
	private AttributeResolverDescriptor descriptor1;
	private AttributeResolverDescriptor descriptor1WithIssuer;
	private AttributeResolverDescriptor descriptor1WithNoCache;
	
	private IMocksControl control;
	
	private AttributeDesignatorKey.Builder attr0;
	private AttributeDesignatorKey.Builder attr1;
	private AttributeDesignatorKey.Builder key;
	
	@Before
	public void init()
	{
		this.control = createStrictControl();
		this.cache = control.createMock(PolicyInformationPointCacheProvider.class);
		this.registry = control.createMock(ResolverRegistry.class);
		this.resolver1 = control.createMock(AttributeResolver.class);
		this.resolver2 = control.createMock(AttributeResolver.class);
		
		this.context = control.createMock(EvaluationContext.class);
		
		this.pip = PolicyInformationPointBuilder
		.builder("testPip")
		.withCacheProvider(cache)
		.build(registry);
		
		this.attr0 = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testAttributeId1")
				.dataType(StringType.STRING);
		
		this.attr1 = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testAttributeId1")
				.dataType(IntegerType.INTEGER);
		
		this.key = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("username")
				.dataType(StringType.STRING);
		
		this.descriptor1 = AttributeResolverDescriptorBuilder
				.builder("testId1", "Test Resolver", 
						AttributeCategories.SUBJECT_ACCESS)
				.cache(30)
				.attribute("testAttributeId1", StringType.STRING)
				.attribute("testAttributeId2", IntegerType.INTEGER)
				.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
				.build();

		this.descriptor1WithIssuer = AttributeResolverDescriptorBuilder
				.builder("testId2", "Test Resolver", "Issuer", 
						AttributeCategories.SUBJECT_ACCESS)
				.cache(40)
				.attribute("testAttributeId1", StringType.STRING)
				.attribute("testAttributeId2", IntegerType.INTEGER)
				.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
				.build();

		
		this.descriptor1WithNoCache = AttributeResolverDescriptorBuilder
		.builder("testId3", "Test Resolver", "Issuer",
				AttributeCategories.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId1", StringType.STRING)
		.attribute("testAttributeId2", IntegerType.INTEGER)
		.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsCachable() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey a1 = attr1.build();
		AttributeDesignatorKey k = key.build();
		
		AttributeSet result = AttributeSet
				.builder(descriptor1)
				.attribute("testAttributeId1", StringType.STRING.bagOf("v1"))
				.build();
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0))
		.andReturn(ImmutableList.of(resolver1, resolver2));
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		
		expect(context.resolve(eq(k))).andReturn(StringType.STRING.bagOf("testUser"));
		
		Capture<ResolverContext> resolverContext1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx))).andReturn(result);
		
		context.setResolvedDesignatorValue(eq(a0), eq(StringType.STRING.bagOf("v1")));
		context.setResolvedDesignatorValue(eq(a1), eq(IntegerType.INTEGER.emptyBag()));
		context.setResolvedDesignatorValue(eq(a0), eq(StringType.STRING.bagOf("v1")));

		
		Capture<ResolverContext> resolverContext2 = new Capture<ResolverContext>();
		
		cache.putAttributes(capture(resolverContext2), eq(result));
		
		context.setDecisionCacheTTL(descriptor1.getPreferreredCacheTTL());
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
	//	assertSame(resolverContext1.getValue(), resolverContext2.getValue());

		control.verify();
	}
	
	
	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet() throws Exception
	{
		
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, attr0.build())).andReturn(
				ImmutableList.of(resolver1, resolver2));
		
		
		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();
		
		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();
		
		
		AttributeSet result2 = AttributeSet
				.builder(descriptor1WithIssuer)
				.attribute("testAttributeId1", StringType.STRING.bagOf("v1"))
				.build();
		
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(key.build()))
		.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx1))).andReturn(result2);
		
		context.setResolvedDesignatorValue(attr0.issuer(descriptor1WithIssuer.getIssuer()).build(),
						StringType.STRING.bagOf(StringType.STRING.create("v1")));
			
		

		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, attr0.build());
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);

		control.verify();
	}
	
	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverThrowsException() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey k = key.build();
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0)).andReturn(
				ImmutableList.of(resolver1, resolver2));
		
		
		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();
		
		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();
		
		AttributeSet result2 = AttributeSet
				.builder(descriptor1WithIssuer)
				.attribute("testAttributeId1", StringType.STRING.bagOf("v1"))
				.build();
		
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(k))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);		
		expect(resolver1.resolve(capture(ctx1))).andThrow(new NullPointerException());
		
			
		
		expect(resolver2.getDescriptor()).andReturn(descriptor1WithIssuer);
		expect(context.resolve(k))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx2))).andReturn(null);
		
		
		expect(resolver2.resolve(capture(ctx2))).andReturn(result2);
		
		context.setResolvedDesignatorValue(
				attr0.issuer(descriptor1WithIssuer.getIssuer()).build(), 
				StringType.STRING.bagOf("v1"));
		
		context.setResolvedDesignatorValue(
				attr1.issuer(descriptor1WithIssuer.getIssuer()).build(), 
				IntegerType.INTEGER.emptyBag());


		Capture<ResolverContext> ctx3 = new Capture<ResolverContext>();
		cache.putAttributes(capture(ctx3), eq(result2));
		
		context.setDecisionCacheTTL(descriptor1WithIssuer.getPreferreredCacheTTL());
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		assertSame(ctx2.getValue(), ctx3.getValue());

		control.verify();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable() 
		throws Exception
	{	
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey k = key.build();
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, k)).andReturn(ImmutableList.of(resolver1));
		expect(resolver1.getDescriptor()).andReturn(descriptor1WithNoCache);
				
		// key resolved
		expect(context.resolve(k))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = AttributeSet
				.builder(descriptor1WithNoCache)
				.attribute("testAttributeId1", StringType.STRING.bagOf("v1"))
				.build();
		
		expect(resolver1.resolve(capture(ctx))).andReturn(result);
		
		context.setResolvedDesignatorValue(
						attr0.issuer(descriptor1WithNoCache.getIssuer()).build(), 
						StringType.STRING.bagOf("v1"));
		
		context.setResolvedDesignatorValue(
						attr1.issuer(descriptor1WithNoCache.getIssuer()).build(), 
						IntegerType.INTEGER.emptyBag());
			
		context.setDecisionCacheTTL(descriptor1WithNoCache.getPreferreredCacheTTL());
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);

		control.verify();
	}
}