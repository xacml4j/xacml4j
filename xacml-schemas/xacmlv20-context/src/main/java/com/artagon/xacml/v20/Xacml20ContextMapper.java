package com.artagon.xacml.v20;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.oasis.xacml.v20.context.ActionType;
import org.oasis.xacml.v20.context.AttributeType;
import org.oasis.xacml.v20.context.AttributeValueType;
import org.oasis.xacml.v20.context.DecisionType;
import org.oasis.xacml.v20.context.EnvironmentType;
import org.oasis.xacml.v20.context.ObjectFactory;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.context.ResourceContentType;
import org.oasis.xacml.v20.context.ResourceType;
import org.oasis.xacml.v20.context.ResponseType;
import org.oasis.xacml.v20.context.ResultType;
import org.oasis.xacml.v20.context.StatusCodeType;
import org.oasis.xacml.v20.context.StatusType;
import org.oasis.xacml.v20.context.SubjectType;
import org.oasis.xacml.v20.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.policy.ObligationType;
import org.oasis.xacml.v20.policy.ObligationsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

class Xacml20ContextMapper
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20ContextMapper.class);
	
	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:2.0:resource:resource-id";
	
	private final static Map<Decision, DecisionType> decisionMapping = new HashMap<Decision, DecisionType>();
	
	static{
		decisionMapping.put(Decision.DENY, DecisionType.DENY);
		decisionMapping.put(Decision.PERMIT, DecisionType.PERMIT);
		decisionMapping.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE);
		decisionMapping.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE);
		decisionMapping.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE);
		decisionMapping.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE);
		decisionMapping.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE);
	}
	
	private static JAXBContext context;
	
	static{
		try{
			context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		}catch(JAXBException e){
		}
	}
	
	public Xacml20ContextMapper(){
		Preconditions.checkState(context != null, 
				"Failed to initialize JAXB context");
	}
	
	static JAXBContext getJaxbContext(){
		return context;
	}
	
	public ResponseType create(Response response)
	{
		ResponseType responseV2 = new ResponseType();
		List<ResultType> results = responseV2.getResult();
		for(Result resultV3 : response.getResults()){
			results.add(create(resultV3));
		}
		return responseV2;
	}
		
	private ResultType create(Result result)
	{
		ResultType resultv2 = new ResultType();
		resultv2.setStatus(createStatus(result.getStatus()));
		resultv2.setResourceId(getResourceId(result));
		resultv2.setObligations(getObligations(result));
		resultv2.setDecision(decisionMapping.get(result.getDecision()));
		return resultv2;
	}
	
	private StatusType createStatus(Status status)
	{
		StatusType statusType = new StatusType();
		StatusCodeType codeType = new StatusCodeType();
		statusType.setStatusCode(codeType);
		codeType.setValue(status.getStatusCode().getValue().toString());
		statusType.setStatusMessage(status.getMessage());
		return statusType;
	}
	
	/**
	 * Tries to locate resource id attribute
	 * 
	 * @param result an evaluation result
	 * @return a resource id attribute
	 */
	private String getResourceId(Result result)
	{
		Attributes resource = result.getAttribute(AttributeCategoryId.RESOURCE);
		if(resource == null){
			return null;
		}
		Collection<Attribute> attrs = resource.getAttributes(RESOURCE_ID);
		if(attrs.size() == 1){
			Attribute resourceId = Iterables.getOnlyElement(attrs);
			return Iterables.getOnlyElement(resourceId.getValues()).toXacmlString();
		}
		Collection<AttributeValue> values =  resource.getAttributeValues(
				CONTENT_SELECTOR, XacmlDataTypes.XPATHEXPRESSION.getType());
		if(values.isEmpty() ||
				values.size() > 1){
			return null;
		}
		return Iterables.getOnlyElement(values).toXacmlString();
	}
	
	public ObligationsType getObligations(Result result)
	{
		Collection<Obligation> obligations = result.getObligations();
		if(obligations.isEmpty()){
			return null;
		}
		ObligationsType obligationsv2  = new ObligationsType();
		for(Obligation o : obligations){
			obligationsv2.getObligation().add(create(o));
		}
		return obligationsv2;
	}
	
	private ObligationType create(Obligation o){
		ObligationType obligation = new ObligationType();
		for(AttributeAssignment a : o.getAttributes()){
			obligation.getAttributeAssignment().add(create(a));
		}
		return obligation;
	}
	
	private AttributeAssignmentType create(AttributeAssignment a)
	{
		AttributeAssignmentType attr = new AttributeAssignmentType();
		com.artagon.xacml.v3.AttributeValueType t = (com.artagon.xacml.v3.AttributeValueType)(a.getAttribute().getType());
		attr.setDataType(t.getDataTypeId());
		attr.setAttributeId(a.getAttributeId());
		attr.getContent().add(a.getAttribute().toXacmlString());
		return attr;
	}
	
	public Request create(RequestType req) throws RequestSyntaxException
	{
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		if(!req.getResource().isEmpty()){
			
			for(ResourceType resource : req.getResource()){
				attributes.add(createResource(resource, req.getResource().size() > 1));
			}
		}
		if(!req.getSubject().isEmpty())
		{
			Multimap<AttributeCategoryId, Attributes> map = LinkedHashMultimap.create();
			for(SubjectType subject : req.getSubject()){
				Attributes attr =  createSubject(subject);
				map.put(attr.getCategoryId(), attr);
			}
			attributes.addAll(normalize(map));
		}
		if(req.getAction() != null)
		{
			attributes.add(createAction(req.getAction()));
		}
		if(req.getEnvironment() != null)
		{
			attributes.add(createEnviroment(req.getEnvironment()));
		}
		return new Request(false, attributes);
	}
	
	public Collection<Attributes> normalize(Multimap<AttributeCategoryId, Attributes> attributes)
	{
		Collection<Attributes> normalized = new LinkedList<Attributes>();
		for(AttributeCategoryId categoryId : attributes.keySet()){
			Collection<Attributes> byCategory = attributes.get(categoryId);
			Collection<Attribute> categoryAttr = new LinkedList<Attribute>();
			for(Attributes a : byCategory){
				categoryAttr.addAll(a.getAttributes());
			}
			normalized.add(new Attributes(categoryId, categoryAttr));
		}
		return normalized;
	}
	
	private Attributes createSubject(SubjectType subject) throws RequestSyntaxException
	{
		AttributeCategoryId category = getCategoryId(subject.getSubjectCategory());
		if(log.isDebugEnabled()){
			log.debug("Processing subject category=\"{}\"", category);
		}
		return new Attributes(category, create(subject.getAttribute(), category, false));
	}
	
	private AttributeCategoryId getCategoryId(String id) throws RequestSyntaxException
	{
		AttributeCategoryId category = AttributeCategoryId.parse(id);
		if(category == null){
			throw new RequestSyntaxException("Unknown attribute category=\"%s\"", id);
		}
		return category;
	}
	
	private Attributes createEnviroment(EnvironmentType subject) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.ENVIRONMENT, 
				null, create(subject.getAttribute(), AttributeCategoryId.ENVIRONMENT, false));
	}
	
	private Attributes createAction(ActionType subject) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.ACTION, 
				null, create(subject.getAttribute(), AttributeCategoryId.ACTION, false));
	}
	
	private Attributes createResource(ResourceType resource, 
			boolean multipleResources) throws RequestSyntaxException
	{
		Node content = getResourceContent(resource);
		if(content != null){
			content = DOMUtil.copyNode(content);
		}
		return new Attributes(AttributeCategoryId.RESOURCE, 
				content, 
				create(resource.getAttribute(), AttributeCategoryId.RESOURCE, false));
	}
	
	private Node getResourceContent(ResourceType resource)
	{
		ResourceContentType content = resource.getResourceContent();
		if(content == null){
			return null;
		}
		for(Object o : content.getContent())
		{
			if(o instanceof Element){
				Node node = (Node)o;
				return node;
			}
		}
		return null;
	}
	
	private Collection<Attribute> create(Collection<AttributeType> contextAttributes, 
			AttributeCategoryId category, boolean incudeInResultResourceId) 
		throws RequestSyntaxException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : contextAttributes){
			attributes.add(createAttribute(a, category, incudeInResultResourceId));
		}
		return attributes;
	}
	
	private Attribute createAttribute(AttributeType a, AttributeCategoryId category, 
				boolean incudeInResultResourceId) 
		throws RequestSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			AttributeValue value = createValue(a.getDataType(), v, category);
			if(log.isDebugEnabled()){
				log.debug("Found attribute value=\"{}\" in request", value);
			}
			values.add(value);
		}
		return new Attribute(a.getAttributeId(), a.getIssuer(), 
				a.getAttributeId().equals(RESOURCE_ID)?incudeInResultResourceId:false, values);
	}
	
	private AttributeValue createValue(String dataTypeId, 
			AttributeValueType value, 
			AttributeCategoryId categoryId) 
		throws RequestSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new RequestSyntaxException("Attribute does not have content");
		}
		com.artagon.xacml.v3.AttributeValueType dataType = XacmlDataTypes.getByTypeId(dataTypeId);
		if(dataType == null){
			throw new RequestSyntaxException(
					"DataTypeId=\"%s\" can be be " +
					"resolved to valid XACML type", dataTypeId);
		}
		Object o = Iterables.getOnlyElement(content);
		if(dataType.equals(XacmlDataTypes.XPATHEXPRESSION.getType())){
			String xpath = Xacml20XPathTo30Transformer.transform20PathTo30((String)o);
			return dataType.create(xpath, categoryId);
		}
		return dataType.create(o);
	}
}
