package com.example.bansherni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.adapters.AdminProblemsAdapter;
import com.example.bansherni.adapters.UserProblemsAdapter;
import com.example.bansherni.api.Constants;
import com.example.bansherni.model.Problem;
import com.example.bansherni.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity {

    LinearLayout mNoItemsPlaceHolder;
    RecyclerView mProblemsList;
    ArrayList<Problem> problems;
    AdminProblemsAdapter mProblemsAdapter;

    ImageView mRefreshBtn;
    ImageView mLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bindViews();

        hidePlaceHolder();

        mRefreshBtn.setOnClickListener(v -> {
            getUserReports();
        });

        mLogoutBtn.setOnClickListener(v -> logOut());

    }

    private void bindViews() {
        mNoItemsPlaceHolder = findViewById(R.id.place_holder);
        mProblemsList = findViewById(R.id.problems_list);
        mRefreshBtn = findViewById(R.id.refresh_btn);
        mLogoutBtn = findViewById(R.id.logout_btn);
    }

    void showPlaceHolder(){
        mNoItemsPlaceHolder.setVisibility(View.VISIBLE);
    }

    void hidePlaceHolder(){
        mNoItemsPlaceHolder.setVisibility(View.INVISIBLE);
    }

    void getUserReports(){

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();
        mRefreshBtn.setEnabled(false);

        String url = "api/requests/all.php";

        problems = new ArrayList<>();

        AndroidNetworking.get(url)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        mRefreshBtn.setEnabled(true);

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                //getting the problems from the response
                                JSONArray reportsArray = obj.getJSONArray("data");
                                Problem problem;
                                String adminPhone = null;
                                for(int i = 0; i < reportsArray.length(); i++){
                                    JSONObject problemJson = reportsArray.getJSONObject(i);
                                    problem = new Problem(
                                            Integer.parseInt(problemJson.getString("id")),
                                            Integer.parseInt(problemJson.getString("user_id")),
                                            problemJson.getString("user_name"),
                                            problemJson.getString("user_phone"),
                                            problemJson.getString("details"),
                                            Integer.parseInt(problemJson.getString("type")),
                                            Integer.parseInt(problemJson.getString("status")),
                                            Double.parseDouble(problemJson.getString("lat")),
                                            Double.parseDouble(problemJson.getString("lng")),
                                            Double.parseDouble(problemJson.getString("price"))
                                    );
                                    problems.add(problem);
                                }



                                if(problems.isEmpty()){
                                    showPlaceHolder();
                                }else {
                                    mProblemsAdapter = new AdminProblemsAdapter(AdminMainActivity.this, problems);
                                    mProblemsList.setAdapter(mProblemsAdapter);
                                    hidePlaceHolder();
                                }


                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(AdminMainActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mRefreshBtn.setEnabled(true);
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mRefreshBtn.setEnabled(true);
                        Toast.makeText(AdminMainActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void logOut(){
        SharedPrefManager.getInstance(this).logout();
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserReports();
    }

}