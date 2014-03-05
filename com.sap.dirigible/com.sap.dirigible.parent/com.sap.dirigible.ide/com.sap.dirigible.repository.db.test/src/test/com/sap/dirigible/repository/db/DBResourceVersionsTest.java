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

package test.com.sap.dirigible.repository.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.api.IResourceVersion;
import com.sap.dirigible.repository.db.DBRepository;

public class DBResourceVersionsTest {

	private static IRepository repository;

	@Before
	public void setUp() {
		DataSource dataSource = DBRepositoryTest.createLocal();
		try {
			repository = new DBRepository(dataSource, "guest", false); //$NON-NLS-1$
		} catch (Exception e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testCheckVersions() {
		try {
			IResource resource = repository
					.getResource("/testCollection/versionedFile.txt"); //$NON-NLS-1$
			if (resource.exists()) {
				resource.delete();
			}
			resource = repository
					.createResource("/testCollection/versionedFile.txt", //$NON-NLS-1$
							"Version 1".getBytes()); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			List<IResourceVersion> versions = repository
					.getResourceVersions("/testCollection/versionedFile.txt"); //$NON-NLS-1$
			assertEquals(versions.size(), 1);
			IResourceVersion version = versions.get(0);
			assertArrayEquals(version.getContent(), "Version 1".getBytes()); //$NON-NLS-1$
			assertEquals(version.getVersion(), 1);

			resource.setContent("Version 2".getBytes()); //$NON-NLS-1$

			versions = repository
					.getResourceVersions("/testCollection/versionedFile.txt"); //$NON-NLS-1$
			assertEquals(versions.size(), 2);
			version = versions.get(0);
			assertArrayEquals(version.getContent(), "Version 1".getBytes()); //$NON-NLS-1$
			assertEquals(version.getVersion(), 1);
			version = versions.get(1);
			assertArrayEquals(version.getContent(), "Version 2".getBytes()); //$NON-NLS-1$
			assertEquals(version.getVersion(), 2);

			resource.delete();

			versions = repository
					.getResourceVersions("/testCollection/versionedFile.txt"); //$NON-NLS-1$
			assertEquals(versions.size(), 0);

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

}
