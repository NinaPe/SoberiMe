package com.example.soberime_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;

import com.example.soberime_v3.poviciRecyclerView.PoviciAdapter;
import com.example.soberime_v3.poviciRecyclerView.PoviciObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class PoviciActivity extends AppCompatActivity {

    private String korisnikIliVozac,userId;
    private RecyclerView recyclerViewPovici;
    private RecyclerView.Adapter poviciAdapter;
    private RecyclerView.LayoutManager poviciLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_povici);


        recyclerViewPovici = (RecyclerView) findViewById(R.id.poviciRecyclerView);
        recyclerViewPovici.setNestedScrollingEnabled(true);
        recyclerViewPovici.setHasFixedSize(true);

        poviciLayoutManager = new LinearLayoutManager(PoviciActivity.this);
        recyclerViewPovici.setLayoutManager(poviciLayoutManager);
        poviciAdapter = new PoviciAdapter(getDataSetPovik(), PoviciActivity.this);
        recyclerViewPovici.setAdapter(poviciAdapter);

        korisnikIliVozac = getIntent().getExtras().getString("korisnikIliVozac");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserPoviciIds();



    }

    private void getUserPoviciIds() {
        DatabaseReference userPoviciDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(korisnikIliVozac).child(userId).child("povici");
        userPoviciDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot povici: dataSnapshot.getChildren()){
                        FetchPovikInfo(povici.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void FetchPovikInfo(String key) {
        DatabaseReference poviciDatabase = FirebaseDatabase.getInstance().getReference().child("povici").child(key);
        poviciDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                   String povikId = dataSnapshot.getKey();
                   Long vreme = 0L;
                   String destinacija = null;
                   for(DataSnapshot child : dataSnapshot.getChildren()) {
                       if(child.getKey().equals("vreme")) {
                           vreme = Long.valueOf(child.getValue().toString());
                       }
                       if(child.getKey().equals("destinacija")) {
                           destinacija = child.getValue().toString();
                       }
                   }
                    PoviciObject obj;
                   if(destinacija != null) {
                       obj = new PoviciObject(povikId,"Дестинација: " + destinacija, getVreme(vreme));
                   }
                   else {
                       obj = new PoviciObject(povikId,"Дестинација: ---", getVreme(vreme));

                   }
                   resultPovik.add(obj);
                   poviciAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private String getVreme(Long vreme) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(vreme*1000);
        String datum = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString();
        return datum;
    }

    private ArrayList resultPovik = new ArrayList<PoviciObject>();

    private ArrayList<PoviciObject> getDataSetPovik() {
        return resultPovik;
    }
}
