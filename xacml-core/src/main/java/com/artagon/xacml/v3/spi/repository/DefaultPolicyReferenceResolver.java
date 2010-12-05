package com.artagon.xacml.v3.spi.repository;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.spi.PolicyReferenceResolver;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * A default implementation of {@link PolicyReferenceResolver}.
 * Maintains a cache of resolved policies by the reference
 * 
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyReferenceResolver implements PolicyReferenceResolver
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyReferenceResolver.class);
	
	private ConcurrentMap<PolicyIDReference, Policy> policyIDRefCache;
	private ConcurrentMap<PolicySetIDReference, PolicySet> policySetIDRefCache;
	
	private PolicyRepository repository;
	private boolean enableRefCache;
	
	protected DefaultPolicyReferenceResolver(
			PolicyRepository repository){
		this(repository, true, 32);
	}
	
	protected DefaultPolicyReferenceResolver(
			PolicyRepository policyRepository, 
			boolean enabledRefCache, int size)
	{
		Preconditions.checkNotNull(policyRepository);
		this.repository = policyRepository;
		this.enableRefCache = enabledRefCache;
		this.policyIDRefCache = new MapMaker()
		.initialCapacity(size)
		.softKeys()
		.softValues()
		.makeMap();
		this.policySetIDRefCache = new MapMaker()
		.initialCapacity(size)
		.softKeys()
		.softValues()
		.makeMap();
	}
	
	/**
	 * A default implementation invokes 
	 * {@link #getPolicy(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public Policy resolve(PolicyIDReference ref)
			throws PolicyResolutionException 
	{
		Policy p =  policyIDRefCache.get(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found Policy id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  repository.getPolicy(
					ref.getId(), 
					ref.getVersionMatch(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policyIDRefCache.put(ref, p);
		}
		return p;
	}
	
	/**
	 * A default implementation invokes 
	 * {@link #getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException 
	{
		PolicySet p = policySetIDRefCache.get(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found PolicySet id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  repository.getPolicySet(
					ref.getId(), 
					ref.getVersionMatch(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policySetIDRefCache.put(ref, p);
		}
		return p;
	}
	
	protected final void clearRefCahce(){
		policyIDRefCache.clear();
		policySetIDRefCache.clear();
	}
}
