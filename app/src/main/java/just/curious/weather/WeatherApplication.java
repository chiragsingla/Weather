package just.curious.weather;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import just.curious.weather.modules.ApplicationModule;

public class WeatherApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(modules().toArray());
    }

    private List<Object> modules(){
        return Arrays.<Object>asList(new ApplicationModule(this));
    }

    public void inject(Object object){
        objectGraph.inject(object);
    }

    public ObjectGraph getApplicationGraph(){
        return objectGraph;
    }

}
