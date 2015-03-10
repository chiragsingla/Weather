package just.curious.weather.modules;

import android.app.Activity;
import android.location.LocationManager;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import just.curious.weather.WeatherActivity;
import just.curious.weather.WeatherForecastListAdapter;
import just.curious.weather.WeatherFragment;
import just.curious.weather.helpers.DayFormatter;
import just.curious.weather.helpers.TemperatureFormatter;
import just.curious.weather.pojo.WeatherForecast;
import just.curious.weather.service.LocationService;
import just.curious.weather.service.WeatherService;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.subscriptions.CompositeSubscription;

@Module(
        library = true,
        addsTo = ApplicationModule.class,
        injects = {
                WeatherFragment.class,
                WeatherActivity.class
        }

)
public class FragmentModule {

    Activity activity;

    public FragmentModule(Activity activity){
        this.activity = activity;
    }

    @Provides @Singleton
    CompositeSubscription providesCompositeSubscription(){
        return new CompositeSubscription();
    }

    @Provides @Singleton
    LocationService providesLocationService(LocationManager locationManager){
        return new LocationService(locationManager);
    }

    @Provides @Singleton
    DayFormatter providesDayFormatter(){
        return new DayFormatter(activity);
    }

    @Provides @Singleton
    TemperatureFormatter providesTemperatureFormatter(){
        return new TemperatureFormatter();
    }

    @Provides @Singleton
    WeatherForecastListAdapter provideswWeatherForecastListAdapter(DayFormatter dayFormatter, TemperatureFormatter temperatureFormatter){
        return new WeatherForecastListAdapter(new ArrayList<WeatherForecast>(), activity, dayFormatter, temperatureFormatter);
    }

    @Provides @Singleton
    RestAdapter providesRestAdapter(){
        final String WEB_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5";
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        return restAdapter;
    }

    @Provides @Singleton
    WeatherService providesWeatherService(RestAdapter restAdapter){
        return new WeatherService(restAdapter);
    }

}
