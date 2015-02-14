package com.sap.dirigible.ide.common.dual;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientService;

/**
 * This class is supposed to be used in RAP environment only
 * A class with the same fully qualified name exist in *.rcp plugin for standalone use
 *
 */
public class DualParameters {
	
	public static final String RUNTIME_URL = "runtimeUrl"; //$NON-NLS-1$
	public static final String SERVICES_URL = "servicesUrl"; //$NON-NLS-1$
	public static final String RUNTIME_URL_DEFAULT = ""; //$NON-NLS-1$
	public static final String SERVICES_URL_DEFAULT = ""; //$NON-NLS-1$
	
	public static final String HC_LOCAL_HTTP_PORT = "HC_LOCAL_HTTP_PORT"; //$NON-NLS-1$
	public static final String HC_APPLICATION_URL = "HC_APPLICATION_URL"; //$NON-NLS-1$
	public static final String HC_APPLICATION = "HC_APPLICATION"; //$NON-NLS-1$
	public static final String HC_ACCOUNT = "HC_ACCOUNT"; //$NON-NLS-1$
	public static final String HC_REGION = "HC_REGION"; //$NON-NLS-1$
	public static final String HC_HOST = "HC_HOST"; //$NON-NLS-1$
	
	public static final String GUEST_USER = "guest"; //$NON-NLS-1$
	
	public static final String ENABLE_ROLES = "enableRoles"; //$NON-NLS-1$
	
	public static void initSystemParameters() {
		HttpServletRequest req = RWT.getRequest();
		String parameterHC_HOST = System.getProperty(HC_HOST);
		req.getSession().setAttribute(HC_HOST, parameterHC_HOST);
		String parameterHC_REGION = System.getProperty(HC_REGION);
		req.getSession().setAttribute(HC_REGION, parameterHC_REGION);
		String parameterHC_ACCOUNT = System.getProperty(HC_ACCOUNT);
		req.getSession().setAttribute(HC_ACCOUNT, parameterHC_ACCOUNT);
		String parameterHC_APPLICATION = System.getProperty(HC_APPLICATION);
		req.getSession().setAttribute(HC_APPLICATION, parameterHC_APPLICATION);
		String parameterHC_APPLICATION_URL = System.getProperty(HC_APPLICATION_URL);
		req.getSession().setAttribute(HC_APPLICATION_URL, parameterHC_APPLICATION_URL);
		String parameterHC_LOCAL_HTTP_PORT = System.getProperty(HC_LOCAL_HTTP_PORT);
		req.getSession().setAttribute(HC_LOCAL_HTTP_PORT, parameterHC_LOCAL_HTTP_PORT);
	}
	
	public static String get(String name) {
		String parameter = (String) RWT.getRequest().getSession().getAttribute(name);
		return parameter;
	}
	
	public static Object getObject(String name) {
		Object parameter = RWT.getRequest().getSession().getAttribute(name);
		return parameter;
	}

	public static void set(String name, String value) {
		RWT.getRequest().getSession().setAttribute(name, value);
	}
	
	public static void setObject(String name, Object value) {
		RWT.getRequest().getSession().setAttribute(name, value);
	}
	
	public static String getRuntimeUrl() {
		String runtimeUrl = get(RUNTIME_URL);
		if (runtimeUrl == null) {
			runtimeUrl = RUNTIME_URL_DEFAULT;
		}
		return runtimeUrl;
	}
	
	public static String getServicesUrl() {
		String runtimeUrl = get(RUNTIME_URL);
		if (runtimeUrl == null
				|| "".equals(runtimeUrl)) {
			runtimeUrl = RWT.getRequest().getContextPath();
		}
		String servicesUrl = get(SERVICES_URL);
		if (servicesUrl == null) {
			servicesUrl = SERVICES_URL_DEFAULT;
		}
		
		return runtimeUrl + servicesUrl;
	}

	public static <T extends ClientService> T getService(Class<T> clazz) {
		return RWT.getClient().getService(clazz);
	}
	
	public static String getContextPath() {
		return RWT.getRequest().getContextPath();
	}
	
	public static HttpServletRequest getRequest() {
		return RWT.getRequest();
	}
	
	public static String getUserName() {
		String user = null;
		try {
			user = RWT.getRequest().getRemoteUser();

		} catch (Throwable t) {
			user = GUEST_USER;
		}
		if (user == null) {
			user = GUEST_USER;
		}
		return user;
	}
	
	public static Boolean isRolesEnabled() {
		Boolean rolesEnabled = Boolean.parseBoolean(get(ENABLE_ROLES));
		return rolesEnabled;
	}

	public static boolean isUserInRole(String role) {
		if (isRolesEnabled()) {
			return RWT.getRequest().isUserInRole(role);
		} else {
			return true;
		}
	}

	public static String getSessionId() {
		String sessionId = RWT.getRequest().getSession(true).getId();
		return sessionId;
	}
	
	public static final boolean isRAP() {
		return true;
	}
	
	public static final boolean isRCP() {
		return false;
	}
}

