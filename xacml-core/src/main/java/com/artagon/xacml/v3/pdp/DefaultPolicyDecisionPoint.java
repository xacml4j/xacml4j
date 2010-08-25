package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.profiles.RequestContextHandlerChain;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.google.common.base.Preconditions;

public final class DefaultPolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
	private EvaluationContextFactory factory;
	private PolicyDomain policyDomain;
	private RequestContextHandlerChain requestProcessingPipeline;
	
	public DefaultPolicyDecisionPoint(
			List<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyDomain policyRepository)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policyRepository);
		this.factory = factory;
		this.policyDomain = policyRepository;
		this.requestProcessingPipeline = new RequestContextHandlerChain(handlers);
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyDomain policyRepostory)
	{
		this(Collections.<RequestProfileHandler>emptyList(), factory, policyRepostory);
	}

	@Override
	public ResponseContext decide(RequestContext request)
	{
		Collection<Result> results = requestProcessingPipeline.handle(request, this);
		return new ResponseContext(results);			
	}
	
	@Override
	public Result requestDecision(RequestContext request) 
	{
		EvaluationContext context = factory.createContext(request);
		Collection<Attributes> includeInResult = request.getIncludeInResultAttributes();
		Decision decision = policyDomain.evaluate(context);
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					new Status(StatusCode.createOk()), includeInResult);
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status), includeInResult);
		}
		return new Result(
				decision, 
				new Status(StatusCode.createOk()),
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				context.getEvaluatedPolicies());
	}

	@Override
	public XPathProvider getXPathProvider() {
		return factory.getXPathProvider();
	}	
}
