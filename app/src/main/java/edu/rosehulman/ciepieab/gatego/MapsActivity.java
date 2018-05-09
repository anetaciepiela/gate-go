package edu.rosehulman.ciepieab.gatego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
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

import org.joda.time.DateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.log1p;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NearbyFragment.OnSwipeListener, GoogleApiClient.OnConnectionFailedListener, LoginFragment.OnLoginListener {

    private GoogleMap mMap;
    private EditText addAirportEditText;
    private Calendar mCalendar;
    private DateFormat mTodayTomorrowFormatter;
    private DateFormat mOtherDayFormatter;
    private ImageView mMenuButton;
    private List<Route> mRoutes;
    private String mUserId;
    private boolean isLoggedIn;

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
    private Spinner mRouteSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        initializeListeners();
        setupGoogleSignIn();

        if (savedInstanceState == null) {
            switchToLoginFragment();
        }
    }

    private void logout() {
        mAuth.signOut();
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

    private void initializeListeners() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d("TAG", "USER LOGIN");
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

    private void initializeAppInfo() {
        mAirportRef = FirebaseDatabase.getInstance().getReference().child("airport");
        mAirportRef.keepSynced(true);

        mGateRef = FirebaseDatabase.getInstance().getReference().child("gate");
        mGateRef.keepSynced(true);

        mRouteRef = FirebaseDatabase.getInstance().getReference().child("route");
        mRouteRef.keepSynced(true);

        mUserRoutesRef = FirebaseDatabase.getInstance().getReference().child("user").child(mUserId).child("routes");
        mUserRoutesRef.keepSynced(true);

        mRoutes = new ArrayList<Route>();
        mCalendar = Calendar.getInstance();
        mTodayTomorrowFormatter = new SimpleDateFormat("MM/dd/yy");
        mOtherDayFormatter = new SimpleDateFormat("E-MM/dd/yy");

        mRouteSpinner = findViewById(R.id.route_spinner);
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, mRoutes);


        addAirportEditText = findViewById(R.id.add_airport_editText);
        mMenuButton = findViewById(R.id.nearby_frag_butt);

        addAirportEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (mAirports.size() == 0) {
                showAddAirportDialog();
                //}
            }
        });

        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showNearbyFragment();
                logout();
            }
        });
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

    private void switchToMapsFragment() {
        if(isLoggedIn) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(mLoginFragment);
            ft.commit();
        }
//        else {
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.commit();
//        }
        Log.d("TAG", "IN SWITCH TO MAPS FRAG");
    }

    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mLoginFragment = new LoginFragment();
        ft.replace(R.id.map_view, mLoginFragment, "Login");
        ft.commit();
    }

    private void showNearbyFragment() {
        Fragment nearbyFrag = new NearbyFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.map_view, nearbyFrag);
        ft.commit();
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

    @SuppressLint("ResourceType")
    private void showAddRouteDialog(final String airportKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_route, null, false);
        //TODO DATE TYPE
        //Capture widgets
        final AutoCompleteTextView startGateEditText = view.findViewById(R.id.start_gate_editText);
        startGateEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final AutoCompleteTextView destGateNameEditText = view.findViewById(R.id.dest_gate_editText);
        destGateNameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        Query gatesByAirport = mGateRef.orderByChild("airportKey").equalTo(airportKey);
        gatesByAirport.addValueEventListener(new GateValueEventListener(startGateEditText, destGateNameEditText));

        final LinearLayout dateLayout = view.findViewById(R.id.date_layout);
        final TextView dateTextView = view.findViewById(R.id.date_text_view);
        final String todaysDate = mTodayTomorrowFormatter.format(mCalendar.getTime());
        final String defaultDate = getResources().getQuantityString(R.plurals.date_format, 1, todaysDate);
        dateTextView.setText(defaultDate);
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarDialog(todaysDate, dateTextView);
                //dateTextView.setText(showCalendarDialog(todaysDate));
                //String selectedFormattedDate = showCalendarDialog(todaysDate);
                //dateTextView.setText(selectedFormattedDate);
            }
        });
        builder.setTitle("Enter Your Gate Information");
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: get all input data, add route (model object) to mAirports
                String startGateLabel = startGateEditText.getText().toString();
                String destGateLabel = destGateNameEditText.getText().toString();
                String enteredDate = dateTextView.getText().toString();

                String routeID = "R_" + airportKey + "_" + startGateLabel + "_" + destGateLabel;
                String startGateID = airportKey + "_" + startGateLabel;
                String destGateID = airportKey + "_" + destGateLabel;

                Log.d("ROUTEID", routeID);
//                Query getRoute = mRouteRef.equalTo(routeID);
                mRouteRef.child(routeID).addValueEventListener(new RouteValueEventListener(routeID, enteredDate, startGateID, destGateID));
                //TODO:  add route to firebase under userID

                //TODO: REMOVE DUMMY VALUES
                //LatLng startCoord = new LatLng(39.7171641, -86.2974331);
                //LatLng destCoord = new LatLng(39.7150811, -86.2948602);

//                Route newRoute = new Route(enteredDate, startCoord, destCoord, airportKey);
//                mRoutes.add(newRoute);

