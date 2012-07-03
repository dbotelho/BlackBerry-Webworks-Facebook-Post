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
package com.dbotelho.facebook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import com.dbotelho.facebook.lib.BrowserScreen;
import com.dbotelho.facebook.lib.ConnectionCreator;


import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.Dialog;

/**
 * 
 * @author Daniel Botelho (www.dbotelho.com)
 * 
 */
public class AFunction extends ScriptableFunction {

	public static final String FACEBOOK_POST_FUNCTION = "post";
	public static final String FACEBOOK_CONN_FUNCTION = "connect";
	private String functionType;
	public BrowserScreen browserScreen;

	public void setFunctionType(String functionType) {
		this.functionType = functionType;
	}

	public String getFunctionType() {
		return functionType;
	}

	public Object invoke(Object thiz, Object[] args) throws Exception {

		ScriptableFunction _callback = null;
		ScriptableFunction _error = null;
		String tempp = "";
		String data = null;
		String type = null;
		int idx = 0;
		if (getFunctionType().equals(FACEBOOK_POST_FUNCTION)) {
			String url = null;
			try {
				final UiApplication uiApp = UiApplication.getUiApplication();
				// String url = "";
				// uiApp.invokeAndWait(new CustomAskRunnable(value));
				// return value + "";
				Scriptable config = (Scriptable) args[0];
				_callback = (ScriptableFunction) config.getField("success");
				url = (String) config.getField("url");
				idx++;

				if (config.getField("error") != null) {
					_error = (ScriptableFunction) config.getField("error");
				}
				idx++;
				if (config.getField("type") != null) {
					try {
						type = (String) config.getField("type");
					} catch (Exception e) {
						// TODO: handle exception
						// type = HttpConnection.GET;
						tempp = " type não +e null"
								+ config.getField("c_type").getClass();
					}
				}

				idx++;

				if (config.getField("data") != null) {
					data = (String) config.getField("data");
				}
				idx++;
				/*
				 * if(args.length == 3){ _callback = (ScriptableFunction)
				 * args[2]; }
				 */

				idx++;
				/*
				 * final String _url = url; final String _data = data; final
				 * String _type = type; final ScriptableFunction __callback =
				 * _callback; final ScriptableFunction __error = _error;
				 */
				uiApp.invokeLater(new AJAXRunnable(url, data, type, _callback,
						_error));
				idx++;
				// uiApp.invokeAndWait(new AJAXRunnable("",_callback));
			} catch (Exception e) {
				tempp = e.toString();
				// tempp="erro";
				if (_error != null) {
					final Object[] threadedResult = new Object[1];
					threadedResult[0] = e.getMessage();// url+e.toString()+(Integer.toString(idx))+tempp;
					final ScriptableFunction threadedError = _error;
					new Thread() {
						public void run() {
							try {
								// Pass the result of the spinner back to the
								// handle
								// of the JavaScript callback
								threadedError.invoke(threadedError,
										threadedResult);
							} catch (Exception e) {
								throw new RuntimeException(e.getMessage());
							}
						}
					}.start();
				}
			}

		}else if(getFunctionType().equals(FACEBOOK_CONN_FUNCTION)) {
			//this is the open call
			synchronized(Application.getEventLock())
			{
				final String url = (String)args[0];
				if(browserScreen == null){
					final ScriptableFunction callback = (ScriptableFunction)args[1];
					final Object obj1 = thiz;
					final UiApplication uiApp = UiApplication.getUiApplication();
					 uiApp.invokeLater(new Runnable() {
						
						public void run() {
							browserScreen = new BrowserScreen(url, callback, obj1);
							browserScreen.setOnCloseEvent(new Runnable() {
								
								public void run() {
									browserScreen= null;
								}
							});
							UiApplication.getUiApplication().pushScreen(browserScreen);
							
						}
					});
				}else{
					browserScreen.openUrl(url);
				}
				
			}
		}

		return (args[0]).getClass() + " " + type + " " + idx + " " + tempp
				+ " " + data;
	}

	private class AJAXRunnable implements Runnable {
		private ScriptableFunction _callback = null;
		private ScriptableFunction _error = null;
		private String _url = null;
		private String _data = null;
		private String _type = null;

