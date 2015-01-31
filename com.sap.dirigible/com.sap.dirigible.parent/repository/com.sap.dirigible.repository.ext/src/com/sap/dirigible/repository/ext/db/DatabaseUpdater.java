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

package com.sap.dirigible.repository.ext.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.ICommonConstants;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.ext.db.dialect.IDialectSpecifier;

public class DatabaseUpdater extends AbstractDataUpdater {

	private static final String DASH = " - "; //$NON-NLS-1$
	private static final String AUTOMATIC_DROP_COLUMN_NOT_SUPPORTED = Messages.getString("DatabaseUpdater.AUTOMATIC_DROP_COLUMN_NOT_SUPPORTED"); //$NON-NLS-1$
	private static final String AS = " AS "; //$NON-NLS-1$
	private static final String CREATE_VIEW = "CREATE VIEW "; //$NON-NLS-1$
	private static final String DROP_VIEW = "DROP VIEW "; //$NON-NLS-1$
	private static final String QUERY = "query"; //$NON-NLS-1$
	private static final String CANNOT_BE_CHANGED_TO = Messages.getString("DatabaseUpdater.CANNOT_BE_CHANGED_TO"); //$NON-NLS-1$
	private static final String TYPE2 = Messages.getString("DatabaseUpdater.TYPE2"); //$NON-NLS-1$
	private static final String ADDING_PRIMARY_KEY_COLUMN = Messages.getString("DatabaseUpdater.ADDING_PRIMARY_KEY_COLUMN"); //$NON-NLS-1$
	private static final String ADDING_NOT_NULL_COLUMN = Messages.getString("DatabaseUpdater.ADDING_NOT_NULL_COLUMN"); //$NON-NLS-1$
	private static final String AND_COLUMN = Messages.getString("DatabaseUpdater.AND_COLUMN"); //$NON-NLS-1$
	private static final String INCOMPATIBLE_CHANGE_OF_TABLE = Messages.getString("DatabaseUpdater.INCOMPATIBLE_CHANGE_OF_TABLE"); //$NON-NLS-1$
	private static final String ADD = "ADD "; //$NON-NLS-1$
	private static final String ALTER_TABLE = "ALTER TABLE "; //$NON-NLS-1$
	private static final String COLUMN_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$
	private static final String COLUMN_PRIMARY_KEY = "primaryKey"; //$NON-NLS-1$
	private static final String COLUMN_NOT_NULL = "notNull"; //$NON-NLS-1$
	private static final String COLUMN_LENGTH = "length"; //$NON-NLS-1$
	private static final String COLUMN_TYPE = "type"; //$NON-NLS-1$
	private static final String COLUMN_NAME = "name"; //$NON-NLS-1$
	private static final String COLUMNS = "columns"; //$NON-NLS-1$
	private static final String CREATE_TABLE = "CREATE TABLE "; //$NON-NLS-1$
	private static final String VIEW_NAME = "viewName"; //$NON-NLS-1$
	private static final String VIEW = "VIEW"; //$NON-NLS-1$
	private static final String TABLE = "TABLE"; //$NON-NLS-1$
	private static final String TABLE_NAME = "tableName"; //$NON-NLS-1$
	private static final String DEFAULT = "DEFAULT "; //$NON-NLS-1$
	private static final String PRIMARY_KEY = "PRIMARY KEY "; //$NON-NLS-1$
	private static final String NOT_NULL = "NOT NULL "; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(DatabaseUpdater.class);

	public static final String EXTENSION_TABLE = ".table"; //$NON-NLS-1$
	public static final String EXTENSION_VIEW = ".view"; //$NON-NLS-1$
	public static final String REGISTRY_DATA_STRUCTURES_DEFAULT = ICommonConstants.DATA_CONTENT_REGISTRY_PUBLISH_LOCATION;

	private IRepository repository;
	private DataSource dataSource;
	private String location;
	private DBUtils dbUtils;

	public DatabaseUpdater(IRepository repository, DataSource dataSource,
			String location) {
		this.repository = repository;
		this.dataSource = dataSource;
		this.location = location;
		this.dbUtils = new DBUtils(dataSource);
	}

