/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the LGPLv3                                                #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU Lesser General Public License as published #
 #    by the Free Software Foundation, either version 3 of the License, or     #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU Lesser General Public License for more details.                      #
 #                                                                             #
 #    You should have received a copy of the GNU Lesser General Public License #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.thinclient.update;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.openmeap.constants.FormConstants;
import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpRequestExecuterFactory;
import com.openmeap.http.HttpResponse;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.Application;
import com.openmeap.protocol.dto.ApplicationInstallation;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.SLIC;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.dto.UpdateType;
import com.openmeap.thinclient.AppMgmtClientFactory;
import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.LocalStorageException;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

/**
 * Handles all the business logic for performing an update.
 * Pulled into SLIC core for re-use between Android and RIM OS
 * @author schang
 */
public class UpdateHandler {
	
	private SLICConfig config = null;
	private LocalStorage storage = null;
	private OmMainActivity activity = null;
	private Object interruptLock = new Object();
	private Boolean interrupt = Boolean.FALSE;
	
	public UpdateHandler(OmMainActivity activity, SLICConfig config, LocalStorage storage) {
		this.activity = activity;
		this.setSLICConfig(config);
		this.setLocalStorage(storage);
	}
	
	private void setLocalStorage(LocalStorage storage2) {
		this.storage = storage2;
	}

	private void setSLICConfig(SLICConfig config2) {
		this.config = config2;
	}

	public void handleUpdate() throws WebServiceException {
    	UpdateHeader update = checkForUpdate();
    	if( update!=null ) {
    		this.handleUpdate(update);
    	}
	}
	
	public UpdateHeader checkForUpdate() throws WebServiceException {
		// we'll go ahead and flip the flag that we tried to update now
    	// at the beginning of our intent
		config.setLastUpdateAttempt(new Long(new Date().getTime()));
		
		// put together the communication coordination request
    	ConnectionOpenRequest request = getConnectionOpenRequest();
    	ConnectionOpenResponse response = makeConnectionOpenRequest(request);
    	
    	// we'll use this from now till the next update
    	config.setLastAuthToken(response.getAuthToken());
    	
    	return response.getUpdate();
	}
	
	public ConnectionOpenResponse makeConnectionOpenRequest(ConnectionOpenRequest request) throws WebServiceException {
		// phone home
    	ApplicationManagementService client = AppMgmtClientFactory.newDefault(config.getAppMgmtServiceUrl());
    	ConnectionOpenResponse response = null;
   		response = client.connectionOpen(request);
    	return response;
	}
	
	/**
	 * Uses the configuration to put together a ConnectionOpenRequest object.
	 * @return
	 */
	public ConnectionOpenRequest getConnectionOpenRequest() {
		
    	ConnectionOpenRequest request = new ConnectionOpenRequest();
    	request.setApplication( new Application() );
    	request.getApplication().setInstallation( new ApplicationInstallation() );
    	request.setSlic(new SLIC());
    	
    	Application app = request.getApplication();
    	app.setName(config.getApplicationName());
    	app.setVersionId(config.getApplicationVersion());
    	app.setHashValue(config.getArchiveHash()!=null ? config.getArchiveHash() : "");
    	
    	ApplicationInstallation dev = request.getApplication().getInstallation();
    	dev.setUuid(config.getDeviceUuid());
    	
    	SLIC slic = request.getSlic();
    	slic.setVersionId(SLICConfig.SLIC_VERSION);
    	
    	return request;
	}
	
	/**
	 * Interface so that the HTML5 application can provide a call-back
	 * for update status.
	 *   
	 * @author schang
	 */
	public interface StatusChangeHandler {
		/**
		 * Gets called each percent of completion throughout the duration of the download
		 * @param header
		 * @param bytesRemaining
		 * @param complete
		 */
		public void onStatusChange(UpdateStatus update);
	}
	
	public void handleUpdate(UpdateHeader updateHeader) {
		handleUpdate(updateHeader,null);		
	}
	
