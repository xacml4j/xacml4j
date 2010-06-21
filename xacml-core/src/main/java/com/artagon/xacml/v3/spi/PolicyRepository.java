package com.artagon.xacml.v3.spi;

import java.util.Collection;
import java.util.Map;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.EvaluationContext;

public interface PolicyRepository extends PolicyReferenceResolver
{
	/**
	 * Gets root policies
	 * 
	 * @return a collection of {@link CompositeDecisionRule} instances
	 */
	Collection<CompositeDecisionRule> getPolicies();
	
	/**
	 * Finds applicable decision rules
	 * 
	 * @param context an evaluation context
	 * @return a collection of {@link CompositeDecisionRule} instances
	 */
	Collection<CompositeDecisionRule> findApplicable(EvaluationContext context);
}
