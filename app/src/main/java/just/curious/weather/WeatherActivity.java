package just.curious.weather;

import android.os.Bundle;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import just.curious.weather.helpers.DayFormatter;
import just.curious.weather.helpers.TemperatureFormatter;
import just.curious.weather.modules.FragmentModule;
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
 * Weather Activity.
 * <p/>
 * This is the main activity for our app. It simply serves as a container for the Weather Fragment.
 * We prefer to build our implementation in a fragment because that enables future reuse if, for
 * example we build a tablet version of this app.
 */
public class WeatherActivity extends Activity {

    private ObjectGraph activityGraph;

    @Inject WeatherFragment weatherFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        WeatherApplication application = (WeatherApplication) getApplication();
        activityGraph = application.getApplicationGraph().plus(getModules().toArray());
        activityGraph.inject(this);


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, weatherFragment)
                    .commit();
        }
    }

    /**
     * A list of modules to use for the individual activity graph. Subclasses can override this
     * method to provide additional modules provided they call and include the modules returned by
     * calling {@code super.getModules()}.
     */
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new FragmentModule(this));
    }

    /** Inject the supplied {@code object} using the activity-specific graph. */
    public void inject(Object object) {
        activityGraph.inject(object);
    }
}