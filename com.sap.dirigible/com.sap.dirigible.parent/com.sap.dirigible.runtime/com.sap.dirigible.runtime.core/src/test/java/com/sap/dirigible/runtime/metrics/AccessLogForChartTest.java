package com.sap.dirigible.runtime.metrics;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AccessLogForChartTest {

    @Test
    public void testEnumerator() {
    	try {
			List<List<Object>> allRecords = new ArrayList<List<Object>>();
			List<Object> record = new ArrayList<Object>();
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			record.add("/abc/def");
			record.add(format.parse("2014-01-05 02:00:00"));
			record.add(5);
			allRecords.add(record);
			
			record = new ArrayList<Object>();
			record.add("/abc/def");
			record.add(format.parse("2014-01-05 04:00:00"));
			record.add(5);
			allRecords.add(record);
			
			record = new ArrayList<Object>();
			record.add("/abc/def");
			record.add(format.parse("2014-01-06 08:00:00"));
			record.add(5);
			allRecords.add(record);
			
			record = new ArrayList<Object>();
			record.add("/zyx/qaz");
			record.add(format.parse("2014-01-05 03:00:00"));
			record.add(5);
			allRecords.add(record);
			
			record = new ArrayList<Object>();
			record.add("/zyx/qaz");
			record.add(format.parse("2014-01-06 11:00:00"));
			record.add(5);
			allRecords.add(record);
			
			AccessLogRecordDAO accessLogRecordDAO = new AccessLogRecordDAO();
			String[][] result = accessLogRecordDAO.prepareData(allRecords);
			
			for (int i = 0; i < result.length; i++) {
				String[] row = result[i];
				for (int j = 0; j < row.length; j++) {
					System.out.print(row[j] + " ");
				}
				System.out.println();
			}
			
			// TODO check the result
			
		} catch (ParseException e) {
			assertTrue(e.getMessage(), false);
            e.printStackTrace();
		}
    }


}