		public AJAXRunnable(String url, String data, String type,
				ScriptableFunction callback, ScriptableFunction error) {
			// this.screen = screen;
			_callback = callback;
			_type = type;// ((type!=null) && (type.toUpperCase() == "POST"))?
			// HttpConnection.POST : HttpConnection.GET;
			_url = url;
			_data = data;
			_error = error;
		}

		public void run() {
			HttpConnection httpConn = null;
			StreamConnection s = null;
			StringBuffer raw = new StringBuffer();
			InputStream contentIn = null;
			OutputStream output = null;
			String value = "";
			try {
				String suffix = ConnectionCreator.getConnectionString();

				s = (StreamConnection) Connector.open(_url + suffix,
						Connector.WRITE, true);
				httpConn = (HttpConnection) s;

				httpConn.setRequestProperty("User-Agent", "BlackBerry Client");
				httpConn.setRequestProperty("Accept", "*/*");
				httpConn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");

				httpConn.setRequestMethod(_type);
				// Add the authorized header.
				//httpConn.setRequestProperty(
				//		HttpProtocolConstants.HEADER_AUTHORIZATION,
				//		"Basic YWlyQkFBdjEwOlZQOHE2c0RwMlNwSg==");
				// httpConn.setRequestProperty("TAG", "ggg=");

				if ((_data != null) && (_data.length() > 0)) {

					byte[] postdata = _data.getBytes("UTF-8");
					httpConn.setRequestProperty("Content-Length", Integer
							.toString(postdata.length));
					output = httpConn.openOutputStream();
					output.write(postdata);
				}
				int status = httpConn.getResponseCode();
				if (status == HttpConnection.HTTP_UNAUTHORIZED) {
					s.close();
					s = (StreamConnection) Connector.open(_url + suffix,
							Connector.WRITE, true);
					httpConn = (HttpConnection) s;

					httpConn.setRequestProperty("User-Agent",
							"BlackBerry Client");
					httpConn.setRequestProperty("Accept", "*/*");
					httpConn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");

					httpConn.setRequestMethod(_type);
					// Add the authorized header.
					httpConn.setRequestProperty(
							HttpProtocolConstants.HEADER_AUTHORIZATION,
							"Basic YWlyQkFBdjEwOlZQOHE2c0RwMlNwSg==");
					// httpConn.setRequestProperty("TAG", "ggg=");

					if ((_data != null) && (_data.length() > 0)) {

						byte[] postdata = _data.getBytes("UTF-8");
						httpConn.setRequestProperty("Content-Length", Integer
								.toString(postdata.length));
						output = httpConn.openOutputStream();
						output.write(postdata);

					}
				}
				status = httpConn.getResponseCode();
				value += status;

				/*
				 * value = httpConn.getResponseCode() + " " +
				 * httpConn.getRequestMethod();
				 */
				contentIn = s.openInputStream();
				byte[] data = new byte[4096];
				int length = 0;

				while (-1 != (length = contentIn.read(data))) {
					raw.append(new String(data, 0, length));
				}
				if (contentIn != null) {
					try {
						contentIn.close();
					} catch (Exception ignored) {
					}
				}
				if (output != null) {
					try {
						output.close();
					} catch (Exception ignored) {
					}
				}
				if (httpConn != null) {
					try {
						httpConn.close();
					} catch (Exception ignored) {
					}
				}
				s.close();
				final Object[] threadedResult = new Object[1];
				threadedResult[0] = raw.toString();
				new Thread() {
					public void run() {
						try {
							// Pass the result of the spinner back to the handle
							// of the JavaScript callback
							_callback.invoke(_callback, threadedResult);
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
					}
				}.start();
			} catch (IOException e) {
				if (_error != null) {
					final Object[] threadedResult = new Object[1];
					threadedResult[0] = e.getMessage();
					new Thread() {
						public void run() {
							try {
								// Pass the result of the spinner back to the
								// handle
								// of the JavaScript callback
								_error.invoke(_error, threadedResult);
							} catch (Exception e) {
								throw new RuntimeException(e.getMessage());
							}
						}
					}.start();
				}
			} finally {
				if (contentIn != null) {
					try {
						contentIn.close();
					} catch (Exception ignored) {
					}
				}
				if (output != null) {
					try {
						output.close();
					} catch (Exception ignored) {
					}
				}
				if (httpConn != null) {
					try {
						httpConn.close();
					} catch (Exception ignored) {
					}
				}
			}

		}
	}

}
