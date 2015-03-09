package just.curious.weather.modules;

import android.content.Context;
import android.location.LocationManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import just.curious.weather.WeatherApplication;
import just.curious.weather.annotations.ForApplication;

@Module(library = true)
public class ApplicationModule {

    private final WeatherApplication application;

    public ApplicationModule(WeatherApplication application){
        this.application = application;
    }

    @Provides @Singleton @ForApplication
    Context provideApplicationContext(){
        return application;
    }

    @Provides @Singleton
    LocationManager provideLocationManager(){
        return (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    }

}
