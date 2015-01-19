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

package com.sap.dirigible.runtime.scripting.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/*

 package org.apache.http.examples.client;

 import java.util.ArrayList;
 import java.util.List;

 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;

 public class QuickStart {

 public static void main(String[] args) throws Exception {
 DefaultHttpClient httpclient = new DefaultHttpClient();
 HttpGet httpGet = new HttpGet("http://targethost/homepage");

 HttpResponse response1 = httpclient.execute(httpGet);

 // The underlying HTTP connection is still held by the response object 
 // to allow the response content to be streamed directly from the network socket. 
 // In order to ensure correct deallocation of system resources 
 // the user MUST either fully consume the response content  or abort request 
 // execution by calling HttpGet#releaseConnection().

 try {
 System.out.println(response1.getStatusLine());
 HttpEntity entity1 = response1.getEntity();
 // do something useful with the response body
 // and ensure it is fully consumed
 EntityUtils.consume(entity1);
 } finally {
 httpGet.releaseConnection();
 }

 HttpPost httpPost = new HttpPost("http://targethost/login");
 List <NameValuePair> nvps = new ArrayList <NameValuePair>();
 nvps.add(new BasicNameValuePair("username", "vip"));
 nvps.add(new BasicNameValuePair("password", "secret"));
 httpPost.setEntity(new UrlEncodedFormEntity(nvps));
 HttpResponse response2 = httpclient.execute(httpPost);

 try {
 System.out.println(response2.getStatusLine());
 HttpEntity entity2 = response2.getEntity();
 // do something useful with the response body
 // and ensure it is fully consumed
 EntityUtils.consume(entity2);
 } finally {
 httpPost.releaseConnection();
 }
 }

 }

 */

public class HttpUtils {

	public HttpGet createGet(String strURL) {
		return new HttpGet(strURL);
	}

	public HttpPost createPost(String strURL) {
		return new HttpPost(strURL);
	}

	public HttpPut createPut(String strURL) {
		return new HttpPut(strURL);
	}

	public HttpDelete createDelete(String strURL) {
		return new HttpDelete(strURL);
	}

	public HttpClient createHttpClient() {
		return createHttpClient(true);
	}

	public HttpClient createHttpClient(boolean trustAll) {
		return ProxyUtils.getHttpClient(trustAll);
	}

	public void consume(HttpEntity entity) throws IOException {
		EntityUtils.consume(entity);
	}
	
	public BasicScheme createBasicScheme() {
		return new org.apache.http.impl.auth.BasicScheme();
	}
	
	public DigestScheme createDigestScheme() {
		return new org.apache.http.impl.auth.DigestScheme();
	}
	
	public UsernamePasswordCredentials createUsernamePasswordCredentials(String user, String password) {
		return new UsernamePasswordCredentials(user, password);
	}
	
	public BasicHeader createBasicHeader(String name, String value) {
		return new BasicHeader(name, value);
	}
	
	
	
	

}
