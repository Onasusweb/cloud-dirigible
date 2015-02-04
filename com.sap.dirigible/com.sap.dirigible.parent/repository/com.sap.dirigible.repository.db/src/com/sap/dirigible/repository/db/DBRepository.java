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

package com.sap.dirigible.repository.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.IEntity;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.api.IResourceVersion;
import com.sap.dirigible.repository.api.RepositoryPath;
import com.sap.dirigible.repository.db.dao.DBRepositoryDAO;
import com.sap.dirigible.repository.ext.db.DBUtils;
import com.sap.dirigible.repository.logging.Logger;
import com.sap.dirigible.repository.zip.ZipExporter;
import com.sap.dirigible.repository.zip.ZipImporter;

/**
 * The DB implementation of {@link IRepository}
 * 
 */
public class DBRepository implements IRepository {

	private static final String PROVIDED_ZIP_DATA_CANNOT_BE_NULL = Messages.getString("DBRepository.PROVIDED_ZIP_DATA_CANNOT_BE_NULL"); //$NON-NLS-1$

	private static final String PROVIDED_ZIP_INPUT_STREAM_CANNOT_BE_NULL = Messages.getString("DBRepository.PROVIDED_ZIP_INPUT_STREAM_CANNOT_BE_NULL"); //$NON-NLS-1$

	private static Logger logger = Logger.getLogger(DBRepository.class);

	public static final String PATH_DELIMITER = IRepository.SEPARATOR;

	private static final String WORKSPACE_PATH = IRepository.SEPARATOR;

	private DBRepositoryDAO repositoryDAO;

	private DataSource dataSource;

	private DBUtils dbUtils;

	private String user;
	
	private boolean cacheEnabled;
	
	private SimpleCacheManager cacheManager;
	
	public DBRepository(DataSource dataSource, String user,
			boolean forceRecreate) throws DBBaseException {
		this(dataSource, user, forceRecreate, true);
		logger.debug("exiting constructor"); //$NON-NLS-1$
	}
	
	public DBRepository(DataSource dataSource, String user,
			boolean forceRecreate, boolean cacheEnabled) throws DBBaseException {
		logger.debug("entering constructor"); //$NON-NLS-1$
		try {
			this.dataSource = dataSource;
			this.dbUtils = new DBUtils(dataSource);
			this.user = user;
			this.cacheEnabled = cacheEnabled;
			this.cacheManager = new SimpleCacheManager(!this.cacheEnabled);
			this.repositoryDAO = new DBRepositoryDAO(this);
			this.repositoryDAO.initialize(forceRecreate);
		} catch (SQLException e) {
			throw new DBBaseException(e);
		}
		logger.debug("exiting constructor"); //$NON-NLS-1$
	}
	
	public boolean isCacheEnabled() {
		return cacheEnabled;
	}
	
	public SimpleCacheManager getCacheManager() {
		return cacheManager;
	}

