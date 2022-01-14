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
import com.example.bansherni.adapters.UserProblemsAdapter;
import com.example.bansherni.model.Problem;
import com.example.bansherni.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserMainActivity extends AppCompatActivity {

    LinearLayout mNoItemsPlaceHolder;
    RecyclerView mProblemsList;
    ArrayList<Problem> problems;
    UserProblemsAdapter mProblemsAdapter;
    FloatingActionButton mAddProblemBtn;

    ImageView mRefreshBtn;
    ImageView mLogoutBtn;


    int userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        userId = SharedPrefManager.getInstance(this).getUserId();

        bindViews();

        hidePlaceHolder();

        mLogoutBtn.setOnClickListener(v -> logOut());

        mRefreshBtn.setOnClickListener(v -> {
            getUserReports();
        });

        mAddProblemBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AddOrderActivity.class));
        });

    }

    private void bindViews() {
        mNoItemsPlaceHolder = findViewById(R.id.place_holder);
        mAddProblemBtn = findViewById(R.id.add_order_btn);
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

        String url = "api/requests/user.php?user_id=";
        url += String.valueOf(SharedPrefManager.getInstance(this).getUserId());

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
                                            problemJson.getString("details"),
                                            Integer.parseInt(problemJson.getString("type")),
                                            Integer.parseInt(problemJson.getString("status")),
                                            Double.parseDouble(problemJson.getString("price"))
                                    );
                                    adminPhone = problemJson.getString("admin_phone");
                                    problems.add(problem);
                                }

                                if(adminPhone != null){
                                    SharedPrefManager.getInstance(UserMainActivity.this).saveAdminPhoneNumber(adminPhone);
                                }

                                if(problems.isEmpty()){
                                    showPlaceHolder();
                                }else {
                                    mProblemsAdapter = new UserProblemsAdapter(UserMainActivity.this, problems);
                                    mProblemsList.setAdapter(mProblemsAdapter);
                                    hidePlaceHolder();
                                }


                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(UserMainActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UserMainActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
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