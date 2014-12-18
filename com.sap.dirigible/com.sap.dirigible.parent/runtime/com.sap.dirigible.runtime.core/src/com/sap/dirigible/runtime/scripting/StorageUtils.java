package com.sap.dirigible.runtime.scripting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.dirigible.repository.db.dao.DBMapper;
import com.sap.dirigible.runtime.logger.Logger;

public class StorageUtils extends AbstractStorageUtils {

	private static final Logger logger = Logger.getLogger(StorageUtils.class);

	private static final String DGB_STORAGE = "DGB_STORAGE";
	private static final String STORAGE_PATH = "STORAGE_PATH";
	private static final String STORAGE_DATA = "STORAGE_DATA";
	private static final String STORAGE_TIMESTAMP = "STORAGE_TIMESTAMP";

	private static final String INSERT_INTO_DGB_STORAGE = "INSERT INTO " + DGB_STORAGE + " ("
			+ STORAGE_PATH + ", " + STORAGE_DATA + ", " + STORAGE_TIMESTAMP + ")"
			+ "VALUES (?,?,?)";

	private static final String UPDATE_DGB_STORAGE = "UPDATE " + DGB_STORAGE + " SET "
			+ STORAGE_PATH + " = ?, " + STORAGE_DATA + " = ?, " + STORAGE_TIMESTAMP + " = ?";

	private static final String DELETE_DGB_STORAGE = "DELETE FROM " + DGB_STORAGE;

	private static final String DELETE_DGB_STORAGE_PATH = "DELETE FROM " + DGB_STORAGE + " WHERE "
			+ STORAGE_PATH + " = ?";

	private static final String CREATE_TABLE_DGB_STORAGE = "CREATE TABLE " + DGB_STORAGE + " ("
			+ STORAGE_PATH + " VARCHAR(2048) PRIMARY KEY, " + STORAGE_DATA + " BLOB, "
			+ STORAGE_TIMESTAMP + " TIMESTAMP" + " )";

	private static final String SELECT_COUNT_FROM_DGB_STORAGE = "SELECT COUNT(*) FROM "
			+ DGB_STORAGE;

	private static final String SELECT_DGB_STORAGE = "SELECT * FROM " + DGB_STORAGE + " WHERE "
			+ STORAGE_PATH + " = ?";

	private static final String SELECT_DGB_STORAGE_EXISTS = "SELECT " + STORAGE_PATH + " FROM "
			+ DGB_STORAGE + " WHERE " + STORAGE_PATH + " = ?";

	public StorageUtils(DataSource dataSource) {
		super(dataSource);
	}

	private void checkDB() throws NamingException, SQLException {
		super.checkDB(SELECT_COUNT_FROM_DGB_STORAGE, CREATE_TABLE_DGB_STORAGE);
	}

	public boolean exists(String path) throws SQLException {
		return super.exists(path, SELECT_DGB_STORAGE_EXISTS, SELECT_COUNT_FROM_DGB_STORAGE,
				CREATE_TABLE_DGB_STORAGE);
	}

	public void clear() throws SQLException {
		super.clear(DELETE_DGB_STORAGE, SELECT_COUNT_FROM_DGB_STORAGE, CREATE_TABLE_DGB_STORAGE);
	}

	public void delete(String path) throws SQLException {
		super.delete(path, DELETE_DGB_STORAGE_PATH, SELECT_COUNT_FROM_DGB_STORAGE,
				CREATE_TABLE_DGB_STORAGE);
	}

	public void put(String path, byte[] data) throws SQLException {
		checkMaxSize(data);
		try {
			checkDB();

			if (exists(path)) {
				update(path, data);
			} else {
				insert(path, data);
			}

		} catch (NamingException e) {
			throw new SQLException(e);
		}
	}

	private byte[] checkMaxSize(byte[] data) {
		if (data.length > MAX_STORAGE_FILE_SIZE_IN_BYTES) {
			logger.warn(TOO_BIG_DATA_MESSAGE);
			throw new InvalidParameterException(TOO_BIG_DATA_MESSAGE);
		}
		return data;
	}

	private void insert(String path, byte[] data) throws SQLException {
		DataSource dataSource = this.dataSource;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			PreparedStatement pstmt = connection.prepareStatement(INSERT_INTO_DGB_STORAGE);

			int i = 0;
			pstmt.setString(++i, path);
			pstmt.setBinaryStream(++i, new ByteArrayInputStream(data), data.length);
			pstmt.setTimestamp(++i, new Timestamp(GregorianCalendar.getInstance().getTime().getTime()));

			pstmt.executeUpdate();

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private void update(String path, byte[] data) throws SQLException {
		DataSource dataSource = this.dataSource;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			PreparedStatement pstmt = connection.prepareStatement(UPDATE_DGB_STORAGE);

			int i = 0;
			pstmt.setString(++i, path);
			pstmt.setBinaryStream(++i, new ByteArrayInputStream(data), data.length);
			pstmt.setTimestamp(++i, new Timestamp(GregorianCalendar.getInstance().getTime().getTime()));

			pstmt.executeUpdate();

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	// Retrieve photo data from the cache
	public byte[] get(String path) throws SQLException, IOException {
		try {
			checkDB();

			DataSource dataSource = this.dataSource;
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(SELECT_DGB_STORAGE);
				pstmt.setString(1, path);

				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					return DBMapper.dbToDataBinary(connection, rs, STORAGE_DATA);
				}

			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (NamingException e) {
			throw new SQLException(e);
		}
		return null;
	}
}