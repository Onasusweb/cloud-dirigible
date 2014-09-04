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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.repository.api.IEntity;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.db.DBRepository;

public class SearchTest {

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
	public void testSearchName() {
		try {
			IResource resource = repository.createResource(
					"/testCollectionSearch/param1.txt", "param1".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/param2.txt", "param2".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/param12.txt", "param12".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			repository.removeResource("/testCollectionSearch/param1.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/param2.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/param12.txt"); //$NON-NLS-1$

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchNameUnderRoot() {
		try {
			IResource resource = repository.createResource("/dddd/file1.txt"); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource("/dddd/file2.txt"); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource("/dddd/file3.txt"); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			List<IEntity> entities = repository.searchName("/dddd/", ".txt", false); //$NON-NLS-1$
			assertEquals(3, entities.size());

			repository.removeResource("/dddd/file1.txt"); //$NON-NLS-1$
			repository.removeResource("/dddd/file2.txt"); //$NON-NLS-1$
			repository.removeResource("/dddd/file3.txt"); //$NON-NLS-1$

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchPath() {
		try {
			IResource resource = repository.createResource(
					"/testCollectionSearch/param1.txt", "param1".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/param2.txt", "param2".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/param12.txt", "param12".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			List<IEntity> entities = repository.searchPath("param1", false); //$NON-NLS-1$
			assertEquals(2, entities.size());

			entities = repository.searchPath("Search", false); //$NON-NLS-1$
			assertEquals(4, entities.size());

			entities = repository.searchPath("search", false); //$NON-NLS-1$
			assertEquals(0, entities.size());

			entities = repository.searchPath("search", true); //$NON-NLS-1$
			assertEquals(4, entities.size());

			repository.removeResource("/testCollectionSearch/param1.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/param2.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/param12.txt"); //$NON-NLS-1$

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchText() {
		try {
			IResource resource = repository.createResource(
					"/testCollectionSearch/abc1.txt", "abc def".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/abc2.txt", "ghi jkl".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/abc3.txt", "abc jkl".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			resource = repository.createResource(
					"/testCollectionSearch/xxx4.txt", "xxx yyy".getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			List<IEntity> entities = repository.searchText("abc", false); //$NON-NLS-1$
			assertEquals(3, entities.size());

			entities = repository.searchText("jkl", false); //$NON-NLS-1$
			assertEquals(2, entities.size());

			entities = repository.searchText("Ghi", false); //$NON-NLS-1$
			assertEquals(0, entities.size());

			entities = repository.searchText("Ghi", true); //$NON-NLS-1$
			assertEquals(1, entities.size());

			repository.removeResource("/testCollectionSearch/abc1.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/abc2.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/abc3.txt"); //$NON-NLS-1$
			repository.removeResource("/testCollectionSearch/xxx4.txt"); //$NON-NLS-1$

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

}
