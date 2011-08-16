package realgraffiti.android.data;

import java.util.List;

import realgraffiti.common.dataObjects.Coordinates;
import realgraffiti.common.dataObjects.GraffitiLocationParameters;
import android.content.Context;
/**
 * Save a static LocationParametersGenerator
 * Had to be called from an Activity or from an Intent
 * The form of calling:
 * GraffitiLocationParameters glp = GraffitiLocationParametersGeneratorFactory.getGaffitiLocationParametersGenerator(getApplicationContext()).getCurrentLocationParameters();
 * 
 * @author Rappoport
 *
 */
public class GraffitiLocationParametersGeneratorFactory {
	private static GraffitiLocationParametersGenerator _graffitiLocationParametersGenerator;
	
	public static GraffitiLocationParametersGenerator getGaffitiLocationParametersGenerator(Context context){
	    if(_graffitiLocationParametersGenerator == null) {
	        _graffitiLocationParametersGenerator = new SensorsGraffitiLocationParametersGeneretor(context);
	        ((SensorsGraffitiLocationParametersGeneretor)_graffitiLocationParametersGenerator).startListening(context);
	    }
	    return _graffitiLocationParametersGenerator;
	}

	public static GraffitiLocationParametersGenerator getGaffitiLocationParametersGenerator(){
		return new GraffitiLocationParametersFakeGenerator();
	}

	public static GraffitiLocationParameters getCurrentLocationParameters() {
		return _graffitiLocationParametersGenerator.getCurrentLocationParameters();
	}
	
}
