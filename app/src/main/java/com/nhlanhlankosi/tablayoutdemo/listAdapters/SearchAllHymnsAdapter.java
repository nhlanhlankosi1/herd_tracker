package com.nhlanhlankosi.tablayoutdemo.listAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.interfaces.ItemClickListener;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAllHymnsAdapter extends RecyclerView.Adapter<SearchAllHymnsAdapter.ViewHolder> {

    private final Context context;
    private final List<Cow> cattleList;
    private final ArrayList<Cow> arrayList = new ArrayList<>();

    public SearchAllHymnsAdapter(Context context, List<Cow> cattleList) {
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

        viewHolder.cowNameTv.setText(cattleList.get(position).getName());
        viewHolder.collarIdTv.setText(cattleList.get(position).getCollarId());
        viewHolder.heartRateTv.setText(String.format(Locale.ENGLISH, "%d bpm", cattleList.get(position).getHeartRate()));
        viewHolder.cowTemperatureTv.setText(String.format(Locale.ENGLISH, "%s °C", cattleList.get(position).getTemperature()));

        viewHolder.setItemClickListener((v, absPosition, isLongClick) -> {

            Hymn hymn = cattleList.get(absPosition);
            Activity hymnCategoryActivity = getHymnCategoryActivity(hymn.getExtraHymnIdKey());
            Intent openHymnActivity = new Intent(context, hymnCategoryActivity != null ?
                    hymnCategoryActivity.getClass() : NdebeleHymnsMainActivity.class);
            openHymnActivity.putExtra(hymn.getExtraHymnIdKey(), hymn.getExtraHymnIdValue());
            context.startActivity(openHymnActivity);

        });

    }

    @Override
    public int getItemCount() {
        return cattleList != null ? cattleList.size() : 0;
    }

    public void filter(String charText, SearchHymnListener searchHymnListener) {

        charText = charText.toLowerCase(Locale.getDefault()).trim();
        cattleList.clear();

        if (charText.equals(".")) {
            cattleList.addAll(arrayList);
        } else {
            for (Hymn searchListItem : arrayList) {
                if (searchListItem.getHymnName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    cattleList.add(searchListItem);
                }
            }
        }

        notifyDataSetChanged();

        if (cattleList.isEmpty()) {
            searchHymnListener.onEmptyResultReturnedFor(charText);
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
