package com.sap.dirigible.runtime.scripting.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.dirigible.runtime.RuntimeActivator;
import com.sap.dirigible.runtime.logger.Logger;
import com.sap.dirigible.runtime.scripting.IScriptExecutor;
import com.sap.dirigible.runtime.scripting.IScriptExecutorProvider;

public class EngineUtils {
	
	private static final Logger logger = Logger.getLogger(EngineUtils.class);
	
	private static Map<String, IScriptExecutorProvider> scriptExecutorProviders = Collections.synchronizedMap(new HashMap<String, IScriptExecutorProvider>());
	
	private static Map<String, IScriptExecutorProvider> scriptExecutorProvidersByAlias = Collections.synchronizedMap(new HashMap<String, IScriptExecutorProvider>());
	
	static {
		// register script executor providers
		try {
			BundleContext context = RuntimeActivator.getContext();
			Collection<ServiceReference<IScriptExecutorProvider>> serviceReferences = context.getServiceReferences(IScriptExecutorProvider.class, null);
			for (Iterator<ServiceReference<IScriptExecutorProvider>> iterator = serviceReferences.iterator(); iterator.hasNext();) {
				ServiceReference<IScriptExecutorProvider> serviceReference = iterator.next();
				IScriptExecutorProvider scriptExecutorProvider = context.getService(serviceReference);
				scriptExecutorProviders.put(scriptExecutorProvider.getType(), scriptExecutorProvider);
				scriptExecutorProvidersByAlias.put(scriptExecutorProvider.getAlias(), scriptExecutorProvider);
			}
		} catch (InvalidSyntaxException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static Set<String> getTypes() {
		return scriptExecutorProviders.keySet();
	}
	
	public static Set<String> getAliases() {
		return scriptExecutorProvidersByAlias.keySet();
	}
	
	public static IScriptExecutor createExecutor(String type, HttpServletRequest request) throws IOException {
		IScriptExecutorProvider scriptExecutorProvider = scriptExecutorProviders.get(type);
		IScriptExecutor scriptExecutor = scriptExecutorProvider.createExecutor(request);
		return scriptExecutor;
	}

}
