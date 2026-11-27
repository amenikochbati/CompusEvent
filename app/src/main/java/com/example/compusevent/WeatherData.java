package com.example.compusevent;

public class WeatherData {
    private double temperature;
    private int humidity;
    private double precipitation;
    private double windSpeed;
    private int weatherCode;
    private String weatherDescription;
    private double tempMin;
    private double tempMax;
    private int rainProbability;

    public WeatherData() {
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
        this.weatherDescription = parseWeatherCode(weatherCode);
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public int getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(int rainProbability) {
        this.rainProbability = rainProbability;
    }

    public static String parseWeatherCode(int code) {
        switch (code) {
            case 0:
                return "Ciel dégagé ☀️";
            case 1:
            case 2:
            case 3:
                return "Partiellement nuageux ⛅";
            case 45:
            case 48:
                return "Brouillard 🌫️";
            case 51:
            case 53:
            case 55:
                return "Bruine légère 🌧️";
            case 61:
            case 63:
            case 65:
                return "Pluie 🌧️";
            case 71:
            case 73:
            case 75:
                return "Chute de neige ❄️";
            case 80:
            case 81:
            case 82:
                return "Averses de pluie 🌧️";
            case 95:
            case 96:
            case 99:
                return "Orage 🌩️";
            default:
                return "Temps variable 🌤️";
        }
    }

    /** Version courte pour les cartes de la liste d'événements. */
    public String getShortDescription() {
        switch (weatherCode) {
            case 0:
                return "Dégagé";
            case 1:
            case 2:
            case 3:
                return "Nuageux";
            case 45:
            case 48:
                return "Brouillard";
            case 51:
            case 53:
            case 55:
                return "Bruine";
            case 61:
            case 63:
            case 65:
                return "Pluie";
            case 71:
            case 73:
            case 75:
                return "Neige";
            case 80:
            case 81:
            case 82:
                return "Averses";
            case 95:
            case 96:
            case 99:
                return "Orage";
            default:
                return "Variable";
        }
    }
}
