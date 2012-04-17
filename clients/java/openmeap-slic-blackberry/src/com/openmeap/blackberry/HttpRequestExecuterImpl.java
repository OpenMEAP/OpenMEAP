package com.openmeap.blackberry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;

import org.w3c.dom.Document;

import me.regexp.RE;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.transport.*;
import net.rim.device.api.io.transport.options.*;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.ui.UiApplication;

import com.openmeap.constants.FormConstants;
import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStream;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.http.HttpHeader;
import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.http.HttpResponseImpl;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

public class HttpRequestExecuterImpl implements HttpRequestExecuter {
	
	private final static String STATUS_LINE_PATTERN = "([^\\s]*)\\s([^\\s]*)\\s(.*)";
	
	public HttpResponse postXml(String url, String xmlData) throws HttpRequestException {
		
		return makeRequest(url,xmlData.getBytes(),new HttpHeader[]{new HttpHeader("content-type","text/xml")});
	}

	public HttpResponse postData(String url, Hashtable params) throws HttpRequestException {
		
		try {
			return makeRequest(url,Utils.createParamsString(params).getBytes(),null);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse postData(String url, Hashtable urlParams, Hashtable postParams) throws HttpRequestException {
		
		try {
			return postData(url+"?"+Utils.createParamsString(urlParams),postParams);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse get(String url) throws HttpRequestException {
		return makeRequest(url,null,null);
	}

	public HttpResponse get(String url, Hashtable params) throws HttpRequestException {
		
		try {
			return makeRequest(url,Utils.createParamsString(params).getBytes(),null);
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException(e);
		}
	}

	public void shutdown() {

	}

	private HttpResponse makeRequest(String url, byte[] postData, HttpHeader[] headers) throws HttpRequestException {
		
		String forwardUrl = null;
		
		int[] intTransports = {
				TransportInfo.TRANSPORT_TCP_WIFI,
				TransportInfo.TRANSPORT_WAP2,
				TransportInfo.TRANSPORT_TCP_CELLULAR
			};
		
		TcpCellularOptions tcpOptions = new TcpCellularOptions();
		
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setTransportTypeOptions(TransportInfo.TRANSPORT_TCP_CELLULAR, tcpOptions);
		factory.setAttemptsLimit(5);
		
		HttpResponseImpl response = new HttpResponseImpl();
        ConnectionDescriptor cd = factory.getConnection(url);
        if(cd != null) 
        {
            Connection c = cd.getConnection();
            OutputConnection oc = (OutputConnection) c;
            InputConnection ic = (InputConnection) c;
            
            OutputStream os = null;
            InputStream is = null;
            
            try {
	            os = oc.openOutputStream();
	            if(postData==null) {
	            	makeGETRequest(url,headers,os);
	            } else {
	            	makePOSTRequest(url,headers,postData,os);
	            }
	            
	            is = ic.openInputStream();
	            int statusCode = readStatusLine(is);
	            HttpHeader[] responseHeaders = readHeaders(is);
	            response.setStatusCode(statusCode);
	            response.setHeaders(responseHeaders);
	            response.setResponseBody(is);
	            
	            // see if we need to handle a redirect
	            if(response.getStatusCode()==301 || response.getStatusCode()==303) {
	            	for(int i=0;i<responseHeaders.length;i++) {
	            		HttpHeader header = responseHeaders[i];
	            		if( header.getKey().equalsIgnoreCase("location") ) {
	            			forwardUrl = header.getValue();
	            			break;
	            		}
	            	}
	            }
	            
            } catch(Exception e) {
            	throw new HttpRequestException(e);
            } finally {
            	if(os!=null) {
            		try {
						os.close();
					} catch (IOException e) {
						throw new GenericRuntimeException(e);
					}
            	}
            }
        }
        if(forwardUrl==null) {
        	return response;
        } else {
       		try {
				response.getResponseBody().close();
			} catch (IOException e) {
				throw new GenericRuntimeException();
			}
       		return makeRequest(forwardUrl,postData,headers);
        }        
	}
	
	private void makePOSTRequest(String url, HttpHeader[] headers, byte[] postData, OutputStream os) throws IOException, DigestException, HttpRequestException {
		
		os.write(("POST "+url+" HTTP/1.0\r\n").getBytes());
		os.write(("Content-length: "+postData.length+"\r\n").getBytes());
		os.write(("Content-MD5: "+getMd5OfPostData(postData)+"\r\n").getBytes());
		if(headers!=null) {
	        for(int i=0; i<headers.length; i++) {
	        	HttpHeader header = headers[i];
	        	os.write((header.getKey()+": "+header.getValue()+"\r\n").getBytes());
	        }	           
		}
        os.write("\r\n".getBytes());
        os.write(postData);
        os.flush();
	}
	
	private String getMd5OfPostData(byte[] postData) throws HttpRequestException {
		try
		{
			DigestInputStream md = DigestInputStreamFactory.getDigestInputStream("md5");
	    	md.setInputStream(new ByteArrayInputStream(postData));
			byte[] md5Sum;
			md5Sum = md.digest();
		    byte[] encoded = Base64OutputStream.encode(md5Sum,0,md5Sum.length,false,false);
		    return new String(encoded, FormConstants.CHAR_ENC_DEFAULT);
		} catch (Exception ioe) {
			throw new HttpRequestException(ioe);
		}
	}
	
	private void makeGETRequest(String url, HttpHeader[] headers, OutputStream os) throws IOException {
		
		os.write(("GET "+url+" HTTP/1.0\r\n").getBytes());
		if(headers!=null) {
	        for(int i=0; i<headers.length; i++) {
	        	HttpHeader header = headers[i];
	        	os.write((header.getKey()+": "+header.getValue()+"\r\n").getBytes());
	        }	           
		}
        os.write("\r\n".getBytes());
        os.flush();
	}
	
	private int readStatusLine(InputStream is) throws IOException {
		
		String statusLine;
		statusLine = Utils.readLine(is,FormConstants.CHAR_ENC_DEFAULT);
		RE r = new RE(STATUS_LINE_PATTERN);
		int code = (-1);
		if(r.match(statusLine)) {
			code = Integer.parseInt(r.getParen(2));
		}
		return code;
	}
	
	private HttpHeader[] readHeaders(InputStream is) throws IOException, HttpRequestException {
		
		Vector headers = new Vector();
		String line;
		while(true) {
			line = Utils.readLine(is, FormConstants.CHAR_ENC_DEFAULT);
			if(line.length()==0) {
				break;
			}
			RE m = new RE("([^:]*):(.*)");
			if(m.match(line)) {
				HttpHeader header = new HttpHeader();
				header.setKey(m.getParen(1).trim());
				header.setValue(m.getParen(2).trim());
				headers.addElement(header);
			} else {
				throw new HttpRequestException("Expecting a header line.");
			}
		}
		HttpHeader[] retVals = new HttpHeader[headers.size()];
		for(int i=0;i<headers.size();i++) {
			retVals[i]=(HttpHeader) headers.elementAt(i);
		}
		return retVals;
	}
	
	private static class Listener extends BrowserFieldListener {
		
		private Thread caller;
		private HttpResponseImpl response = new HttpResponseImpl();
		
		public HttpResponse getResponse() {
			return response;
		}
		
		public Listener setCallingThread(Thread caller) {
			this.caller = caller;
			return this;
		}
		
		public void documentLoaded(BrowserField browserField, Document document) {
			response.setStatusCode(200);
			response.setResponseBody(new ByteArrayInputStream(document.getTextContent().getBytes()));
			caller.notifyAll();
		}
		
		public void documentError(BrowserField browserField, Document document) {
			response.setStatusCode(500);
			response.setResponseBody(new ByteArrayInputStream(document.getTextContent().getBytes()));
			caller.notifyAll();
		}
	}	
}
