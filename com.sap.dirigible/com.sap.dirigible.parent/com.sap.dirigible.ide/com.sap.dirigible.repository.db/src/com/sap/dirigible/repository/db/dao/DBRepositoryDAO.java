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

package com.sap.dirigible.repository.db.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dirigible.repository.api.IEntity;
import com.sap.dirigible.repository.api.IResourceVersion;
import com.sap.dirigible.repository.db.DBBaseException;
import com.sap.dirigible.repository.db.DBRepository;
import com.sap.dirigible.repository.db.DBRepositoryPath;
import com.sap.dirigible.repository.db.DBResourceVersion;

/**
 * Utility class for direct database manipulation via JDBC
 * 
 * Base tables:
 * 
 * DGB_FILES - the files and folder registry 
 * DGB_DOCUMENTS - the content of text files separated by chunks 
 * DGB_BINARIES - the content of binary files
 * DGB_SCHEMA_VERSIONS - the version of the current repository schema
 * 
 */
public class DBRepositoryDAO {

	private static final String DATA_ACCESS_OBJECT_FOR_REPOSITORY_NOT_INITIALIZED = Messages.getString("DBRepositoryDAO.DATA_ACCESS_OBJECT_FOR_REPOSITORY_NOT_INITIALIZED"); //$NON-NLS-1$

	private static Logger logger = LoggerFactory
			.getLogger(DBRepositoryDAO.class.getCanonicalName());

	private DBRepository repository;

	private boolean initialized = false;

	private DBObjectDAO dbObjectDAO;

	private DBFolderDAO dbFolderDAO;

	private DBFileDAO dbFileDAO;

	private DBSearchDAO dbSearchDAO;

	private DBFileVersionDAO dbFileVersionDAO;

	public DBRepositoryDAO(DBRepository repository) {
		logger.debug(this.getClass().getCanonicalName(), "entering constructor"); //$NON-NLS-1$

		this.repository = repository;

		this.dbObjectDAO = new DBObjectDAO(this);
		this.dbFolderDAO = new DBFolderDAO(this);
		this.dbFileDAO = new DBFileDAO(this);
		this.dbSearchDAO = new DBSearchDAO(this);
		this.dbFileVersionDAO = new DBFileVersionDAO(this);

		logger.debug(this.getClass().getCanonicalName(), "exiting constructor"); //$NON-NLS-1$
	}

	/**
	 * Getter for the Repository object
	 * 
	 * @return
	 */
	DBRepository getRepository() {
		logger.debug(this.getClass().getCanonicalName(),
				"entering getRepository"); //$NON-NLS-1$
		DBRepository dbRepository = repository;
		logger.debug(this.getClass().getCanonicalName(),
				"exiting getRepository"); //$NON-NLS-1$
		return dbRepository;
	}

	/**
	 * Getter for the DBObjectDAO object
	 * 
	 * @return
	 */
	public DBObjectDAO getDbObjectDAO() {
		return dbObjectDAO;
	}

	/**
	 * Getter for the DBFolderDAO object
	 * 
	 * @return
	 */
	public DBFolderDAO getDbFolderDAO() {
		return dbFolderDAO;
	}

	/**
	 * Getter for the DBFileDAO object
	 * 
	 * @return
	 */
	public DBFileDAO getDbFileDAO() {
		return dbFileDAO;
	}

	/**
	 * Getter for the DBSearchDAO object
	 * 
	 * @return
	 */
	public DBSearchDAO getDbSearchDAO() {
		return dbSearchDAO;
	}

	/**
	 * Initialize the database schema
	 * 
	 * @param forceRecreate
	 *            recreate or hard reset if needed
	 * @return
	 * @throws SQLException
	 */
	public boolean initialize(boolean forceRecreate) throws SQLException {
		logger.debug(this.getClass().getCanonicalName(), "entering initialize"); //$NON-NLS-1$
		synchronized (DBRepositoryDAO.class) {
			Connection connection = null;
			try {
				connection = getRepository().getDbUtils().getConnection();
				initialized = initialize(connection, forceRecreate);
			} finally {
				getRepository().getDbUtils().closeConnection(connection);
			}
		}
		logger.debug(this.getClass().getCanonicalName(), "exiting initialize"); //$NON-NLS-1$
		return initialized;
	}

