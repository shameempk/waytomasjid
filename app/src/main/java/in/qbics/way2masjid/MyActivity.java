package in.qbics.way2masjid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MyActivity extends FragmentActivity {
    // Infowindow and elements
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet; // Distance

    // Dock buttons
    private Button buttonTime;
    private GoogleMap map;

    // Masjid name and distance array
    private String masjidName[] ;
    private String masjidDistance[] ;
    // Count limit of makers
    private int countLimt = 0;
    private int markerCount=0;

    //Incoming values from Splash
    private double saved_mLatitude;
    private double saved_mLongitude;
    private String plainJSON;
    private String[] timeArray = new String[6];


    // Position array for markers
    public static LatLng[] masjidPos;

    // Common popup window elemet
    private PopupWindow popupWindow=null;

    // Infowwindow button listener
    private OnInfoWindowElemTouchListener infoButtonListenerNav;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);


        // Getting values from splash , as intent
        saved_mLatitude=getIntent().getDoubleExtra("mLatitude",0.00);
        saved_mLongitude=getIntent().getDoubleExtra("mLongitude",0.00);
        if(getIntent().getExtras()!=null) {
            plainJSON = getIntent().getStringExtra("plainJSON");
            timeArray = getIntent().getStringArrayExtra("timeArray");
        }
        else{
            Toast.makeText(this,"Please restart the application",Toast.LENGTH_LONG).show();
        }

        // Map and wrapper initialisation
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        map = mapFragment.getMap();


        map.setMyLocationEnabled(true); // Enabling my location button
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                zoomToMyLocation();
                return true;
            }
        });


// DOCK BUTTONS ONCLICKS

        // Dock home button onclick
        Button buttonHome = (Button) findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setBoundAndZoom();
            }
        });

        // Dock time button onclick
        buttonTime = (Button) findViewById(R.id.buttonTime);
        buttonTime.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popup, null);
                // Inside items initialisation
                TextView fajarTime = (TextView) popupView.findViewById(R.id.fajarTime);
                TextView sunriseTime = (TextView) popupView.findViewById(R.id.sunriseTime);
                TextView luharTime = (TextView) popupView.findViewById(R.id.luharTime);
                TextView asarTime = (TextView) popupView.findViewById(R.id.asarTime);
                TextView maghribTime = (TextView) popupView.findViewById(R.id.magribTime);
                TextView ishaTime = (TextView) popupView.findViewById(R.id.ishaTime);

                TableRow fajarRaw = (TableRow) popupView.findViewById(R.id.fajarRaw);
                TableRow luharRaw = (TableRow) popupView.findViewById(R.id.luharRaw);
                TableRow asarRaw = (TableRow) popupView.findViewById(R.id.asarRaw);
                TableRow maghribRaw = (TableRow) popupView.findViewById(R.id.maghribRaw);
                TableRow ishaRaw = (TableRow) popupView.findViewById(R.id.ishaRaw);

                popupWindow = new PopupWindow(
                        popupView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 51);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);

                // Setting the time values
                fajarTime.setText(timeArray[0]);
                sunriseTime.setText(timeArray[1]);
                luharTime.setText(timeArray[2]);
                asarTime.setText(timeArray[3]);
                maghribTime.setText(timeArray[4]);
                ishaTime.setText(timeArray[5]);

                //HIGHLIGHT NEXT PRAYER TIME
                try {
                    double timeList[] = new double[7];


                    timeList[0] = 0.1;
                    timeList[1] = parseToTimeString(timeArray[0]);
                    timeList[2] = parseToTimeString(timeArray[2]);
                    timeList[3] = parseToTimeString(timeArray[3]);
                    timeList[4] = parseToTimeString(timeArray[4]);
                    timeList[5] = parseToTimeString(timeArray[5]);

                    timeList[6] = 23.59;

//                    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Date date = new Date();

                    double cTime = Double.parseDouble(date.getHours() + "." + date.getMinutes());

                    int i;
                    for (i = 0; i < 5; i++) {
                        if (cTime > timeList[i] && cTime <= timeList[i + 1])
                            break;
                    }
                    switch (i + 1) {
                        case 1:
                            fajarRaw.setBackgroundColor(Color.rgb(178, 102, 255));
                            break;
                        case 2:
                            luharRaw.setBackgroundColor(Color.rgb(178, 102, 255));
                            break;
                        case 3:
                            asarRaw.setBackgroundColor(Color.rgb(178, 102, 255));
                            break;
                        case 4:
                            maghribRaw.setBackgroundColor(Color.rgb(178, 102, 255));
                            break;
                        case 5:
                            ishaRaw.setBackgroundColor(Color.rgb(178, 102, 255));
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }

                // Inside popup close button onclick
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(buttonTime, 50, -30);
                popupWindow.setFocusable(true);

            }
        });

        // Dock list button onclick


        Button buttonMasjidList = (Button) findViewById(R.id.buttonMasjid);
        buttonMasjidList.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (countLimt != 0) {

                    LayoutInflater layoutInflater
                            = (LayoutInflater) getBaseContext()
                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView1 = layoutInflater.inflate(R.layout.masjid_popup, null);
                    ListView list;
                    CustomList adapter = new
                            CustomList(MyActivity.this, masjidName, masjidDistance);
                    list = (ListView) popupView1.findViewById(R.id.list);
                    list.setAdapter(adapter);
                    popupWindow = new PopupWindow(
                            popupView1,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    popupWindow.showAtLocation(popupView1, Gravity.BOTTOM, 0, 51);

                    // Inside close button onclick
                    Button btnDismiss = (Button) popupView1.findViewById(R.id.dismiss);
                    btnDismiss.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.showAsDropDown(buttonTime, 50, -30);


                    popupWindow.setFocusable(true);
                } else {
                    setBoundAndZoom();
                }

            }
        });


        // INITIALISING THE PARSERTASK -- ASYNC
        ParserTask p = new ParserTask();
        p.execute(plainJSON);

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(map, getPixelsFromDp(this, 39 + 20));

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.infowindow, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.masjidName);
        this.infoSnippet = (TextView) infoWindow.findViewById(R.id.masjidDistance);
        ImageButton infoButtonNav = (ImageButton) infoWindow.findViewById(R.id.masjidNav);


        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up

        this.infoButtonListenerNav = new OnInfoWindowElemTouchListener(infoButtonNav,
                getResources().getDrawable(R.drawable.nav_press),
                getResources().getDrawable(R.drawable.nav)) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                float[] results = new float[5];
                String navUrl;
                double destLat = marker.getPosition().latitude;
                double destLng = marker.getPosition().longitude;
                Location.distanceBetween(saved_mLatitude, saved_mLongitude, destLat, destLng, results);
                if (results[0] > 1000) {
                    navUrl = "google.navigation:ll=" + destLat + "," + destLng;
                } else {
                    navUrl = "google.navigation:ll=" + destLat + "," + destLng + "&mode=w";
                }

                startNav(navUrl);

            }
        };

        infoButtonNav.setOnTouchListener(infoButtonListenerNav);


        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText("Distance : " + marker.getSnippet());
                infoButtonListenerNav.setMarker(marker);
