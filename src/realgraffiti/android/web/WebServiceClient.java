package realgraffiti.android.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.google.gson.Gson;

public class WebServiceClient {
	 
    public enum RequestMethod {
		POST
	}

    private ArrayList <NameValuePair> headers;
    private Map<String, Object> params;
    private Map<String, byte[]> files;
    
    private String url;
 
    private int responseCode;
    private String message;
 
    private String response;
 
    public WebServiceClient(String url)
    {
        this.url = url;
        params = new HashMap<String, Object> ();
        headers = new ArrayList<NameValuePair>();
        files = new HashMap<String, byte[]>();
    }
    
    public String getResponse() {
        return response;
    }
 
    public String getErrorMessage() {
        return message;
    }
 
    public int getResponseCode() {
        return responseCode;
    }
 
 
    public void addParam(String name, Object value)
    {
        params.put(name, value);
    }
 
    public void addHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }
 
    public void addFile(String name, byte[] content){
    	files.put(name, content);
    }
    
    public void execute(RequestMethod method)
    {
		HttpPost request = new HttpPost(url);
 		
		addHeadersToRequest(request);
		
		if(files.size() > 0){
			prepareMultipartPostRequest(request);
		}
		else{
			preparePostRequest(request);
		}
		
		executeRequest(request, url);
	}

	private void preparePostRequest(HttpPost request) {
		// TODO Auto-generated method stub
		
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		
		Gson jsonSerializer = new Gson();
		
		for(Entry<String, Object> entry: params.entrySet()){
			StringBody paramValue;

			String json = jsonSerializer.toJson(entry.getValue());
			Log.d("realgraffiti", "json of " + entry.getKey() + " :" + json);
						
			parameters.add(new BasicNameValuePair(entry.getKey(), json));
		}
		
		try {
			request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new RestClientException("RestClient error", e);
		}
	}

	private void addHeadersToRequest(HttpPost request) {
		for(NameValuePair h : headers)
		{
		    request.addHeader(h.getName(), h.getValue());
		}
	}

	private void prepareMultipartPostRequest(HttpPost request) {	
		MultipartEntity httpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		addFilesToMultipartEntity(httpEntity);
		addParametersToMultipartEntity(httpEntity);
		
		request.setEntity(httpEntity);	
	}

	private void addParametersToMultipartEntity(MultipartEntity httpEntity) {
		if(!params.isEmpty()){
			Gson jsonSerializer = new Gson();
			for(Entry<String, Object> entry: params.entrySet()){
				String json = jsonSerializer.toJson(entry.getValue());
				Log.d("realgraffiti", "json of " + entry.getKey() + " :" + json);
				
				StringBody paramValue;
				try {
					paramValue = new StringBody(json);
				} catch (UnsupportedEncodingException e) {
					throw new RestClientException("RestClient Error", e);
				}
				httpEntity.addPart(entry.getKey(), paramValue);
			}
		}
	}

	private void addFilesToMultipartEntity(MultipartEntity httpEntity) {
		int i = 0;
		for(String name:files.keySet()){
			ByteArrayBody fileContent = new ByteArrayBody(files.get(name), name);
			httpEntity.addPart("file" + i, fileContent);
		}
	}

    private void executeRequest(HttpUriRequest request, String url)
    {
        HttpClient client = new DefaultHttpClient();
 
        HttpResponse httpResponse;
 
        try {
            httpResponse = client.execute(request);
            
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
            
            Log.d("realgraffiti", "Response code: " + responseCode);
            
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null) {
 
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);
                Log.d("realgraffiti", "Response content: " + response);
                // Closing the input stream will trigger connection release
                instream.close();
            }
 
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            throw new RestClientException("RestClient error", e);
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            throw new RestClientException("RestClient error", e);
        }
    }
 
    private static String convertStreamToString(InputStream is) {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw new RestClientException("RestClient error", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	throw new RestClientException("RestClient error", e);
            }
        }
        return sb.toString();
    }
    
    public Object getResponseObject(Type type){
    	Gson gson = new Gson();
    	String json = getResponse().trim();
    	Object responseObject = gson.fromJson(json, type);
    	
    	return responseObject;
    }
    
    @SuppressWarnings("serial")
	public static class RestClientException extends RuntimeException{
    	public RestClientException(String message){
    		super(message);
    	}
    	
    	public RestClientException(String message, Throwable innerException){
    		super(message, innerException);
    	}
    }
}