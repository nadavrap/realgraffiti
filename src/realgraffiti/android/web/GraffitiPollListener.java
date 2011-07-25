package realgraffiti.android.web;

import java.util.Collection;

import realgraffiti.common.dataObjects.Graffiti;

public interface GraffitiPollListener{
	 void onPollingData(Collection<Graffiti> graffities);
}