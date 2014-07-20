package in.qbics.way2masjid;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

class HandleJSON {
    private static String fajar = "fajr";
    private static String sunrise = "sunrise";
    private static String zuhr = "zuhr";
    private static String asr = "asr";
    private static String maghrib = "maghrib";
    private static String isha = "isha";

    private double gmtOffset;

    private String urlString = null;

    public volatile boolean parsingComplete = true;
    public HandleJSON(String url){
        this.urlString = url;
    }
    public String getFajar(){
        return fajar;
    }
    public String getSunrise(){
        return sunrise;
    }
    public String getZuhr(){
        return zuhr;
    }
    public String getAsr(){
        return asr;

    }
    public String getMaghrib(){
        return maghrib;

    }
    public String getIsha(){
        return isha;

    }
    public String getGMTOffset()
    {
        return Double.toString(gmtOffset);
    }
    @SuppressLint("NewApi")

    // PARSE JSON TO GET PRAYER TIME:
    void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);
            JSONObject date  = reader.getJSONObject("1");
            fajar = date.getString("fajr");
            sunrise = date.getString("sunrise");
            zuhr = date.getString("zuhr");
            asr = date.getString("asr");
            maghrib = date.getString("maghrib");
            isha = date.getString("isha");
            parsingComplete = false;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    //PARSE JSON TO GET GMT OFFSET

    void readAndParseJSONForGMT(String in) {
        try {
            Calendar calendar=Calendar.getInstance();
            int nDate=calendar.get(Calendar.DATE);
            JSONObject reader = new JSONObject(in);
            String rawOffset = reader.getString("rawOffset");
            double rawoffset = Double.parseDouble(rawOffset);
            gmtOffset= rawoffset/3600.0;
            parsingComplete = false;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void fetchJSON(final String calcWhat) throws IOException{
                   String data="";
                   InputStream stream = null;
                   HttpURLConnection urlConnection = null;

                try {
                     URL url = new URL(urlString);

                    // Creating an http connection to communicate with url

                    urlConnection = (HttpURLConnection)url.openConnection();

                    // Connecting to url
                    urlConnection.connect();
                    stream = urlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer sb  = new StringBuffer();
                    String line = "";
                    while( ( line = br.readLine())  != null){
                        sb.append(line);
                    }
                    data = sb.toString();
                    br.close();
                    if (calcWhat=="offsetCalculation")
                    readAndParseJSONForGMT(data);
                    if (calcWhat=="prayerTimeCalculation")
                    readAndParseJSON(data);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();

                }
                finally {
                    urlConnection.disconnect();
                }
    }

}