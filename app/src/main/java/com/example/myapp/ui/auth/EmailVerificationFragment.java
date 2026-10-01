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
import bushfireAlert.com.example.myapp.databinding.FragmentEmailVerificationBinding;
import bushfireAlert.com.example.myapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmailVerificationFragment extends Fragment {

    private static final String DATABASE_URL = "https://bushfire-alert-final-default-rtdb.firebaseio.com/";
    private FragmentEmailVerificationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmailVerificationBinding.inflate(inflater, container, false);

        binding.btnResendEmail.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Verification email resent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.btnContinueToLogin.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (user.isEmailVerified()) {
                        fetchAndSaveUserData(user.getUid());
                    } else {
                        Toast.makeText(getContext(), "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Navigation.findNavController(v).navigate(R.id.navigation_login);
            }
        });

        return binding.getRoot();
    }

    private void fetchAndSaveUserData(String uid) {
        DatabaseReference db = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(uid);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    saveLoginState(user.email, user.name, user.phone);
                } else {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    saveLoginState(email, "User", "");
                }
                if (isAdded()) {
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_home, null,
                        new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}