//                drawRoute(newRoute);
//                zoomToRouteView(newRoute);

                Snackbar snackbar = Snackbar.make(findViewById(R.id.map_view), getResources().getString(R.string.route_time), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Start Navigation", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: start navigation!
                    }
                });
                snackbar.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.create().show();
    }

    private void zoomToRouteView(Route route) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //builder.include(route.getStartGate());
        //builder.include(route.getDestGate());
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        mMap.animateCamera(cu);
    }

    @NonNull
    private String getFormattedDate(String selectedDate, String todaysDate) {
        Log.d("Day", String.valueOf(Integer.parseInt(selectedDate.substring(3, 4))));
        int dateDifference = abs(Integer.parseInt(todaysDate.substring(3, 5)) - Integer.parseInt(selectedDate.substring(3, 5)));
        String selectedDateFormatted = "";
        if (dateDifference == 0) {
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 1, selectedDate);
        } else if (dateDifference == 1) {
            //TODO: FORMAT
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 2, selectedDate);
        } else {
            //TODO FORMAT
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 3, selectedDate);
        }
        return selectedDateFormatted;
    }

    private void showCalendarDialog(final String todaysDate, final TextView dateTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_calendar, null, false);
        final CalendarView calendarView = view.findViewById(R.id.calendar_view);
        final GregorianCalendar calendar = new GregorianCalendar();
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
            }
        });
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("Correct DATE", getFormattedDate(mTodayTomorrowFormatter.format(calendar.getTime()), todaysDate));
                dateTextView.setText(getFormattedDate(mTodayTomorrowFormatter.format(calendar.getTime()), todaysDate));
            }
        });

        builder.create().show();
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

        // Add a marker in Sydney and move the camera
        LatLng indyairport = new LatLng(39.7168593, -86.29559519999998);
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(indyairport).title("Marker at airport"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indyairport, 16));

//        mMap.setIndoorEnabled(true);


        //mMap.moveCamera(CameraUpdateFactory.newLatLng(indyairport));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo((float) indyairport.latitude));
        LatLng gateF9 = new LatLng(41.9736345, -87.9058268);
        LatLng gateF6 = new LatLng(41.9743681, -87.9062536);
        mMap.addMarker(new MarkerOptions().position(gateF9).title("Marker at Gate"));
        mMap.addMarker(new MarkerOptions().position(gateF6).title("Gate F6"));
        Polyline polyline = googleMap.addPolyline((new PolylineOptions())
                .add(gateF6, gateF9));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gateF9));
    }

//    private void drawRoute(Route route) {
//        DateTime now = new DateTime();
//        try {
//            DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
//                    .mode(TravelMode.WALKING)
//                    .origin(Double.toString(route.getStartGate().latitude) + "," + Double.toString(route.getStartGate().longitude))
//                    .destination(Double.toString(route.getDestGate().latitude) + "," + Double.toString(route.getDestGate().longitude))
//                    .departureTime(now)
//                    .await();
//            Log.d("TAG", "DirectionsResult:  " + result);
//            addMarkersToMap(result, mMap);
//            addPolyline(result, mMap);
//        } catch (ApiException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].startLocation.lat, results.routes[0].legs[0].startLocation.lng)).title(results.routes[0].legs[0].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].endLocation.lat, results.routes[0].legs[0].endLocation.lng)).title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
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
    public void onSwipe() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fm.popBackStackImmediate();
        ft.commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ERROR", "Connection to Google failed");
    }

    @Override
    public void onLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mOnCompleteListener);
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
            //AutoCompleteTextView airportAbbrText = findViewById(R.id.airport_name_editText);
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
            //AutoCompleteTextView airportAbbrText = findViewById(R.id.airport_name_editText);
            startGateAutoComplete.setAdapter(autoAdapter);
            destGateAutoComplete.setAdapter(autoAdapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RouteValueEventListener implements ValueEventListener {

        private String routeID;
        private String enteredDate;
        private String startGateID;
        private String destGateID;


        public RouteValueEventListener(String routeID, String enteredDate, String startGateID, String destGateID) {
            this.routeID = routeID;
            this.enteredDate = enteredDate;
            this.startGateID = startGateID;
            this.destGateID = destGateID;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //TODO: we're going to want to get a route from "route" and then create a route object.
            //TODO: then push to userRoutesRef
            Log.d("LOOK AT THIS", dataSnapshot.toString());
            if (dataSnapshot != null) {
                Route route = dataSnapshot.getValue(Route.class);
                route.setRouteID(dataSnapshot.getKey());
                route.setDate(enteredDate);
                mRoutes.add(0, route);
                String userRouteID = mUserRoutesRef.push().getKey();
                mUserRoutesRef.child(userRouteID).setValue(route);
            }
            else {
                Route route = new Route(routeID, enteredDate, startGateID, destGateID, new ArrayList<Double>(), new ArrayList<Double>());
                mRoutes.add(0, route);
                String userRouteID = mUserRoutesRef.push().getKey();
                mUserRoutesRef.child(userRouteID).setValue(route);
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
