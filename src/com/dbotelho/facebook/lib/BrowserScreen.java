package com.dbotelho.facebook.lib;

import java.rmi.ServerException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.html2.HTMLDocument;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.system.EventLogger;

import net.rim.device.api.script.ScriptableFunction;

import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.io.URI;

class MyFieldManager extends VerticalFieldManager {

	int width;
	int height;

	MyFieldManager(int w, int h) {
		super(Manager.VERTICAL_SCROLL | Manager.HORIZONTAL_SCROLL);
		width = w;
		height = h;
	}

	public void sublayout(int w, int h) {
		super.sublayout(w, h);
		setExtent(width, height);
	}
}

public class BrowserScreen extends MainScreen {
	BrowserField browserfield;
	private Dialog screen;
	// The progress bar
	private GaugeField progressBar;
	// The progress tracker object that will provide values to update the
	// progress bar (GaugeField)
	private BrowserFieldLoadProgressTracker progressTracker;
	private Runnable onClose;
	public void setOnCloseEvent(Runnable onClose){
		this.onClose = onClose;
	}
	
	public boolean onClose(){
		if(onClose != null){
			new Thread(onClose).start();
		}
		return super.onClose();
	}

	public String getRequestParameter(String url, String param) {
		Hashtable values = new Hashtable();

		int s = url.indexOf("?");
		int e = 0;

		while (s != -1) {
			e = url.indexOf("=", s);
			String name = url.substring(s + 1, e);
			s = e + 1;
			e = url.indexOf("&", s);

			if (e < 0) {
				values.put(name, url.substring(s, url.length()));
			} else {
				values.put(name, url.substring(s, e));
			}

			s = e;
		}

		for (Enumeration num = values.keys(); num.hasMoreElements();) {
			String key = (String) num.nextElement();
			if (key.equals(param)) {
				return (String) values.get(key);
			}
		}
		return null;
	}

	public BrowserScreen(String url, final ScriptableFunction func,
			final Object thiz) {
		
		new Thread(new Runnable() {

			public void run() {

				try {
					Object[] args = new Object[2];
					args[0] = "end";
					func.invoke(thiz, args);
				} catch (Exception e) {
				}
			}
		});
		screen = new Dialog(Dialog.OK,
				"Connecting to facebook. Please wait...", 0, Bitmap
						.getPredefinedBitmap(Bitmap.EXCLAMATION),
				Dialog.GLOBAL_STATUS);

		progressTracker = new BrowserFieldLoadProgressTracker(10f);
		BrowserFieldConfig config = new BrowserFieldConfig();
		config.setProperty(BrowserFieldConfig.NAVIGATION_MODE,
				BrowserFieldConfig.NAVIGATION_MODE_CARET);

		String unsabotageUserAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.56 Safari/536.5";
		config.setProperty(BrowserFieldConfig.USER_AGENT, unsabotageUserAgent);
		config.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		config.setProperty(BrowserFieldConfig.INITIAL_SCALE, new Float(1));
		config.setProperty(BrowserFieldConfig.ENABLE_COOKIES, Boolean.TRUE);
		config.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);


		MyFieldManager m = new MyFieldManager(Display.getWidth(), Display
				.getHeight());
		add(m);

		browserfield = new BrowserField(config);
		browserfield.getConfig().setProperty(BrowserFieldConfig.CONTROLLER, new FBProtocol(browserfield));
		progressBar = new GaugeField("", 0, 100, 0, Field.USE_ALL_WIDTH);

