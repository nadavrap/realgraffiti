package realgraffiti.android.data;


import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.reflect.TypeToken;
import android.content.Context;
import android.util.Log;

import realgraffiti.android.R;
import realgraffiti.android.data.WebServiceClient.RequestMethod;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dto.GraffitiDto;
import realgraffiti.common.dto.GraffitiLocationParametersDto;

public class RealGraffitiDataProxy implements RealGraffitiData{
	private Context _context;
	
	private final String ACTION_KEY = "action";
	private final String ACTION_PARAMETER_KEY = "object";
	
	public RealGraffitiDataProxy(Context context){
		_context = context;
	}
	
	@Override
	public boolean addNewGraffiti(GraffitiDto graffitiDto){	
		Log.d("realgraffiti", "Add new graffiti");
		
		String uploadUrl = getUploadUrl();
		Log.d("realgraffiti", "Upload url: " + uploadUrl);
		
		WebServiceClient client = new WebServiceClient(uploadUrl);
		client.addParam(ACTION_KEY, _context.getString(R.string.addGraffiti));
		client.addParam(ACTION_PARAMETER_KEY, graffitiDto);
		client.addFile("file", graffitiDto.get_imageData());
		
		client.execute(RequestMethod.POST);
		
		int responseCode = client.getResponseCode();
		String response = client.getResponse();

		return responseCode == HttpURLConnection.HTTP_OK;
	}

	private String getUploadUrl(){
		String serverPath = _context.getString(R.string.ServerPath);
		String url = serverPath + "/" +  _context.getString(R.string.serverInfoServlet);
		String action = _context.getString(R.string.getUploadUrlAction);
		WebServiceClient client = new WebServiceClient(url);
		client.addParam(ACTION_KEY, action);
		
		client.execute(RequestMethod.POST);
		
		String uploadUrl = client.getResponse();
		
		return serverPath + uploadUrl.trim();
	}
	
	@Override
	public Collection<GraffitiDto> getNearByGraffiti(
			GraffitiLocationParametersDto graffitiLocationParameters) {
		String url = _context.getString(R.string.ServerPath);
		url += "/" + _context.getString(R.string.RealGraffitiDataServlet);
		
		WebServiceClient client = new WebServiceClient(url);
		String actionName = _context.getString(R.string.getNearByGraffiti);
		client.addParam(ACTION_KEY, actionName);
		client.addParam(ACTION_PARAMETER_KEY, graffitiLocationParameters);
		
		client.execute(WebServiceClient.RequestMethod.POST);
	
		Type collectionType = new TypeToken<ArrayList<GraffitiDto>>(){}.getType();
		Collection<GraffitiDto> nearByGraffiti = (ArrayList<GraffitiDto>)client.getResponseObject(collectionType);
		return nearByGraffiti;		
	}
	
	@Override
	public byte[] getGraffitiImage(Long graffitiKey) {
		String url = _context.getString(R.string.ServerPath);
		url += "/" + _context.getString(R.string.RealGraffitiDataServlet);
			
		WebServiceClient client = new WebServiceClient(url);
		
		String actionName =  _context.getString(R.string.getGraffitiImage);
		client.addParam(ACTION_KEY, actionName);
		client.addParam(ACTION_PARAMETER_KEY, graffitiKey);
		client.execute(WebServiceClient.RequestMethod.POST);
		
		byte[] imageData = (byte[])client.getResponseObject(byte[].class);
		return imageData;
	}
	
	  public class SkipTypeStrategy implements ExclusionStrategy {
		    private final Class<?> typeToSkip;
	
		    private SkipTypeStrategy(Class<?> typeToSkip) {
		      this.typeToSkip = typeToSkip;
		    }
	
		    public boolean shouldSkipClass(Class<?> clazz) {
		      return false;
		    }
	
		    public boolean shouldSkipField(FieldAttributes f) {
		      return f.getClass().equals(typeToSkip);
		    }
		  }


}


