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
import java.util.Random;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.db.DBRepository;

public class DBFileRenameTest {

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
	public void testRenameByCollection() {
		ICollection collection = null;
		IResource resource = null;
		try {
			collection = repository.createCollection(
					"/a/b/c"); //$NON-NLS-1$
			assertNotNull(collection);
			assertTrue(collection.exists());
			
			byte[] bytes = new byte[400000];
			for (int i = 0; i < bytes.length; i++) {
				int ch = 'A' + new Random().nextInt(20);
				bytes[i] = (byte) ch;
			}

			String base64 = DatatypeConverter.printBase64Binary(bytes);

			resource = repository.createResource(
					"/a/b/c/toBeRemoved1.txt", bytes, false, //$NON-NLS-1$
					"text/plain"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			
			collection.renameTo("x");
			
			collection = repository.getCollection(
					"/a/b/x"); //$NON-NLS-1$
			assertNotNull(collection);
			assertTrue(collection.exists());
			
			resource = repository.getResource(
					"/a/b/x/toBeRemoved1.txt"); //$NON-NLS-1$
			assertNotNull(collection);
			assertTrue(collection.exists());
			
			String base64back = DatatypeConverter
					.printBase64Binary(resource.getContent());

			assertEquals(base64, base64back);


		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} finally {
			try {
				repository.removeCollection("/a");
			} catch (IOException e) {
				assertTrue(e.getMessage(), false);
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testRenameByFile() {
		ICollection collection = null;
		IResource resource = null;
		try {
			collection = repository.createCollection(
					"/a/b/c"); //$NON-NLS-1$
			assertNotNull(collection);
			assertTrue(collection.exists());
			
			byte[] bytes = new byte[400000];
			for (int i = 0; i < bytes.length; i++) {
				int ch = 'A' + new Random().nextInt(20);
				bytes[i] = (byte) ch;
			}

			String base64 = DatatypeConverter.printBase64Binary(bytes);

			resource = repository.createResource(
					"/a/b/c/toBeRemoved1.txt", bytes, false, //$NON-NLS-1$
					"text/plain"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());
			
			resource.renameTo("toBeRemoved2.txt");
			
			resource = repository.getResource(
					"/a/b/c/toBeRemoved2.txt"); //$NON-NLS-1$
			assertNotNull(collection);
			assertTrue(collection.exists());
			assertEquals("toBeRemoved2.txt", resource.getName());
			
			String base64back = DatatypeConverter
					.printBase64Binary(resource.getContent());

			assertEquals(base64, base64back);


		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} finally {
			try {
				repository.removeCollection("/a");
			} catch (IOException e) {
				assertTrue(e.getMessage(), false);
				e.printStackTrace();
			}
		}
	}
}
