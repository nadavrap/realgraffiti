package realgraffiti.android.data;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Collection;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import android.content.Context;
import android.os.Debug;
import android.util.Log;

import realgraffiti.android.R;
import realgraffiti.android.data.RestClient.RequestMethod;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dto.GraffitiDto;
import realgraffiti.common.dto.GraffitiLocationParametersDto;

public class RealGraffitiDataProxy implements RealGraffitiData{
	private Context _context;
	
	public RealGraffitiDataProxy(Context context){
		_context = context;
	}
	
	@Override
	public boolean addNewGraffiti(GraffitiDto graffitiDto){
		String serverPath = _context.getString(R.string.ServerPath);
		
		String uploadUrl = serverPath + getUploadUrl();
		Log.d("realgraffiti", "upload url: " + uploadUrl);
		
		Gson gson = new GsonBuilder()
			.addSerializationExclusionStrategy(new SkipTypeStrategy(Byte[].class))
			.create();
		
		Log.d("realgraffiti", "object json: " + gson.toJson(graffitiDto));
		RestClient client = new RestClient(uploadUrl);
		client.addParam("object", gson.toJson(graffitiDto, GraffitiDto.class));
		client.addFile("file", graffitiDto.get_imageData());
		
		client.execute(RequestMethod.POST);
		int responseCode = client.getResponseCode();
		String response = client.getResponse();
		Log.d("realgraffiti", "request reponse: " + response);
		return responseCode == HttpURLConnection.HTTP_OK;
	}

	private String getUploadUrl(){
		String serverPath = _context.getString(R.string.ServerPath);
		String url = serverPath + "/" +  _context.getString(R.string.serverInfoServlet);
		String action = _context.getString(R.string.getUploadUrlAction);
		RestClient client = new RestClient(url);
		client.addParam("action", action);
		
		client.execute(RequestMethod.GET);
		
		String uploadUrl = client.getResponse();
		
		return uploadUrl.trim();
	}
	
	@Override
	public Collection<GraffitiDto> getNearByGraffiti(
			GraffitiLocationParametersDto graffitiLocationParameters) {
		// TODO Auto-generated method stub
		return null;
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


