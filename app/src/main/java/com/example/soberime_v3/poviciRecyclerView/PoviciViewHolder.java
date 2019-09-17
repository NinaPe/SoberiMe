package com.example.soberime_v3.poviciRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soberime_v3.R;

public class PoviciViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView destinationId;
    public TextView time;
    public TextView povikId;

    public PoviciViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        destinationId = (TextView) itemView.findViewById(R.id.destinacijaId);
        time = (TextView) itemView.findViewById(R.id.vreme);
        povikId = (TextView) itemView.findViewById(R.id.povikId);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), PoviciSingleActivity.class);
        Bundle b = new Bundle();
        b.putString("povikId", povikId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
