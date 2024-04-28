package com.nhlanhlankosi.tablayoutdemo.listAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.interfaces.ItemClickListener;
import com.nhlanhlankosi.tablayoutdemo.models.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final Context context;
    private final List<Notification> notificationsList;

    public NotificationsAdapter(Context context, List<Notification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_my_notifications_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        viewHolder.title.setText(notificationsList.get(position).getTitle());
        viewHolder.message.setText(notificationsList.get(position).getMessage());

        switch (notificationsList.get(position).getType()) {
            case "extreme_temperatures":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.temp_high));
                break;
            case "escape_alert":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.cow_wrong_location));
                break;
            case "low_battery":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.low_battery));
                break;
            case "unusual_activity":
            case "sudden_changes_in_behavior":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.unusual_activity));
                break;
            case "adverse_weather_conditions":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bad_weather));
                break;
            case "system_updates":
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.system_updates));
                break;
            case "abnormal_vital_signs":
            default:
                viewHolder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heart_rate_notification));
                break;

        }

        viewHolder.setItemClickListener((v, absPosition, isLongClick) -> {

        });

    }

    @Override
    public int getItemCount() {
        return notificationsList != null ? notificationsList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView title;
        private final TextView message;

        private final ImageView img;

        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.notification_title_txt);
            message = itemView.findViewById(R.id.notification_msg_txt);
            img = itemView.findViewById(R.id.notification_img);

            itemView.setOnClickListener(this);

        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAbsoluteAdapterPosition(), false);
        }

    }

}
