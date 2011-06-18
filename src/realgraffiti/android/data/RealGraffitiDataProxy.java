package realgraffiti.android.data;

import java.util.Collection;


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
	public boolean addNewGraffiti(GraffitiDto GraffitiDto){
		String uploadUrl = getUploadUrl();
		Log.d("realgraffiti", "upload url: " + uploadUrl);
		
		RestClient client = new RestClient(uploadUrl);
		return true;
	}

	private String getUploadUrl(){
		String url = _context.getString(R.string.serverInfoServlet);
		String action = _context.getString(R.string.getUploadUrlAction);
		RestClient client = new RestClient(url);
		client.AddParam("action", action);
		
		client.Execute(RequestMethod.GET);
		return client.getResponse();
	}
	
	@Override
	public Collection<GraffitiDto> getNearByGraffiti(
			GraffitiLocationParametersDto graffitiLocationParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
