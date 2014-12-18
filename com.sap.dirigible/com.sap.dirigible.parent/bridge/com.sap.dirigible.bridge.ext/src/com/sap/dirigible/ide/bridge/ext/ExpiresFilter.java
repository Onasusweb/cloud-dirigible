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

package com.sap.dirigible.ide.bridge.ext;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExpiresFilter implements Filter {

	private static final String CACHE_CONTROL = "Cache-control"; //$NON-NLS-1$
	private static final String MAX_AGE_10000 = "max-age=10000"; //$NON-NLS-1$
	private static final String PRAGMA = "Pragma"; //$NON-NLS-1$
	private static final String EXPIRES = "Expires"; //$NON-NLS-1$
	private static final String RWT_RESOURCES_THEMES_IMAGES = "rwt-resources/themes/images/"; //$NON-NLS-1$
	private static final String RWT_RESOURCES_GENERATED = "rwt-resources/generated"; //$NON-NLS-1$

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		chain.doFilter(request, response);
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		if (req.getRequestURL() != null 
				&& (req.getRequestURL().indexOf(RWT_RESOURCES_GENERATED) >= 0
				|| req.getRequestURL().indexOf(RWT_RESOURCES_THEMES_IMAGES) >= 0)) {
			res.setDateHeader(EXPIRES, (new GregorianCalendar(3000, 1, 1)).getTime().getTime());
			res.setHeader(PRAGMA, MAX_AGE_10000);
			res.setHeader(CACHE_CONTROL, MAX_AGE_10000);
		}
	}

	@Override
	public void destroy() {
	}

}
