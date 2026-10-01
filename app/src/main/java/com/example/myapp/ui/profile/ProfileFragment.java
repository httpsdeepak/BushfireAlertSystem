package bushfireAlert.com.example.myapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentProfileBinding;
import bushfireAlert.com.example.myapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "Settings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_ACCESSIBILITY = "accessibility";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String DEFAULT_VALUE = "---";

    private static final String ADMIN_EMAIL = "admin@bushfire.com.au";
    private static final String ADMIN_PASSWORD = "11111111";
    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_PHONE = "0450278277";

    private static final String USER2_EMAIL = "abcd@gmail.com";
    private static final String USER2_PASSWORD = "11111111";
    private static final String USER2_NAME = "ABCD";
    private static final String USER2_PHONE = "0400000000";

    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";

    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initializeUI();

        return binding.getRoot();
    }

    private void initializeUI() {
        setupDarkMode();
        setupAccessibility();
        setupLogin();
        setupContactUs();
        updateUIBasedOnLoginStatus();
    }

    private void setupDarkMode() {
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        binding.switchDarkMode.setChecked(isDarkMode);
        
        binding.switchDarkMode.setOnCheckedChangeListener((v, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            updateNightMode(isChecked);
        });
    }

    private void updateNightMode(boolean isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? 
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void setupAccessibility() {
        boolean isAccessibilityOn = sharedPreferences.getBoolean(KEY_ACCESSIBILITY, false);
        binding.switchAccessibility.setChecked(isAccessibilityOn);
        
        binding.switchAccessibility.setOnCheckedChangeListener((v, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_ACCESSIBILITY, isChecked).apply();
            if (isChecked) {
                playAlertSound();
            }
        });
    }

    private void playAlertSound() {
        try {
            MediaPlayer mp = MediaPlayer.create(requireContext(), R.raw.ambulance_alert);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            } else {
                playDefaultAlarm();
            }
        } catch (Exception e) {
            playDefaultAlarm();
        }
    }

    private void playDefaultAlarm() {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            Ringtone r = RingtoneManager.getRingtone(requireContext(), alert);
            if (r != null) {
                r.play();
            }
        } catch (Exception ignored) {}
    }

    private void setupContactUs() {
        binding.btnContactEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:Bushfirealertsystem@help.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bushfire Alert Support");
            try {
                startActivity(Intent.createChooser(intent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                showToast("There are no email clients installed.");
            }
        });
    }

    private void setupLogin() {
        binding.btnLogin.setOnClickListener(v -> {
            // Navigate to LoginFragment
            Navigation.findNavController(v).navigate(R.id.navigation_login);
        });

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
            showToast("Logged out");
            
            // Redirect to login and clear all previous fragments
            Navigation.findNavController(v).navigate(R.id.navigation_login, null, 
                new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build());
        });
    }

    private void updateUIBasedOnLoginStatus() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        
        binding.cardLogin.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        binding.btnLogout.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
        
        // Remove old listener if exists
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }

        if (isLoggedIn) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Set temporary loading text
            binding.tvProfileEmail.setText("Loading...");
            binding.tvProfileName.setText("Loading...");
            binding.tvProfilePhone.setText("Loading...");

            userRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(uid);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;
                    
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        binding.tvProfileEmail.setText(user.email);
                        binding.tvProfileName.setText(user.name);
                        binding.tvProfilePhone.setText(user.phone);
                        
                        // Sync local prefs for offline/fast access
                        sharedPreferences.edit()
                                .putString(KEY_USER_EMAIL, user.email)
                                .putString(KEY_USER_NAME, user.name)
                                .putString(KEY_USER_PHONE, user.phone)
                                .apply();
                    } else {
                        // If user data doesn't exist in DB (fallback)
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        binding.tvProfileEmail.setText(email != null ? email : DEFAULT_VALUE);
                        binding.tvProfileName.setText("User");
                        binding.tvProfilePhone.setText(DEFAULT_VALUE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (binding != null) {
                        showToast("Failed to load profile: " + error.getMessage());
                    }
                }
            };
            userRef.addValueEventListener(userListener);
        } else {
            binding.tvProfileEmail.setText(DEFAULT_VALUE);
            binding.tvProfileName.setText(DEFAULT_VALUE);
            binding.tvProfilePhone.setText(DEFAULT_VALUE);
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
        binding = null;
    }
}
