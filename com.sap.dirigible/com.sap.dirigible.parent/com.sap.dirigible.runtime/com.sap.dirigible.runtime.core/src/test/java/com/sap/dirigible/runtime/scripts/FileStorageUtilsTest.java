package com.sap.dirigible.runtime.scripts;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.runtime.scripting.AbstractStorageUtils;
import com.sap.dirigible.runtime.scripting.FileStorageUtils;
import com.sap.dirigible.runtime.scripting.FileStorageUtils.FileStorageFile;
import com.sap.dirigible.runtime.utils.DataSourceUtils;

public class FileStorageUtilsTest {

	private static final String PATH = "/a/b/c";
	private static final byte[] DATA = "Some data".getBytes();
	private static final String CONTENT_TYPE = "application/pdf";
	private static final byte[] OTHER_DATA = "Other data".getBytes();
	private static final byte[] TOO_BIG_DATA = new byte[AbstractStorageUtils.MAX_STORAGE_FILE_SIZE_IN_BYTES + 1];

	private FileStorageUtils fileStorage;

	@Before
	public void setUp() throws Exception {
		fileStorage = new FileStorageUtils(DataSourceUtils.createLocal());
	}

	@Test
	public void testPut() throws Exception {
		fileStorage.put(PATH, DATA, CONTENT_TYPE);
		FileStorageFile retrieved = fileStorage.get(PATH);
		assertArrayEquals(DATA, retrieved.data);
		assertEquals(CONTENT_TYPE, retrieved.contentType);
	}

	@Test
	public void testPutTooBigData() throws Exception {
		try {
			fileStorage.put(PATH, TOO_BIG_DATA, CONTENT_TYPE);
			fail("Test should fail, because " + AbstractStorageUtils.TOO_BIG_DATA_MESSAGE);
		} catch (InvalidParameterException e) {
			assertEquals(AbstractStorageUtils.TOO_BIG_DATA_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void testClear() throws Exception {
		fileStorage.put(PATH, DATA, CONTENT_TYPE);
		fileStorage.clear();
		assertNull(fileStorage.get(PATH));
	}

	@Test
	public void testDelete() throws Exception {
		fileStorage.put(PATH, DATA, CONTENT_TYPE);
		fileStorage.delete(PATH);
		assertNull(fileStorage.get(PATH));
	}

	@Test
	public void testSet() throws Exception {
		fileStorage.put(PATH, DATA, CONTENT_TYPE);
		fileStorage.put(PATH, OTHER_DATA, CONTENT_TYPE);
		FileStorageFile retrieved = fileStorage.get(PATH);
		assertArrayEquals(OTHER_DATA, retrieved.data);
		assertEquals(CONTENT_TYPE, retrieved.contentType);
	}

}
