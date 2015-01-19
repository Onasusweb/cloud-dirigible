package com.sap.dirigible.runtime.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.sap.dirigible.runtime.scripting.utils.DbUtils;

public class DbUtilsTest {

	private DataSource dataSource;
	private DbUtils dbUtils;

	@Before
	public void setUp() {
		dataSource = DataSourceUtils.createLocal();
		dbUtils = new DbUtils(dataSource);
	}

	@Test
	public void testGetNext() {
		try {
			dbUtils.dropSequence("TEST_SEQ"); //$NON-NLS-1$
			int value = dbUtils.getNext("TEST_SEQ"); //$NON-NLS-1$
			assertEquals(1, value);
			value = dbUtils.getNext("TEST_SEQ"); //$NON-NLS-1$
			assertEquals(2, value);
			value = dbUtils.getNext("TEST_SEQ"); //$NON-NLS-1$
			assertEquals(3, value);
		} catch (SQLException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateSequence() {
		try {
			dbUtils.createSequence("TEST_SEQ1", 0); //$NON-NLS-1$
			dbUtils.dropSequence("TEST_SEQ1"); //$NON-NLS-1$
		} catch (SQLException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testDropSequence() {
		try {
			dbUtils.dropSequence("TEST_SEQ2"); //$NON-NLS-1$
			int value = dbUtils.getNext("TEST_SEQ2"); //$NON-NLS-1$
			assertEquals(1, value);
			dbUtils.dropSequence("TEST_SEQ2"); //$NON-NLS-1$
			value = dbUtils.getNext("TEST_SEQ2"); //$NON-NLS-1$
			assertEquals(1, value);
		} catch (SQLException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

	@Test
	public void testExistSequence() {
		try {
			dbUtils.dropSequence("TEST_SEQ3"); //$NON-NLS-1$
			boolean value = dbUtils.existSequence("TEST_SEQ3"); //$NON-NLS-1$
			assertEquals(false, value);
			dbUtils.createSequence("TEST_SEQ3", 0); //$NON-NLS-1$
			value = dbUtils.existSequence("TEST_SEQ3"); //$NON-NLS-1$
			assertEquals(true, value);
		} catch (SQLException e) {
			assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

}