	@Override
	public void executeUpdate(List<String> knownFiles) throws Exception {
		if (knownFiles.size() == 0) {
			return;
		}

		try {
			Connection connection = dataSource.getConnection();
			DatabaseMetaData databaseMetaData = getMetaData(connection);
			String productName = connection.getMetaData().getDatabaseProductName();
			IDialectSpecifier dialectSpecifier = DBUtils.getDialectSpecifier(productName);
			
			try {
				for (Iterator<String> iterator = knownFiles.iterator(); iterator
						.hasNext();) {
					String dsDefinition = iterator.next();
					if (dsDefinition.endsWith(EXTENSION_TABLE)) {
						executeTableUpdate(connection, databaseMetaData, dialectSpecifier, dsDefinition);
					} else if (dsDefinition.endsWith(EXTENSION_VIEW)) {
						executeViewUpdate(connection, databaseMetaData, dialectSpecifier, dsDefinition);
					}
				}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
//			throw new Exception(e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
//			throw new Exception(e);
		}
	}
	
	@Override
	public void executeUpdate(List<String> knownFiles, HttpServletRequest request) throws Exception {
		executeUpdate(knownFiles);
	}

	private void executeTableUpdate(Connection connection, DatabaseMetaData databaseMetaData, IDialectSpecifier dialectSpecifier, String dsDefinition)
			throws SQLException, IOException {
		JsonObject dsDefinitionObject = parseTable(dsDefinition);
		String tableName = dsDefinitionObject.get(TABLE_NAME).getAsString();
		tableName = tableName.toUpperCase();
		ResultSet rs = null;
		try {
			rs = databaseMetaData.getTables(null, null, tableName,
					new String[] { TABLE, VIEW });
			if (rs.next()) {
				// String retrievedTableName = rs.getString(3);
				executeTableUpdate(connection, dialectSpecifier, dsDefinitionObject);
			} else {
				executeTableCreate(connection, dialectSpecifier, dsDefinitionObject);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
	}

	private DatabaseMetaData getMetaData(Connection connection)
			throws SQLException {
		// get the database metadata
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		return databaseMetaData;
	}

	private void executeViewUpdate(Connection connection, DatabaseMetaData databaseMetaData, IDialectSpecifier dialectSpecifier, String dsDefinition)
			throws SQLException, IOException {
		JsonObject dsDefinitionObject = parseView(dsDefinition);
		String viewName = dsDefinitionObject.get(VIEW_NAME).getAsString();
		viewName = viewName.toUpperCase();
		executeViewCreateOrReplace(connection, dsDefinitionObject);
	}

	private void executeTableCreate(Connection connection, IDialectSpecifier dialectSpecifier,
			JsonObject dsDefinitionObject) throws SQLException {
		StringBuilder sql = new StringBuilder();
		String tableName = dsDefinitionObject.get(TABLE_NAME).getAsString();

		sql.append(CREATE_TABLE + tableName + " ("); //$NON-NLS-1$

		JsonArray columns = dsDefinitionObject.get(COLUMNS).getAsJsonArray();
		int i = 0;
		for (JsonElement jsonElement : columns) {
			if (jsonElement instanceof JsonObject) {
				if (i > 0 && i < columns.size()) {
					sql.append(", "); //$NON-NLS-1$
				}
				JsonObject jsonObject = (JsonObject) jsonElement;
				String name = jsonObject.get(COLUMN_NAME).getAsString();
				String type = dbUtils.specifyDataType(connection, jsonObject
						.get(COLUMN_TYPE).getAsString());
				String length = jsonObject.get(COLUMN_LENGTH).getAsString();
				boolean notNull = jsonObject.get(COLUMN_NOT_NULL)
						.getAsBoolean();
				boolean primaryKey = jsonObject.get(COLUMN_PRIMARY_KEY)
						.getAsBoolean();
				String defaultValue = jsonObject.get(COLUMN_DEFAULT_VALUE)
						.getAsString();

				sql.append(name + " " + type); //$NON-NLS-1$
				if (DBSupportedTypesMap.VARCHAR.equals(type)
						|| DBSupportedTypesMap.CHAR.equals(type)) {
					sql.append("(" + length + ") "); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sql.append(" "); //$NON-NLS-1$
				}
				if (notNull) {
					sql.append(NOT_NULL);
				}
				if (primaryKey) {
					sql.append(PRIMARY_KEY);
				}
				if (defaultValue != null && !"".equals(defaultValue)) { //$NON-NLS-1$
					sql.append(DEFAULT + defaultValue + " "); //$NON-NLS-1$
				}
			}
			i++;
		}

		sql.append(")"); //$NON-NLS-1$
		String sqlExpression = sql.toString();
		try {
			logger.info(sqlExpression);
			executeUpdateSQL(connection, sqlExpression);
		} catch (SQLException e) {
			logger.error(sqlExpression);
			throw new SQLException(sqlExpression, e);
		}
	}

	private void executeUpdateSQL(Connection connection, String sql)
			throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.executeUpdate();
	}

	private void executeTableUpdate(Connection connection, IDialectSpecifier dialectSpecifier,
			JsonObject dsDefinitionObject) throws SQLException {
		StringBuilder sql = new StringBuilder();
		String tableName = dsDefinitionObject.get(TABLE_NAME).getAsString()
				.toUpperCase();

		Map<String, String> columnDefinitions = new HashMap<String, String>();
		ResultSet rsColumns = connection.getMetaData().getColumns(null, null,
				tableName, "%"); //$NON-NLS-1$
		while (rsColumns.next()) {
			String typeName = DBSupportedTypesMap.getTypeName(rsColumns
					.getInt(5));
			columnDefinitions.put(rsColumns.getString(4).toUpperCase(),
					typeName);
		}

		sql.append(ALTER_TABLE + tableName + " "); //$NON-NLS-1$

		JsonArray columns = dsDefinitionObject.get(COLUMNS).getAsJsonArray();
		int i = 0;
		StringBuffer addSql = new StringBuffer();
		addSql.append(dialectSpecifier.getAlterAddOpen());
		
		for (JsonElement jsonElement : columns) {

			if (jsonElement instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) jsonElement;
				String name = jsonObject.get(COLUMN_NAME).getAsString()
						.toUpperCase();
				String type = dbUtils.specifyDataType(connection, jsonObject
						.get(COLUMN_TYPE).getAsString().toUpperCase());
				String length = jsonObject.get(COLUMN_LENGTH).getAsString();
				boolean notNull = jsonObject.get(COLUMN_NOT_NULL)
						.getAsBoolean();
				boolean primaryKey = jsonObject.get(COLUMN_PRIMARY_KEY)
						.getAsBoolean();
				String defaultValue = jsonObject.get(COLUMN_DEFAULT_VALUE)
						.getAsString();

				if (!columnDefinitions.containsKey(name)) {
					if (i > 0) {
						addSql.append(", "); //$NON-NLS-1$
					}
					addSql.append(name + " " + type); //$NON-NLS-1$
					if (DBSupportedTypesMap.VARCHAR.equals(type)
							|| DBSupportedTypesMap.CHAR.equals(type)) {
						addSql.append("(" + length + ") "); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						addSql.append(" "); //$NON-NLS-1$
					}
					if (notNull) {
						// sql.append("NOT NULL ");
						throw new SQLException(INCOMPATIBLE_CHANGE_OF_TABLE
								+ tableName + AND_COLUMN + name
								+ ADDING_NOT_NULL_COLUMN);
					}
					if (primaryKey) {
						// sql.append("PRIMARY KEY ");
						throw new SQLException(INCOMPATIBLE_CHANGE_OF_TABLE
								+ tableName + AND_COLUMN + name
								+ ADDING_PRIMARY_KEY_COLUMN);
					}
					if (defaultValue != null && !"".equals(defaultValue)) { //$NON-NLS-1$
						sql.append(DEFAULT + defaultValue + " "); //$NON-NLS-1$
					}
					i++;
				} else if (!columnDefinitions.get(name).equals(type)) {
					throw new SQLException(INCOMPATIBLE_CHANGE_OF_TABLE
							+ tableName + AND_COLUMN + name + TYPE2
							+ columnDefinitions.get(name)
							+ CANNOT_BE_CHANGED_TO + type);
				}
			}

		}
		
		// TODO Derby does not support multiple ADD in a single statement!
		
		
		if (i > 0) {
			addSql.append(dialectSpecifier.getAlterAddClose());
			sql.append(addSql.toString());
		}
		
		if (columnDefinitions.size() > columns.size()) {
			throw new SQLException(INCOMPATIBLE_CHANGE_OF_TABLE
					+ tableName + DASH + AUTOMATIC_DROP_COLUMN_NOT_SUPPORTED);
		}
		
		if (i > 0) {
			String sqlExpression = sql.toString();
			try {
				logger.info(sqlExpression);
				executeUpdateSQL(connection, sqlExpression);
			} catch (SQLException e) {
				logger.error(sqlExpression);
				throw new SQLException(sqlExpression, e);
			}
		}
	}

	private JsonObject parseTable(String dsDefinition) throws IOException {
		// {
		// "tableName":"table_name",
		// "columns":
		// [
		// {
		// "name":"id",
		// "type":"INTEGER",
		// "length":"0",
		// "notNull":"true",
		// "primaryKey":"true"
		// },
		// {
		// "name":"text",
		// "type":"VARCHAR",
		// "length":"32",
		// "notNull":"false",
		// "primaryKey":"false"
		// },
		// ]
		// }
		IRepository repository = this.repository;
		IResource resource = repository.getResource(this.location
				+ dsDefinition);
		String content = new String(resource.getContent());
		JsonParser parser = new JsonParser();
		JsonObject dsDefinitionObject = (JsonObject) parser.parse(content);

		// TODO validate the parsed content has the right structure

		return dsDefinitionObject;
	}

	private void executeViewCreateOrReplace(Connection connection,
			JsonObject dsDefinitionObject) throws SQLException {
		StringBuilder sql = new StringBuilder();
		String viewName = dsDefinitionObject.get(VIEW_NAME).getAsString()
				.toUpperCase();
		String query = dsDefinitionObject.get(QUERY).getAsString();

		String sqlExpression = null;
		ResultSet rs = connection.getMetaData().getTables(null, null, viewName,
				new String[] { TABLE, VIEW });
		if (rs.next()) {
			sql.append(DROP_VIEW + viewName);
			sqlExpression = sql.toString();
			try {
				logger.info(sqlExpression);
				executeUpdateSQL(connection, sqlExpression);
			} catch (SQLException e) {
				logger.error(sqlExpression);
				logger.error(e.getMessage(), e);
			}
		}

		sql = new StringBuilder();
		sql.append(CREATE_VIEW + viewName + AS + query);

		sqlExpression = sql.toString();
		try {
			logger.info(sqlExpression);
			executeUpdateSQL(connection, sqlExpression);
		} catch (SQLException e) {
			logger.error(sqlExpression);
			throw new SQLException(sqlExpression, e);
		}
	}

	private JsonObject parseView(String dsDefinition) throws IOException {
		// {
		// "viewName":"view_name",
		// "query":"SELECT * FROM table_name"
		// }
		IRepository repository = this.repository;
		IResource resource = repository.getResource(this.location
				+ dsDefinition);
		String content = new String(resource.getContent());
		JsonParser parser = new JsonParser();
		JsonObject dsDefinitionObject = (JsonObject) parser.parse(content);

		// TODO validate the parsed content has the right structure

		return dsDefinitionObject;
	}

	@Override
	public void enumerateKnownFiles(ICollection collection,
			List<String> dsDefinitions) throws IOException {
		if (collection.exists()) {
			List<IResource> resources = collection.getResources();
			for (Iterator<IResource> iterator = resources.iterator(); iterator
					.hasNext();) {
				IResource resource = iterator.next();
				if (resource != null && resource.getName() != null) {
					if (resource.getName().endsWith(EXTENSION_TABLE)
							|| resource.getName().endsWith(EXTENSION_VIEW)) {
						String fullPath = collection.getPath().substring(
								this.location.length())
								+ IRepository.SEPARATOR + resource.getName();
						dsDefinitions.add(fullPath);
					}
				}
			}

			List<ICollection> collections = collection.getCollections();
			for (Iterator<ICollection> iterator = collections.iterator(); iterator
					.hasNext();) {
				ICollection subCollection = iterator.next();
				enumerateKnownFiles(subCollection, dsDefinitions);
			}
		}
	}

	@Override
	public IRepository getRepository() {
		return this.repository;
	}

	@Override
	public String getLocation() {
		return this.location;
	}
}
