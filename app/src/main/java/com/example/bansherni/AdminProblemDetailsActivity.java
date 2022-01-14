package com.example.bansherni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.api.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminProblemDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    ImageView mWhatsapp;
    ImageView mTypeIcon;
    TextView mUserName;
    TextView mProblemDetails;
    TextView mStatusCode;
    TextView mPrice;
    TextView mPhone;
    CardView deleteBtn;

    //from sender intent
    int id, userId, type, status;
    String userName, details, phone;
    double lat, lon, price;

    public static String TAG = "mapFragment";
    private static final float NORMAL_ZOOM = 15f;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_problem_details);

        bindViews();


        Intent sender = getIntent();
        if(sender != null){
            id = sender.getIntExtra("problem_id", -1);
            userId = sender.getIntExtra("user_id", -1);
            type = sender.getIntExtra("type", -1);
            status = sender.getIntExtra("status", -1);
            userName = sender.getStringExtra("user_name");
            phone = sender.getStringExtra("phone");
            details = sender.getStringExtra("details");
            lat = sender.getDoubleExtra("lat", -1);
            lon = sender.getDoubleExtra("lon", -1);
            price = sender.getDoubleExtra("price", -1);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true)
                .mapType(GoogleMap.MAP_TYPE_NORMAL);

        mWhatsapp.setOnClickListener(v -> {
            String contact = "+966 " + phone; // use country code with your phone number
            String url = "https://api.whatsapp.com/send?phone=" + contact;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        deleteBtn.setOnClickListener(v -> deleteUserRequest());

        fillData();

    }

    private void bindViews() {
        mUserName = findViewById(R.id.user_name);
        mPrice = findViewById(R.id.price);
        mStatusCode = findViewById(R.id.status_code);
        mTypeIcon = findViewById(R.id.type_icon);
        mPhone = findViewById(R.id.phone);
        mProblemDetails = findViewById(R.id.problem_details);
        mWhatsapp = findViewById(R.id.whatsapp);
        deleteBtn = findViewById(R.id.delete_Btn);
    }

    private void fillData(){
        mUserName.setText(userName);
        mProblemDetails.setText(details);
        mPhone.setText(String.valueOf(phone));
        mPrice.setText(price + getResources().getString(R.string.price_unit));

        switch (type){
            case Constants.TYPE_BATTERY :
                mTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_car_battery));
                break;
            case Constants.TYPE_ENGINE :
                mTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_engine));
                break;
            case Constants.TYPE_FUEL :
                mTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_fuel));
                break;
            case Constants.TYPE_TIRES :
                mTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_tire));
                break;
        }

        switch (status){
            case Constants.REQUEST_STATUS_NEW :
                mStatusCode.setTextColor(getResources().getColor(R.color.status_new));
                mStatusCode.setText(getResources().getString(R.string.status_new));
                break;
            case Constants.REQUEST_STATUS_PROCESSING:
                mStatusCode.setTextColor(getResources().getColor(R.color.status_processing));
                mStatusCode.setText(getResources().getString(R.string.status_processing));
                break;
            case Constants.REQUEST_STATUS_COMPLETE :
                mStatusCode.setTextColor(getResources().getColor(R.color.status_completed));
                mStatusCode.setText(getResources().getString(R.string.status_completed));
                break;
            case Constants.REQUEST_STATUS_REJECTED :
                mStatusCode.setTextColor(getResources().getColor(R.color.status_rejected));
                mStatusCode.setText(getResources().getString(R.string.status_rejected));
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker for the car location and move the camera
        LatLng myLocation = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title(getResources().getString(R.string.car_location)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, NORMAL_ZOOM));
    }

    void deleteUserRequest(){
        final ProgressDialog pDialog = new ProgressDialog(AdminProblemDetailsActivity.this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post("api/requests/delete.php")
                .addBodyParameter("id", String.valueOf(id))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                Toast.makeText(AdminProblemDetailsActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                finish();

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(AdminProblemDetailsActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        Toast.makeText(AdminProblemDetailsActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}