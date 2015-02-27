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

package com.sap.dirigible.repository.api;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * This interface represents a repository. It allows for querying, modifying and
 * navigating through collections and resources.
 * 
 */
public interface IRepository {

	public static final String SEPARATOR = ICommonConstants.SEPARATOR; //$NON-NLS-1$

	/**
	 * Returns an instance of <code>ICollection</code> which represents the root
	 * collection of the repository.
	 * <p>
	 * This method does not throw any exceptions for convenience but is not
	 * guaranteed to return a valid collection. One should check this by using
	 * the {@link ICollection#isValid()} method.
	 */
	public ICollection getRoot();

	/**
	 * This method creates a new empty collection at the specified path.
	 * <p>
	 * The returned value is an instance of <code>ICollection</code> which
	 * represents the newly created collection.
	 */
	public ICollection createCollection(String path) throws IOException;

	/**
	 * Returns an <code>ICollection</code> instance representing the resource at
	 * the specified path.
	 * <p>
	 * The collection may not exist at the specified path.
	 */
	public ICollection getCollection(String path);

	/**
	 * This method removes the collection with the specified path from the
	 * repository.
	 */
	public void removeCollection(String path) throws IOException;

	/**
	 * Returns whether a collection with the specified path exists in the
	 * repository.
	 */
	public boolean hasCollection(String path) throws IOException;

	/**
	 * This method creates a new empty resource at the specified path.
	 * <p>
	 * The returned value is an instance of <code>IResource</code> that
	 * represents the newly created resource.
	 */
	public IResource createResource(String path) throws IOException;

	/**
	 * This method creates a new resource at the specified path and fills it
	 * with the specified content.
	 * <p>
	 * The returned value is an instance of <code>IResource</code> that
	 * represents the newly created resource.
	 */
	public IResource createResource(String path, byte[] content) throws IOException;

	/**
	 * This method creates a new empty resource at the specified path.
	 * <p>
	 * The returned value is an instance of <code>IResource</code> that
	 * represents the newly created resource.
	 */
	public IResource createResource(String path, byte[] content, boolean isBinary,
			String contentType) throws IOException;

	/**
	 * This method creates a new empty, or override resource at the specified path.
	 * <p>
	 * The returned value is an instance of <code>IResource</code> that
	 * represents the newly created resource.
	 */
	public IResource createResource(String path, byte[] content, boolean isBinary,
			String contentType, boolean override) throws IOException;

	/**
	 * Returns an instance of <code>IResource</code> which represents the
	 * resource located at the specified path.
	 * <p>
	 * The resource may not exist at the specified path.
	 */
	public IResource getResource(String path);

	/**
	 * This method removes the resource at the specified path from the
	 * repository.
	 */
	public void removeResource(String path) throws IOException;

	/**
	 * Returns whether a resource with the specified path exists in the
	 * repository.
	 */
	public boolean hasResource(String path) throws IOException;

	/**
	 * Disposes of this repository.
	 * <p>
	 * Calling this method allows for the repository to release all allocated
	 * resources.
	 * <p>
	 * Calling this method more than once will be a no-op.
	 */
	public void dispose();

	/**
	 * Imports content from zip file to the repository, based on the relative
	 * root
	 * 
	 * @param zipInputStream
	 * @param relativeRoot
	 * @throws IOException
	 */
	public void importZip(ZipInputStream zipInputStream, String relativeRoot) throws IOException;

	/**
	 * Imports content from zip file to the repository, based on the relative
	 * root
	 * 
	 * @param zipInputStream
	 * @param relativeRoot
	 * @param override
	 * @throws IOException
	 */
	public void importZip(ZipInputStream zipInputStream, String relativeRoot, boolean override) throws IOException;

	/**
	 * Imports content from zip file to the repository, based on the relative
	 * root
	 * 
	 * @param data
	 *            the Zip file as byte array
	 * @param relativeRoot
	 * @throws IOException
	 */
	public void importZip(byte[] data, String relativeRoot) throws IOException;

	/**
	 * Imports content from zip file to the repository, based on the relative
	 * root
	 * 
	 * @param data
	 *            the Zip file as byte array
	 * @param relativeRoot
	 * @throws IOException
	 */
	public void importZip(byte[] data, String relativeRoot, boolean override) throws IOException;

	/**
	 * Export all the content under the given path(s) with the target repository
	 * instance Include the last segment of the relative roots during the
	 * archiving
	 * 
	 * @param relativeRoot
	 * @return
	 * @throws IOException
	 */
	public byte[] exportZip(List<String> relativeRoots) throws IOException;

	/**
	 * Export all the content under the given path with the target repository
	 * instance Include or NOT the last segment of the relative root during the
	 * archiving
	 * 
	 * @param relativeRoot
	 *            single root
	 * @param inclusive
	 *            whether to include the last segment of the root or to pack its
	 *            content directly in the archive
	 * @return
	 * @throws IOException
	 */
	public byte[] exportZip(String relativeRoot, boolean inclusive) throws IOException;

	/**
	 * Search the given parameter in the names of the files and folders ( means
	 * *parameter)
	 * 
	 * @param parameter
	 * @param caseInsensitive
	 * @return
	 * @throws IOException
	 */
	public List<IEntity> searchName(String parameter, boolean caseInsensitive) throws IOException;

	/**
	 * Search the given parameter in the names of the files and folders ( means
	 * *parameter) under specified root folder (means *root)
	 * 
	 * @param root
	 * @param parameter
	 * @param caseInsensitive
	 * @return
	 * @throws IOException
	 */
	public List<IEntity> searchName(String root, String parameter, boolean caseInsensitive)
			throws IOException;

	/**
	 * Search the given given parameter in the names of the files and folders
	 * (means *parameter*)
	 * 
	 * @param parameter
	 * @param caseInsensitive
	 * @return
	 * @throws IOException
	 */
	public List<IEntity> searchPath(String parameter, boolean caseInsensitive) throws IOException;

	/**
	 * Search the given given parameter in the names of the files and folders as
	 * well as in the content of the text files
	 * 
	 * @param parameters
	 * @param caseInsensitive
	 * @return
	 * @throws IOException
	 */
	public List<IEntity> searchText(String parameter, boolean caseInsensitive) throws IOException;

	/**
	 * Retrieve all the kept versions of a given resource
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public List<IResourceVersion> getResourceVersions(String path) throws IOException;

	/**
	 * Retrieve a particular version of a given resource
	 * 
	 * @param path
	 * @param version
	 * @return
	 * @throws IOException
	 */
	public IResourceVersion getResourceVersion(String path, int version) throws IOException;

	/**
	 * Clean-up the file versions older than a month For full fledged SCM
	 * system, use external e.g. Git
	 * 
	 * @throws IOException
	 */
	public void cleanupOldVersions() throws IOException;

}
