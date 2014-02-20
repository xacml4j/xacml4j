package org.xacml4j.v30.spi.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionInvocationException;
import org.xacml4j.v30.pdp.FunctionParamSpec;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class FunctionSpecBuilder
{
	private final String functionId;
	private final String legacyId;
	private final List<FunctionParamSpec> paramSpec;
	private boolean hadVarArg = false;
	private boolean lazyArgumentEvaluation;
	private Types types;
	
	private FunctionSpecBuilder(String functionId, Types types){
		this(functionId, null, types);
	}

	private FunctionSpecBuilder(String functionId, String legacyId, Types types){
		Preconditions.checkNotNull(functionId);
		this.functionId = functionId;
		this.legacyId = legacyId;
		this.paramSpec = new LinkedList<FunctionParamSpec>();
		this.types = types;
	}

	public static FunctionSpecBuilder  builder(String functionId, String legacyId, Types types){
		return new FunctionSpecBuilder(functionId, legacyId, types);
	}

	public static FunctionSpecBuilder  builder(String functionId, Types types){
		return builder(functionId, null, types);
	}

	public FunctionSpecBuilder funcRefParam()
	{
		this.paramSpec.add(new FunctionParamFuncReferenceSpec());
		return this;
	}

	public FunctionSpecBuilder param(ValueType type){
		return param(type, null, false);
	}
	
	public FunctionSpecBuilder param(ValueType type, ValueExpression defautlValue, boolean optional){
		Preconditions.checkNotNull(type);
		Preconditions.checkState(!hadVarArg,
				String.format("Can't add parameter after variadic parameter"));
		if(defautlValue != null){
			Preconditions.checkArgument(type.equals(defautlValue.getEvaluatesTo()));
		}
		if(defautlValue != null && optional){
			throw new XacmlSyntaxException(
					"Function=\"%s\" can not have default " +
					"value and be optional at the same time", 
					functionId);
		}
		if(paramSpec.size() == 0 && 
				defautlValue != null){
			throw new XacmlSyntaxException(
					"First parameter function=\"%s\" can not have default value", 
					functionId);
		}
		
		if(paramSpec.size() == 0 && 
				optional){
			throw new XacmlSyntaxException(
					"First parameter function=\"%s\" can not be optional", 
					functionId);
		}
		this.paramSpec.add(new FunctionParamValueTypeSpec(type, defautlValue, optional));
		return this;
	}

	public FunctionSpecBuilder lazyArgEval(){
		this.lazyArgumentEvaluation = true;
		return this;
	}

	public FunctionSpecBuilder param(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min > 1, "Max and min should be different at least by 1");
		hadVarArg = true;
		this.paramSpec.add(new FunctionParamValueTypeSequenceSpec(min, max, type));
		return this;
	}

	public FunctionSpecBuilder anyBag() {
		this.paramSpec.add(new FunctionParamAnyBagSpec());
		return this;
	}

	public FunctionSpecBuilder anyAttribute() {
		this.paramSpec.add(new FunctionParamAnyAttributeSpec());
		return this;
	}

	public FunctionSpec build(FunctionReturnTypeResolver returnType,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(functionId,
				legacyId, types, paramSpec, returnType, invocation, lazyArgumentEvaluation);
	}

	public FunctionSpec build(FunctionReturnTypeResolver returnType,
			FunctionParametersValidator validator,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(functionId,
				legacyId, 
				types, 
				paramSpec, 
				returnType,
				invocation,
				validator,
				lazyArgumentEvaluation);
	}

	public FunctionSpec build(ValueType returnType,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				invocation);
	}

	public FunctionSpec build(ValueType returnType,
			FunctionParametersValidator validator,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				validator,
				invocation);
	}

	/**
	 * A XACML function specification implementation
	 */
	static final class FunctionSpecImpl implements FunctionSpec
	{
		private final static Logger log = LoggerFactory.getLogger(FunctionSpecImpl.class);

		private String functionId;
		private String legacyId;
		private List<FunctionParamSpec> parameters = new LinkedList<FunctionParamSpec>();
		private boolean evaluateParameters = false;

		private FunctionInvocation invocation;
		private FunctionReturnTypeResolver resolver;
		private FunctionParametersValidator validator;
		private Types types;

		/**
		 * Constructs function spec with given function
		 * identifier and parameters
		 *
		 * @param functionId a function identifier
		 * @param legacyId a legacy identifier
		 * @param params a function parameters spec
		 * @param resolver a function return type resolver
		 * @param invocation a function implementation
		 * @param evaluateParameters a flag indicating
		 * if function parameters needs to be evaluated
		 * before passing them to the function
		 */
		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				Types types,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				FunctionParametersValidator validator,
				boolean evaluateParameters){
			Preconditions.checkNotNull(functionId);
			Preconditions.checkNotNull(params);
			Preconditions.checkNotNull(invocation);
			Preconditions.checkNotNull(resolver);
			Preconditions.checkNotNull(types);
			this.functionId = functionId;
			this.parameters.addAll(params);
			this.resolver = resolver;
			this.validator = validator;
			this.invocation = invocation;
			this.evaluateParameters = evaluateParameters;
			this.legacyId = legacyId;
			this.types = types;
		}

		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				Types types,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean evaluateParameters){
			this(functionId, legacyId, types, params, resolver, invocation, null, evaluateParameters);
		}

		public FunctionSpecImpl(
				String functionId,
				Types types,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean lazyParamEval){
			this(functionId, null, types, params, resolver, invocation, null, lazyParamEval);
		}

		@Override
		public  String getId(){
			return functionId;
		}
		
		@Override
		public String getLegacyId() {
			return legacyId;
		}
		
		public Types getTypes(){
			return types;
		}

		@Override
		public final FunctionParamSpec getParamSpecAt(int index){
			return parameters.get(index);
		}

		@Override
		public boolean isRequiresLazyParamEval() {
			return evaluateParameters;
		}

		@Override
		public boolean isVariadic(){
			return parameters.isEmpty()?false:parameters.get(parameters.size() - 1).isVariadic();
		}

		@Override
		public  int getNumberOfParams(){
			return parameters.size();
		}

		@Override
		public ValueType resolveReturnType(List<Expression> arguments) {
			return resolver.resolve(this, arguments);
		}

		@Override
		public <T extends ValueExpression> T invoke(EvaluationContext context,
				Expression ...arguments) throws EvaluationException {
			return this.<T>invoke(context, Arrays.asList(arguments));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ValueExpression> T invoke(EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {

			try
			{
				if(context.isValidateFuncParamsAtRuntime()){
					if(log.isDebugEnabled()){
						log.debug("Validating " +
								"function=\"{}\" parameters", functionId);
					}
					validateParameters(arguments);
				}
				T result = (T)invocation.invoke(this, context,
						isRequiresLazyParamEval()?arguments:evaluate(context, arguments));
				if(log.isDebugEnabled()){
					log.debug("Function=\"{}\" " +
							"invocation result=\"{}\"", getId(), result);
				}
				return result;
			}
			catch(EvaluationException e){
				throw e;
			}
			catch(Exception e){
				if(log.isDebugEnabled()){
					log.debug("Failed to invoke function", e);
				}
				throw new FunctionInvocationException(context, this, e,
						"Failed to invoke function=\"%s\"", getId());
			}
		}

		@Override
		public void validateParametersAndThrow(List<Expression> arguments) throws XacmlSyntaxException
		{
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			ListIterator<Expression> expIt = arguments.listIterator();
			while(it.hasNext())
			{
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					throw new XacmlSyntaxException(
							"Expression at index=\"%d\", " +
							"can't be used as function=\"%s\" parameter",
							expIt.nextIndex() - 1, functionId);
				}
				if(!it.hasNext() &&
						expIt.hasNext()){
					throw new XacmlSyntaxException(
							"Expression at index=\"%d\", " +
							"can't be used as function=\"%s\" parameter",
							expIt.nextIndex() - 1, functionId);
				}
			}
			if(!validateAdditional(arguments)){
				throw new XacmlSyntaxException("Failed addition validation");
			}
		}

		@Override
		public boolean validateParameters(List<Expression> arguments)
		{
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			ListIterator<Expression> expIt = arguments.listIterator();
			while(it.hasNext())
			{
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					return false;
				}
				if(!it.hasNext() &&
						expIt.hasNext()){
					return false;
				}
			}
			return validateAdditional(arguments);
		}

		/**
		 * Evaluates given array of function parameters
		 *
		 * @param context an evaluation context
		 * @param arguments function invocation arguments
		 * parameters
		 * @return an array of evaluated parameters
		 * @throws EvaluationException if an evaluation
		 * error occurs
		 */
		private List<Expression> evaluate(EvaluationContext context, List<Expression> arguments)
			throws EvaluationException
		{
			List<Expression> eval = new ArrayList<Expression>(arguments.size());
			for(Expression exp : arguments){
				eval.add((exp == null)?null:exp.evaluate(context));
			}
			return eval;
		}

		/**
		 * Additional function parameter validation function
		 *
		 * @param arguments an array of additional function arguments
		 * @return {@code true} if a given parameter is valid
		 * according specification
		 */
		private boolean validateAdditional(List<Expression> arguments){
			return (validator == null)?true:validator.validate(this, arguments);
		}

		@Override
		public String toString(){
			return Objects.toStringHelper(this)
					.add("functionId", functionId)
					.add("legacyId", legacyId)
					.add("evaluateParams", evaluateParameters)
					.add("params", parameters)
					.toString();
		}
	}

}
