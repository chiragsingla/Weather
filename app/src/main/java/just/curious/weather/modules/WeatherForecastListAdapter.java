package just.curious.weather.modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import just.curious.weather.R;
import just.curious.weather.helpers.DayFormatter;
import just.curious.weather.helpers.TemperatureFormatter;
import just.curious.weather.pojo.WeatherForecast;

/**
 * Provides items for our list view.
 */
public class WeatherForecastListAdapter extends ArrayAdapter {

    DayFormatter dayFormatter;

    TemperatureFormatter temperatureFormatter;

    @Inject
    public WeatherForecastListAdapter(final List<WeatherForecast> weatherForecasts,
                                      final Context context, DayFormatter dayFormatter, TemperatureFormatter temperatureFormatter) {
        super(context, 0, weatherForecasts);
        this.dayFormatter = dayFormatter;
        this.temperatureFormatter = temperatureFormatter;

    }

    @Override
    public boolean isEnabled(final int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.weather_forecast_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.day);
            viewHolder.descriptionTextView = (TextView) convertView
                    .findViewById(R.id.description);
            viewHolder.maximumTemperatureTextView = (TextView) convertView
                    .findViewById(R.id.maximum_temperature);
            viewHolder.minimumTemperatureTextView = (TextView) convertView
                    .findViewById(R.id.minimum_temperature);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final WeatherForecast weatherForecast = (WeatherForecast) getItem(position);

        final String day = dayFormatter.format(weatherForecast.getTimestamp());
        viewHolder.dayTextView.setText(day);
        viewHolder.descriptionTextView.setText(weatherForecast.getDescription());
        viewHolder.maximumTemperatureTextView.setText(
                temperatureFormatter.format(weatherForecast.getMaximumTemperature()));
        viewHolder.minimumTemperatureTextView.setText(
                temperatureFormatter.format(weatherForecast.getMinimumTemperature()));

        return convertView;
    }

    /**
     * Cache to avoid doing expensive findViewById() calls for each getView().
     */
    private class ViewHolder {
        private TextView dayTextView;
        private TextView descriptionTextView;
        private TextView maximumTemperatureTextView;
        private TextView minimumTemperatureTextView;
    }
}
