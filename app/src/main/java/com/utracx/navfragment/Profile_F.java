package com.utracx.navfragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.utracx.R;
import com.utracx.api.model.rest.firebase.FirebaseRequestBody;
import com.utracx.api.model.rest.login.LoginData;
import com.utracx.api.model.rest.login.LoginResponseData;
import com.utracx.api.model.rest.login.ProfileUpdateResponse;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.FirebaseInstanceIdCall;
import com.utracx.api.request.calls.LoginRequestCall;
import com.utracx.api.request.calls.VehicleListRequestCall;
import com.utracx.api.request.interfaces.LoginCallback;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.util.helper.SharedPreferencesHelper;
import com.utracx.view.adapter.AlertVehicleAdapter;
import com.utracx.viewmodel.AlertVehicleListActivityViewModel;
import com.utracx.viewmodel.DashBoardViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_ADDRESS;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_EMAIL;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_EMAIL_;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_FULLNAME;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_ID;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_MOB;
import static com.utracx.api.request.calls.LoginRequestCall.BUNDLE_KEY_USER_PASSWORD;
import static com.utracx.util.ConstantVariables.VEHICLE_MOVE_TYPE;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserAddress;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserFullname;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserMobile;


public class Profile_F extends AppCompatActivity implements LoginCallback {

