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

package com.sap.dirigible.repository.db.dialect;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.dirigible.repository.db.DBSupportedTypesMap;

public class PostgreSQLDBSpecifier implements IDialectSpecifier {
	
	private static final String LIMIT_D_OFFSET_D = "LIMIT %d OFFSET %d";  //$NON-NLS-1$
	
	private static final String POSTGRESQL_TIMESTAMP = "TIMESTAMP"; //$NON-NLS-1$
	private static final String POSTGRESQL_FLOAT = "REAL"; //$NON-NLS-1$
	private static final String POSTGRESQL_BLOB = "BYTEA"; //$NON-NLS-1$
	private static final String POSTGRESQL_CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP"; //$NON-NLS-1$

	@Override
	public String createLimitAndOffset(int limit, int offset) {
		return String.format(LIMIT_D_OFFSET_D, limit, offset);
	}
	
	@Override
	public String specify(String sql) {
		sql = sql.replace(DIALECT_CURRENT_TIMESTAMP, POSTGRESQL_CURRENT_TIMESTAMP);
		sql = sql.replace(DIALECT_TIMESTAMP, POSTGRESQL_TIMESTAMP);
		sql = sql.replace(DIALECT_BLOB, POSTGRESQL_BLOB);
		return sql;
	}

	@Override
	public String getSpecificType(String commonType) {
		if (DBSupportedTypesMap.FLOAT.equals(commonType)) {
			return POSTGRESQL_FLOAT;
		}
		return commonType;
	}

	@Override
	public String createTopAndStart(int limit, int offset) {
		return "";  //$NON-NLS-1$
	}

	@Override
	public boolean isSchemaFilterSupported() {
		return false;
	}

	@Override
	public String getSchemaFilterScript() {
		return null;
	}

	@Override
	public String getAlterAddOpen() {
		return " ADD COLUMN ";
	}

	@Override
	public String getAlterAddClose() {
		return "";
	}

	@Override
	public InputStream getBinaryStream(ResultSet resultSet, String columnName) throws SQLException {
		return new ByteArrayInputStream(resultSet.getBytes(columnName));
	}

}