	@Override
	public ICollection getRoot() {
		logger.debug("entering getRoot"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(
				WORKSPACE_PATH);
		DBCollection dbCollection = new DBCollection(this, wrapperPath);
		logger.debug("exiting getRoot"); //$NON-NLS-1$
		return dbCollection;
	}

	@Override
	public ICollection createCollection(String path) throws IOException {
		logger.debug("entering createCollection"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final DBCollection collection = new DBCollection(this, wrapperPath);
		collection.create();
		logger.debug("exiting createCollection"); //$NON-NLS-1$
		return collection;
	}

	@Override
	public ICollection getCollection(String path) {
		logger.debug("entering getCollection"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		DBCollection dbCollection = new DBCollection(this, wrapperPath);
		logger.debug("exiting getCollection"); //$NON-NLS-1$
		return dbCollection;
	}

	@Override
	public void removeCollection(String path) throws IOException {
		logger.debug("entering removeCollection"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final ICollection collection = new DBCollection(this, wrapperPath);
		collection.delete();
		logger.debug("exiting removeCollection"); //$NON-NLS-1$
	}

	@Override
	public boolean hasCollection(String path) throws IOException {
		logger.debug("entering hasCollection"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final ICollection collection = new DBCollection(this, wrapperPath);
		boolean result = collection.exists();
		logger.debug("exiting hasCollection"); //$NON-NLS-1$
		return result;
	}

	@Override
	public IResource createResource(String path) throws IOException {
		logger.debug("entering createResource"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final IResource resource = new DBResource(this, wrapperPath);
		resource.create();
		logger.debug("exiting createResource"); //$NON-NLS-1$
		return resource;
	}

	@Override
	public IResource createResource(String path, byte[] content)
			throws IOException {
		logger.debug("entering createResource with Content"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final IResource resource = new DBResource(this, wrapperPath);
		resource.setContent(content);
		logger.debug("exiting createResource with Content"); //$NON-NLS-1$
		return resource;
	}

	@Override
	public IResource createResource(String path, byte[] content,
			boolean isBinary, String contentType) throws IOException {
		logger.debug("entering createResource with Content"); //$NON-NLS-1$
		try {
			getRepositoryDAO().createFile(path, content, isBinary, contentType);
		} catch (DBBaseException e) {
			throw new IOException(e);
		}
		final IResource resource = getResource(path);
		logger.debug("exiting createResource with Content"); //$NON-NLS-1$
		return resource;
	}

	@Override
	public IResource getResource(String path) {
		logger.debug("entering getResource"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		DBResource dbResource = new DBResource(this, wrapperPath);
		logger.debug("exiting getResource"); //$NON-NLS-1$
		return dbResource;
	}

	@Override
	public void removeResource(String path) throws IOException {
		logger.debug("entering removeResource"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final IResource resource = new DBResource(this, wrapperPath);
		resource.delete();
		logger.debug("exiting removeResource"); //$NON-NLS-1$
	}

	@Override
	public boolean hasResource(String path) throws IOException {
		logger.debug("entering hasResource"); //$NON-NLS-1$
		final RepositoryPath wrapperPath = new RepositoryPath(path);
		final IResource resource = new DBResource(this, wrapperPath);
		boolean result = resource.exists();
		logger.debug("exiting hasResource"); //$NON-NLS-1$
		return result;
	}

	@Override
	public void dispose() {
		repositoryDAO.dispose();
	}

	public DBRepositoryDAO getRepositoryDAO() {
		return repositoryDAO;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public DBUtils getDbUtils() {
		return dbUtils;
	}

	public String getUser() {
		return user;
	}

	@Override
	public void importZip(ZipInputStream zipInputStream, String relativeRoot)
			throws IOException {
		if (zipInputStream == null) {
			logger.error(PROVIDED_ZIP_INPUT_STREAM_CANNOT_BE_NULL);
			throw new IOException(PROVIDED_ZIP_INPUT_STREAM_CANNOT_BE_NULL);
		}
		ZipImporter.importZip(this, zipInputStream, relativeRoot);
	}

	@Override
	public void importZip(byte[] data, String relativeRoot) throws IOException {
		if (data == null) {
			logger.error(PROVIDED_ZIP_DATA_CANNOT_BE_NULL);
			throw new IOException(PROVIDED_ZIP_DATA_CANNOT_BE_NULL);
		}
		ZipImporter.importZip(this, new ZipInputStream(
				new ByteArrayInputStream(data)), relativeRoot);
	}

	@Override
	public byte[] exportZip(List<String> relativeRoots) throws IOException {
		return ZipExporter.exportZip(this, relativeRoots);
	}

	@Override
	public byte[] exportZip(String relativeRoot, boolean inclusive)
			throws IOException {
		return ZipExporter.exportZip(this, relativeRoot, inclusive);
	}

	@Override
	public List<IEntity> searchName(String parameter, boolean caseInsensitive)
			throws IOException {
		return repositoryDAO.searchName(parameter, caseInsensitive);
	}

	@Override
	public List<IEntity> searchName(String root, String parameter, boolean caseInsensitive)
			throws IOException {
		return repositoryDAO.searchName(root, parameter, caseInsensitive);
	}
	
	@Override
	public List<IEntity> searchPath(String parameter, boolean caseInsensitive)
			throws IOException {
		return repositoryDAO.searchPath(parameter, caseInsensitive);
	}

	@Override
	public List<IEntity> searchText(String parameter, boolean caseInsensitive)
			throws IOException {
		return repositoryDAO.searchText(parameter, caseInsensitive);
	}

	@Override
	public List<IResourceVersion> getResourceVersions(String path)
			throws IOException {
		return repositoryDAO.getResourceVersionsByPath(path);
	}

	@Override
	public IResourceVersion getResourceVersion(String path, int version)
			throws IOException {
		return new DBResourceVersion(this, new RepositoryPath(path), version);
	}
	
	@Override
	public void cleanupOldVersions() throws IOException {
		repositoryDAO.cleanupOldVersions();
	}

}