	/**
	 * Handles processing an update requested by the application management service
	 * @param updateHeader
	 */
	public void handleUpdate(final UpdateHeader updateHeader, final StatusChangeHandler eventHandler) {
		
		final UpdateStatus update = new UpdateStatus(updateHeader,0,false);
		
		if( eventHandler!=null ) {
			Thread activeUpdateThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
		            	_handleUpdate(update,eventHandler);
		        	} catch( Exception e ) {
		        		UpdateException ue = null;
		        		if(e instanceof UpdateException ) {
							ue = (UpdateException)e;
						} else {
							ue = new UpdateException(UpdateResult.UNDEFINED,e.getMessage(),e);
						}
		        		config.setLastUpdateResult(ue.getUpdateResult().toString());
		        		update.setError(ue);
		        		eventHandler.onStatusChange(update);
		        	}
		        }
		    });
			activeUpdateThread.start();
		} else {
			try {
				_handleUpdate(update,eventHandler);
			} catch(Exception e) {
				UpdateException ue = null;
        		if(e instanceof UpdateException ) {
					ue = (UpdateException)e;
				} else {
					ue = new UpdateException(UpdateResult.UNDEFINED,e.getMessage(),e);
				}
        		config.setLastUpdateResult(ue.getUpdateResult().toString());
        		throw new GenericRuntimeException(ue.getMessage(),ue);
			}
		}
	}
	
	public void clearInterruptFlag() {
		synchronized(interruptLock) {
			interrupt = Boolean.FALSE;
		}
	}
	
	public void interruptRunningUpdate() {
		synchronized(interruptLock) {
			interrupt = Boolean.TRUE;
		}
	}
	
	private void _handleUpdate(UpdateStatus update, StatusChangeHandler eventHandler) throws UpdateException {
		
		String lastUpdateResult = config.getLastUpdateResult();
		
		boolean hasTimedOut = hasUpdatePendingTimedOut();
		if( !hasTimedOut && lastUpdateResult!=null && lastUpdateResult.compareTo(UpdateResult.PENDING.toString())==0 ) {
			return;
		} else {
			config.setLastUpdateResult(UpdateResult.PENDING.toString());
		}
		
		UpdateHeader updateHeader = update.getUpdateHeader();
		
		// if the new version is the original version,
		// then we'll just update the app version, delete internal storage and return
		String versionId = updateHeader.getVersionIdentifier();
		if( config.isVersionOriginal(versionId).booleanValue() ) {
			_revertToOriginal(update, eventHandler);
			return;
		}
		
		if( ! deviceHasEnoughSpace(update).booleanValue() ) {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			throw new UpdateException(UpdateResult.OUT_OF_SPACE,"Need more space to install than is available");
		}
		
		try {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			if( ! downloadToArchive(update, eventHandler).booleanValue() ) {
				return;
			}
		} catch( Exception ioe ) {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"An issue occurred downloading the archive",ioe);
		} 
		
		if( ! archiveIsValid(update).booleanValue() ) {
			throw new UpdateException(UpdateResult.HASH_MISMATCH,"hash value of update does not match file hash value");
		}
			
		
		installArchive(update);
		
		// at this point, the archive should be of no use to us
		try {
			storage.deleteImportArchive();
		} catch(LocalStorageException lse) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"Could not delete import archive",lse);
		}
		
		try {
			storage.resetStorage();
		} catch(LocalStorageException lse) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"Could not reset storage",lse);
		}
		
		config.setLastUpdateResult(UpdateResult.SUCCESS.toString());
		
		config.setApplicationVersion(update.getUpdateHeader().getVersionIdentifier());
		config.setArchiveHash(update.getUpdateHeader().getHash().getValue());
		
		String newPrefix = storage.getStorageRoot()+update.getUpdateHeader().getHash().getValue();
		config.setStorageLocation(newPrefix);

		config.setApplicationUpdated(Boolean.TRUE);
		
		if( eventHandler!=null ) { 
			update.setComplete(true);
        	eventHandler.onStatusChange(update);
        } else {
        	activity.restart();
        }
	}
	
	public Boolean deviceHasEnoughSpace(UpdateStatus update) throws UpdateException {
		// test to make sure the device has enough space for the installation
		Long avail;
		try {
			avail = storage.getBytesFree();
		} catch (LocalStorageException e) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"Could not determine the number of bytes available",e);
		}
		return new Boolean(avail.longValue() > update.getUpdateHeader().getInstallNeeds().longValue()); 
	}
	
	/**
	 * 
	 * @param update
	 * @param eventHandler
	 * @return true if completed, false if interrupted
	 * @throws IOException
	 */
	public Boolean downloadToArchive(UpdateStatus update, StatusChangeHandler eventHandler) throws UpdateException {
		// download the file to import.zip
		OutputStream os = null;
		InputStream is = null;
		HttpRequestExecuter requester = HttpRequestExecuterFactory.newDefault();
		HttpResponse updateRequestResponse;
		try {
			updateRequestResponse = requester.get(update.getUpdateHeader().getUpdateUrl());
		} catch(HttpRequestException e){
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"An issue occurred fetching the update archive",e);
		}
		if( updateRequestResponse.getStatusCode()!=200 )
			throw new UpdateException(UpdateResult.RESPONSE_STATUS_CODE,"Status was "+updateRequestResponse.getStatusCode()+", expecting 200" );
		try {
			os = storage.getImportArchiveOutputStream();
			is = updateRequestResponse.getResponseBody();

			byte[] bytes = new byte[1024];
	        int count = is.read(bytes);
	        
	        int contentLength = (int)updateRequestResponse.getContentLength();
	        int contentDownloaded = 0;
	        int lastContentDownloaded = contentDownloaded;
	        int percent = contentLength/100;
	        
	        while( count!=(-1) ) {
	        	os.write(bytes,0,count);
	        	count = is.read(bytes);
	        	contentDownloaded += count;
	        	if( eventHandler!=null && lastContentDownloaded + percent < contentDownloaded ) {
	        		update.setBytesDownloaded(contentDownloaded);
	        		eventHandler.onStatusChange(update);
	        		lastContentDownloaded = contentDownloaded;
	        	}
	        	synchronized(interruptLock) {
		        	if( interrupt.booleanValue() ) {
		        		clearInterruptFlag();
		        		throw new UpdateException(UpdateResult.INTERRUPTED,"Download of archive was interrupted");
		        	}
	        	}
	        }
		} catch(IOException lse) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,lse.getMessage(),lse);
		} catch(LocalStorageException lse) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,lse.getMessage(),lse);
		} finally {
			try {
				storage.closeOutputStream(os);
				storage.closeInputStream(is);
			} catch (LocalStorageException e) {
				throw new UpdateException(UpdateResult.IO_EXCEPTION,e.getMessage(),e);
			}
			
			// have to hang on to the requester till the download is complete,
			// so that we can retain control over when the connection manager shut's down
			requester.shutdown();
		}
		return Boolean.TRUE;
	}
	
	public Boolean archiveIsValid(UpdateStatus update) throws UpdateException {
		try {
			// validate the zip file against the hash of the response
			InputStream fis = null;
			try {
				fis = storage.getImportArchiveInputStream();
				String hashValue = Utils.hashInputStream(update.getUpdateHeader().getHash().getAlgorithm().value(), fis);
				if( !hashValue.equals(update.getUpdateHeader().getHash().getValue()) ) {
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			} finally {
				storage.closeInputStream(fis);
			}
		} catch(Exception e) {
			throw new UpdateException(UpdateResult.UNDEFINED,"The archive failed validation",e);
		}
	}
	
	public void installArchive(UpdateStatus update) throws UpdateException {
		try {
			storage.unzipImportArchive(update);
		} catch (LocalStorageException e) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"The archive failed to install",e);
		}
	}
	
	private boolean hasUpdatePendingTimedOut() {
		Integer pendingTimeout = config.getUpdatePendingTimeout();
		Long lastAttempt = config.getLastUpdateAttempt();
		Long currentTime = new Long(new Date().getTime());
		if( lastAttempt!=null ) {
			return currentTime.longValue() > lastAttempt.longValue()+(pendingTimeout.intValue()*1000);
		}
		return true;
	}
	
	public void initialize(OmWebView webView) {
        
        // if this application is configured to fetch updates,
        // then check for them now
		activity.setReadyForUpdateCheck(false);
		Boolean shouldPerformUpdateCheck = activity.getConfig().shouldPerformUpdateCheck();
		webView = webView!=null ? webView : activity.createDefaultWebView();
		activity.runOnUiThread(new InitializeWebView(webView));
        if( shouldPerformUpdateCheck!=null && shouldPerformUpdateCheck.equals(Boolean.TRUE) ) {
        	new Thread(new UpdateCheck(webView)).start();
        }
	}
	
	private void _revertToOriginal(UpdateStatus update, StatusChangeHandler eventHandler) throws UpdateException {
		config.setApplicationVersion(update.getUpdateHeader().getVersionIdentifier());
		config.setArchiveHash(update.getUpdateHeader().getHash().getValue());
		try {
			storage.resetStorage();
		} catch(LocalStorageException lse) {
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"Could not reset storage",lse);
		}
 		config.setLastUpdateResult(UpdateResult.SUCCESS.toString());
 		if( eventHandler!=null ) { 
			update.setComplete(true);
        	eventHandler.onStatusChange(update);
        } else {
        	activity.restart();
        }
 		return;
	}
    
	private static String SOURCE_ENCODING = FormConstants.CHAR_ENC_DEFAULT;
	private static String CONTENT_TYPE = FormConstants.CONT_TYPE_HTML;
    private class InitializeWebView implements Runnable {
    	private OmWebView webView;
		public InitializeWebView(OmWebView webView) {
    		this.webView = webView;
    	}
    	public void run() {
	    	// here after, everything is handled by the html and javascript
	        try {
	        	Boolean justUpdated = config.getApplicationUpdated();
	        	if( justUpdated!=null && justUpdated.booleanValue()==true ) {
	        		config.setApplicationUpdated(Boolean.FALSE);
	        	}
	        	activity.setWebView(webView);
	        	String baseUrl = config.getAssetsBaseUrl();
	        	String pageContent = activity.getRootWebPageContent();
	        	webView.loadDataWithBaseURL(baseUrl, pageContent, CONTENT_TYPE, SOURCE_ENCODING, null);
        		activity.setContentView(webView);
	        } catch( Exception e ) {
	        	throw new GenericRuntimeException(e);
	        }
	    }
    }
    
    private class UpdateCheck implements Runnable {
    	final private OmWebView webView;
    	public UpdateCheck(OmWebView webView) {
    		this.webView = webView;
    	}
		public void run() {
			int count=0;
			while(activity.getReadyForUpdateCheck() && count<500) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					;
				}
				count++;
			}
			UpdateHeader update = null;
			WebServiceException err = null;
        	try {
        		update = checkForUpdate();
        	} catch( WebServiceException wse ) {
        		err = wse;
        	}
        	if( update!=null && update.getType()==UpdateType.IMMEDIATE ) {
        		try {
        			activity.runOnUiThread(new Runnable(){
        				public void run() {
        					activity.doToast("MANDATORY UPDATE\n\nThere is an immediate update.  The application will restart.  We apologise for any inconvenience.", true);
        					webView.clearView();
        				}
        			});
        			handleUpdate(update);
        			storage.setupSystemProperties();
        			update=null;
        		} catch( Exception e ) {
            		err = new WebServiceException(WebServiceException.TypeEnum.CLIENT_UPDATE,e.getMessage(),e);
            	}
        	} else {
        		try {
					webView.setUpdateHeader(update, err, storage.getBytesFree());
				} catch (LocalStorageException e) {
					throw new GenericRuntimeException(e);
				}
        	}
		}
    }
}
