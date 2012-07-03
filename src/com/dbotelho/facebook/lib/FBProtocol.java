/*
* Copyright 2012 Daniel Botelho
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.dbotelho.facebook.lib;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.http.HttpHeaders;

/**
 * 
 * @author Daniel Botelho (www.dbotelho.com)
 * 
 */
public class FBProtocol extends ProtocolController{
	 private BrowserField browserField;
	 
	 private String cookie;
	 private Hashtable cookies;

	public FBProtocol(BrowserField arg0){
		super(arg0);
		browserField = arg0;
		cookies = new Hashtable();
	}
	
	public InputConnection handleResourceRequest(BrowserFieldRequest request) 
    throws Exception {

		HttpConnection httpConn = null;
		String suffix = ConnectionCreator.getConnectionString();

		StreamConnection s = (StreamConnection) Connector.open(request.getURL() + suffix,
				Connector.READ_WRITE, true);
		httpConn = (HttpConnection) s;

		httpConn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.56 Safari/536.5");
		httpConn.setRequestProperty("Accept", "*/*");
		httpConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		httpConn.setRequestProperty("Connection",
		"Keep-alive");
		
		
		if(cookies.size()>0){
			httpConn.setRequestProperty("Cookie",
					cookie);
		}

		if(request.getPostData()!=null){
			httpConn.setRequestProperty("Content-Length", Integer
					.toString(request.getPostData().length));
			httpConn.setRequestMethod(HttpConnection.POST);

			httpConn.openOutputStream().write(request.getPostData());
		}else{
			httpConn.setRequestMethod(HttpConnection.GET);
		}

		
		try{
			for (int i=0; ;i++){
				String headerName = httpConn.getHeaderFieldKey(i);
				String headerValue = httpConn.getHeaderField(i);
				if(headerName ==null && headerValue==null){
					break;
				}
				if("Set-Cookie".equalsIgnoreCase(headerName)){
					if(headerValue.indexOf(";")> 0){
						String headerValue1 = headerValue.substring(0, headerValue.indexOf(";"));
						if(headerValue1.indexOf("=")>0){
							String key1 = headerValue1.substring(0, headerValue1.indexOf("="));
							String value1 = headerValue1.substring(headerValue1.indexOf("=")+1);
							if(value1.equalsIgnoreCase("delete")){
								cookies.remove(key1);
							}else{
								cookies.put(key1, value1);
							}
						}
					}
				}
			}
			cookie="";
			Enumeration keys = cookies.keys();
			String key, value;
			boolean first=true;
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				// Less efficient b/c I have to do a lookup every time.
				value = (String) cookies.get(key);
				if (value != null) {
					if(first){
						cookie = key+"="+value;
						first=false;
					}else{
						cookie += (";"+key+"="+value);
					}
				}
			}
		}catch (Exception e) {
			httpConn.setRequestProperty("Exception",
					e.getMessage());
		}

		return httpConn;
	}

}
