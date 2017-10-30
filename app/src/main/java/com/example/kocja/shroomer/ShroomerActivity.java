package com.example.kocja.shroomer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

public class ShroomerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationClient;
    Location yourlocation;
    int addShroomrequest = 2, REQUEST_SHROOMDATA = 3;
    int indexOfType;
    Marker publicMarker, shroomMarker;
    Resources resource;
    List<markerLocatio> allLocations;
    LatLng markerpositionclicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shroomer);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Location location = new Location(LocationManager.GPS_PROVIDER);
        final FloatingActionButton addShroom = findViewById(R.id.floatingActionButton);
        resource = getApplicationContext().getResources();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                yourlocation = location;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ShroomerActivity.this);
                                builder.setTitle("Locaton is not found");
                                builder.setMessage("location has not been found");
                                builder.setCancelable(true);
                                builder.show();
                            }
                        }
                    });
            mMap.setMyLocationEnabled(true);
        }
        else {
            ActivityCompat.requestPermissions(ShroomerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
        }

        //Initialize database
        FlowManager.init(FlowConfig.builder(getApplicationContext())
                .addDatabaseConfig(DatabaseConfig.builder(AppDatabase.class)
                        .databaseName("AppDatabase")
                        .build())
                .build());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

        SQLite.select()
                .from(markerLocatio.class)
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<markerLocatio>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<markerLocatio> tResult) {
                        allLocations = tResult;
                        for (markerLocatio marker : allLocations) {
                            LatLng singlelat = new LatLng(Double.parseDouble(marker.latitude), Double.parseDouble(marker.longtitude));
                            Bitmap bitmap;
                            //loop through all the entries and determine the latitude and longtitude, check what type of icon it is, set the marker
                            switch (marker.indexOfType) {
                                case 0:
                                    bitmap = BitmapFactory.decodeResource(resource, R.drawable.icons8_mushroom_96);
                                    break;
                                case 1:
                                    bitmap = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_down);
                                    break;
                                case 2:
                                    bitmap = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_upward);
                                    break;
                                case 3:
                                    bitmap = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_forward);

                                    break;
                                default:
                                    bitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_delete_black_24dp);
                                    break;
                            }
                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 72, 72, false));
                            mMap.addMarker(new MarkerOptions().position(singlelat).title(marker.InfoWindow).icon(descriptor));
                        }
                    }
                }).execute();
        //If clicked on map, add a marker and save it into the database. Old. Uselesss now, will have to remove
        //Still good for debugging though
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker maker = mMap.addMarker(new MarkerOptions().position(latLng).title("shroom found"));

                markerLocatio locatio = new markerLocatio();
                locatio.latitude = Double.toString(latLng.latitude);
                locatio.longtitude = Double.toString(latLng.longitude);
                locatio.InfoWindow = maker.getTitle();
                locatio.save();
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                publicMarker = marker;
                Intent shroomData = new Intent(ShroomerActivity.this, com.example.kocja.shroomer.shroomData.class);
                markerpositionclicked = marker.getPosition();
                shroomData.putExtra("setLatitude", markerpositionclicked.latitude);
                startActivityForResult(shroomData, REQUEST_SHROOMDATA);

            }
        });

        final Intent elseAddShroomIntent = new Intent(ShroomerActivity.this, com.example.kocja.shroomer.addShroom.class);
        addShroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLite.select()
                        .from(markerLocatio.class)
                        .async()
                        .queryListResultCallback(new QueryTransaction.QueryResultListCallback<markerLocatio>() {
                            @Override
                            public void onListQueryResult(QueryTransaction transaction, @NonNull List<markerLocatio> tResult) {

                                boolean isDoneOnce = false;
                                allLocations = tResult;
                                for(markerLocatio marker : allLocations) {


                                    location.setLatitude(Double.parseDouble(marker.latitude));
                                    location.setLongitude(Double.parseDouble(marker.longtitude));

                                    float distanceTo = yourlocation.distanceTo(location);

                                    if(!isDoneOnce && distanceTo <= 3.00){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ShroomerActivity.this)
                                                .setTitle("Same plant?")
                                                .setMessage("One or more mushrooms have been found to be near your location. Is it on the same plant?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        elseAddShroomIntent.putExtra("Id",69);
                                                        startActivityForResult(elseAddShroomIntent, addShroomrequest);

                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        startActivityForResult(elseAddShroomIntent, addShroomrequest);
                                                    }
                                                });
                                        builder.show();
                                        isDoneOnce = true;
                                    }
                                    else{
                                        startActivityForResult(elseAddShroomIntent, addShroomrequest);
                                    }
                                }
                                if(allLocations.size() == 0){
                                    startActivityForResult(elseAddShroomIntent, addShroomrequest);
                                }

                            }
                        }).execute();




            }

        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == addShroomrequest && resultCode == RESULT_OK && data != null) {

            indexOfType = data.getIntExtra("indexOfImage", -1);
            Bitmap bitmap1;

            switch (indexOfType) {
                case 0:
                    bitmap1 = BitmapFactory.decodeResource(resource, R.drawable.icons8_mushroom_96);
                    break;
                case 1:
                    bitmap1 = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_down);
                    break;
                case 2:
                    bitmap1 = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_upward);
                    break;
                case 3:
                    bitmap1 = BitmapFactory.decodeResource(resource, R.mipmap.ic_arrow_forward);

                    break;
                default:
                    bitmap1 = BitmapFactory.decodeResource(resource, R.drawable.ic_delete_black_24dp);
                    break;
            }
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap1, 72, 72, false));
            shroomMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(yourlocation.getLatitude(), yourlocation.getLongitude())).title("Mushroom found").icon(descriptor));
        }
        else if (requestCode == REQUEST_SHROOMDATA && resultCode == RESULT_OK && data != null) {

            LatLng markerlocation = publicMarker.getPosition();
            String latitude = Double.toString(markerlocation.latitude);
            String longtitude = Double.toString(markerlocation.longitude);
            SQLite.select()
                    .from(markerLocatio.class)
                    .where(markerLocatio_Table.latitude.is(latitude))
                    .and(markerLocatio_Table.longtitude.is(longtitude))
                    .async()
                    .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<markerLocatio>() {
                        @Override
                        public void onSingleQueryResult(QueryTransaction transaction, @Nullable markerLocatio markerLocation) {
                            if (markerLocation != null) {
                                markerLocation.delete();
                            }
                        }
                    }).execute();
            publicMarker.remove();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("You have not accepted the permissions needed to run this application, the application will now exit")
                    .setTitle("Warning");
            builder.show();
            finish();
        }

    }
}
