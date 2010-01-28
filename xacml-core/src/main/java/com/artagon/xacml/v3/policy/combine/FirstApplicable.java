package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;

class FirstApplicable<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	private final static Logger log = LoggerFactory.getLogger(FirstApplicable.class);
	
	protected FirstApplicable(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public DecisionResult combine(List<D> decisions,
			EvaluationContext context) 
	{
		log.debug("Combining decisions via algorithm={}", getId());
		for(D d : decisions){
			DecisionResult decision = evaluateIfApplicable(context, d);
			if(decision == DecisionResult.DENY){
				return decision;
			}
			if(decision == DecisionResult.PERMIT){
				return decision;
			}
			if(decision == DecisionResult.NOT_APPLICABLE){
				continue;
			}
			if(decision.isIndeterminate()){
				return decision;
			}
		}
		return DecisionResult.NOT_APPLICABLE;
	}
}
