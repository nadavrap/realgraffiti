package realgraffiti.android.web;

import java.util.ArrayList;
import java.util.Collection;

import android.os.AsyncTask;
import realgraffiti.android.data.GraffitiLocationParametersGeneratorFactory;
import realgraffiti.common.data.RealGraffitiData;
import realgraffiti.common.dataObjects.Graffiti;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;

public class GraffitiServerPoller {
	private RealGraffitiData _realGraffitiData;
	private int _pollingInterval;
	private GraffitiPollListener _polllListener;
	private Collection<Graffiti> _recievedGraffiteis;
	
	private GraffitiPoll _graffitiPolltask;
	
	public GraffitiServerPoller(RealGraffitiData realGraffitiData, int pollingIntervapl){
		_realGraffitiData = realGraffitiData;
		_pollingInterval = pollingIntervapl;
		_recievedGraffiteis = new ArrayList<Graffiti>();
	}
	
	public void setOnPoll(GraffitiPollListener pollListener){
		_polllListener = pollListener;
	}
	
	public void beginPolling(){
		_graffitiPolltask = new GraffitiPoll();
		_graffitiPolltask.execute(_pollingInterval);
	}
	
	public void stopPolling(){
		_graffitiPolltask.cancel(true);
		_graffitiPolltask = null;
	}
	
	 private class GraffitiPoll extends AsyncTask<Integer, Collection<Graffiti>, Collection<Graffiti>> {
		 private boolean _running = true;
		 @Override
		protected Collection<Graffiti> doInBackground(Integer... params) {
			 Collection<Graffiti> graffities = null;
			 while(_running){
				 GraffitiLocationParameters graffitiLocationParameters = 
					 GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator().getCurrentLocationParameters();
				 
				graffities = _realGraffitiData.getNearByGraffiti(graffitiLocationParameters);
				
				// find the newly added graffities
				graffities.removeAll(_recievedGraffiteis);
				_recievedGraffiteis.addAll(graffities);
				
				publishProgress(graffities);
				
				try {
					Thread.sleep(params[0]);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			 }
			 
			return graffities;
		}
		 
		 @Override
        protected void onCancelled() {
           _running = false;
        }
		 
	     protected void onProgressUpdate(Collection<Graffiti>... graffities) {
	         _polllListener.onPollingData(graffities[0]);
	     }		
	 }
}


