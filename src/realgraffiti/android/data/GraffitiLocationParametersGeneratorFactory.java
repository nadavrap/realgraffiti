package realgraffiti.android.data;

import java.util.List;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;

public class GraffitiLocationParametersGeneratorFactory {
	static GraffitiLocationParameters _locParam = new GraffitiLocationParameters();
	public static GraffitiLocationParametesrGenerator getGaffitiLocationParametersGenerator(){
		return new GraffitiLocationParametersFakeGenerator();
	}
	public static GraffitiLocationParameters getCurrentLocationParameters() {
		return _locParam;
	}
	public static void setGraffitiLocationOrientation(float values){
		_locParam.setAngle(values);
	}
	public static void setGraffitiLocationCoordinates(Coordinates coor){
		_locParam.setCoordinates(coor);
	}
	public static void setGraffitiLocationSift(List<Double>siftDescriptors){
		_locParam.setSiftDescriptors(siftDescriptors);
	}
}
