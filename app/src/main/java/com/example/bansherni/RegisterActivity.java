package com.example.bansherni;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.api.Constants;
import com.example.bansherni.model.User;
import com.example.bansherni.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText mNameET, mPhoneET, mPasswordET;
    Button  mRegisterBtn;
    TextView mSignUpLoginBtn;
    TextView mAboutUsBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();

        mSignUpLoginBtn.setOnClickListener(v -> {
            finish();
        });

        mRegisterBtn.setOnClickListener(v -> {
            mRegisterBtn.setEnabled(false);
            if(validateUserData()){
                registerUser();
            }
        });

        mAboutUsBtn.setOnClickListener(v -> startActivity(new Intent(this, AboutUsActivity.class)));

    }

    private void bindViews() {
        mNameET = findViewById(R.id.full_name);
        mPhoneET = findViewById(R.id.phone);
        mPasswordET = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.register_btn);
        mSignUpLoginBtn = findViewById(R.id.signup_login_btn);
        mAboutUsBtn = findViewById(R.id.about_btn);
    }

    private boolean validateUserData() {

        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String name = mNameET.getText().toString();
        final String phone = mPhoneET.getText().toString();

        //checking if username is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getResources().getString(R.string.name_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }

        //checking if password is empty
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getResources().getString(R.string.phone_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }


        //checking if password is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, getResources().getString(R.string.password_missing_message), Toast.LENGTH_SHORT).show();
            mRegisterBtn.setEnabled(true);
            return false;
        }


        return true;
    }

    private void registerUser() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String name = mNameET.getText().toString();
        final String phone = mPhoneET.getText().toString().trim();

        AndroidNetworking.post("api/auth/signup.php")
                .addBodyParameter("name", name)
                .addBodyParameter("phone", phone)
                .addBodyParameter("password", pass)
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

                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("data");
                                User user;
                                SharedPrefManager.getInstance(getApplicationContext()).setUserType(Constants.USER);
                                user = new User(
                                        Integer.parseInt(userJson.getString("id")),
                                        userJson.getString("name"),
                                        "+966 "+userJson.getString("phone")
                                );

                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                goToUserMainActivity();
                                finish();

                                mRegisterBtn.setEnabled(true);
                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                mRegisterBtn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        mRegisterBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToUserMainActivity() {
        startActivity(new Intent(this, UserMainActivity.class));
    }
}