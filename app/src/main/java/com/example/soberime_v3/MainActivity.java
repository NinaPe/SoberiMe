package com.example.soberime_v3;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button btnVozac,btnKorinik;
    private ImageButton imgVozac,imgKorisnik;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            setContentView(R.layout.activity_main);

            startService(new Intent(MainActivity.this, onAppKilled.class));

            btnKorinik = (Button) findViewById(R.id.korisnik);
            btnVozac = (Button) findViewById(R.id.vozac);

            imgKorisnik = (ImageButton) findViewById(R.id.korisnikSlika);
            imgVozac = (ImageButton) findViewById(R.id.vozacSlika);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {

            }

            btnVozac.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, NoPermissionActivity.class);
                        startActivity(intent);
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, VozacLoginActivity.class);
                    startActivity(intent);
                    //finish();
                    return;
                }
            });
            imgVozac.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, NoPermissionActivity.class);
                        startActivity(intent);
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, VozacLoginActivity.class);
                    startActivity(intent);
                    //finish();
                    return;
                }
            });

            btnKorinik.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, NoPermissionActivity.class);
                        startActivity(intent);
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, KorisnikLoginActivity.class);
                    startActivity(intent);
                    //finish();
                    return;
                }
            });
            imgKorisnik.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, NoPermissionActivity.class);
                        startActivity(intent);
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, KorisnikLoginActivity.class);
                    startActivity(intent);
                    //finish();
                    return;
                }
            });
        } else {
            final String useId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference temp = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci");
            temp.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    for (String key : map.keySet()) {
                        if (key.equals(useId)) {
                            Intent intent = new Intent(MainActivity.this, VozacMapActivity.class);
                            startActivity(intent);
                            finish();
                            return;

                        }

                    }
                    Intent intent = new Intent(MainActivity.this, KorisnikMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
