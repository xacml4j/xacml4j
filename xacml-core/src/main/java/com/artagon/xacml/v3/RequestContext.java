package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.util.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class RequestContext extends XacmlObject
{	
	private boolean returnPolicyIdList;
	private Multimap<AttributeCategoryId, Attributes> attributes;
	private Map<String, Attributes> byId;
	private Collection<RequestReference> multipleRequests;
	
	/**
	 * Constructs a request with a given attributes
	 * @param attributes
	 */
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences)
	{
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = HashMultimap.create();
		this.multipleRequests = new ArrayList<RequestReference>(requestReferences);
		this.byId = new HashMap<String, Attributes>();
		for(Attributes attr : attributes){
			this.attributes.put(attr.getCategoryId(), attr);
			if(attr.getId() != null){
				this.byId.put(attr.getId(), attr);
			}
		}
	}
	
	/**
	 * Constructs a request with a given attributes
	 * @param attributes
	 */
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes)
	{
		this(returnPolicyIdList, attributes, 
				Collections.<RequestReference>emptyList());
	}
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
	
	public Collection<RequestReference> getRequestReferences(){
		return Collections.unmodifiableCollection(multipleRequests);
	}
	
	public boolean hasMultipleRequests(){
		return !multipleRequests.isEmpty();
	}
	
	/**
	 * Gets all attributes categories contained
	 * in this request context
	 * 
	 * @return an iterator over all categories
	 */
	public Iterable<AttributeCategoryId> getAttributeCategories(){
		return Collections.unmodifiableSet(attributes.keySet());
	}
	
	/**
	 * Gets all attributes for this context
	 * 
	 * @return a map by category id
	 */
	public Map<AttributeCategoryId, Collection<Attributes>> getAttributes(){
		return attributes.asMap();
	}
	
	/**
	 * Resolves attribute reference to {@link Attributes}
	 * 
	 * @param reference an attributes reference
	 * @return {@link Attributes} or <code>null</code> if
	 * reference can not be resolved
	 */
	public Attributes getReferencedAttributes(AttributesReference reference){
		Preconditions.checkNotNull(reference);
		return byId.get(reference.getReferenceId());
	}
	
	/**
	 * Gets all {@link Attributes} from request with
	 * a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes} or
	 * {@link Collections#emptyList()} if given request
	 * does not have attributes of given category
	 */
	public Collection<Attributes> getAttributes(AttributeCategoryId categoryId){
		Preconditions.checkNotNull(categoryId);
		Collection<Attributes> attr =  attributes.get(categoryId);
		return (attr == null)?Collections.<Attributes>emptyList():attr;
	}
	
	/**
	 * Gets all {@link Attributes} instances
	 * from a given  request context which has
	 * attribute with a given identifier
	 * 
	 * @param categoryId an attribute category
	 * @param attributeId an attribute id
	 * @return a collection of {@link Attributes}
	 */
	public Collection<Attributes> getAttributes(
			final AttributeCategoryId categoryId, 
			final String attributeId)
	{
		Preconditions.checkNotNull(categoryId);
		Preconditions.checkNotNull(attributeId);
		 Collection<Attributes> attr = attributes.get(categoryId);
		 if(attr == null){
			 return Collections.emptyList();
		 }
		 return Collections2.filter(attr, new Predicate<Attributes>() {
			@Override
			public boolean apply(Attributes a) {
				return a.containsAttribute(attributeId);
			}
		});
	}
}
