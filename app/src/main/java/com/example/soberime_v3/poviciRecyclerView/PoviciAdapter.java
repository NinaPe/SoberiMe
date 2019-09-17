package com.example.soberime_v3.poviciRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soberime_v3.R;

import java.util.List;

public class PoviciAdapter  extends RecyclerView.Adapter<PoviciViewHolder> {
    private List<PoviciObject> itemList;
    private Context context;

    public PoviciAdapter(List<PoviciObject> itemList, Context context){
        this.itemList = itemList;
        this.context = context;

    }

    @NonNull
    @Override
    public PoviciViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_povici,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        PoviciViewHolder pvh = new PoviciViewHolder(layoutView);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull PoviciViewHolder holder, int position) {
        holder.destinationId.setText(itemList.get(position).getDestinaion());
        holder.time.setText(itemList.get(position).getTime());
        holder.povikId.setText(itemList.get(position).getPovikId());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
