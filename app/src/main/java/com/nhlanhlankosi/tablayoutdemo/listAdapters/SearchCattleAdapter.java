package com.nhlanhlankosi.tablayoutdemo.listAdapters;

import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.BREED;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COLLAR_ID;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_ID;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_NAME;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.COW_PIC_URL;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.GENDER;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.HEART_RATE;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LATITUDE;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.LONGITUDE;
import static com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity.TEMPERATURE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.activities.CowInfoActivity;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.interfaces.ItemClickListener;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchCattleAdapter extends RecyclerView.Adapter<SearchCattleAdapter.ViewHolder> {

    private final Context context;
    private final List<Cow> cattleList;
    private final ArrayList<Cow> arrayList = new ArrayList<>();

    public SearchCattleAdapter(Context context, List<Cow> cattleList) {
        this.context = context;
        this.cattleList = cattleList;
        this.arrayList.addAll(cattleList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_my_herd_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        if (!TextUtils.isEmpty(cattleList.get(position).getCowPicUrl())) {

            Picasso.get()
                    .load(cattleList.get(position).getCowPicUrl())
                    .placeholder(R.drawable.cow_pic_place_holder)
                    .fit()
                    .centerInside()
                    .into(viewHolder.cowPicture);

        }

        viewHolder.cowNameTv.setText(cattleList.get(position).getName());
        viewHolder.collarIdTv.setText(cattleList.get(position).getCollarId());
        viewHolder.heartRateTv.setText(String.format(Locale.ENGLISH, "%d bpm", cattleList.get(position).getHeartRate()));
        viewHolder.cowTemperatureTv.setText(String.format(Locale.ENGLISH, "%s °C", cattleList.get(position).getTemperature()));

        viewHolder.setItemClickListener((v, absPosition, isLongClick) -> {

            Cow cow = cattleList.get(absPosition);
            Bundle cowBundle = new Bundle();
            cowBundle.putString(COW_NAME, cow.getName());
            cowBundle.putString(COW_ID, cow.getId());
            cowBundle.putString(COW_PIC_URL, cow.getCowPicUrl());
            cowBundle.putString(COLLAR_ID, cow.getCollarId());
            cowBundle.putString(GENDER, cow.getGender());
            cowBundle.putString(BREED, cow.getBreed());
            cowBundle.putLong(HEART_RATE, cow.getHeartRate());
            cowBundle.putDouble(TEMPERATURE, cow.getTemperature());
            cowBundle.putDouble(LONGITUDE, cow.getLongitude());
            cowBundle.putDouble(LATITUDE, cow.getLatitude());
            Intent cowInfoIntent = new Intent(context, CowInfoActivity.class);
            cowInfoIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            cowInfoIntent.putExtras(cowBundle);
            context.startActivity(cowInfoIntent);

        });

    }

    @Override
    public int getItemCount() {
        return cattleList != null ? cattleList.size() : 0;
    }

    public void filter(String charText, SearchCattleListener searchCattleListener) {

        charText = charText.toLowerCase(Locale.getDefault()).trim();
        cattleList.clear();

        if (charText.equals(".")) {
            cattleList.addAll(arrayList);
        } else {
            for (Cow searchListItem : arrayList) {
                if (searchListItem.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    cattleList.add(searchListItem);
                }
            }
        }

        notifyDataSetChanged();

        if (cattleList.isEmpty()) {
            searchCattleListener.onEmptyResultReturnedFor(charText);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView cowPicture;
        private final TextView cowNameTv;
        private final TextView collarIdTv;
        private final TextView heartRateTv;
        private final TextView cowTemperatureTv;

        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cowPicture = itemView.findViewById(R.id.cow_picture);
            cowNameTv = itemView.findViewById(R.id.cow_name_tv);
            collarIdTv = itemView.findViewById(R.id.collar_id_tv);
            heartRateTv = itemView.findViewById(R.id.heart_rate_tv);
            cowTemperatureTv = itemView.findViewById(R.id.cow_temperature_tv);

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
