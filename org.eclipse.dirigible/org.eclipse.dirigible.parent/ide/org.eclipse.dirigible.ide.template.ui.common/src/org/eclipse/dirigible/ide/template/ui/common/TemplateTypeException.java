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

package org.eclipse.dirigible.ide.template.ui.common;

public class TemplateTypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TemplateTypeException() {
		super();
	}

	public TemplateTypeException(String message) {
		super(message);
	}

	public TemplateTypeException(Throwable ex) {
		super(ex);
	}

	public TemplateTypeException(String message, Throwable ex) {
		super(message, ex);
	}

}