//                infoButtonListenerTime.setMarker(marker);

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

    }


    // PARSERTASK STARTS
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {


        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);


            } catch (Exception e) {
               e.printStackTrace();
            }
            return places;
        }


        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            // Clears all the existing markers
            map.clear();

            String distString; // Distance string

            // Fixing the marker limit
            try {
                countLimt = list.size();
                if (list.size() > 20) {
                    countLimt = 20;
                }

                markerCount = list.size();
                if (list.size()>5)
                {
                    markerCount=5;
                }
                float[] results = new float[5];
                masjidPos = new LatLng[countLimt];
                masjidName= new String[countLimt];
                masjidDistance = new String[countLimt];

                for (int i = 0; i < countLimt; i++) {

                    // Creating a marker
                    MarkerOptions markerOptions = new MarkerOptions();

                    // Getting a place from the places list
                    HashMap<String, String> hmPlace = list.get(i);


                    // Getting latitude of the place
                    double lat = Double.parseDouble(hmPlace.get("lat"));

                    // Getting longitude of the place
                    double lng = Double.parseDouble(hmPlace.get("lng"));


                    //finding the distance between markers and mylocation
                    Location.distanceBetween(saved_mLatitude, saved_mLongitude, lat, lng, results);

                    // Formating the distance string in km and m
                    if (results[0] > 1000) {
                        BigDecimal bd = new BigDecimal(Float.toString(results[0]/1000));
                        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                        distString = bd + "km";

                    } else {
                        distString = Math.round(results[0]) + "m";
                    }
                    masjidDistance[i] = distString;

                    // Getting name
                    String name = hmPlace.get("place_name");
                    masjidName[i] = name;

                    // Getting the marker position
                    LatLng latLng = new LatLng(lat, lng);

                    // Setting the position for the marker
                    markerOptions.position(latLng);

                    // Setting the title for the marker.
                    //This will be displayed on taping the marker
                    markerOptions.title(name);

                    //Setting marker icon
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                    // Snippet for distance
                    markerOptions.snippet(distString);

                    // setting latlang array
                    masjidPos[i] = latLng;

                    if(i<markerCount)
                    // Placing a marker on position
                    {
                        map.addMarker(markerOptions);

                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            setBoundAndZoom();

        }

    }

    // FUNCTION TO SET ZOOM BOUND : BOUND INCLUDING ALL MARKER AND MY LOCATION
    void setBoundAndZoom() {
        if (countLimt != 0) {
            LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();
            for (int i = 0; i < markerCount; i++) {
                boundBuilder.include(masjidPos[i]);
            }

            boundBuilder.include(new LatLng(saved_mLatitude, saved_mLongitude));
            LatLngBounds zoomBound = boundBuilder.build();
            try {
                CameraUpdate cU = CameraUpdateFactory.newLatLngBounds(zoomBound, 25);
                map.animateCamera(cU);
            } catch (Exception e) {
                Toast.makeText(getApplication(), "Sorry ! something went wrong.", Toast.LENGTH_LONG).show();
            }
        } else {

        }
    }


    private static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    void zoomToMyLocation() {
        CameraUpdate myLocationUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(saved_mLatitude, saved_mLongitude), 17);
        map.animateCamera(myLocationUpdate);
    }

    double parseToTimeString(String timeString) {
        String parts[] = timeString.split(":");
        return Double.parseDouble(parts[0] + "." + parts[1]);
    }

    // to listen back button press
    @Override
    public void onBackPressed() {

        if (popupWindow!=null) {
                popupWindow.dismiss(); // dismiss popup window when back key is pressed
                popupWindow=null;
            } else {
                showExitDialog();
            }
    }

    @Override
    public void onPause(){
        super.onPause();
    }
    // ON RESUMING THE APP
    @Override
    public void onResume() {
        super.onResume();
    }

    // sHOW THE EXIT DIALOG
    void showExitDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyActivity.this);
        alertDialogBuilder.setMessage("Do you really want to exit ?")
                .setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                }
        ).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }
        );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    void startNav(String navUrl)
    {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(navUrl));

        startActivity(intent);
    }

}
