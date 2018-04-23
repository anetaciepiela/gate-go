package edu.rosehulman.ciepieab.gatego;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.lang.Math.abs;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NearbyFragment.OnSwipeListener{

    private GoogleMap mMap;
    private EditText addAirportEditText;
    private Calendar mCalendar;
    private DateFormat mTodayTomorrowFormatter;
    private DateFormat mOtherDayFormatter;
    private ImageView mMenuButton;

    private ArrayList<Airport> mAirports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        //mAirports = new ArrayList();
        mCalendar = Calendar.getInstance();
        mTodayTomorrowFormatter = new SimpleDateFormat("MM/dd/yy");
        mOtherDayFormatter = new SimpleDateFormat("E-MM/dd/yy");

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
                showNearbyFragment();
            }
        });

    }

    private void showNearbyFragment() {
        Fragment nearbyFrag = new NearbyFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.map_view, nearbyFrag);
        ft.commit();
    }

    @SuppressLint("ResourceType")
    private void showAddAirportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_airport, null, false);
        //TODO DATE TYPE
        //Capture widgets
        final EditText airportNameEditText = view.findViewById(R.id.airport_name_editText);
        final EditText startGateEditText = view.findViewById(R.id.start_gate_editText);
        final EditText destGateNameEditText = view.findViewById(R.id.dest_gate_editText);
        final LinearLayout dateLayout = view.findViewById(R.id.date_layout);
        final TextView dateTextView = view.findViewById(R.id.date_text_view);
        final String todaysDate = mTodayTomorrowFormatter.format(mCalendar.getTime());
        String defaultDate = getResources().getQuantityString(R.plurals.date_format, 1,todaysDate);
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
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: get all input data, add an airport (model object) to mAirports
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

    @NonNull
    private String getFormattedDate(String selectedDate, String todaysDate) {
        Log.d("Day", String.valueOf(Integer.parseInt(selectedDate.substring(3, 4))));
        int dateDifference = abs(Integer.parseInt(todaysDate.substring(3, 5)) - Integer.parseInt(selectedDate.substring(3, 5)));
        String selectedDateFormatted = "";
        if(dateDifference == 0) {
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 1,selectedDate);
        }
        else if(dateDifference == 1) {
            //TODO: FORMAT
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 2,selectedDate);
        }
        else {
            //TODO FORMAT
            selectedDateFormatted = getResources().getQuantityString(R.plurals.date_format, 3,selectedDate);
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onSwipe() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fm.popBackStackImmediate();
        ft.commit();
    }
}
