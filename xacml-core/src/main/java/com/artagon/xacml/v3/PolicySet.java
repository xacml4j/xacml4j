package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.List;


public interface PolicySet extends CompositeDecisionRule, Versionable
{
	/**
	 * Gets decision policy description
	 * 
	 * @return description
	 */
	String getDescription();
	
	/**
	 * Gets policy set defaults
	 * 
	 * @return {@link PolicySetDefaults}
	 */
	PolicySetDefaults getDefaults();
	
	/**
	 * Gets rule target
	 * 
	 * @return {@link Target} or
	 * <code>null</code> if rule 
	 * matches any request
	 */
	Target getTarget();
	
	CombinerParameters getCombinerParameters();
	PolicyCombinerParameters getPolicyCombinerParameters();
	PolicySetCombinerParameters getPolicySetCombinerParameters();
	
	List<? extends CompositeDecisionRule> getDecisions();
	
	/**
	 * Gets decision obligations
	 * 
	 * @return collection of {@link ObligationExpression}
	 * instances
	 */
	Collection<ObligationExpression> getObligationExpressions();
	
	/**
	 * Gets decision advice expressions
	 * 
	 * @return collection of {@link AdviceExpression}
	 * instances
	 */
	Collection<AdviceExpression> getAdviceExpressions();
}
