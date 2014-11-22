package com.sap.dirigible.runtime.java;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UpdateJavaServiceJavaExecutorTest extends AbstractJavaExecutorTest {
	private static final String EXPECTED_OUTPUT_FOR_SERVICE = "Hello from Service!"; //$NON-NLS-1$
	private static final String EXPECTED_OUTPUT_FOR_SERVICE_UPDATED1 = "Hello from Service Updated 1!"; //$NON-NLS-1$
	private static final String EXPECTED_OUTPUT_FOR_SERVICE_UPDATED2 = "Hello from Service Updated 2!"; //$NON-NLS-1$
	private static final String EXPECTED_OUTPUT_FOR_SERVICE_UPDATED3 = "Hello from Service Updated 3!"; //$NON-NLS-1$

	@Test
	public void testExecuteTwoServicesFromOneProjectOutputs() throws Exception {
		createResource(RESOURCE_PATH_SERVICE, SOURCЕ_SERVICE);
		execute(MODULE_SERVICE);
		assertEquals(EXPECTED_OUTPUT_FOR_SERVICE, getOutput());

		createResource(RESOURCE_PATH_SERVICE, SOURCЕ_SERVICE_UPDATED1);
		execute(MODULE_SERVICE);
		assertEquals(EXPECTED_OUTPUT_FOR_SERVICE_UPDATED1, getOutput());
		
		createResource(RESOURCE_PATH_SERVICE, SOURCЕ_SERVICE_UPDATED2);
		execute(MODULE_SERVICE);
		assertEquals(EXPECTED_OUTPUT_FOR_SERVICE_UPDATED2, getOutput());
		
		createResource(RESOURCE_PATH_SERVICE, SOURCЕ_SERVICE_UPDATED3);
		execute(MODULE_SERVICE);
		assertEquals(EXPECTED_OUTPUT_FOR_SERVICE_UPDATED3, getOutput());
	}
}
