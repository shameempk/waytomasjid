package in.qbics.way2masjid;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

class CustomList extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] listMasjid;
    private final String[] listDistance;
    public CustomList(Activity context,
                      String[] listMasjid, String[] listDistance) {
        super(context, R.layout.list_single, listMasjid);
        this.context = context;
        this.listMasjid = listMasjid;
        this.listDistance = listDistance;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        //CREATING ADAPTED VIEW OF LIST IN POPUP
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);

        //INITIALIZING LIST SINGLE ROW ELEMENTS
        TextView listMasjid1 = (TextView) rowView.findViewById(R.id.listMasjid);
        TextView listDistance1 = (TextView) rowView.findViewById(R.id.listDistance);
        Button navBtnList = (Button) rowView.findViewById(R.id.BtnToClick);

        listMasjid1.setText(listMasjid[position]);
        listDistance1.setText(listDistance[position]);

        //ONCLICK LISTENER FOR BUTTON IN LIST VIEW
        navBtnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] results = new float[5];
                String navUrl;
               double cLat= MyActivity.masjidPos[position].latitude;
                double cLong= MyActivity.masjidPos[position].longitude;
                Location.distanceBetween(Splash.mLatitude, Splash.mLongitude, cLat, cLong, results);
                if (results[0]>1000)
                {
                    navUrl = "google.navigation:ll=" + cLat + "," + cLong;
                }
                else
                {
                    navUrl = "google.navigation:ll=" + cLat + "," + cLong+"&mode=w";
                }

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(navUrl));
               getContext().startActivity(intent);
            }
        });

        return rowView;
    }
}