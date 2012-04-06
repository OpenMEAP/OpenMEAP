package com.openmeap.util;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;

import com.openmeap.util.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.params.CoreProtocolPNames;

import com.openmeap.constants.FormConstants;

public class FileHandlingHttpRequestExecuterImpl extends HttpRequestExecuterImpl {
	
	@Override
	public HttpResponse postData(String url, Map getParams, Map postParams) throws HttpRequestException {
		
		// test to determine whether this is a file upload or not.
		Boolean isFileUpload = false;
		for( Object o : postParams.values() ) {
			if( o instanceof File ) {
				isFileUpload = true;
				break;
			}
		}
		
		if( isFileUpload ) {
			try {
				HttpPost httpPost = new HttpPost(createUrl(url,getParams));
				
				httpPost.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
				for( Object o : postParams.entrySet() ) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>)o;
					
					if( entry.getValue() instanceof File ) {
						
						// For File parameters
						File file = (File)entry.getValue();
						FileNameMap fileNameMap = URLConnection.getFileNameMap();
						String type = fileNameMap.getContentTypeFor(file.toURL().toString());
						
						entity.addPart( entry.getKey(), new FileBody((( File ) entry.getValue() ), type ));
					} else {
						
						// For usual String parameters
						entity.addPart( entry.getKey(), new StringBody( entry.getValue().toString(), "text/plain", Charset.forName( FormConstants.CHAR_ENC_DEFAULT )));
					}
				}
					
				httpPost.setEntity(entity);
			
				return execute(httpPost);
			} catch(Exception e) {
				throw new HttpRequestException(e);
			}
		} else {
			
			return super.postData(url,getParams,postParams);
		}
	}
}