	/**
	 * The subprocess of the above
	 * 
	 * @param connection
	 * @param forceRecreate
	 * @return
	 */
	private boolean initialize(Connection connection, boolean forceRecreate) {
		logger.debug(this.getClass().getCanonicalName(),
				"entering initialize with connection"); //$NON-NLS-1$
		DBRepositoryInitializer dbRepositoryInitializer = new DBRepositoryInitializer(
				repository, connection, forceRecreate);
		boolean result = dbRepositoryInitializer.initialize();
		logger.debug(this.getClass().getCanonicalName(),
				"exiting initialize with connection"); //$NON-NLS-1$
		return result;
	}

	/**
	 * Check whether the database schema has already been initialized
	 * 
	 * @throws DBBaseException
	 */
	void checkInitialized() throws DBBaseException {
		if (!initialized) {
			throw new DBBaseException(
					DATA_ACCESS_OBJECT_FOR_REPOSITORY_NOT_INITIALIZED);
		}
	}

	/**
	 * Query the database and retrieve the database object based on the provided
	 * path
	 * 
	 * @param path
	 * @return
	 * @throws DBBaseException
	 */
	public DBObject getObjectByPath(String path) throws DBBaseException {
		return this.dbObjectDAO.getObjectByPath(path);
	}

	/**
	 * Return the database folder object
	 * 
	 * @param path
	 * @return
	 * @throws DBBaseException
	 */
	public DBFolder getFolderByPath(String path) throws DBBaseException {
		return this.dbFolderDAO.getFolderByPath(path);
	}

	/**
	 * Return the database file object
	 * 
	 * @param path
	 * @return
	 * @throws DBBaseException
	 */
	public DBFile getFileByPath(String path) throws DBBaseException {
		return this.dbFileDAO.getFileByPath(path);
	}

	/**
	 * Create the database folder object in the target database schema, located
	 * at the given path - cascading
	 * 
	 * @param path
	 * @return
	 * @throws DBBaseException
	 */
	public DBFolder createFolder(String path) throws DBBaseException {
		return this.dbFolderDAO.createFolder(path);
	}

	/**
	 * Delete the database folder object based on the given path
	 * 
	 * @param path
	 * @throws DBBaseException
	 */
	public void removeFolderByPath(String path) throws DBBaseException {
		this.dbFolderDAO.removeFolderByPath(path);
	}

	/**
	 * Create the file (text or binary) at the given path
	 * 
	 * @param path
	 * @param bytes
	 * @param isBinary
	 * @param contentType
	 * @return
	 * @throws DBBaseException
	 */
	public DBFile createFile(String path, byte[] bytes, boolean isBinary,
			String contentType) throws DBBaseException {
		DBFile dbFile = this.dbFileDAO.createFile(path, bytes, isBinary,
				contentType);
		this.dbFileVersionDAO.createFileVersion(path, bytes, isBinary,
				contentType);
		return dbFile;
	}

	/**
	 * Create the document (content chunks)
	 * 
	 * @param resource
	 * @param bytes
	 * @throws DBBaseException
	 */
	public void setDocument(DBFile resource, byte[] bytes)
			throws DBBaseException {
		this.dbFileDAO.setDocument(resource, bytes);
		this.dbFileVersionDAO.createFileVersion(resource.getPath(), bytes,
				resource.isBinary(), resource.getContentType());
	}

	/**
	 * Retrieve the document content - combine the chunks
	 * 
	 * @param resource
	 * @return
	 * @throws DBBaseException
	 */
	public byte[] getDocument(DBFile resource) throws DBBaseException {
		return this.dbFileDAO.getDocument(resource);
	}

	/**
	 * Delete database file by given path
	 * 
	 * @param path
	 * @throws DBBaseException
	 */
	public void removeFileByPath(String path) throws DBBaseException {
		this.dbFileDAO.removeFileByPath(path);
		this.dbFileVersionDAO.removeAllFileVersions(path);
	}
	
	public void cleanupOldVersions() throws DBBaseException {
		Calendar calendar = new GregorianCalendar();
		calendar.roll(Calendar.MONTH, false);
		this.dbFileVersionDAO.removeAllFileVersionsBeforeDate(calendar.getTime());
	}

	public void dispose() {
	}

	/**
	 * Iterate the sub-folders and files of a given folder
	 * 
	 * @param path
	 * @return
	 * @throws DBBaseException
	 */
	public List<DBObject> getChildrenByFolder(String path)
			throws DBBaseException {
		return this.dbFolderDAO.getChildrenByFolder(path);
	}

