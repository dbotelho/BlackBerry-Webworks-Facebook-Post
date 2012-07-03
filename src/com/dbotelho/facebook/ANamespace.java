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

import com.dbotelho.facebook.AFunction;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.component.Dialog;
/**
 * 
 * @author Daniel Botelho (www.dbotelho.com)
 *
 */
public class ANamespace extends Scriptable {
	private AFunction _openFunction;

	public static final String NAME = "com.dbotelho.facebook";
	public static final String FACEBOOK_CONN_CLOSE = "close";

	public ANamespace() {
		_openFunction = new AFunction();
	}

	public Object getField(String name) throws Exception {

		if (name.equals(AFunction.FACEBOOK_POST_FUNCTION) || name.equals(AFunction.FACEBOOK_CONN_FUNCTION)) {
			_openFunction.setFunctionType(name);
			return _openFunction;
		}else if(name.equals(FACEBOOK_CONN_CLOSE)) 
		{
			try
			{
				synchronized(Application.getEventLock()) {
					_openFunction.browserScreen.close();
				}
				return new Boolean(true);
			}
			catch(Exception e)
			{
				return new Boolean(false);
			}
		}
		return super.getField(name);
	}

	public boolean putField(String field, Object value) throws Exception {
		return super.putField(field, value);
	}
}