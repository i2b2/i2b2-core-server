package edu.harvard.i2b2.crc.role;

import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeLocal;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class AuthrizationHelper {

	protected final Log log = LogFactory.getLog(AuthrizationHelper.class);

	private String domainId = null, projectId = null, userId = null;
	private IDAOFactory daoFactory = null;

	public AuthrizationHelper(String domainId, String projectId, String userId,
			IDAOFactory daoFactory) {
		this.domainId = domainId;
		this.projectId = projectId;
		this.userId = userId;
		this.daoFactory = daoFactory;
	}

	/**
	 * 
	 * @param protectionLabel
	 * @param roles
	 * @throws I2B2Exception
	 */
	public void checkRoleForProtectionLabel(String protectionLabel,
			List<String> roles) throws I2B2Exception {
		PriviledgeLocal privilegeLocal = QueryProcessorUtil.getInstance()
				.getPriviledgeLocal();
		privilegeLocal.checkPriviledge(daoFactory, protectionLabel, roles);
	}

	/**
	 * 
	 * @param protectionLabel
	 * @param roles
	 * @throws I2B2Exception
	 */
	public void checkRoleForPluginId(String pluginId, List<String> roles)
			throws I2B2Exception {
		PriviledgeLocal privilegeLocal = QueryProcessorUtil.getInstance()
				.getPriviledgeLocal();
		privilegeLocal.checkPluginPrivilege(daoFactory, pluginId, roles);
	}

	/**
	 * Read the user role from the cache and check for protection lable with the
	 * priviledge table
	 * 
	 * @param protectionLabel
	 * @throws I2B2Exception
	 */
	public void checkRoleForProtectionLabel(String protectionLabel)
			throws I2B2Exception {
		List<String> roles = getRolesFromCache();
		checkRoleForProtectionLabel(protectionLabel, roles);

	}

	public void checkRoleForPluginId(String pluginId) throws I2B2Exception {
		List<String> roles = getRolesFromCache();
		checkRoleForPluginId(pluginId, roles);
	}

	private List<String> getRolesFromCache() throws I2B2Exception {
		//TODO removed cache
		/*
		Cache cache = null;
		try {
			cache = CacheUtil.getCache();
		} catch (MalformedObjectNameException e) {
			log.error("Unable to get cache object "
					+ StackTraceUtil.getStackTrace(e));
			throw new I2B2Exception("Unable to get cache object "
					+ StackTraceUtil.getStackTrace(e));
		} catch (NullPointerException e) {
			log.error("Unable to get cache object "
					+ StackTraceUtil.getStackTrace(e));
			throw new I2B2Exception("Unable to get cache object "
					+ StackTraceUtil.getStackTrace(e));
		}
		*/
		// domainId + "/" + projectId + "/" + userId;
		String rolePath = domainId + "/" + projectId + "/" + userId;
		//List<String> roles = (List<String>) cache.getRoot().get(rolePath);
		List<String> roles = (List<String>) CacheUtil.get(rolePath);

		log.debug("Roles from get " + rolePath);
		if (roles != null) {
			log.debug("Roles from size " + roles.size());
		} else {
			log.error("Could not find the Roles from the cache for ["
					+ rolePath + "]");
			throw new I2B2Exception(
					"Could not find the Roles from the cache for [" + rolePath
							+ "]");
		}
		return roles;
	}
}
