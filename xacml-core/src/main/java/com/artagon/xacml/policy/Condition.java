package com.artagon.xacml.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.util.Preconditions;

/**
 * Condition represents a Boolean expression that refines the applicability 
 * of the rule beyond the predicates implied by its target. 
 * Therefore, it may be absent in the {@link Rule}
 * 
 * @author Giedrius Trumpickas
 */
public final class Condition implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(Condition.class);
	
	private Expression predicate;

	/**
	 * Constructs condition with an predicate
	 * expression
	 * 
	 * @param predicate an expression which always evaluates
	 * to {@link BooleanValue}
	 */
	public Condition(Expression predicate){
		Preconditions.checkArgument(predicate.equals(DataTypes.BOOLEAN.getType()));
		this.predicate = predicate;
	}
	
	/**
	 * Evaluates this condition and returns instance of
	 * {@link ConditionResult}
	 * 
	 * @param context an evaluation context
	 * @return {@link ConditionResult}
	 */
	public ConditionResult evaluate(EvaluationContext context) 
	{
		try
		{
			BooleanValue result = (BooleanValue)predicate.evaluate(context);
			if(log.isDebugEnabled()){
				log.debug("Condition predicate evaluation result=\"{}\"", result);
			}
			return result.getValue()?ConditionResult.TRUE:ConditionResult.FALSE;
		}catch(PolicyEvaluationException e){
			log.debug("Received evaluation exception=\"{}\", result is=\"{}\"", 
					e.getMessage(), ConditionResult.INDETERMINATE);
			return ConditionResult.INDETERMINATE;
		}
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		if(predicate != null){
			predicate.accept(v);
		}
		v.visitLeave(this);
	}
}
