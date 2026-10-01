package bushfireAlert.com.example.myapp.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentLoginBinding;
import bushfireAlert.com.example.myapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {

    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";
    private FragmentLoginBinding binding;

    private static final String ADMIN_EMAIL = "admin@bushfire.com.au";
    private static final String ADMIN_PASSWORD = "11111111";
    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_PHONE = "0450278277";

    private static final String USER2_EMAIL = "abcd@gmail.com";
    private static final String USER2_PASSWORD = "11111111";
    private static final String USER2_NAME = "ABCD";
    private static final String USER2_PHONE = "0400000000";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        binding.btnToSignup.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_signup);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_forgot_password);
        });

        return binding.getRoot();
    }

    private void performLogin(String email, String password) {
        // Support for "old registered" hardcoded accounts
        if ((email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) ||
            (email.equals(USER2_EMAIL) && password.equals(USER2_PASSWORD))) {
            
            String name = email.equals(ADMIN_EMAIL) ? ADMIN_NAME : USER2_NAME;
            String phone = email.equals(ADMIN_EMAIL) ? ADMIN_PHONE : USER2_PHONE;
            
            saveLoginState(email, name, phone);
            Toast.makeText(getContext(), "Logged in as " + name, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_home, null,
                new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build());
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        fetchAndSaveUserData(task.getResult().getUser().getUid());
                    } else {
                        setLoading(false);
                        Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnToSignup.setEnabled(!isLoading);
    }

    private void fetchAndSaveUserData(String uid) {
        DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(uid);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    saveLoginState(user.email, user.name, user.phone);
                    if (isAdded()) {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_home, null,
                            new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build());
                    }
                } else {
                    // Fallback for users in Auth but not in Database
                    String email = "";
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    }
                    saveLoginState(email, "User", "");
                    if (isAdded()) {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_home, null,
                            new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        boolean isValid = true;
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            isValid = false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            isValid = false;
        }
        return isValid;
    }

    private void saveLoginState(String email, String name, String phone) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_email", email)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
