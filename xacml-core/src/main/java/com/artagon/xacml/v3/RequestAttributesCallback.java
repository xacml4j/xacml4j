package com.artagon.xacml.v3;

public interface RequestAttributesCallback 
{
	/**
	 * Gets {@link BagOfAttributeValues} from request context
	 * 
	 * @param <AV>
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @return {@link BagOfAttributeValues} or empty bag
	 * if no matching attribute exist in the request context
	 */
	<AV extends AttributeValue> BagOfAttributeValues<AV> getAttributeValues(
			AttributeCategoryId category, 
			String attributeId, 
			AttributeValueType dataType,
			String issuer);
	
	/**
	 * Method assumes that issuer is not specified for an
	 * attribute in the request context
	 * 
	 * @see {{@link #getAttribute(AttributeCategoryId, String, String)}
	 */
	<AV extends AttributeValue> BagOfAttributeValues<AV> getAttributeValues(
			AttributeCategoryId category, 
			String attributeId, 
			AttributeValueType dataType);
	
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategoryId categoryId, 
			String attributeId, AttributeValueType dataType);
	
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategoryId categoryId, 
			String attributeId, AttributeValueType dataType, String issuer);
}
