package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.azapi.constants.AzCategoryIdSubjectIntermediary;
import org.oasis.xacml.azapi.constants.AzCategoryIdSubjectRecipient;

import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.StringType;

public class DefaultRuleTest extends XacmlPolicyTestCase
{
	private Condition conditionFalse;
	private Condition conditionTrue;
	private Condition conditionIndeterminate;
	
	private Collection<ObligationExpression> obligationExpressions;
	private Collection<AdviceExpression> adviceExpressions;
	
	private IntegerType type1;
	private StringType type2;
	private BooleanType type3;
	@Before
	public void init()
	{
		this.type1 = DataTypes.INTEGER.getType();
		this.type2 = DataTypes.STRING.getType();
		this.type3 = DataTypes.BOOLEAN.getType();
		
		ExplicitFunctionSpecBuilder b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_EQUAL);
		b.withParam(type1).withParam(type1);
		
		FunctionSpec functionTrue = b.build(new MockFunctionImplementation(type3.create(Boolean.TRUE)));
		
		List<Expression> paramsTrue = new LinkedList<Expression>();
		paramsTrue.add(type1.create(10L));
		paramsTrue.add(type1.create(10L));
		Apply applyTrue = functionTrue.createApply(paramsTrue);
		this.conditionTrue = new Condition(applyTrue);
		
		FunctionSpec functionFalse = b.build(new MockFunctionImplementation(type3.create(Boolean.FALSE)));
		List<Expression> paramsFalse = new LinkedList<Expression>();
		paramsFalse.add(type1.create(10L));
		paramsFalse.add(type1.create(10L));
		Apply applyFalse = functionFalse.createApply(paramsFalse);
		this.conditionFalse = new Condition(applyFalse);
		
		MockFunctionImplementation impl = new MockFunctionImplementation(type3.create(Boolean.FALSE));
		impl.setFailWithIndeterminate(true);
		FunctionSpec functionIndeterminate = b.build(impl);
		
		List<Expression> paramsIndeterminate = new LinkedList<Expression>();
		paramsIndeterminate.add(type1.create(10L));
		paramsIndeterminate.add(type1.create(10L));
		Apply applyInderminate = functionIndeterminate.createApply(paramsIndeterminate);
		this.conditionIndeterminate = new Condition(applyInderminate);
		
		assertEquals(ConditionResult.INDETERMINATE, conditionIndeterminate.evaluate(context));
		assertEquals(ConditionResult.FALSE, conditionFalse.evaluate(context));
		assertEquals(ConditionResult.TRUE, conditionTrue.evaluate(context));
		
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		AttributeAssignmentExpression attrExpPermit = new AttributeAssignmentExpression(
				"testAttrId", type2.create("PermitValue"), 
				AzCategoryIdSubjectRecipient.AZ_CATEGORY_ID_SUBJECT_RECIPIENT, 
				null);
		AttributeAssignmentExpression attrExpDeny = new AttributeAssignmentExpression(
				"testAttrId", type2.create("DenyValue"),
				AzCategoryIdSubjectIntermediary.AZ_CATEGORY_ID_SUBJECT_INTERMEDIARY, 
				null);
		
		adviceExpressions.add(new AdviceExpression("testAdvicePermit", Effect.PERMIT, Collections.singletonList(attrExpPermit)));
		adviceExpressions.add(new AdviceExpression("testAdviceDeny", Effect.DENY, Collections.singletonList(attrExpDeny)));
		obligationExpressions.add(new ObligationExpression("testObligationPermit", Effect.PERMIT, Collections.singletonList(attrExpPermit)));
		obligationExpressions.add(new ObligationExpression("testObligationDeny", Effect.DENY, Collections.singletonList(attrExpDeny)));
	
	}
	
	@Test
	public void testRuleWithNoTargetNoConditionEffectPermit()
	{
		Rule r = new DefaultRule("test", null, null, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetNoConditionEffectDeny()
	{
		DefaultRule r = new DefaultRule("test", null, null, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectPermit()
	{
		Rule r = new DefaultRule("test", null, conditionTrue, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectDeny()
	{
		Rule r = new DefaultRule("test", null, conditionTrue, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", null, conditionFalse, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectDeny()
	{
		Rule r = new DefaultRule("test", null, conditionFalse, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", null, conditionIndeterminate, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectDeny()
	{
		Rule r = new DefaultRule("test", null, conditionIndeterminate, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionTrue, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionTrue, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEfectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionTrue, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEffectDeny()
	{
		
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionTrue, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionIndeterminate, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.NOMATCH), conditionIndeterminate, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.NOMATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetMatchConditionTrueEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.MATCH), conditionTrue, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetMatchConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.MATCH), conditionFalse, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetMatchConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.MATCH), conditionIndeterminate, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetMatchConditionTrueEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.MATCH), conditionTrue, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionTrueEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionIndeterminate, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionTrueEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionIndeterminate, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionFalse, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionFalse, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionIndeterminate, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), conditionIndeterminate, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(DecisionResult.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
}
