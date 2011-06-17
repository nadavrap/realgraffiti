package realgraffiti.android.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dto.GraffitiDto;
import realgraffiti.common.dto.GraffitiLocationParametersDto;

public class RealGraffitiDataProxy implements RealGraffitiData{

	@Override
	public void addNewGraffiti(GraffitiDto GraffitiDto) {
			

	}

	@Override
	public Collection<GraffitiDto> getNearByGraffiti(
			GraffitiLocationParametersDto graffitiLocationParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
