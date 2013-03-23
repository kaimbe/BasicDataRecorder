package notes;

public class WeatherDesc {
    private String location;
    private double currentTemp;
    private String synopsis;

    public WeatherDesc(
        String location,
        double currentTemp, 
        String synopsis )
    {
        this.location = location;
        this.currentTemp = currentTemp;
        this.synopsis = synopsis;
    }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return currentTemp;
    }

    public String getSynopsis() {
        return synopsis;
    }
}
