package project_with_two_services;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Service1 {
	
	public void service(HttpServletRequest request, HttpServletResponse response, Map<String, Object> scope) throws Exception {
		System.out.print("Service 1, updated!");
	}
}