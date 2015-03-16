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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IResource;
import org.eclipse.dirigible.repository.db.DBRepository;

public class DBBinaryTest {

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
	public void testCreateBinary() {
		try {
			IResource resource = repository.createResource(
					"/testCollection/toBeRemoved.bin", //$NON-NLS-1$
					new byte[] { 0, 1, 1, 0 }, true, "application/bin"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertTrue(resource.isBinary());
		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testGetBinary() {
		try {
			repository.createResource("/testCollection/toBeRemoved.bin", //$NON-NLS-1$
					new byte[] { 0, 1, 1, 0 }, true, "application/bin"); //$NON-NLS-1$
			IResource resource = repository
					.getResource("/testCollection/toBeRemoved.bin"); //$NON-NLS-1$
			assertNotNull(resource);
			assertTrue(resource.exists());
			assertTrue(resource.isBinary());
			assertTrue(Arrays.equals(resource.getContent(), new byte[] { 0, 1,
					1, 0 }));
		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testRemoveBinary() {
		try {
			repository.createResource("/testCollection/toBeRemoved.bin", //$NON-NLS-1$
					new byte[] { 0, 1, 1, 0 }, true, "application/bin"); //$NON-NLS-1$
			repository.removeResource("/testCollection/toBeRemoved.bin"); //$NON-NLS-1$
			IResource resource = repository
					.getResource("/testCollection/toBeRemoved.bin"); //$NON-NLS-1$
			assertNotNull(resource);
			assertFalse(resource.exists());
		} catch (IOException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

}
