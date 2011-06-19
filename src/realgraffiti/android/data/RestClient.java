package realgraffiti.android.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class RestClient {
	 
    public enum RequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}

	private ArrayList <NameValuePair> params;
    private ArrayList <NameValuePair> headers;
    private Map<String, byte[]> files;
    
    private String url;
 
    private int responseCode;
    private String message;
 
    private String response;
 
    public String getResponse() {
        return response;
    }
 
    public String getErrorMessage() {
        return message;
    }
 
    public int getResponseCode() {
        return responseCode;
    }
 
    public RestClient(String url)
    {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
        files = new HashMap<String, byte[]>();
    }
 
    public void addParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
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
        switch(method) {
            case GET:
            {
                executeGetRequest();
                break;
            }
            case POST:
            {
                executePostRequest();
                break;
            }
        }
    }

	private void executePostRequest() {
		HttpPost request = new HttpPost(url);
 
		for(NameValuePair h : headers)
		{
		    request.addHeader(h.getName(), h.getValue());
		}
		
		if(files.size() > 0){
			prepareMultipartPostRequest(request);
		}
		else if(!params.isEmpty()){
		    preparePostRequest(request);
		}
		
		executeRequest(request, url);
	}

	private void preparePostRequest(HttpPost request) {
		try {
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new RestClientException("RestClient error", e);
		}
	}

	private void prepareMultipartPostRequest(HttpPost request) {
		int i = 0;
		
		for(String name:files.keySet()){
			MultipartEntity httpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			ByteArrayBody fileContent = new ByteArrayBody(files.get(name), name);
			httpEntity.addPart("file" + i, fileContent);
			
			if(!params.isEmpty()){
				for(NameValuePair param: params){
					StringBody paramValue;
					try {
						paramValue = new StringBody(param.getValue());
					} catch (UnsupportedEncodingException e) {
						throw new RestClientException("RestClient Error", e);
					}
					httpEntity.addPart(param.getName(), paramValue);
				}
			}
			request.setEntity(httpEntity);
		}
	}

	private void executeGetRequest() {
		//add parameters
		String combinedParams = "";
		if(!params.isEmpty()){
		    combinedParams += "?";
		    for(NameValuePair p : params)
		    {
		        String paramString;
				try {
					paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					throw new RestClientException("RestClient error", e);
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
		}
 
		HttpGet request = new HttpGet(url + combinedParams);
 
		//add headers
		for(NameValuePair h : headers)
		{
		    request.addHeader(h.getName(), h.getValue());
		}
 
		executeRequest(request, url);
	}
 
    private void executeRequest(HttpUriRequest request, String url)
    {
        HttpClient client = new DefaultHttpClient();
 
        HttpResponse httpResponse;
 
        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
 
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null) {
 
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);
 
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