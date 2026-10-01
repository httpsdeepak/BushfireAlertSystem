package bushfireAlert.com.example.myapp.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("geo/1.0/direct")
    Call<List<GeocodingItem>> getGeocodingSuggestions(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
