package bushfireAlert.com.example.myapp.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentSignupBinding;
import bushfireAlert.com.example.myapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFragment extends Fragment {

    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";
    private FragmentSignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);

        binding.btnSignup.setOnClickListener(v -> {
            if (validateInput()) {
                performSignup();
            }
        });

        binding.tvToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_login);
        });

        return binding.getRoot();
    }

    private void performSignup() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        User user = new User(uid, name, email, phone);
                        
                        DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users");
                        db.child(uid).setValue(user).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                saveLoginState(email, name, phone);
                                Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_LONG).show();
                                Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_home, null,
                                    new androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_graph, true)
                                        .build());
                            } else {
                                Toast.makeText(getContext(), "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveLoginState(String email, String name, String phone) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_email", email)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .apply();
    }

    private boolean validateInput() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean isAgreed = binding.cbAgree.isChecked();

        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        boolean isValid = true;

        if (name.isEmpty()) {
            binding.tilFullName.setError("Full name is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Phone number is required");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isAgreed) {
            Toast.makeText(getContext(), "You must agree to the Terms & Privacy", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
