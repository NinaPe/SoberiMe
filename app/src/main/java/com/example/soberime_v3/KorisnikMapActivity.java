package com.example.soberime_v3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.libraries.places.api.Places;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KorisnikMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Button logout, povik, opciiKorisnik, povici;
    private boolean isLoggingOut = false;
    private LatLng soberiLocation;
    private Boolean povikBoolean = false;
    private Marker mSoberiMeMarker;

    private String destination;
    private LatLng latLngDestination = new LatLng(0,0);

    private LinearLayout vozacInfo;
    private ImageView vozacSlika;
    private TextView vozacIme, vozacTel, vozacKola;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_korisnik_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyCpKOVjga2I_WFfF3qQBp5sBdeylRBfKtA");

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        latLngDestination = new LatLng(0.0, 0.0);

        logout = (Button) findViewById(R.id.logoutKorisnik);
        povik = (Button) findViewById(R.id.povik);
        opciiKorisnik = (Button) findViewById(R.id.settingsKorisnik);
        povici = (Button) findViewById(R.id.poviciKorisnik);

        vozacInfo = (LinearLayout) findViewById(R.id.vozacInfo);
        vozacSlika = (ImageView) findViewById(R.id.vozacSlika);
        vozacIme = (TextView) findViewById(R.id.vozacIme);
        vozacTel = (TextView) findViewById(R.id.vozacTel);
        vozacKola = (TextView) findViewById(R.id.vozacKola);
        ratingBar = (RatingBar)findViewById(R.id.ratingBarVozac);

        //vozacInfo.setVisibility(View.INVISIBLE);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //isLoggingOut = true;
                //izbrishiVozac();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(KorisnikMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        povik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (povikBoolean) {
                    zavrshiVozenjeto();
                } else {
                    povikBoolean = true;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("soberimePovik");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    soberiLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mSoberiMeMarker = mMap.addMarker(new MarkerOptions().position(soberiLocation).title("Собери Ме").icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer)));

                    povik.setText("Се бара возач...");

                    getNajblizokVozac();
                }
            }
        });

        opciiKorisnik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KorisnikMapActivity.this, KorisnikOpciiActivity.class);
                startActivity(intent);
                return;
            }
        });

        vozacInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vozacInfo.setVisibility(View.INVISIBLE);
            }
        });

        povici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KorisnikMapActivity.this, PoviciActivity.class);
                intent.putExtra("korisnikIliVozac","Korisnici");

                startActivity(intent);
                return;
            }
        });


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName();
                latLngDestination = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

    }

    private int radius = 100;
    private Boolean najdenVozac = false;
    private String najdenVozacId;

    private GeoQuery geoQuery;

    private void getNajblizokVozac() {
        DatabaseReference vozacLokacija = FirebaseDatabase.getInstance().getReference().child("dostapniVozaci");

        GeoFire geoFire = new GeoFire(vozacLokacija);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(soberiLocation.latitude, soberiLocation.latitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!najdenVozac && povikBoolean) {
                    najdenVozac = true;
                    najdenVozacId = key;
                    radius = 1;

                    //DatabaseReference vozacRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(najdenVozacId);
                    DatabaseReference vozacRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(najdenVozacId).child("povikPatnik");
                    String korisnikId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("patnikID", korisnikId);
                    map.put("destination", destination);
                    if(latLngDestination != null) {
                        map.put("destinationLat", latLngDestination.latitude);
                        map.put("destinationLng", latLngDestination.longitude);
                    }

                    vozacRef.updateChildren(map);


                    getVozacLokacija();
                    getVozacInfo();
                    getDaliZavrshi();

                    povik.setText("Се бара локацијата на возачот");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!najdenVozac) {
                    radius += 100;
                    if(radius > 3000) {
                        Toast.makeText(getApplicationContext(),"Моментално нема возачи!", Toast.LENGTH_LONG).show();
                         //zavrshiVozenjeto();
                         //povik.performClick();

                    }
                    getNajblizokVozac();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getVozacInfo() {

        DatabaseReference vozacDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(najdenVozacId);
        vozacDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    vozacInfo.setVisibility(View.VISIBLE);
                     Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        vozacIme.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if (map.get("phone") != null) {
                        vozacTel.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if (map.get("car") != null) {
                        vozacKola.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(vozacSlika);
                    }
                    float rating = 0;
                    float total = 0;
                    if(map.get("ocenka") != null) {
                        for (DataSnapshot child : dataSnapshot.child("ocenka").getChildren()) {
                            rating += Integer.valueOf(child.getValue().toString());
                            total++;
                        }
                        if(total != 0)
                            rating/=total;

                        ratingBar.setRating(rating);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference daliZavrshikRef;
    private ValueEventListener daliZavrshiRefListener;

    private void getDaliZavrshi() {
        daliZavrshikRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(najdenVozacId).child("povikPatnik").child("patnikID");
        daliZavrshiRefListener = daliZavrshikRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {//dodelen e patnik

                }
                else {
                    zavrshiVozenjeto();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void zavrshiVozenjeto() {
        povikBoolean = false;

        geoQuery.removeAllListeners();
        if(vozacLokacijaRef != null) {
            vozacLokacijaRef.removeEventListener(vozacLokacijaRefListener);
        }
        if(daliZavrshikRef != null) {
            daliZavrshikRef.removeEventListener(daliZavrshiRefListener);
        }

        if (najdenVozacId != null) {
            DatabaseReference vozacRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(najdenVozacId).child("povikPatnik");
            vozacRef.removeValue();
            najdenVozacId = null;
        }

        najdenVozac = false;
        radius = 1;

        if (mSoberiMeMarker != null) {
            mSoberiMeMarker.remove();
        }

        if (mVozacMarker != null) {
            mVozacMarker.remove();
        }

        vozacInfo.setVisibility(View.GONE);
        vozacTel.setText("");
        vozacIme.setText("");
        vozacSlika.setImageResource(R.mipmap.customer);
        vozacKola.setText("");
        povik.setText("Собери ме");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("soberimePovik");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });


    }

    private Marker mVozacMarker;
    private DatabaseReference vozacLokacijaRef;
    private ValueEventListener vozacLokacijaRefListener;
    private void getVozacLokacija() {
        vozacLokacijaRef = FirebaseDatabase.getInstance().getReference().child("vozaciWorking").child(najdenVozacId).child("l");
        vozacLokacijaRefListener = vozacLokacijaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && povikBoolean) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;
                    povik.setText("Најден е возач");

                    if(map.get(0) != null) {
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null) {
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng vozacLatLng = new LatLng(locationlat, locationlng);
                    if(mVozacMarker != null) {
                        mVozacMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(soberiLocation.latitude);
                    loc1.setLongitude(soberiLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(vozacLatLng.latitude);
                    loc2.setLongitude(vozacLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if(distance < 100) {
                        povik.setText("Возачот е простигнат");
                        try {

                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        povik.setText("Најден е возач: " + String.valueOf(distance/1000) + " km");
                        //mVozacMarker = mMap.addMarker(new MarkerOptions().position(vozacLatLng).title("Вашиот возач"));
                        mVozacMarker = mMap.addMarker(new MarkerOptions().position(vozacLatLng).title("Вашиот возач").icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver)));
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    boolean first = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(KorisnikMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(first) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            first = false;
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //ova e za app kade mapata e main stvara

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(KorisnikMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }
        // fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //fusedLocationProviderClient.requestLocationUpdates(googleApiClient, locationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStop() {
        super.onStop();


    }
}
