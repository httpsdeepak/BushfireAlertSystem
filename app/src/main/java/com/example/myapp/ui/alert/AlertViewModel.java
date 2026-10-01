package bushfireAlert.com.example.myapp.ui.alert;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import bushfireAlert.com.example.myapp.model.AlertItem;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlertViewModel extends AndroidViewModel {
    private final MutableLiveData<List<AlertItem>> allNearbyAlerts = new MutableLiveData<>(new ArrayList<>());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private static class Region {
        String name;
        double lat;
        double lon;

        Region(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private final List<Region> mockRegions = Arrays.asList(
            new Region("Oatley Park", -33.9856, 151.0711),
            new Region("Hurstville Grove", -33.9772, 151.0911),
            new Region("Penshurst", -33.9611, 151.0833),
            new Region("Beverly Hills", -33.9500, 151.0833),
            new Region("Kogarah Bay", -33.9833, 151.1167),
            new Region("Georges River", -33.9861, 151.0114),
            new Region("Wolli Creek", -33.9286, 151.1347),
            new Region("Royal National Park", -34.1319, 151.0632),
            new Region("Heathcote", -34.1353, 150.9631),
            new Region("Lugarno", -33.9961, 151.0433)
    );

    private final String[] adviceList = {
            "Stay indoors and close all windows. Monitor local radio.",
            "Evacuate immediately if you are in a high-risk zone.",
            "Ensure your bushfire survival plan is activated.",
            "Avoid outdoor activities and use of machinery that may spark.",
            "Keep hydrated and check on vulnerable neighbors."
    };

    public AlertViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        
        // Load alerts from Firebase first
        loadAlertsFromFirebase();
        
        fetchCurrentLocation();
        startAlertSimulation();
    }

    private void loadAlertsFromFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("alerts");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AlertItem> alerts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AlertItem item = child.getValue(AlertItem.class);
                    if (item != null) alerts.add(0, item); // Newest first
                }
                if (alerts.isEmpty()) {
                    loadInitialAlerts();
                } else {
                    allNearbyAlerts.setValue(alerts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadInitialAlerts();
            }
        });
    }

    private void loadInitialAlerts() {
        List<AlertItem> initials = new ArrayList<>();
        initials.add(new AlertItem("HIGH FIRE DANGER", "Fire activity reported near Oatley Park. Stay alert.", "2 mins ago", "Oatley Park", adviceList[0], 2));
        initials.add(new AlertItem("MODERATE FIRE DANGER", "Controlled burn in progress at Georges River.", "15 mins ago", "Georges River", adviceList[3], 1));
        initials.add(new AlertItem("EXTREME FIRE DANGER", "Uncontained fire heading North from Heathcote.", "1 hour ago", "Heathcote", adviceList[1], 3));
        initials.add(new AlertItem("LOW FIRE DANGER", "Smoke haze reported in Penshurst area.", "2 hours ago", "Penshurst", adviceList[4], 0));
        initials.add(new AlertItem("HIGH FIRE DANGER", "Spot fires detected in Royal National Park.", "3 hours ago", "Royal National Park", adviceList[2], 2));
        initials.add(new AlertItem("CATASTROPHIC FIRE DANGER", "Evacuation order issued for Lugarno residents.", "4 hours ago", "Lugarno", adviceList[1], 4));
        initials.add(new AlertItem("MODERATE FIRE DANGER", "Small grass fire under control in Beverly Hills.", "6 hours ago", "Beverly Hills", adviceList[0], 1));
        
        // Upload initial alerts to Firebase if database is empty
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("alerts");
        for (AlertItem item : initials) {
            db.push().setValue(item);
        }
        
        allNearbyAlerts.setValue(initials);
    }

    public LiveData<List<AlertItem>> getAllNearbyAlerts() {
        return allNearbyAlerts;
    }

    public void addAlert(AlertItem alert) {
        List<AlertItem> currentAlerts = allNearbyAlerts.getValue();
        List<AlertItem> updatedAlerts = new ArrayList<>();
        if (currentAlerts != null) {
            updatedAlerts.addAll(currentAlerts);
        }
        updatedAlerts.add(0, alert);
        allNearbyAlerts.setValue(updatedAlerts);
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
            }
            requestFreshLocation();
        });
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(request, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                    }
                });
    }

    private void startAlertSimulation() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateNewAlert();
                handler.postDelayed(this, 10000); // New alert every 10 seconds
            }
        }, 5000); 
    }

    private void generateNewAlert() {
        int severity = random.nextInt(5);
        String title;
        switch (severity) {
            case 4: title = "CATASTROPHIC FIRE DANGER"; break;
            case 3: title = "EXTREME FIRE DANGER"; break;
            case 2: title = "HIGH FIRE DANGER"; break;
            case 1: title = "MODERATE FIRE DANGER"; break;
            default: title = "LOW FIRE DANGER"; break;
        }

        Region region = mockRegions.get(random.nextInt(mockRegions.size()));
        String advice = adviceList[random.nextInt(adviceList.length)];

        AlertItem newAlert = new AlertItem(
                title,
                "New alert: Potential bushfire threat in " + region.name + ". Monitor local conditions and follow advice.",
                "Just now",
                region.name,
                advice,
                severity
        );

        // Upload to Firebase
        FirebaseDatabase.getInstance().getReference("alerts").push().setValue(newAlert);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacksAndMessages(null);
    }
}
