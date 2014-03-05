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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.db.DBRepository;

public class ExportZipTest {

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
	public void testExportZip() {

		try {
			repository.createResource("/root1/export/folder3/text3.txt", //$NON-NLS-1$
					"text3".getBytes()); //$NON-NLS-1$
			repository.createResource("/root1/export/folder4/text4.txt", //$NON-NLS-1$
					"text4".getBytes()); //$NON-NLS-1$
			List<String> list = new ArrayList<String>();
			list.add("/root1/export"); //$NON-NLS-1$
			byte[] bytes = repository.exportZip(list);
			ZipInputStream zipInputStream = new ZipInputStream(
					new ByteArrayInputStream(bytes));

			try {
				byte[] buffer = new byte[2048];
				ZipEntry entry;
				int entriesCount = 0;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					String outpath = entry.getName();
					ByteArrayOutputStream output = null;
					try {
						output = new ByteArrayOutputStream();
						int len = 0;
						while ((len = zipInputStream.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}

						if (outpath.equals("export/folder3/text3.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text3", read); //$NON-NLS-1$
							entriesCount++;
						}
						if (outpath.equals("export/folder4/text4.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text4", read); //$NON-NLS-1$
							entriesCount++;
						}

					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
				assertEquals(2, entriesCount);

			} finally {
				// we must always close the zip file.
				zipInputStream.close();
			}
			repository.removeCollection("/root1/export/"); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}

	}

	@Test
	public void testExportZipMultipleRoots() {

		try {
			repository.createResource("/root1/export/folder3/text3.txt", //$NON-NLS-1$
					"text3".getBytes()); //$NON-NLS-1$
			repository.createResource("/root1/export/folder4/text4.txt", //$NON-NLS-1$
					"text4".getBytes()); //$NON-NLS-1$
			repository
					.createResource("/root1/export/folder5/folder6/text7.txt"); //$NON-NLS-1$
			List<String> list = new ArrayList<String>();
			list.add("/root1/export/folder3/text3.txt"); //$NON-NLS-1$
			list.add("/root1/export/folder4/text4.txt"); //$NON-NLS-1$
			list.add("/root1/export/folder5/folder6"); //$NON-NLS-1$
			byte[] bytes = repository.exportZip(list);
			ZipInputStream zipInputStream = new ZipInputStream(
					new ByteArrayInputStream(bytes));

			try {
				byte[] buffer = new byte[2048];
				ZipEntry entry;
				int entriesCount = 0;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					String outpath = entry.getName();
					ByteArrayOutputStream output = null;
					try {
						output = new ByteArrayOutputStream();
						int len = 0;
						while ((len = zipInputStream.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}

						if (outpath.equals("text3.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text3", read); //$NON-NLS-1$
							entriesCount++;
						}
						if (outpath.equals("text4.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text4", read); //$NON-NLS-1$
							entriesCount++;
						}
						if (outpath.equals("folder6/")) { //$NON-NLS-1$
							entriesCount++;
						}

					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
				assertEquals(3, entriesCount);

			} finally {
				// we must always close the zip file.
				zipInputStream.close();
			}
			repository.removeCollection("/root1/export/"); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}

	}

	@Test
	public void testExportZipExclusiveRoot() {

		try {
			repository.createResource("/root1/export/folder3/text3.txt", //$NON-NLS-1$
					"text3".getBytes()); //$NON-NLS-1$
			repository.createResource("/root1/export/folder4/text4.txt", //$NON-NLS-1$
					"text4".getBytes()); //$NON-NLS-1$
			byte[] bytes = repository.exportZip("/root1/export", false); //$NON-NLS-1$
			ZipInputStream zipInputStream = new ZipInputStream(
					new ByteArrayInputStream(bytes));

			try {
				byte[] buffer = new byte[2048];
				ZipEntry entry;
				int entriesCount = 0;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					String outpath = entry.getName();
					ByteArrayOutputStream output = null;
					try {
						output = new ByteArrayOutputStream();
						int len = 0;
						while ((len = zipInputStream.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}

						if (outpath.equals("folder3/text3.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text3", read); //$NON-NLS-1$
							entriesCount++;
						}
						if (outpath.equals("folder4/text4.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text4", read); //$NON-NLS-1$
							entriesCount++;
						}

					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
				assertEquals(2, entriesCount);

			} finally {
				// we must always close the zip file.
				zipInputStream.close();
			}
			repository.removeCollection("/root1/export/"); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}

	}

	@Test
	public void testExportZipResourceOnly() {

		try {
			repository.createResource("/root1/export/folder3/text3.txt", //$NON-NLS-1$
					"text3".getBytes()); //$NON-NLS-1$
			byte[] bytes = repository.exportZip(
					"/root1/export/folder3/text3.txt", false); //$NON-NLS-1$
			ZipInputStream zipInputStream = new ZipInputStream(
					new ByteArrayInputStream(bytes));

			try {
				byte[] buffer = new byte[2048];
				ZipEntry entry;
				int entriesCount = 0;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					String outpath = entry.getName();
					ByteArrayOutputStream output = null;
					try {
						output = new ByteArrayOutputStream();
						int len = 0;
						while ((len = zipInputStream.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}

						if (outpath.equals("text3.txt")) { //$NON-NLS-1$
							String read = new String(output.toByteArray());
							assertEquals("text3", read); //$NON-NLS-1$
							entriesCount++;
						}

					} finally {
						// we must always close the output file
						if (output != null)
							output.close();
					}
				}
				assertEquals(1, entriesCount);

			} finally {
				// we must always close the zip file.
				zipInputStream.close();
			}
			repository.removeCollection("/root1/export/"); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}

	}
}
