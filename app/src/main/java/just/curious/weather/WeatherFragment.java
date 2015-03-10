package just.curious.weather;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import just.curious.weather.helpers.DayFormatter;
import just.curious.weather.helpers.TemperatureFormatter;
import just.curious.weather.WeatherForecastListAdapter;
import just.curious.weather.pojo.CurrentWeather;
import just.curious.weather.pojo.WeatherForecast;
import just.curious.weather.service.LocationService;
import just.curious.weather.service.WeatherService;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Weather Fragment.
 * <p/>
 * Displays the current weather as well as a 7 day forecast for our location. Data is loaded
 * from a web service.
 */
public class WeatherFragment extends Fragment {

    private static final String KEY_CURRENT_WEATHER = "key_current_weather";
    private static final String KEY_WEATHER_FORECASTS = "key_weather_forecasts";
    private static final long LOCATION_TIMEOUT_SECONDS = 20;
    private static final String TAG = WeatherFragment.class.getCanonicalName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mLocationNameTextView;
    private TextView mCurrentTemperatureTextView;
    private ListView mForecastListView;
    private TextView mAttributionTextView;

    @Inject
    CompositeSubscription mCompositeSubscription;

    @Inject
    LocationService locationService;

    @Inject
    DayFormatter dayFormatter;

    @Inject
    TemperatureFormatter temperatureFormatter;

    @Inject
    WeatherService weatherService;

    @Inject
    WeatherForecastListAdapter adapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        mLocationNameTextView = (TextView) rootView.findViewById(R.id.location_name);
        mCurrentTemperatureTextView = (TextView) rootView
                .findViewById(R.id.current_temperature);

        // Set up list view for weather forecasts.
        mForecastListView = (ListView) rootView.findViewById(R.id.weather_forecast_list);
        mForecastListView.setAdapter(adapter);

        mAttributionTextView = (TextView) rootView.findViewById(R.id.attribution);
        mAttributionTextView.setVisibility(View.INVISIBLE);

        // Set up swipe refresh layout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.swipe_refresh_container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.brand_main,
                android.R.color.black,
                R.color.brand_main,
                android.R.color.black);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeather();
            }
        });

        updateWeather();
        return rootView;
    }

    @Override
    public void onDestroy() {
        mCompositeSubscription.unsubscribe();
        super.onDestroy();
    }


    /**
     * Get weather data for the current location and update the UI.
     */
    private void updateWeather() {
        mSwipeRefreshLayout.setRefreshing(true);
        // Get our current location.
        final Observable fetchDataObservable = locationService.getLocation()
                .timeout(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .flatMap(new Func1<Location, Observable<HashMap<String, WeatherForecast>>>() {
                    @Override
                    public Observable<HashMap<String, WeatherForecast>> call(final Location location) {
                        final double longitude = location.getLongitude();
                        final double latitude = location.getLatitude();

                        return Observable.zip(
                                // Fetch current and 7 day forecasts for the location.
                                weatherService.fetchCurrentWeather(longitude, latitude),
                                weatherService.fetchWeatherForecasts(longitude, latitude),

                                // Only handle the fetched results when both sets are available.
                                new Func2<CurrentWeather, List<WeatherForecast>,
                                        HashMap<String, WeatherForecast>>() {
                                    @Override
                                    public HashMap call(final CurrentWeather currentWeather,
                                                        final List<WeatherForecast> weatherForecasts) {

                                        HashMap weatherData = new HashMap();
                                        weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                                        weatherData.put(KEY_WEATHER_FORECASTS, weatherForecasts);
                                        return weatherData;
                                    }
                                }
                        );
                    }
                });

        mCompositeSubscription.add(fetchDataObservable
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<HashMap<String, WeatherForecast>>() {
                            @Override
                            public void onNext(final HashMap<String, WeatherForecast> weatherData) {
                                // Update UI with current weather.
                                final CurrentWeather currentWeather = (CurrentWeather) weatherData
                                        .get(KEY_CURRENT_WEATHER);
                                mLocationNameTextView.setText(currentWeather.getLocationName());
                                mCurrentTemperatureTextView.setText(
                                        temperatureFormatter.format(currentWeather.getTemperature()));

                                // Update weather forecast list.
                                final List<WeatherForecast> weatherForecasts = (List<WeatherForecast>)
                                        weatherData.get(KEY_WEATHER_FORECASTS);
                                final WeatherForecastListAdapter adapter = (WeatherForecastListAdapter)
                                        mForecastListView.getAdapter();
                                adapter.clear();
                                adapter.addAll(weatherForecasts);
                            }

                            @Override
                            public void onCompleted() {
                                mSwipeRefreshLayout.setRefreshing(false);
                                mAttributionTextView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(final Throwable error) {
                                mSwipeRefreshLayout.setRefreshing(false);

                                if (error instanceof TimeoutException) {
                                    Crouton.makeText(getActivity(),
                                            R.string.error_location_unavailable, Style.ALERT).show();
                                } else if (error instanceof RetrofitError
                                        || error instanceof HttpException) {
                                    Crouton.makeText(getActivity(),
                                            R.string.error_fetch_weather, Style.ALERT).show();
                                } else {
                                    Log.e(TAG, error.getMessage());
                                    error.printStackTrace();
                                    throw new RuntimeException("See inner exception");
                                }
                            }
                        })
        );
    }
}
