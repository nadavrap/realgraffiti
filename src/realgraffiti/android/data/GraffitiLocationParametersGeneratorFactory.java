package realgraffiti.android.data;

public class GraffitiLocationParametersGeneratorFactory {
	public static GraffitiLocationParametesrGenerator getGaffitiLocationParametersGenerator(){
		return new GraffitiLocationParametersFakeGenerator();
	}
}
