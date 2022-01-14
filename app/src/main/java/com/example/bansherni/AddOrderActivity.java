package com.example.bansherni;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.api.Constants;
import com.example.bansherni.model.User;
import com.example.bansherni.utils.GpsTracker;
import com.example.bansherni.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class AddOrderActivity extends AppCompatActivity {


    private static final int TIME_TO_START = 1000;
    Spinner mTypeChooser;
    TextInputEditText mDetailsEt;
    Button mRefreshLocationBtn;
    Button mSave;
    Button mCancel;

    double lat = 200, lon = 200;

    int userId;
    int selectedType = -1;

    GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        userId = SharedPrefManager.getInstance(this).getUserId();

        //getting device location
        gpsTracker = new GpsTracker(this);
        if (!gpsTracker.canGetLocation()) {
            gpsTracker.showSettingsAlert();
        }
        gpsTracker.getLocation();
        lat = gpsTracker.getLatitude();
        lon = gpsTracker.getLongitude();

        bindViews();

        setUpTypeChooser();

        mSave.setOnClickListener(v -> {
            if(selectedType == -1){
                Toast.makeText(this, getResources().getString(R.string.type_selection_message), Toast.LENGTH_SHORT).show();
            }else if(lat == 200 || lon == 200 || lat == 0.0 || lon == 0.0){
                Toast.makeText(this, getResources().getString(R.string.location_selection_message), Toast.LENGTH_SHORT).show();
            }else
                submitRequest();
        });

        mCancel.setOnClickListener(v -> {
            finish();
        });

        mRefreshLocationBtn.setOnClickListener(v -> {

            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage("Processing Please wait...");
            pDialog.show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    gpsTracker.getLocation();
                    lat = gpsTracker.getLatitude();
                    lon = gpsTracker.getLongitude();
                    pDialog.dismiss();
                }
            }, TIME_TO_START);
        });


    }

    private void submitRequest() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        int type = selectedType;
        int id = userId;
        String details = mDetailsEt.getText().toString();
        double lat1 = lat;
        double lon1 = lon;
        int status = Constants.REQUEST_STATUS_NEW;
        double price = Constants.STARTING_PRICE;

        AndroidNetworking.post("api/requests/add.php")
                .addBodyParameter("user_id", String.valueOf(id))
                .addBodyParameter("details", details)
                .addBodyParameter("lat", String.valueOf(lat1))
                .addBodyParameter("lng", String.valueOf(lon1))
                .addBodyParameter("type", String.valueOf(type))
                .addBodyParameter("status", String.valueOf(status))
                .addBodyParameter("price", String.valueOf(price))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mSave.setEnabled(true);

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                                finish();

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mSave.setEnabled(true);
                        Toast.makeText(AddOrderActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void bindViews() {
        mTypeChooser = findViewById(R.id.type_chooser);
        mDetailsEt = findViewById(R.id.details);
        mRefreshLocationBtn = findViewById(R.id.add_location_btn);
        mSave = findViewById(R.id.save);
        mCancel = findViewById(R.id.cancel);
    }

    private void setUpTypeChooser(){


        mTypeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(AddOrderActivity.this, getResources().getString(R.string.type_selection_message), Toast.LENGTH_SHORT).show();
            }
        });

    }




}