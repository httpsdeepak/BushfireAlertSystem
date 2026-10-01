package bushfireAlert.com.example.myapp.ui.bookmark;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import bushfireAlert.com.example.myapp.databinding.ItemBookmarkBinding;
import bushfireAlert.com.example.myapp.model.BookmarkItem;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private List<BookmarkItem> items;
    private OnBookmarkRemovedListener listener;

    public interface OnBookmarkRemovedListener {
        void onRemoved(int position);
    }

    public BookmarkAdapter(List<BookmarkItem> items, OnBookmarkRemovedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookmarkBinding binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookmarkItem item = items.get(position);
        holder.binding.tvBookmarkName.setText(item.name);
        holder.binding.tvBookmarkTemp.setText(item.temp);
        holder.binding.tvBookmarkHumid.setText(item.humidity);
        if (item.lastUpdated != null && !item.lastUpdated.isEmpty()) {
            holder.binding.tvLastUpdated.setText("Updated: " + item.lastUpdated);
            holder.binding.tvLastUpdated.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvLastUpdated.setVisibility(android.view.View.GONE);
        }
        holder.binding.btnRemoveBookmark.setOnClickListener(v -> listener.onRemoved(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemBookmarkBinding binding;
        public ViewHolder(ItemBookmarkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
