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

import realgraffiti.common.data.JsonConverter;

import android.util.Log;



public class WebServiceClient {
	 
    public enum RequestMethod {
		POST,
		GET
	}

    private ArrayList <NameValuePair> _headers;
    private Map<String, Object> _params;
    private Map<String, byte[]> _files;
    
    private String _url;
 
    private int _responseCode;
    private String _message;
 
    private InputStream _response;
    private String _responseString;
 
    public WebServiceClient(String url)
    {
        this._url = url;
        _params = new HashMap<String, Object> ();
        _headers = new ArrayList<NameValuePair>();
        _files = new HashMap<String, byte[]>();
    }
    
    public String getResponseString() {
        //try {
			//return convertStreamToString(_response);
        	return _responseString;
		//} catch (IOException e) {
		//	throw new WebServiceClientException("WebServiceClient Error", e);
	    //}
    }
 
    public Object getResponseObject(Type typeOfObject){   	
    	String json = getResponseString().trim();
    	Object responseObject = JsonConverter.fromJson(json, typeOfObject);

    	return responseObject;
    }
    
    public InputStream getResponseInputStream(){
    	return _response;
    }
    
    public String getErrorMessage() {
        return _message;
    }
 
    public int getResponseCode() {
        return _responseCode;
    }
 
 
    public void addParam(String name, Object value)
    {
        _params.put(name, value);
        Log.d("RealGraffiti: WebServiceClient", "Add param " + name + ": " + value.toString());
    }
 
    public void addHeader(String name, String value)
    {
        _headers.add(new BasicNameValuePair(name, value));
        Log.d("RealGraffiti: WebServiceClient", "Add header " + name + ": " + value.toString());
    }
 
    public void addFile(String name, byte[] content){
    	_files.put(name, content);
    	Log.d("RealGraffiti: WebServiceClient", "Add File " + name);
    }
    
    
    public void execute(RequestMethod method)
    {
    	Log.d("RealGraffiti: WebServiceClient", "Execute start");
		HttpUriRequest request = null;
 		
		
		switch(method){
		case POST:		
			request = preparePostRequest(_url);
			break;
		case GET:
			request = prepareGetRequest(_url);
			break;
		}
		
		addHeadersToRequest(request);
		
		executeRequest(request, _url);
		Log.d("RealGraffiti: WebServiceClient", "Execute end");
	}

	private HttpGet prepareGetRequest(String url) {
        //add parameters
        String combinedParams = "";
        if(!_params.isEmpty()){
            combinedParams = createParametersQueryString();
        }
        Log.d("RealGraffiti: WebServiceClient", "prepering GET request: " + url + combinedParams);
        HttpGet request = new HttpGet(url + combinedParams);
        return request;
	}

	private String createParametersQueryString(){
		String combinedParams = "?";
		for(Entry<String, Object> entry: _params.entrySet())
		{
		    String paramString;
			try {
				paramString = entry.getKey() + "=" + URLEncoder.encode((String)entry.getValue(),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				throw new WebServiceClientException("RestClient error", e);
			}
		    if(combinedParams.length() > 1)
		    {
		        combinedParams  +=  "&" + paramString;
		    }
		    else
		    {
		        combinedParams += paramString;
		    }
		}
		return combinedParams;
	}

	private HttpPost preparePostRequest(String url) {
		HttpPost request = new HttpPost(url);
		if(_files.size() > 0){
			prepareMultipartPostRequest(request);
		}
		else{
			preparePostRequestWithoutFiles(request);
		}
		
		return request;
	}

	private void preparePostRequestWithoutFiles(HttpPost request) {
		// TODO Auto-generated method stub
		Log.d("RealGraffiti: WebServiceClient", "Prepare post request. URL: " + _url);
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		
		for(Entry<String, Object> entry: _params.entrySet()){
			StringBody paramValue;

			String json = JsonConverter.toJson(entry.getValue());
			Log.d("RealGraffiti: WebServiceClient", " json of " + entry.getKey() + " :" + json);
						
			parameters.add(new BasicNameValuePair(entry.getKey(), json));
		}
		
		try {
			request.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new WebServiceClientException("RestClient error", e);
		}
	}

	private void addHeadersToRequest(HttpUriRequest request) {
		for(NameValuePair h : _headers)
		{
		    request.addHeader(h.getName(), h.getValue());
		}
	}

	private void prepareMultipartPostRequest(HttpPost request) {
		Log.d("RealGraffiti: WebServiceClient", "Prepare Multipart post request. URL: " + _url);
		
		MultipartEntity httpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		addFilesToMultipartEntity(httpEntity);
		addParametersToMultipartEntity(httpEntity);
		
		request.setEntity(httpEntity);	
	}

	private void addParametersToMultipartEntity(MultipartEntity httpEntity) {
		if(!_params.isEmpty()){
			
			for(Entry<String, Object> entry: _params.entrySet()){
				String json = JsonConverter.toJson(entry.getValue());
				Log.d("RealGraffiti: WebServiceClient", "json of " + entry.getKey() + " :" + json);
				
				StringBody paramValue;
				try {
					paramValue = new StringBody(json);
				} catch (UnsupportedEncodingException e) {
					throw new WebServiceClientException("RestClient Error", e);
				}
				httpEntity.addPart(entry.getKey(), paramValue);
			}
		}
	}

	private void addFilesToMultipartEntity(MultipartEntity httpEntity) {
		int i = 0;
		for(String name:_files.keySet()){
			ByteArrayBody fileContent = new ByteArrayBody(_files.get(name), name);
			httpEntity.addPart("file" + i, fileContent);
		}
	}

    private void executeRequest(HttpUriRequest request, String url)
    {
        HttpClient client = new DefaultHttpClient();
 
        HttpResponse httpResponse;
 
        try {
            httpResponse = client.execute(request);
            
            _responseCode = httpResponse.getStatusLine().getStatusCode();
            _message = httpResponse.getStatusLine().getReasonPhrase();
            
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null) {
 
                InputStream instream = entity.getContent();
                _response = instream;
                _responseString = convertStreamToString(instream);
            }
            
            Log.d("RealGraffiti: WebServiceClient", "Execute complete.");
            Log.d("RealGraffiti: WebServiceClient", "Response code: " + _responseCode + " " + _message);
            Log.d("RealGraffiti: WebServiceClient", "Response : " + _responseString);
            
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            throw new WebServiceClientException("RestClient error", e);
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            throw new WebServiceClientException("RestClient error", e);
        }
    }
 
    private static String convertStreamToString(InputStream is) throws IOException {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw new WebServiceClientException("RestClient error", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	throw new WebServiceClientException("RestClient error", e);
            }
        }

        // Closing the input stream will trigger connection release
        is.close();
        
        return sb.toString();
    }
    

    
    @SuppressWarnings("serial")
	public static class WebServiceClientException extends RuntimeException{
    	public WebServiceClientException(String message){
    		super(message);
    	}
    	
    	public WebServiceClientException(String message, Throwable innerException){
    		super(message, innerException);
    	}
    }
}