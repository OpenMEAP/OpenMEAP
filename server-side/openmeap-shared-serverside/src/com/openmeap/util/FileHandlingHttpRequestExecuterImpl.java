package com.openmeap.util;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.params.CoreProtocolPNames;

public class FileHandlingHttpRequestExecuterImpl extends
		HttpRequestExecuterImpl {
	
	public HttpResponse postData(String url, Map<String, Object> params) throws ClientProtocolException, IOException {
		
		// test to determine whether this is a file upload or not.
		Boolean isFileUpload = false;
		for( Object o : params.values() ) {
			if( o instanceof File ) {
				isFileUpload = true;
				break;
			}
		}
		
		if( isFileUpload ) {
			HttpPost post = new HttpPost( url );
			
			getHttpClient().getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			
			MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
	
			for( Map.Entry<String, Object> entry : params.entrySet() ) {
				if( entry.getValue() instanceof File ) {
					// For File parameters
					File file = (File)entry.getValue();
					FileNameMap fileNameMap = URLConnection.getFileNameMap();
					String type = fileNameMap.getContentTypeFor(file.toURL().toString());
					
					entity.addPart( entry.getKey(), new FileBody((( File ) entry.getValue() ), type ));
				} else {
					// For usual String parameters
					entity.addPart( entry.getKey(), new StringBody( entry.getValue().toString(), "text/plain", Charset.forName( "UTF-8" )));
				}
			}
				
			post.setEntity( entity );
		
			return getHttpClient().execute(post);
		} else {
			
			return super.postData(url,params);
	    	
		}
	}
}
