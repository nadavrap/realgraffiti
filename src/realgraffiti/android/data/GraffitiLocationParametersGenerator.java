package realgraffiti.android.data;

import realgraffiti.common.dataObjects.GraffitiLocationParameters;

public interface GraffitiLocationParametersGenerator {
	boolean isLocationParametersAvailable();
	GraffitiLocationParameters getCurrentLocationParameters();
}
