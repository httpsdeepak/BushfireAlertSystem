package bushfireAlert.com.example.myapp.network;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    public Main main;

    @SerializedName("name")
    public String name;

    public static class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("humidity")
        public int humidity;
    }
}
