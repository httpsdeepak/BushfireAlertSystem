package bushfireAlert.com.example.myapp.ui.auth;

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
import bushfireAlert.com.example.myapp.databinding.FragmentForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);

        binding.btnResetPassword.setOnClickListener(v -> {
            if (validateInput()) {
                sendResetEmail();
            }
        });

        binding.tvBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_login);
        });

        return binding.getRoot();
    }

    private boolean validateInput() {
        String email = binding.etEmail.getText().toString().trim();
        binding.tilEmail.setError(null);

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private void sendResetEmail() {
        String email = binding.etEmail.getText().toString().trim();
        
        // Disable button to prevent multiple clicks
        binding.btnResetPassword.setEnabled(false);
        
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (binding == null) return;
                    
                    binding.btnResetPassword.setEnabled(true);
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), 
                                "Password reset link sent to " + email, 
                                Toast.LENGTH_LONG).show();
                        Navigation.findNavController(binding.getRoot())
                                .navigate(R.id.navigation_login);
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}