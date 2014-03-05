/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.bridge;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalParametersInjector implements Injector {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalParametersInjector.class); 
	
	public static final String HC_LOCAL_HTTP_PORT = "HC_LOCAL_HTTP_PORT"; //$NON-NLS-1$
	public static final String HC_APPLICATION_URL = "HC_APPLICATION_URL"; //$NON-NLS-1$
	public static final String HC_APPLICATION = "HC_APPLICATION"; //$NON-NLS-1$
	public static final String HC_ACCOUNT = "HC_ACCOUNT"; //$NON-NLS-1$
	public static final String HC_REGION = "HC_REGION"; //$NON-NLS-1$
	public static final String HC_HOST = "HC_HOST"; //$NON-NLS-1$
	
	
	@Override
	public void inject(ServletConfig servletConfig, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String parameterHC_HOST = SystemBridge.ENV_PROPERTIES.getProperty(HC_HOST);
		req.getSession().setAttribute(HC_HOST, parameterHC_HOST);
		logger.debug("HC_HOST:" + parameterHC_HOST);
		String parameterHC_REGION = SystemBridge.ENV_PROPERTIES.getProperty(HC_REGION);
		req.getSession().setAttribute(HC_REGION, parameterHC_REGION);
		logger.debug("HC_REGION:" + parameterHC_REGION);
		String parameterHC_ACCOUNT = SystemBridge.ENV_PROPERTIES.getProperty(HC_ACCOUNT);
		req.getSession().setAttribute(HC_ACCOUNT, parameterHC_ACCOUNT);
		logger.debug("HC_ACCOUNT:" + parameterHC_ACCOUNT);
		String parameterHC_APPLICATION = SystemBridge.ENV_PROPERTIES.getProperty(HC_APPLICATION);
		req.getSession().setAttribute(HC_APPLICATION, parameterHC_APPLICATION);
		logger.debug("HC_APPLICATION:" + parameterHC_APPLICATION);
		String parameterHC_APPLICATION_URL = SystemBridge.ENV_PROPERTIES.getProperty(HC_APPLICATION_URL);
		req.getSession().setAttribute(HC_APPLICATION_URL, parameterHC_APPLICATION_URL);
		logger.debug("HC_APPLICATION_URL:" + parameterHC_APPLICATION_URL);
		String parameterHC_LOCAL_HTTP_PORT = SystemBridge.ENV_PROPERTIES.getProperty(HC_LOCAL_HTTP_PORT);
		req.getSession().setAttribute(HC_LOCAL_HTTP_PORT, parameterHC_LOCAL_HTTP_PORT);
		logger.debug("HC_LOCAL_HTTP_PORT:" + parameterHC_LOCAL_HTTP_PORT);
		
	}

}