		BrowserFieldListener listener = new BrowserFieldListener() {
			public void downloadProgress(BrowserField browserField,
					final ContentReadEvent event) throws Exception {
				Application.getApplication().invokeLater(new Runnable() {
					public void run() {
						int value = (int) (100 * progressTracker
								.updateProgress(event));
						progressBar.setValue(value);

						progressBar.setVisualState(Field.VISUAL_STATE_NORMAL);
					}
				});
				super.downloadProgress(browserField, event);
			}

			public void documentCreated(BrowserField browserField,
					ScriptEngine scriptEngine, final Document document)
					throws Exception {
				insert(progressBar, 0);
				new Thread(new Runnable() {

					public void run() {
						try {
							Object[] args = new Object[1];
							args[0] = ((HTMLDocument)document).getURL();
							func.invoke(thiz, args);
						} catch (Exception e) {
						}
					}
				}).start();

				super.documentCreated(browserField, scriptEngine, document);
			}

			public void documentLoaded(BrowserField browserField,
					final Document document) throws Exception {
				new Thread(new Runnable() {

					public void run() {
						try {
							HTMLDocument html = (HTMLDocument) document;
							Object[] args = new Object[2];
							args[0] = html.getURL();
							args[1] = html.getBody().getTextContent();
							func.invoke(thiz, args);
						} catch (Exception e) {
						}
					}
				}).start();

				Application.getApplication().invokeLater(new Runnable() {
					public void run() {
						progressTracker.reset();
						progressBar.setValue(100);

						delete(progressBar);
						if (screen.isDisplayed()) {
							screen.close();
						}
					}
				});
				super.documentLoaded(browserField, document);
			}
		};
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				screen.show();
			}
		});
		browserfield.addListener(listener);
		browserfield.requestContent(url);


		m.add(browserfield);
	}

	public void openUrl(String url) {
		browserfield.requestContent(url);
	}
}

/**
 * BrowserFieldLoadProgressTracker - Keeps track of browser field page-load
 * progress
 * 
 * Challenge: - Number of resources (e.g., images, css, javascript) to load is
 * not know beforehand
 * 
 */
class BrowserFieldLoadProgressTracker {

	// The fraction used to split the remaining load amount in the progress bar
	// after a new resource is found (e.g., 2=half, 3=one third, 5=one fifth)
	// Bar progress typically moves fast at the beginning and slows down
	// progressively as we don't know how many resources still need to be loaded
	private float progressFactor;

	// The percentage value left until the progress bar is fully filled out
	// (initial value=1 or 100%)
	private float percentageLeft;

	// Stores info about the resources being loaded
	private Hashtable resourcesTable; // Map: Resource (Connection) --->

	// ProgressTrackerEntry

	// Stores info about a resource being loaded
	static class ProgressTrackerEntry {
		ProgressTrackerEntry(int bytesRead, int bytesToRead, float percentage) {
			this.bytesRead = bytesRead;
			this.bytesToRead = bytesToRead;
			this.percentage = percentage;
		}

		int bytesRead; // bytes read so far for this resource
		int bytesToRead; // total number of bytes that need to be read for this
		// resource
		float percentage; // the amount (in percentage) this resource represents

		// in the progress bar (e.g., 50%, 25%, 12.5%,
		// 6.25%)

		public void updateBytesRead(int bytesRead) {
			bytesRead += bytesRead;
			if (bytesRead > bytesToRead) { // this can happen when the final
				// size of a resource cannot be
				// anticipated
				bytesToRead = bytesRead;
			}
		}
	}

	public BrowserFieldLoadProgressTracker(float progressFactor) {
		this.progressFactor = progressFactor;
		reset();
	}

	public synchronized void reset() {
		resourcesTable = null;
		percentageLeft = 1f;
	}

	public synchronized float updateProgress(ContentReadEvent event) {

		if (resourcesTable == null) {
			resourcesTable = new Hashtable();
		}

		Object resourceBeingLoaded = event.getSource();

		ProgressTrackerEntry entry = (ProgressTrackerEntry) resourcesTable
				.get(resourceBeingLoaded);
		if (entry == null) {
			float progressPercentage = percentageLeft / progressFactor;
			percentageLeft -= progressPercentage;
			resourcesTable.put(resourceBeingLoaded, new ProgressTrackerEntry(
					event.getItemsRead(), event.getItemsToRead(),
					progressPercentage));
		} else {
			entry.updateBytesRead(event.getItemsRead());
		}

		return getProgressPercentage();
	}

	/**
	 * Returns the amount of items read so far in percentage so that the
	 * progress bar can be updated.
	 * 
	 * @return the amount of items read so far in percentage (0.0-1.0)
	 */
	public synchronized float getProgressPercentage() {
		float percentage = 0f;
		for (Enumeration e = resourcesTable.elements(); e.hasMoreElements();) {
			ProgressTrackerEntry entry = (ProgressTrackerEntry) e.nextElement();
			percentage += ((entry.bytesRead / entry.bytesToRead) * entry.percentage);
		}
		return percentage;
	}

}