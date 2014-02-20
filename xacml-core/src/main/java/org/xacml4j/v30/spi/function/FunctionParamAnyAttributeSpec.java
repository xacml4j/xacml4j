package org.xacml4j.v30.spi.function;

import java.util.ListIterator;

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;

import com.google.common.base.Objects;


final class FunctionParamAnyAttributeSpec extends BaseFunctionParamSpec
{
	public FunctionParamAnyAttributeSpec() {
		super(false, false, null);
	}
	@Override
	public boolean isValidParamType(ValueType type) {
		return (type instanceof AttributeExpType);
	}
	
	@Override
	public boolean validate(ListIterator<Expression> it) {
		if(!it.hasNext()){
			return false;
		}
		Expression exp = it.next();
		return isValidParamType(exp.getEvaluatesTo());
	}

	public String toString(){
		return Objects.
				toStringHelper(this)
				.add("optional", isOptional())
				.add("defaultValue", getDefaultValue())
				.add("variadic", isVariadic())
				.toString();
	}

	@Override
	public int hashCode(){
		return 0;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		return (o instanceof FunctionParamAnyAttributeSpec);
	}

}
