package bushfireAlert.com.example.myapp.ui.bookmark;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import bushfireAlert.com.example.myapp.databinding.FragmentBookmarkBinding;
import bushfireAlert.com.example.myapp.model.BookmarkItem;
import bushfireAlert.com.example.myapp.network.WeatherApiService;
import bushfireAlert.com.example.myapp.network.WeatherResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookmarkFragment extends Fragment {
    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";
    private FragmentBookmarkBinding binding;
    private List<BookmarkItem> bookmarks;
    private BookmarkAdapter adapter;
    private SharedPreferences sharedPreferences;
    private WeatherApiService weatherApiService;
    private static final String WEATHER_API_KEY = "99caccfd20686625d798abfc2a9878ca";
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL = 60000; // 1 minute

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshWeatherData();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("Bookmarks", Context.MODE_PRIVATE);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApiService = retrofit.create(WeatherApiService.class);

        loadBookmarks();
        setupRecyclerView();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new BookmarkAdapter(bookmarks, position -> {
            bookmarks.remove(position);
            saveBookmarks();
            adapter.notifyItemRemoved(position);
            updateEmptyView();
        });
        binding.rvBookmarks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookmarks.setAdapter(adapter);
        updateEmptyView();
    }

    private void loadBookmarks() {
        bookmarks = new ArrayList<>();
        
        // Load locally first
        String json = sharedPreferences.getString("bookmark_list", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<BookmarkItem>>() {}.getType();
            bookmarks.addAll(gson.fromJson(json, type));
        }

        // Sync with Firebase if logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("bookmarks").child(uid);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        bookmarks.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            BookmarkItem item = child.getValue(BookmarkItem.class);
                            if (item != null) bookmarks.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                        refreshWeatherData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            refreshWeatherData();
        }
    }

    private void saveBookmarks() {
        // Save locally
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bookmarks);
        editor.putString("bookmark_list", json);
        editor.apply();

        // Save to Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("bookmarks").child(uid);
            db.setValue(bookmarks);
        }
    }

    private void updateEmptyView() {
        if (bookmarks.isEmpty()) {
            binding.tvNoBookmarks.setVisibility(View.VISIBLE);
            binding.rvBookmarks.setVisibility(View.GONE);
        } else {
            binding.tvNoBookmarks.setVisibility(View.GONE);
            binding.rvBookmarks.setVisibility(View.VISIBLE);
        }
    }

    private void refreshWeatherData() {
        if (!isAdded() || binding == null) return;
        
        for (int i = 0; i < bookmarks.size(); i++) {
            final int index = i;
            BookmarkItem item = bookmarks.get(i);
            weatherApiService.getCurrentWeather(item.lat, item.lon, WEATHER_API_KEY, "metric")
                    .enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (isAdded() && binding != null && response.isSuccessful() && response.body() != null) {
                        item.temp = Math.round(response.body().main.temp) + "°C";
                        item.humidity = response.body().main.humidity + "%";
                        item.lastUpdated = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        adapter.notifyItemChanged(index);
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {}
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
