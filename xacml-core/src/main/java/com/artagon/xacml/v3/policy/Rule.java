package com.artagon.xacml.v3.policy;

public interface Rule extends DecisionRule
{
	/**
	 * Gets rule effect.
	 * 
	 * @return rule effect
	 */
	Effect getEffect();

	/**
	 * Gets rule condition.
	 * 
	 * @return boolean
	 */
	Condition getCondition();
}