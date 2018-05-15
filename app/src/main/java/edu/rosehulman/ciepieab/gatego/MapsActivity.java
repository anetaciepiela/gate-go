package edu.rosehulman.ciepieab.gatego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.log1p;
import static java.lang.Math.toIntExact;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LoginFragment.OnLoginListener, AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    private List<Route> mRoutes;
    private String mUserId;
    private boolean isLoggedIn;
    private LatLng currStartGateCoord;
    private LatLng currDestGateCoord;
    private Route currentRoute;
    private Spinner mRouteSpinner;
    private TextView mSpinnerTitleTextView;
    private FloatingActionButton mFab;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mGateRef;
    private DatabaseReference mAirportRef;
    private DatabaseReference mRouteRef;
    private DatabaseReference mUserRoutesRef;
    private LoginFragment mLoginFragment;
    private GoogleApiClient mGoogleApiClient;
    private OnCompleteListener mOnCompleteListener;
    private static final int RC_GOOGLE_LOGIN = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAirportDialog();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        initializeListeners();
        setupGoogleSignIn();

        if (savedInstanceState == null) {
            switchToLoginFragment();
        }
    }

    private void initializeListeners() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    mUserId = user.getUid();
                    isLoggedIn = true;
                    switchToMapsFragment();
                    initializeAppInfo();
                }
                else {
                    switchToLoginFragment();
                }
            }
        };
    }
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void logout() {
        mAuth.signOut();
    }

    private void initializeAppInfo() {
        mAirportRef = FirebaseDatabase.getInstance().getReference().child("airport");
        mAirportRef.keepSynced(true);

        mGateRef = FirebaseDatabase.getInstance().getReference().child("gate");
        mGateRef.keepSynced(true);

        mRouteRef = FirebaseDatabase.getInstance().getReference().child("route");
        mRouteRef.keepSynced(true);

        mRoutes = new ArrayList<Route>();

        mUserRoutesRef = FirebaseDatabase.getInstance().getReference().child("user").child(mUserId).child("routes");
        mUserRoutesRef.addChildEventListener(new UserRouteChildEventListener());
        mUserRoutesRef.keepSynced(true);


        mRouteSpinner = findViewById(R.id.route_spinner);
        mSpinnerTitleTextView = findViewById(R.id.spinner_title_textview);
    }

    public void showAddAirportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_airport, null, false);
        final AutoCompleteTextView airportNameEditText = view.findViewById(R.id.airport_name_editText);
        airportNameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mAirportRef.addValueEventListener(new AirportValueEventListener(airportNameEditText));
        builder.setTitle("Add Airport");
        builder.setPositiveButton(getResources().getString(R.string.next), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String airportKey = airportNameEditText.getText().toString();
                showAddRouteDialog(airportKey);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.create().show();
    }

    private void showAddRouteDialog(final String airportKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_route, null, false);
        //Capture widgets
        final EditText routeNameEditText = view.findViewById(R.id.route_name_editText);
        final AutoCompleteTextView startGateEditText = view.findViewById(R.id.start_gate_editText);
        startGateEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final AutoCompleteTextView destGateNameEditText = view.findViewById(R.id.dest_gate_editText);
        destGateNameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        Query gatesByAirport = mGateRef.orderByChild("airportKey").equalTo(airportKey);
        gatesByAirport.addListenerForSingleValueEvent(new GateValueEventListener(startGateEditText, destGateNameEditText));

        builder.setTitle("Enter Your Gate Information");
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String startGateLabel = startGateEditText.getText().toString();
                String destGateLabel = destGateNameEditText.getText().toString();
                String routeName = routeNameEditText.getText().toString();

                String routeID = "R_" + airportKey + "_" + startGateLabel + "_" + destGateLabel;
                String startGateID = airportKey + "_" + startGateLabel;
                String destGateID = airportKey + "_" + destGateLabel;

                mRouteRef.child(routeID).addValueEventListener(new RouteValueEventListener(routeName, routeID, startGateID, destGateID));

                Snackbar snackbar = Snackbar.make(findViewById(R.id.map_view), getResources().getString(R.string.snackbar), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Start Navigation", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateView(mRoutes.size() - 1);
                    }
                });
                snackbar.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == RC_GOOGLE_LOGIN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if(result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(this, mOnCompleteListener);
                }
                else {
                    Log.d("LOGIN ERROR", "Google authentication failed");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        if (id == R.id.action_delete_routes) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select which route to delete");
        builder.setItems(getRouteNames(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Route route = mRoutes.get(which);
                mUserRoutesRef.child(route.getKey()).removeValue();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private String[] getRouteNames() {
        String[] routeNames = new String[mRoutes.size()];
        for (int i = 0; i < mRoutes.size(); i++) {
            routeNames[i] = mRoutes.get(i).toString();
        }
        return routeNames;
    }

    private void switchToMapsFragment() {
        if(isLoggedIn) {
            mFab.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.VISIBLE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(mLoginFragment);
            ft.commit();
        }
    }

    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mLoginFragment = new LoginFragment();
        ft.replace(R.id.map_view, mLoginFragment, "Login");
        mFab.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
        ft.commit();
    }

    private void updateView(int pos) {
        mMap.clear();
        currentRoute = mRoutes.get(pos);
        mSpinnerTitleTextView.setText(currentRoute.toString());
        DatabaseReference startGateByRoute = mGateRef.child(currentRoute.getStartGateID());
        startGateByRoute.keepSynced(true);
        startGateByRoute.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Gate startGate = dataSnapshot.getValue(Gate.class);
                currStartGateCoord = new LatLng(startGate.getLatitude(), startGate.getLongitude());
                mMap.addMarker(new MarkerOptions().position(currStartGateCoord).title("Start"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currStartGateCoord, 20));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        DatabaseReference destGateByRoute = mGateRef.child(currentRoute.getDestGateID());
        destGateByRoute.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Gate destGate = dataSnapshot.getValue(Gate.class);
                currDestGateCoord = new LatLng(destGate.getLatitude(), destGate.getLongitude());
                mMap.addMarker(new MarkerOptions().position(currDestGateCoord).title("Destination"));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if (currentRoute.getLatPolyPoint() != null && currentRoute.getLatPolyPoint() != null) {
            PolylineOptions polylineOptions = new PolylineOptions();
            for (int i = 0; i < currentRoute.getLatPolyPoint().size(); i++) {
                LatLng currCoord = new LatLng(currentRoute.getLatPolyPoint().get(i), currentRoute.getLongPolyPoint().get(i));
                polylineOptions.add(currCoord);
            }
            mMap.addPolyline(polylineOptions);
        }
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ERROR", "Connection to Google failed");
    }

    @Override
    public void onGoogleLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_GOOGLE_LOGIN);
    }

    private class AirportValueEventListener implements ValueEventListener {
        AutoCompleteTextView airportAutoComplete;

        public AirportValueEventListener(AutoCompleteTextView airportNameEditText) {
            airportAutoComplete = airportNameEditText;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final List<String> mAirports = new ArrayList<String>();

            for (DataSnapshot airportSnapshot : dataSnapshot.getChildren()) {
                String airportAbbr = airportSnapshot.child("abbreviation").getValue().toString();
                mAirports.add(airportAbbr);
            }
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, mAirports);
            airportAutoComplete.setAdapter(autoAdapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class GateValueEventListener implements ValueEventListener {
        private AutoCompleteTextView startGateAutoComplete;
        private AutoCompleteTextView destGateAutoComplete;

        public GateValueEventListener(AutoCompleteTextView startGateEditText, AutoCompleteTextView destGateNameEditText) {
            startGateAutoComplete = startGateEditText;
            destGateAutoComplete = destGateNameEditText;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final List<String> mGates = new ArrayList<String>();

            for (DataSnapshot gateSnapshot : dataSnapshot.getChildren()) {

                String gateLabel = gateSnapshot.child("label").getValue().toString();
                mGates.add(gateLabel);
            }
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, mGates);
            startGateAutoComplete.setAdapter(autoAdapter);
            destGateAutoComplete.setAdapter(autoAdapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RouteValueEventListener implements ValueEventListener {
        private String routeName;
        private String routeID;
        private String startGateID;
        private String destGateID;

        public RouteValueEventListener(String routeName, String routeID, String startGateID, String destGateID) {
            this.routeName = routeName;
            this.routeID = routeID;
            this.startGateID = startGateID;
            this.destGateID = destGateID;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                Route route = dataSnapshot.getValue(Route.class);
                route.setRouteID(dataSnapshot.getKey());
                String userRouteID = mUserRoutesRef.push().getKey();
                mUserRoutesRef.child(userRouteID).setValue(route);
            }
            else {
                Route route = new Route(routeName, routeID, startGateID, destGateID, new ArrayList<Double>(), new ArrayList<Double>());
                String userRouteID = mUserRoutesRef.push().getKey();
                mUserRoutesRef.child(userRouteID).setValue(route);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class UserRouteChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Route route = dataSnapshot.getValue(Route.class);
            route.setKey(dataSnapshot.getKey());
            mRoutes.add(route);
            setAdapter();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String routeKeyToDelete = dataSnapshot.getKey();
            for(Route r : mRoutes) {
                if(routeKeyToDelete.equals(r.getKey())) {
                    mRoutes.remove(r);
                    if (mRoutes.isEmpty()) {
                        updateToDefault();
                    }
                    else {
                        updateView(0);
                    }
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private void setAdapter() {
        ArrayAdapter<Route> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mRoutes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRouteSpinner.setAdapter(spinnerAdapter);
        mRouteSpinner.setOnItemSelectedListener(this);
    }

    private void updateToDefault() {
        mMap.clear();
        mSpinnerTitleTextView.setText(R.string.my_routes);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 1));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateView(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
