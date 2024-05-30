package com.nhlanhlankosi.tablayoutdemo.listAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;

import java.util.List;

public class CattleAdapter extends RecyclerView.Adapter<CattleAdapter.CattleViewHolder> {

    private List<Cow> cattleList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cow cow);
    }

    public CattleAdapter(List<Cow> cattleList, OnItemClickListener listener) {
        this.cattleList = cattleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CattleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cow, parent, false);
        return new CattleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CattleViewHolder holder, int position) {
        Cow cow = cattleList.get(position);
        holder.cowName.setText(cow.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(cow));
    }

    @Override
    public int getItemCount() {
        return cattleList.size();
    }

    static class CattleViewHolder extends RecyclerView.ViewHolder {
        TextView cowName;

        CattleViewHolder(@NonNull View itemView) {
            super(itemView);
            cowName = itemView.findViewById(R.id.cowName);
        }
    }
}

