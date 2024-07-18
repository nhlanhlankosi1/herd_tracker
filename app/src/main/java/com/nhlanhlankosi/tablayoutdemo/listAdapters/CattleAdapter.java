package com.nhlanhlankosi.tablayoutdemo.listAdapters;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;

import java.util.List;

public class CattleAdapter extends RecyclerView.Adapter<CattleAdapter.ViewHolder> {

    private List<Cow> cattleList;
    private OnItemClickListener listener;
    private Dialog dialog;
    public CattleAdapter(List<Cow> cattleList, OnItemClickListener listener, Dialog dialog) {
        this.cattleList = cattleList;
        this.listener = listener;
        this.dialog = dialog;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cow cow = cattleList.get(position);
        holder.bind(cow, listener);
    }

    @Override
    public int getItemCount() {
        return cattleList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Cow cow);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView cowName;

        public ViewHolder(View itemView) {
            super(itemView);
            cowName = itemView.findViewById(R.id.cowName);
        }

        public void bind(final Cow cow, final OnItemClickListener listener) {
            cowName.setText(cow.getName());
            itemView.setOnClickListener(v -> {
                listener.onItemClick(cow);
                dialog.dismiss();  // Dismiss the dialog when a cow is clicked
            });
        }
    }
}


