package com.example.soberime_v3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VozacMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Button logout, opcii, btnPatnikStatus, povici;
    private Switch vSwitch;

    private int status = 0;

    private boolean isLoggingOut = false;

    private String patnikId = "", destinacija;
    private LatLng latLngDestinacija, patnikLatLng;
    private float vozenjeRastojanie = 0;

    private LinearLayout korisnikInfo, korisnikInfoButton;
    private ImageView korisnikSlika;
    private TextView korisnikIme, korisnikTel, korisnikDestinacija;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vozac_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();

        //latLngDestinacija = new LatLng(0.0, 0.0);

        logout = (Button) findViewById(R.id.logout);
        opcii = (Button) findViewById(R.id.opciiVozac);
        povici = (Button) findViewById(R.id.poviciVozac);
        btnPatnikStatus = (Button) findViewById(R.id.patnikStatus);

        vSwitch = (Switch) findViewById(R.id.vozamSwitch);

        vSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    zapishiVozac();

                }
                else {
                    izbrishiVozac();
                }
            }
        });

        korisnikInfoButton = (LinearLayout) findViewById(R.id.korisnikInfoButton);
        korisnikInfo = (LinearLayout) findViewById(R.id.korisnikInfo);
        korisnikSlika = (ImageView) findViewById(R.id.korisnikSlika);
        korisnikIme = (TextView) findViewById(R.id.korisnikIme);
        korisnikTel = (TextView) findViewById(R.id.korisnikTel);
        korisnikDestinacija = (TextView) findViewById(R.id.korisnikDestinacija);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                izbrishiVozac();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(VozacMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        opcii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VozacMapActivity.this, VozacOpciiActivity.class);
                startActivity(intent);
                return;

            }
        });

        povici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VozacMapActivity.this, PoviciActivity.class);
                intent.putExtra("korisnikIliVozac","Vozaci");

                startActivity(intent);
                return;
            }
        });

        btnPatnikStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case 1:
                        status = 2;
                        eraseRolylines();
                        if(latLngDestinacija.latitude != 0.0 && latLngDestinacija.longitude != 0.0) {
                            getRouteToMarker(latLngDestinacija);
                        }
                        btnPatnikStatus.setText("Заврши возењето");

                        break;
                    case 2:
                        zapisiVozenje();
                        zavrshiVozenjeto();
                        break;

                }
            }
        });

        korisnikInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                korisnikInfo.setVisibility(View.INVISIBLE);
                korisnikInfo.setBackgroundColor(Color.parseColor("#80000000"));
            }
        });



        getPatnik();
    }



    private void getPatnik() {
        String vozacId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference patnikRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(vozacId).child("povikPatnik").child("patnikID");
        patnikRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {//dodelen e patnik
                    status = 1;
                    patnikId = dataSnapshot.getValue().toString();
                    getPatnikInfo();
                    getPatnikDestinaija();
                    getPatnikLokacija();
                }//DataSnapshot dava false koga kje se izbrishe deteto
                else {
                    zavrshiVozenjeto();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPatnikDestinaija() {
        String vozacId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference patnikRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(vozacId).child("povikPatnik");
        patnikRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {//dodelen e patnik
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination") != null) {
                        destinacija = map.get("destination").toString();
                        korisnikDestinacija.setText("Дестинација: " + destinacija);
                    }
                    else {
                        korisnikDestinacija.setText("Дестинација: --" );
                    }

                    Double destinacijaLat = 0.0;
                    Double destinacijaLng = 0.0;

                    if(map.get("destinationLat") != null) {
                        destinacijaLat = Double.valueOf(map.get("destinationLat").toString());
                    }


                    if(map.get("destinationLng") != null) {
                        destinacijaLng = Double.valueOf(map.get("destinationLng").toString());
                    }
                    latLngDestinacija = new LatLng(destinacijaLat, destinacijaLng);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPatnikInfo() {
        korisnikInfoButton.setVisibility(View.VISIBLE);
        korisnikInfo.setBackgroundColor(Color.WHITE);
        DatabaseReference korisnikDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Korisnici").child(patnikId);
        korisnikDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name") != null) {
                        korisnikIme.setText(map.get("name").toString());
                    }

                    if(map.get("phone") != null) {
                        korisnikTel.setText(map.get("phone").toString());
                    }

                    if(map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(korisnikSlika);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker soberiMeMarker;
    private DatabaseReference patnikLocacijaRef;
    private ValueEventListener patnikLokacijaRefListener;
    private void getPatnikLokacija() {
        patnikLocacijaRef = FirebaseDatabase.getInstance().getReference().child("soberimePovik").child(patnikId).child("l");
        patnikLokacijaRefListener = patnikLocacijaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !patnikId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;

                    if(map.get(0) != null) {
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null) {
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }

                    patnikLatLng = new LatLng(locationlat, locationlng);
                    soberiMeMarker = mMap.addMarker(new MarkerOptions().position(patnikLatLng).title("Вашиот патник").icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer)));
                    getRouteToMarker(patnikLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void zapisiVozenje() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference vozacRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(userId).child("povici");
        DatabaseReference patnikRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Korisnici").child(patnikId).child("povici");
        DatabaseReference poviciRef = FirebaseDatabase.getInstance().getReference().child("povici");
        String povikID =  poviciRef.push().getKey();
        vozacRef.child(povikID).setValue(true);
        patnikRef.child(povikID).setValue(true);

        HashMap map = new HashMap();
        map.put("vozac", userId);
        map.put("patnik", patnikId);
        map.put("ocenka" ,0);
        map.put("vreme", getCurrentTimeStamp());
        map.put("destinacija", destinacija);
        map.put("location/from/lat", patnikLatLng.latitude);
        map.put("location/from/lng", patnikLatLng.longitude);
        map.put("location/to/lat", latLngDestinacija.latitude);
        map.put("location/to/lng", latLngDestinacija.longitude);
        map.put("rastojanie", vozenjeRastojanie/1000.0);
        poviciRef.child(povikID).updateChildren(map);

    }

    private Long getCurrentTimeStamp() {
        Long timeStamp = System.currentTimeMillis()/1000;
        return timeStamp;
    }

    private void zavrshiVozenjeto() {
        btnPatnikStatus.setText("Собери патник");
        eraseRolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference vozacRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Vozaci").child(userId).child("povikPatnik");
        vozacRef.removeValue();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("soberimePovik");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(patnikId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

        patnikId = "";

        if(soberiMeMarker != null) {
            soberiMeMarker.remove();
        }
        if(patnikLokacijaRefListener != null) {
            patnikLocacijaRef.removeEventListener(patnikLokacijaRefListener);
        }
        korisnikInfoButton.setVisibility(View.GONE);
        korisnikInfo.setVisibility(View.VISIBLE);
        korisnikTel.setText("");
        korisnikIme.setText("");
        korisnikSlika.setImageResource(R.mipmap.customer);
        korisnikSlika.setImageResource(R.mipmap.customer);
        korisnikDestinacija.setText("Дестинација: --" );
        vozenjeRastojanie = 0;

    }


    private void getRouteToMarker(LatLng patnikLatLng) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyCpKOVjga2I_WFfF3qQBp5sBdeylRBfKtA")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), patnikLatLng)
                .build();
        routing.execute();

    }

    boolean first = true;
    boolean vozi = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(VozacMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
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
        if(getApplicationContext() != null) {
            //if(!patnikId.equals("")) {
            //    vozenjeRastojanie += lastLocation.distanceTo(location);
            //}

            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(first || vozi) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                first = false;
            }


            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dostapniRef = FirebaseDatabase.getInstance().getReference("dostapniVozaci");
            DatabaseReference workingRef = FirebaseDatabase.getInstance().getReference("vozaciWorking");
            GeoFire geoFireDostapni = new GeoFire(dostapniRef);
            GeoFire geoFireWorking = new GeoFire(workingRef);
            switch (patnikId) {
                case "":
                    geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    geoFireDostapni.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    vozi = false;
                    break;

                default:
                    geoFireDostapni.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    vozi = true;
                    break;
            }


        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //ova e za app kade mapata e main stvara
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void zapishiVozac() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(VozacMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }
        // fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //fusedLocationProviderClient.requestLocationUpdates(googleApiClient, locationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void izbrishiVozac() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("dostapniVozaci");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

    }



    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorAccent,R.color.primary_dark_material_light};


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {


    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
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

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()/1000+"km : duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            vozenjeRastojanie += route.get(i).getDistanceValue();
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
