package bushfireAlert.com.example.myapp.ui.alert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentAlertDetailBinding;
import bushfireAlert.com.example.myapp.model.AlertItem;

public class AlertDetailFragment extends Fragment {

    private FragmentAlertDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlertDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            AlertItem alert = (AlertItem) getArguments().getSerializable("alert");
            if (alert != null) {
                displayAlertDetails(alert);
            }
        }

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnCallEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:000"));
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void displayAlertDetails(AlertItem alert) {
        binding.tvDetailTitle.setText(alert.getTitle());
        binding.tvDetailTime.setText("Issued: " + alert.getTimestamp());
        binding.tvDetailLocation.setText(alert.getLocation());
        binding.tvDetailSummary.setText(alert.getMessage());
        binding.tvDetailAdvice.setText(alert.getAdvice());

        // Set color based on severity
        int colorRes;
        switch (alert.getSeverity()) {
            case 4: colorRes = R.color.rating_cat; break;
            case 3: colorRes = R.color.rating_ext; break;
            case 2: colorRes = R.color.rating_high; break;
            case 1: colorRes = R.color.rating_mod; break;
            default: colorRes = R.color.rating_low; break;
        }
        binding.tvDetailTitle.setTextColor(getResources().getColor(colorRes));
        binding.cardDetail.setStrokeColor(getResources().getColor(colorRes));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
