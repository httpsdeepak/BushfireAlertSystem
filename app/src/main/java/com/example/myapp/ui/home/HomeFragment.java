package bushfireAlert.com.example.myapp.ui.home;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import bushfireAlert.com.example.myapp.MainActivity;
import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentHomeBinding;
import bushfireAlert.com.example.myapp.databinding.ItemHarmedAreaBinding;
import bushfireAlert.com.example.myapp.model.BookmarkItem;
import bushfireAlert.com.example.myapp.network.GeocodingItem;
import bushfireAlert.com.example.myapp.network.WeatherApiService;
import bushfireAlert.com.example.myapp.network.WeatherResponse;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";
    private static final String WEATHER_API_KEY = "99caccfd20686625d798abfc2a9878ca";
    private static final String BASE_URL = "https://api.openweathermap.org/";

    private FragmentHomeBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private int currentRating = 50;
    
    private WeatherApiService weatherApiService;
    private ArrayAdapter<GeocodingItem> searchAdapter;
    private boolean isInitialLocationSet = false;
    private List<BookmarkItem> bookmarkedList;
    private SharedPreferences bookmarkPrefs;
    private Gson gson = new Gson();

    private static class HarmedArea {
        String name;
        double lat;
        double lon;
        float distance;
        String temp = "--°C";
        String humidity = "--%";

        HarmedArea(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private final List<HarmedArea> allAreas = Arrays.asList(
            new HarmedArea("Parramatta", -33.8150, 151.0011),
            new HarmedArea("Blacktown", -33.7715, 150.9063),
            new HarmedArea("Penrith", -33.7507, 150.6942),
            new HarmedArea("Bankstown", -33.9173, 151.0358),
            new HarmedArea("Liverpool", -33.9222, 150.9242),
            new HarmedArea("Chatswood", -33.7961, 151.1780),
            new HarmedArea("Hornsby", -33.7033, 151.0967),
            new HarmedArea("Ryde", -33.8154, 151.1075),
            new HarmedArea("Manly", -33.7997, 151.2848),
            new HarmedArea("Bondi", -33.8915, 151.2767),
            new HarmedArea("Cronulla", -34.0531, 151.1492),
            new HarmedArea("Campbelltown", -34.0667, 150.8167),
            new HarmedArea("Newcastle", -32.9283, 151.7817),
            new HarmedArea("Wollongong", -34.4278, 150.8931),
            new HarmedArea("Blue Mountains", -33.7121, 150.3119),
            new HarmedArea("Melbourne", -37.8136, 144.9631),
            new HarmedArea("Brisbane", -27.4705, 153.0260),
            new HarmedArea("Perth", -31.9505, 115.8605),
            new HarmedArea("Adelaide", -34.9285, 138.6007),
            new HarmedArea("Canberra", -35.2809, 149.1300),
            new HarmedArea("Darwin", -12.4634, 130.8456),
            new HarmedArea("Hobart", -42.8821, 147.3272),
            new HarmedArea("Geelong", -38.1499, 144.3617),
            new HarmedArea("Gold Coast", -28.0167, 153.4000),
            new HarmedArea("Sunshine Coast", -26.6500, 153.0667)
    );

    // User Stories for 5 Different Users
    private final String[] userTypes = {"Resident", "Tourist", "Firefighter", "Elderly User", "Commuter"};
    private final String[] userStories = {
            "USER STORY (Resident): As a local resident, I want to receive push notifications for any extreme fire ratings within 10km of my house, so that I can prepare my home and family according to our bushfire survival plan.",
            "USER STORY (Tourist): As a tourist visiting the area, I want to see nearby bushfire-prone zones on a map and receive safety advice in simple English, so that I can avoid dangerous areas during my trip.",
            "USER STORY (Firefighter): As a local volunteer firefighter, I want to see real-time humidity and temperature correlations across different regions, so that I can better predict potential hotspots and fire behavior.",
            "USER STORY (Elderly User): As an elderly person with limited mobility, I want to have a one-tap emergency call button and loud audio alerts for catastrophic warnings, so that I can quickly seek assistance when needed.",
            "USER STORY (Commuter): As a daily commuter passing through rural areas, I want to check the current fire danger rating of my destination suburb before I start my journey, so that I don't get trapped by road closures or fires."
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted || coarseLocationGranted) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    if (binding != null) binding.tvLocationName.setText("Wynyard");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        bookmarkPrefs = requireActivity().getSharedPreferences("Bookmarks", Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        loadBookmarks();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApiService = retrofit.create(WeatherApiService.class);

        binding.btnContact.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_emergency));
        binding.btnSafetyTips.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_emergency));
        binding.btnBookmark.setOnClickListener(v -> toggleBookmark());

        setupUserStories();
        setupSearch();

        return binding.getRoot();
    }

    private void setupSearch() {
        searchAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.autoCompleteSearch.setAdapter(searchAdapter);

        binding.autoCompleteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    fetchSuggestions(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.autoCompleteSearch.setOnItemClickListener((parent, view, position, id) -> {
            GeocodingItem selected = searchAdapter.getItem(position);
            if (selected != null) {
                Location loc = new Location("manual");
                loc.setLatitude(selected.lat);
                loc.setLongitude(selected.lon);
                onLocationReceived(loc);
                
                // Set name manually if it's a known location from search
                binding.tvLocationName.setText(selected.name);
                
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.autoCompleteSearch.getWindowToken(), 0);
            }
        });
    }

    private void fetchSuggestions(String query) {
        weatherApiService.getGeocodingSuggestions(query, 5, WEATHER_API_KEY).enqueue(new Callback<List<GeocodingItem>>() {
            @Override
            public void onResponse(Call<List<GeocodingItem>> call, Response<List<GeocodingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchAdapter.clear();
                    searchAdapter.addAll(response.body());
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<GeocodingItem>> call, Throwable t) {
                Log.e(TAG, "Search error", t);
            }
        });
    }

    private void setupUserStories() {
        // Expand/Collapse logic
        binding.cardSystemFeatures.setOnClickListener(v -> {
            if (binding.expandableStories.getVisibility() == View.GONE) {
                binding.expandableStories.setVisibility(View.VISIBLE);
            } else {
                binding.expandableStories.setVisibility(View.GONE);
            }
        });

        // Initialize Tabs for 5 different users
        binding.tabsStories.removeAllTabs();
        for (String user : userTypes) {
            binding.tabsStories.addTab(binding.tabsStories.newTab().setText(user));
        }

        // Tab Selection Logic
        binding.tabsStories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < userStories.length) {
                    binding.tvStoryDetail.setText(userStories[position]);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Select first user story by default
        if (userStories.length > 0) {
            binding.tvStoryDetail.setText(userStories[0]);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchCurrentLocation();
        startDataSimulation();
    }

    private void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchCurrentLocation() {
        if (getContext() == null) return;
        
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            // Default to Wynyard if permission is not yet granted
            binding.tvLocationName.setText("Wynyard");
            return;
        }

        binding.tvLocationName.setText("Locating...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                onLocationReceived(location);
            } else {
                binding.tvLocationName.setText("Wynyard");
            }
            requestFreshLocation();
        }).addOnFailureListener(e -> {
            binding.tvLocationName.setText("Wynyard");
            requestFreshLocation();
        });
    }

    private void requestFreshLocation() {
        if (getContext() == null || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(request, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        onLocationReceived(location);
                    } else if (currentLocation == null) {
                        binding.tvLocationName.setText("Wynyard");
                    }
                })
                .addOnFailureListener(e -> {
                    if (currentLocation == null) {
                        binding.tvLocationName.setText("Wynyard");
                    }
                });
    }

    private void onLocationReceived(Location location) {
        if (binding == null) return;
        currentLocation = location;
        updateLocationUI(location);
        updateHarmedAreas();
        fetchWeatherData(location.getLatitude(), location.getLongitude());
        updateBookmarkIcon();
    }

    private void fetchWeatherData(double lat, double lon) {
        weatherApiService.getCurrentWeather(lat, lon, WEATHER_API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateWeatherUI(response.body());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Weather API error", t);
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (binding == null) return;

        int temp = Math.round(weather.main.temp);
        int humidity = weather.main.humidity;

        binding.tvTemperature.setText(String.format(Locale.getDefault(), "%d°C", temp));
        binding.tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", humidity));

        // Update Rating based on weather
        // Simple formula: Rating = (Temp * 1.5) + (100 - Humidity) / 2
        int calculatedRating = (int) ((temp * 1.2) + (100 - humidity) * 0.5);
        calculatedRating = Math.min(100, Math.max(10, calculatedRating));
        
        currentRating = calculatedRating;
        binding.tvRatingValue.setText(String.valueOf(currentRating));
        binding.gaugeView.setProgress(currentRating);

        updateRatingLabel(currentRating);
        
        if (!isInitialLocationSet) {
            binding.tvLocationName.setText(weather.name);
            isInitialLocationSet = true;
            updateBookmarkIcon();
        }
    }

    private void loadBookmarks() {
        String json = bookmarkPrefs.getString("bookmark_list", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<BookmarkItem>>() {}.getType();
            bookmarkedList = gson.fromJson(json, type);
        } else {
            bookmarkedList = new ArrayList<>();
        }
    }

    private void saveBookmarks() {
        // Save locally
        String json = gson.toJson(bookmarkedList);
        bookmarkPrefs.edit().putString("bookmark_list", json).apply();
        
        // Save to Firebase if logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("bookmarks").child(uid);
            db.setValue(bookmarkedList);
        }
    }

    private void toggleBookmark() {
        if (currentLocation == null) return;
        
        String name = binding.tvLocationName.getText().toString();
        if (name.equals("Locating...") || name.equals("Wynyard")) return;

        BookmarkItem existing = findBookmark(name);
        if (existing != null) {
            bookmarkedList.remove(existing);
            Toast.makeText(getContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
        } else {
            bookmarkedList.add(new BookmarkItem(name, currentLocation.getLatitude(), currentLocation.getLongitude()));
            Toast.makeText(getContext(), "Location bookmarked", Toast.LENGTH_SHORT).show();
        }
        saveBookmarks();
        updateBookmarkIcon();
    }

    private BookmarkItem findBookmark(String name) {
        for (BookmarkItem item : bookmarkedList) {
            if (item.name.equalsIgnoreCase(name)) return item;
        }
        return null;
    }

    private void updateBookmarkIcon() {
        if (binding == null) return;
        String name = binding.tvLocationName.getText().toString();
        if (findBookmark(name) != null) {
            binding.btnBookmark.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            binding.btnBookmark.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void updateRatingLabel(int rating) {
        String label;
        int colorRes;
        if (rating > 80) {
            label = "CATASTROPHIC";
            colorRes = R.color.rating_cat;
        } else if (rating > 60) {
            label = "EXTREME";
            colorRes = R.color.rating_ext;
        } else if (rating > 40) {
            label = "HIGH";
            colorRes = R.color.rating_high;
        } else if (rating > 20) {
            label = "MODERATE";
            colorRes = R.color.rating_mod;
        } else {
            label = "LOW";
            colorRes = R.color.rating_low;
        }
        
        binding.tvRatingLabel.setText(label);
        binding.tvRatingLabel.setTextColor(getResources().getColor(colorRes));

        // Check for alerts
        boolean isAccessibilityOn = sharedPreferences.getBoolean("accessibility", false);
        if (rating > 60) {
            sendNotification(label + " Bushfire Alert", 
                    label + " Bushfire Alert: bushfire rating is " + label.toLowerCase() + " (" + rating + ")");
            if (isAccessibilityOn) {
                playAlertSound();
            }
        }
    }

    private void updateLocationUI(Location location) {
        if (getContext() == null || binding == null) return;
        
        // Use Geocoder to get the name if we don't have it set from search
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String name = address.getLocality();
                    if (name == null) name = address.getSubAdminArea();
                    
                    final String locationName = (name != null) ? name : "Unknown Area";
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding != null && !isInitialLocationSet) {
                                binding.tvLocationName.setText(locationName);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error", e);
            }
        }).start();
    }

    private void startDataSimulation() {
        handler.removeCallbacksAndMessages(null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (binding != null) {
                    updateHarmedAreas();
                    // Also refresh weather data if we have a location
                    if (currentLocation != null) {
                        fetchWeatherData(currentLocation.getLatitude(), currentLocation.getLongitude());
                    }
                    handler.postDelayed(this, 60000); // Refresh every minute
                }
            }
        });
    }


    private void playAlertSound() {
        try {
            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.ambulance_alert);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            } else {
                Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarm == null) alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getContext(), alarm);
                r.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHarmedAreas() {
        if (binding == null || currentLocation == null) return;
        binding.containerHarmedAreas.removeAllViews();
        
        List<HarmedArea> nearbyAreas = new ArrayList<>();
        
        // Calculate distance for each area and filter within 100km
        for (HarmedArea area : allAreas) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), 
                    area.lat, area.lon, results);
            
            float distanceInKm = results[0] / 1000;
            if (distanceInKm <= 100) {
                area.distance = distanceInKm;
                nearbyAreas.add(area);
            }
        }

        // Sort by distance (closest first)
        Collections.sort(nearbyAreas, (o1, o2) -> Float.compare(o1.distance, o2.distance));

        if (nearbyAreas.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("No high-risk areas detected within 100km.");
            tv.setTextColor(getResources().getColor(R.color.text_secondary));
            tv.setPadding(0, 16, 0, 0);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            binding.containerHarmedAreas.addView(tv);
            return;
        }

        for (int i = 0; i < Math.min(5, nearbyAreas.size()); i++) {
            HarmedArea area = nearbyAreas.get(i);
            ItemHarmedAreaBinding itemBinding = ItemHarmedAreaBinding.inflate(getLayoutInflater(), binding.containerHarmedAreas, false);
            
            itemBinding.tvAreaName.setText(String.format(Locale.getDefault(), "%s (%.1f km)", area.name, area.distance));
            itemBinding.tvAreaTemp.setText(area.temp);
            itemBinding.tvAreaHumid.setText(area.humidity);
            
            binding.containerHarmedAreas.addView(itemBinding.getRoot());

            // Fetch real weather for this area
            fetchWeatherForArea(area, itemBinding);
        }
    }

    private void fetchWeatherForArea(HarmedArea area, ItemHarmedAreaBinding itemBinding) {
        weatherApiService.getCurrentWeather(area.lat, area.lon, WEATHER_API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (isAdded() && binding != null && response.isSuccessful() && response.body() != null) {
                    area.temp = Math.round(response.body().main.temp) + "°C";
                    area.humidity = response.body().main.humidity + "%";
                    
                    itemBinding.tvAreaTemp.setText(area.temp);
                    itemBinding.tvAreaHumid.setText(area.humidity);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {}
        });
    }

    private void sendNotification(String title, String content) {
        if (getContext() == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
