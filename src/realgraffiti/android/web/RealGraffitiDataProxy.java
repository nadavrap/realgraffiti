package realgraffiti.android.web;


import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
//import com.google.gson.FieldAttributes;
import com.google.gson.reflect.TypeToken;
import android.content.Context;
import android.util.Log;

import realgraffiti.android.R;
import realgraffiti.android.data.GraffitiUtils;
import realgraffiti.android.web.WebServiceClient.RequestMethod;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.*;


public class RealGraffitiDataProxy implements RealGraffitiData{
	private Context _context;
	
	private final String ACTION_KEY = "action";
	private final String ACTION_PARAMETER_KEY = "object";
	
	public RealGraffitiDataProxy(Context context){
		_context = context;
		Log.d("RealGraffiti: DataProxy","Created");
	}
	
	@Override
	public boolean addNewGraffiti(Graffiti graffiti){	
		Log.d("RealGraffiti: DataProxy","Add new graffiti");
		
		String serverPath = _context.getString(R.string.ServerPath);
		String url = serverPath + "/" +  _context.getString(R.string.RealGraffitiDataServlet);
		
		WebServiceClient client = new WebServiceClient(url);
		client.addParam(ACTION_KEY, _context.getString(R.string.addGraffiti));
		client.addParam(ACTION_PARAMETER_KEY, graffiti);
		
		client.execute(RequestMethod.POST);
		
		int responseCode = client.getResponseCode();

		return responseCode == HttpURLConnection.HTTP_OK;
	}
	
	@Override
	public Collection<Graffiti> getNearByGraffiti(
			GraffitiLocationParameters graffitiLocationParameters, int rangeInMeters) {
		Log.d("RealGraffiti: DataProxy","Get near by graffiti start");
		
		String url = _context.getString(R.string.ServerPath);
		url += "/" + _context.getString(R.string.RealGraffitiDataServlet);
		Log.d("RealGraffiti: DataProxy", "URL: " + url);
		
		WebServiceClient client = new WebServiceClient(url);
		String actionName = _context.getString(R.string.getNearByGraffiti);
		client.addParam(ACTION_KEY, actionName);
		client.addParam(ACTION_PARAMETER_KEY, graffitiLocationParameters);
		
		client.execute(WebServiceClient.RequestMethod.POST);
		
		Type collectionType = new TypeToken<ArrayList<Graffiti>>(){}.getType();
		Collection<Graffiti> allGraffiti = (ArrayList<Graffiti>)client.getResponseObject(collectionType);
		Collection<Graffiti> nearByGraffiti = GraffitiUtils.filterGraffitiesByDistance(allGraffiti, graffitiLocationParameters.getCoordinates(), rangeInMeters);
		
		Log.d("RealGraffiti: DataProxy" , "Recieved " + allGraffiti.size() + " graffiti, " + nearByGraffiti.size() + " of them are near by");
		Log.d("RealGraffiti: DataProxy","Get near by graffiti end");
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

	@Override
	public byte[] getGraffitiWallImage(Long graffitiKey) {
		String url = _context.getString(R.string.ServerPath);
		url += "/" + _context.getString(R.string.RealGraffitiDataServlet);
			
		WebServiceClient client = new WebServiceClient(url);
		
		String actionName =  _context.getString(R.string.getGraffitiWallImage);
		client.addParam(ACTION_KEY, actionName);
		client.addParam(ACTION_PARAMETER_KEY, graffitiKey);
		client.execute(WebServiceClient.RequestMethod.POST);
		
		byte[] wallImageData = (byte[])client.getResponseObject(byte[].class);
		return wallImageData;
	}
}


