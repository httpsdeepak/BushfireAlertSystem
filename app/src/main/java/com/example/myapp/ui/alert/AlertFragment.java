package bushfireAlert.com.example.myapp.ui.alert;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.databinding.FragmentAlertBinding;
import bushfireAlert.com.example.myapp.model.AlertItem;

import java.util.ArrayList;
import java.util.List;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;
    private AlertViewModel viewModel;
    private List<AlertItem> displayedAlerts = new ArrayList<>();
    private AlertAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlertBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);

        setupRecyclerView();
        setupButtons();

        // Observe ViewModel for alerts and display all of them
        viewModel.getAllNearbyAlerts().observe(getViewLifecycleOwner(), alerts -> {
            updateDisplayList(alerts);
        });

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AlertAdapter(displayedAlerts);
        binding.rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAlerts.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnAlertEmergency.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_emergency));
        binding.btnAlertContact.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_emergency));
        
        // Hide the "Load Old Alerts" button as we are showing all alerts now
        binding.btnLoadOldAlerts.setVisibility(View.GONE);
    }

    private void updateDisplayList(List<AlertItem> allAlerts) {
        displayedAlerts.clear();
        displayedAlerts.addAll(allAlerts);
        adapter.notifyDataSetChanged();
        updateAlertCount(allAlerts.size());
    }

    private void updateAlertCount(int count) {
        if (binding == null) return;
        binding.tvAlertCount.setText(String.valueOf(count));
        binding.tvAlertCountSub.setText(count + " Active Alerts in your area");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
