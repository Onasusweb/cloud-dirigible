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

package org.eclipse.dirigible.repository.api;

import javax.annotation.Resource;

/**
 * Utility class for handling {@link Resource} objects.
 * 
 */
public final class ResourceUtil {

	/**
	 * Returns the extension of the resource name if there is such, otherwise
	 * returns <code>null</code>. If there is a dot but there is no extension,
	 * then this method returns the empty string.
	 * <p>
	 * For example:
	 * <ul>
	 * <li><b>request.xml</b> yields <b>"xml"</b></li>
	 * <li><b>page.html</b> yields <b>"html"</b></li>
	 * <li><b>sample.</b> yields <b><code>""</code></b></li>
	 * <li><b>sample</b> yields <b><code>null</code></b></li>
	 * </ul>
	 * 
	 * @param resource
	 *            resource who's name extension will be returned.
	 * @return the extension of a resource name
	 */
	public static String getResourceExtension(IResource resource) {
		final String name = resource.getName();
		final int lastDotIndex = name.lastIndexOf('.');
		if (lastDotIndex == -1) {
			return null;
		}
		return name.substring(lastDotIndex + 1);
	}

	/**
	 * Returns the name of a resource without the extension.
	 * <p>
	 * For example:
	 * <ul>
	 * <li><b>request.xml</b> yields <b>"request"</b></li>
	 * <li><b>page.html</b> yields <b>"page"</b></li>
	 * <li><b>sample.</b> yields <b><code>"sample"</code></b></li>
	 * <li><b>sample</b> yields <b><code>"sample"</code></b></li>
	 * </ul>
	 * 
	 * @param resource
	 *            resource who's pure name will be returned.
	 * @return the name of a resource without the extension at the end.
	 */
	public static String getResourcePureName(IResource resource) {
		final String name = resource.getName();
		final int lastDotIndex = name.lastIndexOf('.');
		if (lastDotIndex == -1) {
			return name;
		}
		return name.substring(0, lastDotIndex);
	}

	/*
	 * Disable instantiation
	 */
	private ResourceUtil() {
		super();
	}

}