	/**
	 * Retrieve the binary content of a file
	 * 
	 * @param resource
	 * @return
	 * @throws DBBaseException
	 */
	public byte[] getBinary(DBFile resource) throws DBBaseException {
		return this.dbFileDAO.getBinary(resource);
	}

	/**
	 * Create the binary content of for a given file, as before this clean the
	 * content if any
	 * 
	 * @param resource
	 * @param bytes
	 * @param contentType
	 * @throws DBBaseException
	 */
	public void setBinary(DBFile resource, byte[] bytes, String contentType)
			throws DBBaseException {
		this.dbFileDAO.setBinary(resource, bytes, contentType);
	}

	/**
	 * Search for files and folders containing the parameter in their name
	 * (means %parameter)
	 * 
	 * @param parameter
	 * @return
	 */
	public List<IEntity> searchName(String parameter, boolean caseInsensitive) {
		return this.dbSearchDAO.searchName(parameter, caseInsensitive);
	}

	/**
	 * Search for files and folders containing the parameter in their name
	 * (means %parameter%)
	 * 
	 * @param parameter
	 * @return
	 */
	public List<IEntity> searchPath(String parameter, boolean caseInsensitive) {
		return this.dbSearchDAO.searchPath(parameter, caseInsensitive);
	}

	/**
	 * Search for files containing the parameter in their content
	 * 
	 * @param parameter
	 * @param caseInsensitive
	 * @return
	 */
	public List<IEntity> searchText(String parameter, boolean caseInsensitive) {
		return this.dbSearchDAO.searchInPathAndText(parameter, caseInsensitive);
	}

	public DBFileVersion getFileVersionByPath(String path, int version) {
		return this.dbFileVersionDAO.getFileVersionByPath(path, version);
	}

	public List<DBFileVersion> getFileVersionsByPath(String path) {
		return this.dbFileVersionDAO.getFileVersionsByPath(path);
	}

	public List<IResourceVersion> getResourceVersionsByPath(String path) {
		List<IResourceVersion> resultList = new ArrayList<IResourceVersion>();
		List<DBFileVersion> fileVersions = getFileVersionsByPath(path);
		for (Iterator<DBFileVersion> iterator = fileVersions.iterator(); iterator
				.hasNext();) {
			DBFileVersion dbFileVersion = iterator.next();
			resultList.add(new DBResourceVersion(getRepository(),
					new DBRepositoryPath(path), dbFileVersion.getVersion()));
		}
		return resultList;
	}

	public void renameFolder(String path, String newPath) {
		this.dbFolderDAO.renameFolderByPath(path, newPath);
	}
	
	public void renameFile(String path, String newPath) {
		this.dbFileDAO.renameFileByPath(path, newPath);
	}

//	public void renameFileByPath(String path, String newPath)
//			throws DBBaseException {
//
//		// resourcesCache.remove(path);
//
//		renameDocument(getFileByPath(path), newPath);
//
//		String script = getRepository().getDbUtils().readScript(
//				"sql/rename_file_by_path.sql");
//
//		Connection connection = null;
//		PreparedStatement preparedStatement = null;
//		try {
//			connection = getRepository().getDbUtils().getConnection();
//			preparedStatement = getRepository().getDbUtils()
//					.getPreparedStatement(connection, script);
//			preparedStatement.setString(1, newPath);
//			preparedStatement.setString(2, path);
//			preparedStatement.executeUpdate();
//		} catch (SQLException e) {
//			throw new DBBaseException(e);
//		} finally {
//			getRepository().getDbUtils().closeStatement(preparedStatement);
//			getRepository().getDbUtils().closeConnection(connection);
//		}
//
//	}
//
//	private void renameDocument(DBFile resource, String newPath)
//			throws DBBaseException {
//
//		String script = getRepository().getDbUtils().readScript(
//				"sql/rename_document.sql");
//
//		Connection connection = null;
//		PreparedStatement preparedStatement = null;
//		try {
//			connection = getRepository().getDbUtils().getConnection();
//			preparedStatement = getRepository().getDbUtils()
//					.getPreparedStatement(connection, script);
//			preparedStatement.setString(1, newPath);
//			preparedStatement.setString(2, resource.getPath());
//			preparedStatement.executeUpdate();
//		} catch (SQLException e) {
//			throw new DBBaseException(e);
//		} finally {
//			getRepository().getDbUtils().closeStatement(preparedStatement);
//			getRepository().getDbUtils().closeConnection(connection);
//		}
//	}
}
