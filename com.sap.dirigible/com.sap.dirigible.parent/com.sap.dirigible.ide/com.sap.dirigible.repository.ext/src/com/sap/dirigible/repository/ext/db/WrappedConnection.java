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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrappedConnection implements Connection {

	private static final Logger logger = LoggerFactory.getLogger(WrappedConnection.class);

	private Connection originalConnection;

	private long timeAcquired;

	private WrappedDataSource dataSource;

	public WrappedConnection(Connection originalConnection, WrappedDataSource dataSource) {
		super();
		this.originalConnection = originalConnection;
		this.timeAcquired = System.currentTimeMillis();
		this.dataSource = dataSource;
	}

	public long getTimeAcquired() {
		logger.debug("called - getTimeAcquired(): " + timeAcquired);
		return timeAcquired;
	}

	public long getTimeUsed() {
		long timeUsed = System.currentTimeMillis() - timeAcquired;
		logger.debug("called - getTimeUsed(): " + timeUsed);
		return timeUsed;
	}

	public void clearWarnings() throws SQLException {
		logger.debug("entering - clearWarnings()");
		originalConnection.clearWarnings();
		logger.debug("exiting - clearWarnings()");
	}

	public void close() throws SQLException {
		logger.debug("entering - close()");
		originalConnection.close();
		dataSource.closedConnection(this);
		logger.debug("exiting - close()");
	}

	public void commit() throws SQLException {
		logger.debug("entering - commit()");
		originalConnection.commit();
		logger.debug("exiting - commit()");
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		logger.debug("called - createArrayOf(String typeName, Object[] elements)");
		return originalConnection.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		logger.debug("called - createBlob()");
		return originalConnection.createBlob();
	}

	public Clob createClob() throws SQLException {
		logger.debug("called - createClob()");
		return originalConnection.createClob();
	}

	public NClob createNClob() throws SQLException {
		logger.debug("called - createNClob()");
		return originalConnection.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		logger.debug("called - createSQLXML()");
		return originalConnection.createSQLXML();
	}

	public Statement createStatement() throws SQLException {
		logger.debug("called - createStatement()");
		return originalConnection.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		logger.debug("called - createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)");
		return originalConnection.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		logger.debug("called - createStatement(int resultSetType, int resultSetConcurrency)");
		return originalConnection.createStatement(resultSetType, resultSetConcurrency);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		logger.debug("called - createStatement(String typeName, Object[] attributes)");
		return originalConnection.createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException {
		logger.debug("called - getAutoCommit()");
		return originalConnection.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		logger.debug("called - getCatalog()");
		return originalConnection.getCatalog();
	}

	public Properties getClientInfo() throws SQLException {
		logger.debug("called - getClientInfo()");
		return originalConnection.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		logger.debug("called - getClientInfo(String name)");
		return originalConnection.getClientInfo(name);
	}

	public int getHoldability() throws SQLException {
		logger.debug("called - getHoldability()");
		return originalConnection.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		logger.debug("called - getMetaData()");
		return originalConnection.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		logger.debug("called - getTransactionIsolation()");
		return originalConnection.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		logger.debug("called - getTypeMap()");
		return originalConnection.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		logger.debug("called - getWarnings()");
		return originalConnection.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		logger.debug("called - isClosed()");
		return originalConnection.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		logger.debug("called - isReadOnly()");
		return originalConnection.isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException {
		logger.debug("called - isValid(int timeout)");
		return originalConnection.isValid(timeout);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		logger.debug("called - isWrapperFor(Class<?> iface)");
		return originalConnection.isWrapperFor(iface);
	}

	public String nativeSQL(String sql) throws SQLException {
		logger.debug("called - nativeSQL(String sql): " + sql);
		return originalConnection.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		logger.debug("called - prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability): "
				+ sql);
		return originalConnection.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		logger.debug("called - prepareCall(String sql, int resultSetType, int resultSetConcurrency): "
				+ sql);
		return originalConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		logger.debug("called - prepareCall(String sql): " + sql);
		return originalConnection.prepareCall(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		logger.debug("called - prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability): "
				+ sql);
		return originalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		logger.debug("called - prepareStatement(String sql, int resultSetType, int resultSetConcurrency): "
				+ sql);
		return originalConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		logger.debug("called - prepareStatement(String sql, int autoGeneratedKeys): " + sql);
		return originalConnection.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		logger.debug("called - prepareStatement(String sql, int[] columnIndexes): " + sql);
		return originalConnection.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		logger.debug("called - prepareStatement(String sql, String[] columnNames): " + sql);
		return originalConnection.prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		logger.debug("called - prepareStatement(String sql): " + sql);
		return originalConnection.prepareStatement(sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		logger.debug("called - releaseSavepoint(Savepoint savepoint)");
		originalConnection.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		logger.debug("called - rollback()");
		originalConnection.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		logger.debug("called - rollback(Savepoint savepoint)");
		originalConnection.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		logger.debug("called - setAutoCommit(boolean autoCommit)");
		originalConnection.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		logger.debug("called - setCatalog(String catalog)");
		originalConnection.setCatalog(catalog);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		logger.debug("called - setClientInfo(Properties properties)");
		originalConnection.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		logger.debug("called - setClientInfo(String name, String value)");
		originalConnection.setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException {
		logger.debug("called - setHoldability(int holdability)");
		originalConnection.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		logger.debug("called - setReadOnly(boolean readOnly)");
		originalConnection.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		logger.debug("called - setSavepoint()");
		return originalConnection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		logger.debug("called - setSavepoint(String name)");
		return originalConnection.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		logger.debug("called - setTransactionIsolation(int level): " + level);
		originalConnection.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		logger.debug("called - setTypeMap(Map<String, Class<?>> map)");
		originalConnection.setTypeMap(map);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		logger.debug("called - unwrap(Class<T> iface)");
		return originalConnection.unwrap(iface);
	}

	public void abort(Executor arg0) throws SQLException {
	}

	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	public String getSchema() throws SQLException {
		return null;
	}

	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
	}

	public void setSchema(String arg0) throws SQLException {
	}

}
