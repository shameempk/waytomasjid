package in.qbics.way2masjid;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class Splash extends Activity implements LocationListener {
    public static double mLatitude=0.00, mLongitude=0.00;

    private final String[] timeArray = new String[6];

    private String plainJSON = null;

    private Location location;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        int gPlaystatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(gPlaystatus!= ConnectionResult.SUCCESS)
        {
            gPlayServicesNotFoundAlert();
        }


        // Getting LocationManager object from System Service LOCATION_SERVICE
        this.getLocation();

        // SHOW THE EXIT DIALOG
        if (!isNetworkAvailable(Splash.this)) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);
            alertDialogBuilder.setMessage("No connectivity found. Please check your network settings.")
                    .setCancelable(false).setNegativeButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    }
            );
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        // STARTS PREFETCH ASYNC
        PreFetch preFetch = new PreFetch();
        preFetch.execute();

    }


    private class PreFetch extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... voids) {


            //PLACES JSON URL
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=").append(mLatitude).append(",").append(mLongitude);
            sb.append("&rankby=distance");
            sb.append("&types=mosque");
            sb.append("&sensor=true");
            sb.append("&key=Your-Places-api-key");


            Calculator calc = new Calculator();

            String urlGMTOffset = "https://maps.googleapis.com/maps/api/timezone/json?location=";
            String finalUrl1 = urlGMTOffset + mLatitude + "," + mLongitude + "&timestamp=" + calc.getTimeStamp();


            HandleJSON obj1 = new HandleJSON(finalUrl1);
            try {
                obj1.fetchJSON("offsetCalculation");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (obj1.parsingComplete) ;
            String offset = obj1.getGMTOffset();


            // GET PRAYER TIMES

            String urlPrayerTime = "http://api.xhanch.com/islamic-get-prayer-time.php?lng=";
            String finalUrl = urlPrayerTime + mLongitude + "&lat=" + mLatitude + "&yy=" + calc.getnYear() + "&mm=" + calc.getnMonth() + "&gmt=" + offset + "&m=json";
            HandleJSON obj = new HandleJSON(finalUrl);
            try {
                obj.fetchJSON("prayerTimeCalculation");
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (obj.parsingComplete) ;

            try {
                plainJSON = downloadUrl(sb.toString());
                timeArray[0] = obj.getFajar();
                timeArray[1] = obj.getSunrise();
                timeArray[2] = obj.getZuhr();
                timeArray[3] = obj.getAsr();
                timeArray[4] = obj.getMaghrib();
                timeArray[5] = obj.getIsha();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return plainJSON;
        }


        @Override
        protected void onPostExecute(String plainJSON) {

            super.onPostExecute(plainJSON);

            // STARTS MYACTIVITY
            Intent openStartingPoint = new Intent("android.intent.action.MyActivity");
            openStartingPoint.putExtra("mLatitude",mLatitude);
            openStartingPoint.putExtra("mLongitude",mLongitude);
            openStartingPoint.putExtra("timeArray",timeArray);
            openStartingPoint.putExtra("plainJSON",plainJSON);
            startActivity(openStartingPoint);

        }

    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);


            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    //GPS DISABLED MESSAGE
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Location Settings are turned off. Please turn on.")
                .setCancelable(false)
                .setPositiveButton("Goto Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        }
                );
        alertDialogBuilder.setNegativeButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                }
        );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //CHECKS THE INTERNET CONNECTIVITY
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {

        getLocation();
    }

    // THE POSITION ACQUIRING ALGORITHM
    Location getLocation() {
        try {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Clear the gps cache
            locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", null);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                showGPSDisabledAlertToUser();
            } else {
                boolean canGetLocation = true;
                if (!isGPSEnabled)
                    Toast.makeText(getApplication(), "Please turn on GPS for accuracy !", Toast.LENGTH_LONG).show();

                if (isGPSEnabled) {

                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                mLatitude = location.getLatitude();
                                mLongitude = location.getLongitude();
                            }
                        }
                    }
                }

                // if location values are null or location values are older then 15 minutes ; 1000*60*5
                if ((mLatitude == 0.00) || ((System.currentTimeMillis() - location.getTime()) > 300000)) {

                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                mLatitude = location.getLatitude();
                                mLongitude = location.getLongitude();
                            }
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "No location service found !", Toast.LENGTH_LONG).show();
        }

        return location;
    }

    // ERROR MESSAGE IF GOOGLE PLAY SERVICES ARE NOT FOUND
    public void gPlayServicesNotFoundAlert()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);
        alertDialogBuilder.setMessage("Sorry ! Please install/update Google Play Services from PlayStore .")
                .setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                    }
                }
        ).setNegativeButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                }
        );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}