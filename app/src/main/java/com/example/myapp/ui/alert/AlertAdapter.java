package bushfireAlert.com.example.myapp.ui.alert;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import bushfireAlert.com.example.myapp.R;
import bushfireAlert.com.example.myapp.model.AlertItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<AlertItem> alertList;

    public AlertAdapter(List<AlertItem> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertItem item = alertList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getMessage());
        holder.tvTime.setText(item.getTimestamp());
        holder.tvLocation.setText("📍 " + item.getLocation());

        // Color coding based on severity
        int color;
        switch (item.getSeverity()) {
            case 4: color = Color.parseColor("#C0392B"); break; // Catastrophic
            case 3: color = Color.parseColor("#E74C3C"); break; // Extreme
            case 2: color = Color.parseColor("#E67E22"); break; // High
            case 1: color = Color.parseColor("#F1C40F"); break; // Moderate
            default: color = Color.parseColor("#27AE60"); break; // Low
        }
        holder.tvTitle.setTextColor(color);
        holder.card.setStrokeColor(color);

        holder.btnViewDetails.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("alert", item);
            Navigation.findNavController(v).navigate(R.id.action_navigation_alert_to_navigation_alert_detail, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime, tvLocation;
        MaterialCardView card;
        MaterialButton btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            tvLocation = itemView.findViewById(R.id.tvAlertLocation);
            card = (MaterialCardView) itemView;
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
