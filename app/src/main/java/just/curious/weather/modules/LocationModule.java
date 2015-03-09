package just.curious.weather.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

public class LocationModule {
    String provideUrl(){
        return "http://api.openweathermap.org/data/2.5";
    }
}
