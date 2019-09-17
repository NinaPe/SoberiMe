package com.example.soberime_v3.poviciRecyclerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.soberime_v3.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PoviciSingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private String povikId, userId;

    private TextView lokacijaV;
    private TextView rastojanieV;
    private TextView datumV;
    private TextView userIme;
    private TextView userTel;
    private TextView vozenjeCena;

    private ImageView userSlika;

    private RatingBar ratingBar;

    private DatabaseReference poviciVozenjeInfoDb;

    private LatLng destinacijaLatLng, patnikLatLng;

    private String rastojanie;
    private int cena;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_povici_single);

        polylines = new ArrayList<>();

        povikId = getIntent().getExtras().getString("povikId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSingle);
        mMapFragment.getMapAsync(this);

        lokacijaV = (TextView) findViewById(R.id.vozenjeOdDo);
        rastojanieV = (TextView) findViewById(R.id.vozenjeRastojanie);
        datumV = (TextView) findViewById(R.id.vozenjeVreme);
        userIme = (TextView) findViewById(R.id.userIme);
        userTel = (TextView) findViewById(R.id.userTel);
        vozenjeCena = (TextView) findViewById(R.id.vozenjeCena);
        
        userSlika = (ImageView) findViewById(R.id.userImage);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        poviciVozenjeInfoDb = FirebaseDatabase.getInstance().getReference().child("povici").child(povikId);

        getVozenjeInfo();
    }

    private String patnikId, userVozacPatnik, vozacId;
    private void getVozenjeInfo() {
        poviciVozenjeInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if(child.getKey().equals("patnik")) {
                            patnikId = child.getValue().toString();
                            if(!patnikId.equals(userId)) {
                                userVozacPatnik = "Vozaci";
                                getUserInfo("Korisnici", patnikId);
                            }

                        }

                        if(child.getKey().equals("vozac")) {
                            vozacId = child.getValue().toString();
                            if(!vozacId.equals(userId)) {
                                userVozacPatnik = "Korisnici";
                                getUserInfo("Vozaci", vozacId);
                                displayPatnikObjects();
                            }
                        }

                        if(child.getKey().equals("vreme")) {
                            datumV.setText(getVreme(Long.valueOf(child.getValue().toString())));
                        }

                        if(child.getKey().equals("ocenka")) {
                            ratingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }

                        if(child.getKey().equals("destinacija")) {
                            lokacijaV.setText(child.getValue().toString());
                        }

                        if(child.getKey().equals("rastojanie")) {
                            if(!lokacijaV.getText().toString().equals("Од - До")) {
                                rastojanie = child.getValue().toString();
                                rastojanieV.setText(rastojanie.substring(0, Math.min(rastojanie.length(), 5)) + " km");
                                vozenjeCena.setText(String.valueOf(Double.parseDouble(rastojanie.substring(0, Math.min(rastojanie.length(), 5))) * 30).substring(0, Math.min(rastojanie.length(), 6)));
                            }
                            else {
                                vozenjeCena.setText("30 денари/km");
                            }

                        }

                        if(child.getKey().equals("location")) {
                            patnikLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinacijaLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinacijaLatLng != new LatLng(0,0)) {
                                getRouteToMarker();
                            }
                        }



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayPatnikObjects() {
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                poviciVozenjeInfoDb.child("ocenka").setValue(rating);
                DatabaseReference vozacOcenkaDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(vozacId).child("ocenka");
                vozacOcenkaDb.child(povikId).setValue(rating);

            }
        });
    }

    private void getUserInfo(String drugUser, String drugUserId) {
        DatabaseReference drugUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(drugUser).child(drugUserId);
        drugUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null) {
                        userIme.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null) {
                        userTel.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null) {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(userSlika);
                    }
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


    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .key("AIzaSyCpKOVjga2I_WFfF3qQBp5sBdeylRBfKtA")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(patnikLatLng, destinacijaLatLng)
                .build();
        routing.execute();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorAccent,R.color.primary_dark_material_light};


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            //Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            //Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {


    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(patnikLatLng);
        builder.include(destinacijaLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padd = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padd);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(patnikLatLng).title("Патник").icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer)));
        mMap.addMarker(new MarkerOptions().position(destinacijaLatLng).title("Дестинација"));

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void eraseRolylines() {
        for(Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }
}
