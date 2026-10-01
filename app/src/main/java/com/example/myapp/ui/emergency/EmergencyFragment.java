package bushfireAlert.com.example.myapp.ui.emergency;

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
import bushfireAlert.com.example.myapp.databinding.FragmentEmergencyBinding;
import bushfireAlert.com.example.myapp.databinding.ItemSafetyInstructionBinding;

public class EmergencyFragment extends Fragment {

    private FragmentEmergencyBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmergencyBinding.inflate(inflater, container, false);

        binding.btnBack.setOnClickListener(v -> {
            if (!Navigation.findNavController(v).popBackStack()) {
                // If there's no back stack, just navigate to home as a fallback
                Navigation.findNavController(v).navigate(R.id.navigation_home);
            }
        });

        binding.btnCallEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:000"));
            startActivity(intent);
        });

        setupSafetyInstructions();

        return binding.getRoot();
    }

    private void setupSafetyInstructions() {
        String[] instructions = {
                "Stay hydrated by drinking plenty of water, even if you don't feel thirsty.",
                "Keep your home cool by closing curtains and blinds during the heat of the day.",
                "Have an evacuation plan and practice it regularly with all family members.",
                "Prepare an emergency kit with essentials like water, food, and first aid.",
                "Stay informed by monitoring local news, radio, and emergency apps.",
                "Never leave children or pets in a parked car, even for a short time.",
                "Clear dry leaves, twigs, and debris from around your home and gutters.",
                "Keep a battery-powered radio and flashlight handy in case of power outages.",
                "Check on elderly neighbors or those who may need extra assistance.",
                "Know your local bushfire risk and the locations of nearby safe places.",
                "Wear light-colored, loose-fitting clothing to help stay cool.",
                "Avoid strenuous outdoor activities during the peak heat of the day."
        };

        binding.containerSafetyInstructions.removeAllViews();
        for (String instruction : instructions) {
            ItemSafetyInstructionBinding itemBinding = ItemSafetyInstructionBinding.inflate(getLayoutInflater(), binding.containerSafetyInstructions, false);
            itemBinding.tvInstruction.setText(instruction);
            binding.containerSafetyInstructions.addView(itemBinding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}