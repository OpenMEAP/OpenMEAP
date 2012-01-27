package com.openmeap.thinclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.Hash;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.protocol.dto.Result;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.dto.UpdateNotification;
import com.openmeap.protocol.dto.UpdateType;
import com.openmeap.thinclient.update.UpdateException;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.util.Utils;

public class RESTAppMgmtClient implements ApplicationManagementService {
	
	private String serviceUrl = null;

	private HttpRequestExecuter requester = null;
	
	public RESTAppMgmtClient(String serviceUrl, HttpRequestExecuter requestMaker) {
		this.serviceUrl = serviceUrl;
		this.requester = requestMaker;
	}
	
	@SuppressWarnings("unused")
	public ConnectionOpenResponse connectionOpen(ConnectionOpenRequest request) throws WebServiceException {
		
		ConnectionOpenResponse response = null;
		
		Map<String,Object> postData = new HashMap<String,Object>();
		postData.put(UrlParamConstants.ACTION, "connection-open-request");
		postData.put(UrlParamConstants.DEVICE_UUID, request.getApplication().getInstallation().getUuid());
		postData.put(UrlParamConstants.APP_NAME, request.getApplication().getName());
		postData.put(UrlParamConstants.APP_VERSION, request.getApplication().getVersionId());
		postData.put(UrlParamConstants.APPARCH_HASH, request.getApplication().getHashValue());
		postData.put(UrlParamConstants.SLIC_VERSION, request.getSlic().getVersionId());
		
		HttpResponse httpResponse = null;
		InputSource responseInputSource = null;
		String responseText = null;
		try {
			httpResponse = requester.postData(serviceUrl, postData);
			if( httpResponse.getStatusLine().getStatusCode()!=200 ) {
				throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,"posting to the service resulted in a "+httpResponse.getStatusLine().getStatusCode()+" status code");
			}
			responseText = Utils.readInputStream(httpResponse.getEntity().getContent(), "UTF-8");
		} catch (ClientProtocolException e) {
			throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,e);
		} catch (IllegalStateException e) {
			throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,e);
		} catch (IOException e) {
			throw new WebServiceException(WebServiceException.TypeEnum.CLIENT_CONNECTION,e);
		}
		
		// now we parse the response into a ConnectionOpenResponse object
		if( responseText!=null ) {
			Result result = new Result();
			JSONObjectBuilder builder = new JSONObjectBuilder();
			try {
				result = (Result)builder.fromJSON(new JSONObject(responseText), result);
				if( result.getError()!=null ) {
					throw new WebServiceException(WebServiceException.TypeEnum.valueOf(result.getError().getCode().toString()),result.getError().getMessage());
				}
			} catch( JSONException e ) {
				throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,e);
			}
			response = result.getConnectionOpenResponse();
		}
		
		return response;
	}

	public void notifyUpdateResult(UpdateNotification notification) {
	}
}
