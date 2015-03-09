package just.curious.weather.helpers;

public class TemperatureFormatter {

    public String format(float temperature) {
        return String.valueOf(Math.round(temperature)) + "°";
    }
}
