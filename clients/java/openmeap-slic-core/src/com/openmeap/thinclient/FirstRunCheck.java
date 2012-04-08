package com.openmeap.thinclient;

import java.io.ByteArrayInputStream;
import java.net.URL;

import com.openmeap.util.Utils;

/**
 * Makes the first run hit to usage.openmeap.com.
 *
 * Dear Developer/Project Management,
 *
 * We've created this function to make a single hit to our tracking url
 * per unique install.  An effort has been made to make it secure and
 * non-reversible.  An effort has been made so that, even should it fail,
 * it only happens once.
 *
 * We're hoping you'll leave this code functional in your production 
 * release so that we can build-up value by tracking unique installs.
 *
 * Yours truly, OpenMEAP
 */
public class FirstRunCheck implements Runnable {
	private SLICConfig config = null;
	private String macAddress = null;
	public FirstRunCheck(SLICConfig config, String macAddress) {
		this.config = config;
		this.macAddress = macAddress;
	}
	public void run() {
		if( config.isDevelopmentMode().equals(Boolean.TRUE) ) {
			return;
		}
		if( config.getNotFirstRun()==null ) {
			config.setNotFirstRun(Boolean.TRUE);
			try {
				String macWithSalt = macAddress+".OPENMEAP#$!@3__234";
				String hashValue = Utils.hashInputStream("sha1", new ByteArrayInputStream(macWithSalt.getBytes("UTF-8")));
				URL url = new URL("http://usage.openmeap.com/tracker.gif?hash="+hashValue);
				url.getFile();
			} catch( Exception ioe ) {
				// again, we don't want to be a bother here...let's just bail
				return;
			}
		}
	}
}
