package com.openmeap.thinclient.update;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;

import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.Application;
import com.openmeap.protocol.dto.ApplicationInstallation;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.SLIC;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.thinclient.AppMgmtClientFactory;
import com.openmeap.thinclient.LocalStorage;
import com.openmeap.thinclient.SLICConfig;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.util.HttpRequestExecuterFactory;
import com.openmeap.util.Utils;

/**
 * Handles all the business logic for performing an update.
 * Pulled into SLIC core for re-use between Android and RIM OS
 * @author schang
 */
public class UpdateHandler {
	
	private SLICConfig config = null;
	private LocalStorage storage = null;
	private Boolean interrupt = false;
	
	public UpdateHandler(SLICConfig config, LocalStorage storage) {
		this.setSLICConfig(config);
		this.setLocalStorage(storage);
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
		config.setLastUpdateAttempt(new Date().getTime());
		
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
		        	} catch( UpdateException ue ) {
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
			} catch(UpdateException ue) {
				config.setLastUpdateResult(ue.getUpdateResult().toString());
				throw new RuntimeException(ue);
			}
		}
	}
	
	public void clearInterruptFlag() {
		synchronized(interrupt) {
			interrupt = false;
		}
	}
	
	public void interruptRunningUpdate() {
		synchronized(interrupt) {
			interrupt = true;
		}
	}
	
	private void _handleUpdate(UpdateStatus update, StatusChangeHandler eventHandler) throws UpdateException {
		
		String lastUpdateResult = config.getLastUpdateResult();
		
		Boolean hasTimedOut = hasUpdatePendingTimedOut();
		if( !hasTimedOut && lastUpdateResult!=null && lastUpdateResult.compareTo(UpdateResult.PENDING.toString())==0 ) {
			return;
		} else {
			config.setLastUpdateResult(UpdateResult.PENDING.toString());
		}
		
		UpdateHeader updateHeader = update.getUpdateHeader();
		
		// if the new version is the original version,
		// then we'll just update the app version, delete internal storage and return
		String versionId = updateHeader.getVersionIdentifier();
		if( config.isVersionOriginal(versionId) ) {
			config.setApplicationVersion(versionId);
			config.setArchiveHash(updateHeader.getHash().getValue());
     		storage.resetStorage();
     		config.setLastUpdateResult(UpdateResult.SUCCESS.toString());
     		return;
		}
		
		if( !deviceHasEnoughSpace(update) ) {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			throw new UpdateException(UpdateResult.OUT_OF_SPACE,"Need more space to install than is available");
		}
		
		try {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			if( !downloadToArchive(update, eventHandler) ) {
				return;
			}
		} catch( IOException ioe ) {
			// TODO: whether this is a deal breaker or not should be configurable.  client should have the ability to override default behavior.  default behavior should be informative
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"May need more space to install than is available",ioe);
		} 
		
		try {
			 
			if( !archiveIsValid(update) ) {
				throw new UpdateException(UpdateResult.HASH_MISMATCH,"hash value of update does not match file hash value");
			}
				
			
			installArchive(update);
			
			config.setLastUpdateResult(UpdateResult.SUCCESS.toString());
			
			// at this point, the archive should be of no use to us
			storage.deleteImportArchive();
			
			// delete the content at the old internal storage prefix
			// TODO: decide whether this should be done pending notifying of the update or not
			config.setApplicationVersion(update.getUpdateHeader().getVersionIdentifier());
			config.setArchiveHash(update.getUpdateHeader().getHash().getValue());
			
			storage.resetStorage();
			
			String newPrefix = "com.openmeap.storage."+update.getUpdateHeader().getHash().getValue();
			config.setStorageLocation(newPrefix);

			config.setApplicationUpdated(true);
			
			if( eventHandler!=null ) { 
				update.setComplete(true);
	        	eventHandler.onStatusChange(update);
	        }

		} catch( IOException ioe ) {
			// TODO: leave it up the customer to determine how to handle this.
			throw new UpdateException(UpdateResult.IO_EXCEPTION,"An IOException occurred",ioe);
		} catch( NoSuchAlgorithmException nsae ) {
			throw new UpdateException(UpdateResult.PLATFORM,"The Java platform of the device does not support the hash algorithm used.",nsae);
		}
	}
	
	public Boolean deviceHasEnoughSpace(UpdateStatus update) {
		// test to make sure the device has enough space for the installation
		Long avail = storage.getBytesFree();
		return avail.compareTo(update.getUpdateHeader().getInstallNeeds()) > 0; 
	}
	
	/**
	 * 
	 * @param update
	 * @param eventHandler
	 * @return true if completed, false if interrupted
	 * @throws IOException
	 */
	public Boolean downloadToArchive(UpdateStatus update, StatusChangeHandler eventHandler) throws UpdateException, IOException {
		// download the file to import.zip
		OutputStream os = null;
		InputStream is = null;
		HttpRequestExecuter requester = HttpRequestExecuterFactory.newDefault();
		HttpResponse updateRequestResponse = requester.get(update.getUpdateHeader().getUpdateUrl());
		if( updateRequestResponse.getStatusLine().getStatusCode()!=200 )
			throw new UpdateException(UpdateResult.RESPONSE_STATUS_CODE,"Status was "+updateRequestResponse.getStatusLine().getStatusCode()+", expecting 200" );
		try {
			os = storage.getImportArchiveOutputStream();
			is = updateRequestResponse.getEntity().getContent();

			byte[] bytes = new byte[1024];
	        int count = is.read(bytes);
	        
	        int contentLength = (int)updateRequestResponse.getEntity().getContentLength();
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
	        	synchronized(interrupt) {
		        	if( interrupt ) {
		        		clearInterruptFlag();
		        		throw new UpdateException(UpdateResult.INTERRUPTED,"Download of archive was interrupted");
		        	}
	        	}
	        }
		} finally {
			os.close();
			is.close();
			
			// have to hang on to the requester till the download is complete,
			// so that we can retain control over when the connection manager shut's down
			requester.shutdown();
		}
		return true;
	}
	
	public Boolean archiveIsValid(UpdateStatus update) throws IOException, NoSuchAlgorithmException {
		// validate the zip file against the hash of the response
		InputStream fis = storage.getImportArchiveInputStream();
		try {
			String hashValue = Utils.hashInputStream(
					update.getUpdateHeader().getHash().getAlgorithm().value(), fis);
			
			// TODO: handle hash validation failure differently
			
			if( !hashValue.equals(update.getUpdateHeader().getHash().getValue()) ) {
				return false;
			}
			return true;
		} finally {
			fis.close();
		}
	}
	
	public void installArchive(UpdateStatus update) throws UpdateException, IOException {
		
		// at this point, we've verified that:
		//   1) we have enough space on the device
		//   2) the archive downloaded is what was expected

		ZipInputStream zis = null;
		String newPrefix = "com.openmeap.storage."+update.getUpdateHeader().getHash().getValue();
		try {
			zis = new ZipInputStream( storage.getImportArchiveInputStream() );
		    ZipEntry ze;
		    while ((ze = zis.getNextEntry()) != null) {
		    	if( ze.isDirectory() )
		    		continue;
		        FileOutputStream baos = storage.openFileOutputStream(newPrefix,ze.getName());
		        try {
		        	byte[] buffer = new byte[1024];
		        	int count;
		        	while ((count = zis.read(buffer)) != -1) {
		        		baos.write(buffer, 0, count);
		        	}
		        }
		        catch( Exception e ) {
		        	;// TODO: something, for the love of god.
		        }
		        finally {
		        	baos.close();
		        }
		    }
		} catch( IOException e ) {
			
			// delete the recently unzipped assets
			
			throw new UpdateException(UpdateResult.IMPORT_UNZIP,"Failed to extract import archive.",e);
		} finally {
			if( zis!=null )
				zis.close();
		}
	}
	
	private Boolean hasUpdatePendingTimedOut() {
		Integer pendingTimeout = config.getUpdatePendingTimeout();
		Long lastAttempt = config.getLastUpdateAttempt();
		Long currentTime = new Date().getTime();
		if( lastAttempt!=null ) {
			return currentTime > lastAttempt+(pendingTimeout*1000);
		}
		return true;
	}
	
	/* ACCESSORS BELOW HERE */
	
	public void setLocalStorage(LocalStorage storage) {
		this.storage = storage;
	}
	public LocalStorage getLocalStorage() {
		return this.storage;
	}
	
	public void setSLICConfig(SLICConfig config) {
		this.config = config;
	}
	public SLICConfig getSLICConfig() {
		return this.config;
	}
}
