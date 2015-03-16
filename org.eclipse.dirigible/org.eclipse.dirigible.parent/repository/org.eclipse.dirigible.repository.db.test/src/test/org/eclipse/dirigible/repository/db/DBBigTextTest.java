/******************************************************************************* 
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 *******************************************************************************/

package test.org.eclipse.dirigible.repository.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IResource;
import org.eclipse.dirigible.repository.db.DBRepository;

public class DBBigTextTest {

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
	public void testBigText() {
		IResource resource = null;
		try {
			byte[] bytes = new byte[400000];
			for (int i = 0; i < bytes.length; i++) {
				int ch = 'A' + new Random().nextInt(20);
				bytes[i] = (byte) ch;
			}

			String base64 = DatatypeConverter.printBase64Binary(bytes);

			resource = repository.createResource(
					"/testCollection/toBeRemoved1.txt", bytes, false, //$NON-NLS-1$
					"text/plain"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			IResource resourceBack = repository
					.getResource("/testCollection/toBeRemoved1.txt"); //$NON-NLS-1$
			String base64back = DatatypeConverter
					.printBase64Binary(resourceBack.getContent());

			assertEquals(base64, base64back);

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} finally {
			try {
				if (resource != null && resource.exists()) {
					repository.removeResource("/testCollection/toBeRemoved1.txt"); //$NON-NLS-1$
					resource = repository
							.getResource("/testCollection/toBeRemoved1.txt"); //$NON-NLS-1$
					assertNotNull(resource);
					assertFalse(resource.exists());
				}
			} catch (IOException e) {
				assertTrue(e.getMessage(), false);
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testSpacesText() {
		IResource resource = null;
		try {
			byte[] bytes = new byte[400000];
			for (int i = 0; i < bytes.length; i++) {
				int ch = ' ';
				bytes[i] = (byte) ch;
			}

			resource = repository.createResource(
					"/testCollection/toBeRemoved2.txt", bytes, false, //$NON-NLS-1$
					"text/plain"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertFalse(resource.isBinary());

			IResource resourceBack = repository
					.getResource("/testCollection/toBeRemoved2.txt"); //$NON-NLS-1$

			byte[] bytesBack = resourceBack.getContent();
			assertEquals(bytes.length, bytesBack.length);
			assertTrue(Arrays.equals(bytes, bytesBack));

		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} finally {
			try {
				if (resource != null && resource.exists()) {
					repository.removeResource("/testCollection/toBeRemoved2.txt"); //$NON-NLS-1$
					resource = repository
							.getResource("/testCollection/toBeRemoved2.txt"); //$NON-NLS-1$
					assertNotNull(resource);
					assertFalse(resource.exists());
				}
			} catch (IOException e) {
				assertTrue(e.getMessage(), false);
				e.printStackTrace();
			}
		}
	}

}
