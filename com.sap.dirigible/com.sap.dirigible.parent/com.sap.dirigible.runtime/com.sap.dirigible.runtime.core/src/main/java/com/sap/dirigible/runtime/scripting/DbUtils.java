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

package com.sap.dirigible.runtime.scripting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.sap.dirigible.repository.db.DBUtils;
import com.sap.dirigible.repository.db.dialect.IDialectSpecifier;

/**
 * Utilities for Services using Database
 * 
 */
public class DbUtils {

	private static final String DELETE_FROM = "DELETE FROM "; //$NON-NLS-1$
	private static final String VALUES = " VALUES (?, ?)"; //$NON-NLS-1$
	private static final String INSERT_INTO = "INSERT INTO "; //$NON-NLS-1$
	private static final String SET_SEQ_VALUE = " SET SEQ_VALUE=? "; //$NON-NLS-1$
	private static final String UPDATE = "UPDATE "; //$NON-NLS-1$
	private static final String SEQ_VALUE = "SEQ_VALUE"; //$NON-NLS-1$
	private static final String WHERE_SEQ_NAME = " WHERE SEQ_NAME=?"; //$NON-NLS-1$
	private static final String SELECT_FROM = "SELECT * FROM "; //$NON-NLS-1$
	private static final String SEQ_NAME_VARCHAR_128_SEQ_VALUE_INTEGER = " (SEQ_NAME VARCHAR(128) NOT NULL PRIMARY KEY, SEQ_VALUE INTEGER)"; //$NON-NLS-1$
	private static final String CREATE_TABLE = "CREATE TABLE "; //$NON-NLS-1$
	private static final String DGB_SEQUENCES = "DGB_SEQUENCES"; //$NON-NLS-1$
	private static final String SELECT_COUNT_FROM = "SELECT COUNT(*) FROM "; //$NON-NLS-1$
	private DataSource dataSource;

	public DbUtils(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int getNext(String sequenceName) throws SQLException {
		Connection connection = dataSource.getConnection();
		try {
			checkSequenceTable(connection);
			int value = increaseSequence(connection, sequenceName);
			if (value == -1) {
				value = increaseSequence(connection, sequenceName);
			}
			return value;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	public int createSequence(String sequenceName, int start)
			throws SQLException {
		Connection connection = dataSource.getConnection();
		try {
			checkSequenceTable(connection);
			insertSequence(connection, sequenceName, start);
			return 0;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	public int dropSequence(String sequenceName) throws SQLException {
		Connection connection = dataSource.getConnection();
		try {
			checkSequenceTable(connection);
			deleteSequence(connection, sequenceName);
			return 0;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	public boolean existSequence(String sequenceName) throws SQLException {
		Connection connection = dataSource.getConnection();
		try {
			checkSequenceTable(connection);
			boolean exists = selectSequence(connection, sequenceName);
			return exists;
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private void checkSequenceTable(Connection connection) throws SQLException {
		String sql = SELECT_COUNT_FROM + DGB_SEQUENCES;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeQuery();
		} catch (Exception e) {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			sql = CREATE_TABLE + DGB_SEQUENCES
					+ SEQ_NAME_VARCHAR_128_SEQ_VALUE_INTEGER;
			preparedStatement = connection.prepareStatement(sql);
			try {
				preparedStatement.executeUpdate();
			} finally {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			}
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}

	}

	private int increaseSequence(Connection connection, String sequenceName)
			throws SQLException {
		// TODO select for update...
		String sql = SELECT_FROM + DGB_SEQUENCES + WHERE_SEQ_NAME;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, sequenceName);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int value = resultSet.getInt(SEQ_VALUE);
				value++;
				updateSequence(connection, sequenceName, value);
				return value;
			}
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
		insertSequence(connection, sequenceName, 0);

		return -1;
	}

	private void updateSequence(Connection connection, String sequenceName,
			int value) throws SQLException {

		PreparedStatement preparedStatement = null;
		try {
			String sql = UPDATE + DGB_SEQUENCES + SET_SEQ_VALUE
					+ WHERE_SEQ_NAME;
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, value);
			preparedStatement.setString(2, sequenceName);
			preparedStatement.executeUpdate();
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private void insertSequence(Connection connection, String sequenceName,
			int start) throws SQLException {
		PreparedStatement preparedStatement = null;
		try {
			String sql = INSERT_INTO + DGB_SEQUENCES + VALUES;
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, sequenceName);
			preparedStatement.setInt(2, start);
			preparedStatement.executeUpdate();
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private void deleteSequence(Connection connection, String sequenceName)
			throws SQLException {
		PreparedStatement preparedStatement = null;
		try {
			String sql = DELETE_FROM + DGB_SEQUENCES + WHERE_SEQ_NAME;
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, sequenceName);
			preparedStatement.executeUpdate();
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private boolean selectSequence(Connection connection, String sequenceName)
			throws SQLException {
		String sql = SELECT_FROM + DGB_SEQUENCES + WHERE_SEQ_NAME;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, sequenceName);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				return true;
			}
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}

		return false;
	}
	
	public String createLimitAndOffset(String limit, String offset) throws SQLException {
		return createLimitAndOffset(
				NumberUtils.toInt(StringEscapeUtils.escapeSql(limit)), 
				NumberUtils.toInt(StringEscapeUtils.escapeSql(offset)));
	}
	
	public String createLimitAndOffset(int limit, String offset) throws SQLException {
		return createLimitAndOffset(limit, 
				NumberUtils.toInt(StringEscapeUtils.escapeSql(offset)));
	}
	
	public String createLimitAndOffset(String limit, int offset) throws SQLException {
		return createLimitAndOffset(
				NumberUtils.toInt(StringEscapeUtils.escapeSql(limit)), 
				offset);
	}
	
	public String createLimitAndOffset(int limit, int offset) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			String productName = connection.getMetaData().getDatabaseProductName();
			IDialectSpecifier dialectSpecifier = DBUtils.getDialectSpecifier(productName);
			return dialectSpecifier.createLimitAndOffset(limit, offset);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	public String createTopAndStart(int limit, int offset) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			String productName = connection.getMetaData().getDatabaseProductName();
			IDialectSpecifier dialectSpecifier = DBUtils.getDialectSpecifier(productName);
			return dialectSpecifier.createTopAndStart(limit, offset);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

}
