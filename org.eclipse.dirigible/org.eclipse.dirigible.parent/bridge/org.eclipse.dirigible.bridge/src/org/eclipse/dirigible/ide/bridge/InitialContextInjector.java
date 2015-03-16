/******************************************************************************* 
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.ide.bridge;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialContextInjector implements Injector {
	
	private static final Logger logger = LoggerFactory.getLogger(InitialContextInjector.class);
	
	public static final String INITIAL_CONTEXT = "INITIAL_CONTEXT"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.dirigible.ide.bridge.Injector#inject(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void inject(ServletConfig servletConfig, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		InitialContext initialContext = (InitialContext) req.getSession().getAttribute(INITIAL_CONTEXT);
		if (initialContext == null) {
			try {
				initialContext = new InitialContext();
				req.getSession().setAttribute(INITIAL_CONTEXT, initialContext);
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
}
