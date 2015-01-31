package com.sap.dirigible.repository.ext.extensions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.ext.db.DBUtils;
import com.sap.dirigible.repository.ext.security.Messages;
import com.sap.dirigible.repository.ext.utils.RequestUtils;

public class ExtensionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ExtensionManager.class);
	
	private static final String DATABASE_ERROR = Messages.getString("SecurityManager.DATABASE_ERROR"); //$NON-NLS-1$
	
	private static final String GET_EXTENSIONS = 						"/com/sap/dirigible/repository/ext/extensions/sql/get_extensions.sql"; //$NON-NLS-1$
	
	private static final String GET_EXTENSION = 						"/com/sap/dirigible/repository/ext/extensions/sql/get_extension.sql"; //$NON-NLS-1$
	
	private static final String GET_EXTENSION_POINTS = 					"/com/sap/dirigible/repository/ext/extensions/sql/get_extension_points.sql"; //$NON-NLS-1$
	
	private static final String GET_EXTENSION_POINT = 					"/com/sap/dirigible/repository/ext/extensions/sql/get_extension_point.sql"; //$NON-NLS-1$
	
	private static final String INSERT_EXTENSION = 						"/com/sap/dirigible/repository/ext/extensions/sql/insert_extension.sql"; //$NON-NLS-1$
	
	private static final String UPDATE_EXTENSION = 						"/com/sap/dirigible/repository/ext/extensions/sql/update_extension.sql"; //$NON-NLS-1$
	
	private static final String INSERT_EXTENSION_POINT = 				"/com/sap/dirigible/repository/ext/extensions/sql/insert_extension_point.sql"; //$NON-NLS-1$
	
	private static final String UPDATE_EXTENSION_POINT = 				"/com/sap/dirigible/repository/ext/extensions/sql/update_extension_point.sql"; //$NON-NLS-1$
	
	private static final String REMOVE_EXTENSION = 						"/com/sap/dirigible/repository/ext/extensions/sql/remove_extension.sql"; //$NON-NLS-1$
	
	private static final String REMOVE_EXTENSION_POINT = 				"/com/sap/dirigible/repository/ext/extensions/sql/remove_extension_point.sql"; //$NON-NLS-1$
	
	private static final String REMOVE_EXTENSIONS_BY_EXTENSION_POINT = 	"/com/sap/dirigible/repository/ext/extensions/sql/remove_extensions_by_extension_point.sql"; //$NON-NLS-1$
	
	private static ExtensionManager instance;

	private DataSource dataSource;

	private IRepository repository;

	private DBUtils dbUtils;

	public static ExtensionManager getInstance(IRepository repository,
			DataSource dataSource) {
		if (instance == null) {
			instance = new ExtensionManager(repository, dataSource);
		}
		return instance;
	}

	public ExtensionManager(IRepository repository, DataSource dataSource) {
		this.dataSource = dataSource;
		this.repository = repository;
		this.dbUtils = new DBUtils(dataSource);
	}

	public IRepository getRepository() {
		return this.repository;
	}

	public DBUtils getDBUtils() {
		return this.dbUtils;
	}
	
	public String[] getExtensions(String extensionPoint) throws SQLException, IOException {
		List<String> extensions = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			
			String script = getDBUtils().readScript(connection,
					GET_EXTENSIONS, this.getClass());
			statement = connection.prepareStatement(script);
			statement.setString(1, extensionPoint);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				extensions.add(resultSet.getString(1));
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}

		return extensions.toArray(new String[]{});
	}
	
	public ExtensionDefinition getExtension(String extension, String extensionPoint) throws SQLException, IOException {
		ExtensionDefinition extensionDefinition = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			
			String script = getDBUtils().readScript(connection,
					GET_EXTENSION, this.getClass());
			statement = connection.prepareStatement(script);
			statement.setString(1, extension);
			statement.setString(2, extensionPoint);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				extensionDefinition = new ExtensionDefinition();
				extensionDefinition.setLocation(resultSet.getString("EXT_LOCATION"));
				extensionDefinition.setExtensionPoint(resultSet.getString("EXT_EXTPOINT_LOCATION"));
				extensionDefinition.setDescription(resultSet.getString("EXT_DESCRIPTION"));
				extensionDefinition.setCreatedBy(resultSet.getString("EXT_CREATED_BY"));
				extensionDefinition.setCreatedAt(resultSet.getTimestamp("EXT_CREATED_AT"));
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}

		return extensionDefinition;
	}
	
	public ExtensionPointDefinition getExtensionPoint(String extensionPoint) throws SQLException, IOException {
		ExtensionPointDefinition extensionPointDefinition = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			
			String script = getDBUtils().readScript(connection,
					GET_EXTENSION_POINT, this.getClass());
			statement = connection.prepareStatement(script);
			statement.setString(1, extensionPoint);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				extensionPointDefinition = new ExtensionPointDefinition();
				extensionPointDefinition.setLocation(resultSet.getString("EXTPOINT_LOCATION"));
				extensionPointDefinition.setDescription(resultSet.getString("EXTPOINT_DESCRIPTION"));
				extensionPointDefinition.setCreatedBy(resultSet.getString("EXTPOINT_CREATED_BY"));
				extensionPointDefinition.setCreatedAt(resultSet.getTimestamp("EXTPOINT_CREATED_AT"));
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}

		return extensionPointDefinition;
	}
	
	public String[] getExtensionPoints() throws SQLException, IOException {
		List<String> extensionPoints = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			
			String script = getDBUtils().readScript(connection,
					GET_EXTENSION_POINTS, this.getClass());
			statement = connection.prepareStatement(script);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				extensionPoints.add(resultSet.getString(1));
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}

		return extensionPoints.toArray(new String[]{});
	}
	
	public void createExtension(String extension,
			String extensionPoint, String description, HttpServletRequest request) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			String script = getDBUtils().readScript(connection, INSERT_EXTENSION, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, extension);
			statement.setString(2, extensionPoint);
			statement.setString(3, description);
			statement.setString(4, RequestUtils.getUser(request));

			statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}
	
	public void updateExtension(String extension,
			String extensionPoint, String description, HttpServletRequest request) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			String script = getDBUtils().readScript(connection, UPDATE_EXTENSION, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, description);
			statement.setString(2, extension);
			statement.setString(3, extensionPoint);

			statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}


	public void createExtensionPoint(String extensionPoint, String description, HttpServletRequest request) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			String script = getDBUtils().readScript(connection, INSERT_EXTENSION_POINT, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, extensionPoint);
			statement.setString(2, description);
			statement.setString(3, RequestUtils.getUser(request));

			statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}
	
	public void updateExtensionPoint(String extensionPoint, String description, HttpServletRequest request) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			String script = getDBUtils().readScript(connection, UPDATE_EXTENSION_POINT, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, description);
			statement.setString(2, extensionPoint);

			statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}
	
	public void removeExtension(String extension, String extensionPoint) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			String script = getDBUtils().readScript(connection, REMOVE_EXTENSION, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, extension);
			statement.setString(2, extensionPoint);

			statement.executeUpdate();
			
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}
	
	

	public void removeExtensionPoint(String extensionPoint) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			
			removeExtensionsByExtensionPoint(connection, extensionPoint);
			
			String script = getDBUtils().readScript(connection, REMOVE_EXTENSION_POINT, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, extensionPoint);

			statement.executeUpdate();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(DATABASE_ERROR, e);
			}
		}
	}
	
	private void removeExtensionsByExtensionPoint(Connection connection, String extensionPoint) throws SQLException, IOException {

		PreparedStatement statement = null;
		try {
			String script = getDBUtils().readScript(connection, REMOVE_EXTENSIONS_BY_EXTENSION_POINT, this.getClass());
			statement = connection.prepareStatement(script);
			
			statement.setString(1, extensionPoint);

			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}


}
