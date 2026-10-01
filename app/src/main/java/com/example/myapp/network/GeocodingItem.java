package bushfireAlert.com.example.myapp.network;

import com.google.gson.annotations.SerializedName;

public class GeocodingItem {
    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public double lat;

    @SerializedName("lon")
    public double lon;

    @SerializedName("country")
    public String country;

    @SerializedName("state")
    public String state;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (state != null && !state.isEmpty()) {
            sb.append(", ").append(state);
        }
        if (country != null && !country.isEmpty()) {
            sb.append(" (").append(country).append(")");
        }
        return sb.toString();
    }
}
