package com.example.android.ssshdbpractice;

import android.Manifest;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final int REQUEST_PERMISSION_CODE = 007;

    private final String TAG = "SsshApp";

    private GoogleMap mMap;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;

    private FloatingActionButton addFab, deleteFab;

    private Marker marker;

    private int flag = 0;

    private GoogleApiClient mClient;
    private Geofencing mGeofencing;

    private NoteViewModel noteViewModel;
    private LiveData<List<Note>> noteList;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        if (!checkPermissionFromDevice()) {
            requestPermission();
        }

        checkLocationEnabled();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        addFab = findViewById(R.id.add_fab);
        deleteFab = findViewById(R.id.delete_fab);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);


        drawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        setNavigationViewListner();


        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        mGeofencing = new Geofencing(this, mClient);
        //else mGeofencing.unRegisterAllGeofences();

        mGeofencing.registerAllGeofences();

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Please search a place to add a marker !!", Toast.LENGTH_LONG).show();
            }
        });

        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                a_builder.setMessage("Are you sure you want to delete your geofence !!")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                deleteMarker();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = a_builder.create();
                alert.setTitle("Delete ");
                alert.show();
            }
        });


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                marker.showInfoWindow();
                flag = 1;

                addFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createMarker(place.getLatLng(), place.getId());

                    }
                });


            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void createMarker(final LatLng latLng, String placeId) {
        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
        intent.putExtra(AddNoteActivity.EXTRA_LATITUDE, latLng.latitude);
        intent.putExtra(AddNoteActivity.EXTRA_LONGITUDE, latLng.longitude);
        intent.putExtra(AddNoteActivity.EXTRA_PLACE_ID, placeId);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng jaipur = new LatLng(26.9124, 75.7873);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jaipur, 12));

        googleMap.setPadding(0, 175, 9, 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        }


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                deleteFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                        a_builder.setMessage("Are you sure you want to delete your geofence !!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        marker.remove();
                                        flag = 0;

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                        AlertDialog alert = a_builder.create();
                        alert.setTitle("Delete Marker");
                        alert.show();

                    }
                });
                return false;
            }
        });


    }

    private void deleteMarker() {

        NoteViewModel noteViewModel;
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        noteViewModel.deleteAllNotes();


    }


    @Override
    public void onBackPressed() {

        if (flag == 0) {
            super.onBackPressed();
        } else if (flag == 1) {
            marker.remove();
            flag = 0;
        }

    }


    private void setNavigationViewListner() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.road_map: {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.terrain: {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            }

            case R.id.satellite: {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }

            case R.id.show_geofences_list:
                Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
                startActivity(intent);
                break;


        }

        if (flag == 1) {
            marker.remove();
            flag = 0;
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData();
        Log.i(TAG, "API Client Connection Successful!");
    }


    @Override
    public void onConnectionSuspended(int i) {
        mClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "API Client Connection Failed!");
    }


    public void refreshPlacesData() {

        final Note[] data = new Note[1];

        Log.e(TAG, "refresh places data");

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                noteList = noteViewModel.getAllNotes();
                if (noteList.getValue().size() == 0)
                    return;
                else
                    data[0] = noteList.getValue().get(0);
            }

        });


        if (data[0] == null) {
            Log.e(TAG, "returning from function");
            return;
        }

        List<String> guids = new ArrayList<String>();

        LatLng latLng = new LatLng(data[0].getLatitude(), data[0].getLongitude());
        guids.add(data[0].getPlaceId());

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(data[0].getRadius());
        circleOptions.strokeColor(Color.RED);
        circleOptions.fillColor(0x30ff0000);
        circleOptions.strokeWidth(3);
        mMap.addCircle(circleOptions);
        Log.e(TAG, "adding circle");

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                guids.toArray(new String[guids.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                Log.e(TAG, "______" + places.toString());
                mGeofencing.updateGeofencesList(places, data[0].getRadius(),
                        data[0].getTitle(), data[0].getDescription(), data[0].getMode());
                mGeofencing.registerAllGeofences();
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int location_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        return write_external_storage_result == PackageManager.PERMISSION_GRANTED
                && location_result == PackageManager.PERMISSION_GRANTED;
    }

    private void checkLocationEnabled(){

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            new AlertDialog.Builder(this)
                    .setTitle("Location is not enabled")  // GPS not found
                    .setMessage("Enable location ?") // Want to enable?
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

    }

}

