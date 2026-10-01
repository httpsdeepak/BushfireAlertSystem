package bushfireAlert.com.example.myapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import bushfireAlert.com.example.myapp.databinding.ActivityMainBinding;
import bushfireAlert.com.example.myapp.ui.alert.AlertViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "bushfire_alerts";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize AlertViewModel at Activity level to start background simulation
        new ViewModelProvider(this).get(AlertViewModel.class);

        createNotificationChannel();
        requestNotificationPermission();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Set start destination dynamically
            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                navGraph.setStartDestination(R.id.navigation_home);
            } else {
                navGraph.setStartDestination(R.id.navigation_login);
            }
            navController.setGraph(navGraph);

            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_login || destination.getId() == R.id.navigation_signup) {
                    binding.bottomNav.setVisibility(View.GONE);
                } else {
                    binding.bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bushfire Alerts";
            String description = "Notifications for bushfire emergency ratings";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }
}