    LinearLayout ll_Email, ll_Phone, llAdress, llCpassword, llFullName;
    EditText etEmail,etpOpass, etpNpass, etpCpass, etPhone, etAddress, tvUsername, etCPassword, etRegno;
    TextView  tvUserImg, tvUpVehicleNo;
    Button btnUpEmail, btnUpPhone, btnCEmail, btnCPhone, btnUpAddress, btnUpCpass, btnCaddrees, btnCpassword, btnUpName, btnCName;
    private boolean isLogoutFlag = false;
    DashBoardViewModel dashBoardViewModel;
    String userMob, userMail, uid, userFullname, userAddress, uPass, userEmail;
    ImageView ivShowoldPass, ivShownewPass, ivShowconPass;
    LoginData loginData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userMail = getUserEmail(this);
        userFullname = getUserFullname(this);

        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);

        UserDataEntity data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                SharedPreferencesHelper.getUserEmail(this)
        );

        uid = data.getUserID();
        uPass = data.getPassword();

        makeLoginWebCall(userMail,uPass);

        initViews();
        initClicks();
        setUIValues();
        hideView();
    }

    private void makeLoginWebCall(@NonNull String username, @NonNull String password) {
        ApiUtils.getInstance().getSOService().sendLoginData(username, password).enqueue(
                new LoginRequestCall(username, password, this)
        );
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phno);
        etCPassword = findViewById(R.id.tv_cPassword);
        etAddress = findViewById(R.id.et_address);
        ll_Email = findViewById(R.id.ll_upEmail);
        ll_Phone = findViewById(R.id.ll_upPhone);
        llAdress = findViewById(R.id.ll_upAdress);
        llCpassword = findViewById(R.id.ll_upPassword);
        llFullName = findViewById(R.id.ll_upFullname);
        etpOpass = findViewById(R.id.etp_passOld);
        etpNpass = findViewById(R.id.etp_passNew);
        etpCpass = findViewById(R.id.etp_passConform);
        btnUpName = findViewById(R.id.btn_upName);
        btnUpEmail = findViewById(R.id.btn_upEmail);
        btnUpPhone = findViewById(R.id.btn_upPhone);
        btnCName = findViewById(R.id.btn_cName);
        btnCEmail = findViewById(R.id.btn_cEmail);
        btnCPhone = findViewById(R.id.btn_cPhone);
        btnUpAddress = findViewById(R.id.btn_upAdress);
        btnUpCpass = findViewById(R.id.btn_upPassword);
        btnCaddrees = findViewById(R.id.btn_cAdress);
        btnCpassword = findViewById(R.id.btn_cPassword);
        ivShowoldPass = findViewById(R.id.show_pass_old);
        ivShownewPass = findViewById(R.id.show_pass_new);
        ivShowconPass = findViewById(R.id.show_npass_conf);
        tvUpVehicleNo = findViewById(R.id.tv_upVehicle_no);

        tvUserImg = findViewById(R.id.iv_vehicle);
        tvUsername = findViewById(R.id.et_username);

        userMail = getUserEmail(this);
        userFullname = getUserFullname(this);

        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);

        UserDataEntity data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                SharedPreferencesHelper.getUserEmail(this)
        );

        uid = data.getUserID();
        uPass = data.getPassword();
    }

    private void initClicks() {

        findViewById(R.id.ib_logout).setOnClickListener(v -> logoutUserNow());
        findViewById(R.id.backView).setOnClickListener(backView -> finish());

        etCPassword.setOnFocusChangeListener((v, hasFocus) -> setUpdatePassword());
        etPhone.setOnFocusChangeListener((v, hasFocus) -> setUpdatePhone());
        tvUsername.setOnFocusChangeListener((v, hasFocus) -> setUpdateFullname());
        etEmail.setOnFocusChangeListener((v, hasFocus) -> setUpdateEmail());
        etAddress.setOnFocusChangeListener((v, hasFocus) -> setUpdateAddress());

        tvUpVehicleNo.setOnClickListener(v -> {
            Intent i = new Intent(this,UpdateVehicleNo.class);
            startActivity(i);
                });
    }

    private void setUIValues() {
        if(SharedPreferencesHelper.getUserDetails(this) != null) {

            LoginData userDetails = SharedPreferencesHelper.getUserDetails(this);
            userEmail = "";

            if(userDetails.getMobile() != null)
                userMob = userDetails.getMobile();
            if(userDetails.getEmail() != null)
                userEmail = userDetails.getEmail();
            if(userDetails.getAddress() != null)
                userAddress = userDetails.getAddress();
            if(userDetails.getFullname() != null)
                userFullname = userDetails.getFullname();

            if(userFullname != "") {
                tvUsername.setText(userFullname);

                String nav_title = null;

                String fl_img = userFullname.substring(0, 1).toUpperCase();

                if (userFullname.contains(" ")) {
                    String[] bits = userFullname.split(" ");
                    String lastOne = bits[bits.length - 1];
                    String fl = lastOne.substring(0, 1).toUpperCase();
                    nav_title = fl_img + fl;
                } else {
                    nav_title = fl_img;
                }

                tvUserImg.setText(nav_title);
            }


            etPhone.setText(userMob);
            etEmail.setText(userEmail);
            etAddress.setText(userAddress);
            tvUsername.setText(userFullname);
        }
    }

    private void hideView() {
        ll_Email.setVisibility(View.GONE);
        ll_Phone.setVisibility(View.GONE);
        llAdress.setVisibility(View.GONE);
        llCpassword.setVisibility(View.GONE);
        llFullName.setVisibility(View.GONE);
    }

    private void setUpdateFullname() {
        llFullName.setVisibility(View.VISIBLE);
        ll_Email.setVisibility(View.GONE);
        ll_Phone.setVisibility(View.GONE);
        llAdress.setVisibility(View.GONE);
        llCpassword.setVisibility(View.GONE);

        tvUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged (Editable s){
                String newName = s.toString();
                if (!newName.isEmpty() && !newName.equals(userFullname)) {
                    btnUpName.setBackgroundColor(getResources().getColor(R.color.cardOrangeColor));
                    btnUpName.setOnClickListener(v1 -> updateName(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                btnUpName.setBackgroundColor(getResources().getColor(R.color.colorDisableOrange));
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });


        btnCName.setOnClickListener(v1 -> {
            llFullName.setVisibility(View.GONE);
            tvUsername.setText(userFullname);
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(tvUsername.getWindowToken(), 0);
        });

    }

    private void setUpdateEmail() {


        ll_Email.setVisibility(View.VISIBLE);
        ll_Phone.setVisibility(View.GONE);
        llAdress.setVisibility(View.GONE);
        llCpassword.setVisibility(View.GONE);
        llFullName.setVisibility(View.GONE);

        etEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged (Editable s){
                String newEmail = s.toString();
                if (!newEmail.isEmpty() && !newEmail.equals(userMail)) {
                    btnUpEmail.setBackgroundColor(getResources().getColor(R.color.cardOrangeColor));
                    btnUpEmail.setOnClickListener(v1 -> updateEmail(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                btnUpEmail.setBackgroundColor(getResources().getColor(R.color.colorDisableOrange));
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });


        btnCEmail.setOnClickListener(v1 -> {
            ll_Email.setVisibility(View.GONE);
            etEmail.setText(userEmail);
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
        });
    }

    private void setUpdatePhone() {

        ll_Phone.setVisibility(View.VISIBLE);
        ll_Email.setVisibility(View.GONE);
        llAdress.setVisibility(View.GONE);
        llCpassword.setVisibility(View.GONE);
        llFullName.setVisibility(View.GONE);

        etPhone.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged (Editable s){
                String newNo = s.toString();
                if (s.length() == 10 && !newNo.equals(userMob)) {
                    btnUpPhone.setBackgroundColor(getResources().getColor(R.color.cardOrangeColor));
                    btnUpPhone.setOnClickListener(v1 -> updatePhone(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                btnUpPhone.setBackgroundColor(getResources().getColor(R.color.colorDisableOrange));
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {


            }
        });


        btnCPhone.setOnClickListener(v1 -> {
            ll_Phone.setVisibility(View.GONE);
            etPhone.setText(userMob);
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);
        });
    }

    private void setUpdateAddress() {

        llAdress.setVisibility(View.VISIBLE);
        ll_Phone.setVisibility(View.GONE);
        ll_Email.setVisibility(View.GONE);
        llCpassword.setVisibility(View.GONE);
        llFullName.setVisibility(View.GONE);

        etAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged (Editable s){
                String newAddress = s.toString();
                if (!newAddress.isEmpty() && !newAddress.equals(userAddress)) {
                    btnUpAddress.setBackgroundColor(getResources().getColor(R.color.cardOrangeColor));
                    btnUpAddress.setOnClickListener(v1 -> updateAddress(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                btnUpAddress.setBackgroundColor(getResources().getColor(R.color.colorDisableOrange));
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {


            }
        });


        btnCaddrees.setOnClickListener(v1 -> {
            llAdress.setVisibility(View.GONE);
            etAddress.setText(userAddress);
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(etAddress.getWindowToken(), 0);
        });

    }

    private void setUpdatePassword() {
        llCpassword.setVisibility(View.VISIBLE);
        ll_Email.setVisibility(View.GONE);
        ll_Phone.setVisibility(View.GONE);
        llAdress.setVisibility(View.GONE);
        llFullName.setVisibility(View.GONE);

        btnUpCpass.setOnClickListener(v1 -> setChangePassword() );

        btnCpassword.setOnClickListener(v1 -> {
            llCpassword.setVisibility(View.GONE);
            etpCpass.setText("");
            etpNpass.setText("");
            etpOpass.setText("");
            ivShowoldPass.setImageResource(R.drawable.pass_hide);
            etpOpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShownewPass.setImageResource(R.drawable.pass_hide);
            etpNpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShowconPass.setImageResource(R.drawable.pass_hide);
            etpCpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
        });
    }

    private void updateName(Editable s) {
        tvUsername.setText(s);
        llFullName.setVisibility(View.GONE);
        LoginData loginData = new LoginData();
        loginData.setFullname(s.toString());
        updateProfileDetails(loginData);
    }

    private void updateAddress(Editable s) {
        etAddress.setText(s);
        llAdress.setVisibility(View.GONE);

        LoginData loginData = new LoginData();
        loginData.setAddress(s.toString());
        updateProfileDetails(loginData);
    }

    private void updateEmail(Editable s) {
        etEmail.setText(s);
        ll_Email.setVisibility(View.GONE);
        LoginData loginData = new LoginData();
        loginData.setEmail(s.toString());
        updateProfileDetails(loginData);
    }

    private void updatePhone(Editable s) {

        etPhone.setText(s);
        ll_Phone.setVisibility(View.GONE);

        LoginData loginData = new LoginData();
        loginData.setMobile(s.toString());
        updateProfileDetails(loginData);

    }

    private void updateProfileDetails(LoginData loginData) {


        ApiUtils.getInstance().getSOService().updateProfile(userMail,uPass,loginData).enqueue(new Callback<ProfileUpdateResponse>() {
            @Override
            public void onResponse(Call<ProfileUpdateResponse> call, Response<ProfileUpdateResponse> response) {
                refreshLoginDetails();
            }

            @Override
            public void onFailure(Call<ProfileUpdateResponse> call, Throwable t) {
                Toast.makeText(Profile_F.this, "Something went wrong! please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void refreshLoginDetails(){

        ApiUtils.getInstance().getSOService().sendLoginData(userMail,uPass).enqueue(
                new LoginRequestCall(getUserEmail(this),uPass, Profile_F.this));


    }

    private void setChangePassword() {

        String sOldPassword = etpOpass.getText().toString().trim();
        String NewPassword = etpNpass.getText().toString().trim();
        String NewCPassword = etpCpass.getText().toString().trim();

        if(sOldPassword.isEmpty() | sOldPassword.length() < 6){
            if (TextUtils.isEmpty(sOldPassword)) {
                etpOpass.setError("Password is Required");
                return;
            }

        }

        if (TextUtils.isEmpty(NewPassword) | NewPassword.length() < 6 ) {

            if (TextUtils.isEmpty(NewPassword)) {
                etpNpass.setError("Password is Required");
                return;
            }

            if (NewPassword.length() < 6) {
                etpNpass.setError("Password must contain minimum 6 characters");
                return;
            }
        }

        if (TextUtils.isEmpty(NewCPassword) | NewCPassword.length() < 6 ) {
            if (TextUtils.isEmpty(NewCPassword)) {
                etpCpass.setError("Password is Required");
                return;
            }

            if (NewCPassword.length() < 6) {
                etpCpass.setError("Password must contain minimum 6 characters");
                return;
            }
        }

        if(sOldPassword.equals(uPass)) {

            if (NewCPassword.equals(NewPassword)) {

                llCpassword.setVisibility(View.GONE);
                etpCpass.setText("");
                etpNpass.setText("");
                etpOpass.setText("");
                ivShowoldPass.setImageResource(R.drawable.pass_hide);
                etpOpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivShownewPass.setImageResource(R.drawable.pass_hide);
                etpNpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivShowconPass.setImageResource(R.drawable.pass_hide);
                etpCpass.setTransformationMethod(PasswordTransformationMethod.getInstance());


                LoginData loginData = new LoginData();
                loginData.setPassword(NewPassword);
                updateProfileDetails(loginData);

            } else {
                Toast.makeText(this, "Password Miss Matched", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            etpOpass.setError("Invalid password");
            return;
        }


    }


    public void ShowHidePass(View view) {

        if (view.getId() == R.id.show_pass_old) {
            if (etpOpass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setImageResource(R.drawable.show_pass);

                //Show Password
                etpOpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                etpOpass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }

        if (view.getId() == R.id.show_pass_new) {
            if (etpNpass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setImageResource(R.drawable.show_pass);

                //Show Password
                etpNpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                etpNpass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
        if (view.getId() == R.id.show_npass_conf) {
            if(etpCpass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView)(view)).setImageResource(R.drawable.show_pass);

                //Show Password
                etpCpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            }else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                etpCpass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }

        }
    }

    private void logoutUserNow() {

        //flag to prevent the logout cancel of API calls
        isLogoutFlag = true;

        //call to the server
        unregisterFirebaseInstanceID();

        //Call to firebase SDK to delete the firebase token
        destroyFirebaseInstanceID();

        //Clear all the tables in the DB
        dashBoardViewModel.deleteAllData();

        //clear all values saved in the shared preferences
        SharedPreferencesHelper.clearPreferences(this);

        //Move to login activity
        NavigationHelper.clearNavigateToLoginActivity(this);
    }

    private void destroyFirebaseInstanceID() {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                    } catch (Exception e) {
                        Log.e(TAG, "logoutUserNow: unable to destroy firebase id", e);
                    }
                }
        );
    }

    private void unregisterFirebaseInstanceID() {
        Executors.newSingleThreadExecutor().execute(
                () -> {


                    UserDataEntity userData = dashBoardViewModel.fetUserDataByEmail(getUserEmail(this));
                    if (userData != null) {
                        FirebaseRequestBody firebaseRequestBody = new FirebaseRequestBody(
                                userData.getUsername(),
                                userData.getPassword(),
                                userData.getFirebaseInstanceID()
                        );

                        ApiUtils
                                .getInstance()
                                .getSOService()
                                .unregisterFirebaseForUser(firebaseRequestBody)
                                .enqueue(new FirebaseInstanceIdCall(firebaseRequestBody));
                    }
                }
        );
    }


    @Override
    public void onLoginCompleted(Bundle dataBundle) {
        Gson gson = new Gson();

        String usernameEmail = dataBundle.getString(BUNDLE_KEY_USER_EMAIL_, null);
        String userMobile= dataBundle.getString(BUNDLE_KEY_USER_MOB, null);
        String userFullname= dataBundle.getString(BUNDLE_KEY_USER_FULLNAME, null);
        String userAddress= dataBundle.getString(BUNDLE_KEY_USER_ADDRESS, null);
        String password = dataBundle.getString(BUNDLE_KEY_USER_PASSWORD, null);

        LoginData loginData = new LoginData();
        loginData.setMobile(userMobile);
        loginData.setAddress(userAddress);
        loginData.setEmail(usernameEmail);
        loginData.setFullname(userFullname);
        loginData.setPassword(password);

        SharedPreferencesHelper.saveUserDetails(this,gson.toJson(loginData));

        setUIValues();
    }

    @Override
    public void onLoginFailed(Bundle data) {

    }

    @Override
    public void onLoginError(@Nullable Bundle data) {

    }
}