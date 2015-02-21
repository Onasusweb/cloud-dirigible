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

package com.sap.dirigible.repository.ext.security;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.ICommonConstants;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.ext.db.AbstractDataUpdater;
import com.sap.dirigible.repository.logging.Logger;

public class SecurityUpdater extends AbstractDataUpdater {

	private static final String NODE_ROLE = "role";
	private static final String NODE_ROLES = "roles";
	private static final String NODE_LOCATION = "location";
	public static final String EXTENSION_ACCESS = ".access"; //$NON-NLS-1$
	public static final String REGISTRY_SECURITY_CONSTRAINTS_DEFAULT = ICommonConstants.SECURITY_REGISTRY_PUBLISH_LOCATION;
	
	private static final Logger logger = Logger.getLogger(SecurityUpdater.class);

	private IRepository repository;
	private DataSource dataSource;
	private String location;
	private SecurityManager securityManager;

	public SecurityUpdater(IRepository repository, DataSource dataSource,
			String location) {
		this.repository = repository;
		this.dataSource = dataSource;
		this.location = location;
		this.securityManager = SecurityManager.getInstance(repository,
				dataSource);
	}

	@Override
	public void executeUpdate(List<String> knownFiles,
			HttpServletRequest request, List<String> errors) throws Exception {
		if (knownFiles.size() == 0) {
			return;
		}

		try {
			Connection connection = dataSource.getConnection();

			try {
				for (Iterator<String> iterator = knownFiles.iterator(); iterator
						.hasNext();) {
					String dsDefinition = iterator.next();
					try {
						if (dsDefinition.endsWith(EXTENSION_ACCESS)) {
							executeAccessUpdate(connection, dsDefinition, request);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						errors.add(e.getMessage());
					}
				}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void executeAccessUpdate(Connection connection,
			String scDefinition, HttpServletRequest request)
			throws SQLException, IOException, SecurityException {
		JsonArray scDefinitionArray = parseAccess(scDefinition);
		for (Iterator<?> iter = scDefinitionArray.iterator(); iter.hasNext();) {
			JsonObject locationObject = (JsonObject) iter.next();
			String locationName = locationObject.get(NODE_LOCATION).getAsString(); //$NON-NLS-1$
			JsonArray rolesArray = locationObject.get(NODE_ROLES).getAsJsonArray(); //$NON-NLS-1$
			for (Iterator<?> iter2 = rolesArray.iterator(); iter2.hasNext();) {
				JsonObject rolesObject = (JsonObject) iter2.next();
				String roleName = rolesObject.get(NODE_ROLE).getAsString(); //$NON-NLS-1$
				updateRole(locationName, roleName, request);
			}
		}
	}

	private void updateRole(String locationName, String roleName,
			HttpServletRequest request) throws SecurityException {
		this.securityManager.secureLocationWithRole(locationName, roleName,
				request);
	}

	private JsonArray parseAccess(String dsDefinition) throws IOException {
		// [
		// {
		// "location":"/${projectName}/secured",
		// "roles":
		// [
		// {"role":"User"},
		// {"role":"PowerUser"}
		// ]
		// },
		// {
		// "location":"/${projectName}/confidential",
		// "roles":
		// [
		// {"role":"Administrator"}
		// ]
		// }
		// ]

		IRepository repository = this.repository;
		IResource resource = repository.getResource(this.location
				+ dsDefinition);
		String content = new String(resource.getContent());
		JsonParser parser = new JsonParser();
		JsonArray dsDefinitionObject = (JsonArray) parser.parse(content);

		// TODO validate the parsed content has the right structure

		return dsDefinitionObject;
	}

	public void enumerateKnownFiles(ICollection collection,
			List<String> dsDefinitions) throws IOException {
		if (collection.exists()) {
			List<IResource> resources = collection.getResources();
			for (Iterator<IResource> iterator = resources.iterator(); iterator
					.hasNext();) {
				IResource resource = iterator.next();
				if (resource != null && resource.getName() != null) {
					if (resource.getName().endsWith(EXTENSION_ACCESS)) {
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
	public void applyUpdates() throws IOException, Exception {
		List<String> knownFiles = new ArrayList<String>();
		ICollection srcContainer = this.repository.getCollection(this.location);
		if (srcContainer.exists()) {
			enumerateKnownFiles(srcContainer, knownFiles);// fill knownFiles[]
															// with urls to
															// recognizable
															// repository files
			executeUpdate(knownFiles, null);// execute the real updates
		}
	}
	
	@Override
	public IRepository getRepository() {
		return repository;
	}
	
	@Override
	public String getLocation() {
		return location;
	}
	
	@Override
	public void executeUpdate(List<String> knownFiles, List<String> errors) throws Exception {
		executeUpdate(knownFiles, null, errors);
	}
	
}
