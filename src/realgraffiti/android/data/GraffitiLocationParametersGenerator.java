package realgraffiti.android.data;

import realgraffiti.common.dataObjects.GraffitiLocationParameters;

public interface GraffitiLocationParametersGenerator {
	void startTracking();
	void stopTracking();
	boolean isLocationParametersAvailable();
	GraffitiLocationParameters getCurrentLocationParameters();
}
