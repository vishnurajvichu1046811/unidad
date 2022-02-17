package com.utracx.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.SharedPreferencesHelper;
import com.utracx.viewmodel.AlertListActivityViewModel;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserMobile;
import java.util.List;
import java.util.regex.Pattern;

import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;


public class ChangeMobnoActivity extends AppCompatActivity {

    private LiveData<List<VehicleInfo>> currentVehicleInfo;

    EditText NewMob_no;
    TextView tvmobInfo;
    Button btnOK;

    private AlertListActivityViewModel activityViewModel;

    VehicleInfo vehicleInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changemobno);

        String userMob = getUserMobile(this);

        UserDataEntity data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                SharedPreferencesHelper.getUserEmail(this)
        );
        String uid = data.getUserID();

        NewMob_no = findViewById(R.id.et_Mob_No);

        tvmobInfo = findViewById(R.id.tv_mobInfo);
        btnOK = findViewById(R.id.btn_No_Save);

        String firstPart = userMob.substring(userMob.length() -2);

        tvmobInfo.setText("Conform your old mobile number ********"+firstPart);

        findViewById(R.id.backView).setOnClickListener(backView -> finish());


                btnOK.setOnClickListener(v -> {

                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(NewMob_no.getWindowToken(), 0);

                    if(btnOK.getText().equals("update")){

                        String sMobno = NewMob_no.getText().toString();

                        if (TextUtils.isEmpty(sMobno) | sMobno.length() <= 10) {

                            if (TextUtils.isEmpty(sMobno)) {
                                NewMob_no.setError("Mobile number is Required");
                                return;
                            }

                            if (sMobno.length() < 10) {
                                NewMob_no.setError("Please enter valid mobile number");
                                return;
                            } else {
                                NewMob_no.setText("");
                                Toast.makeText(this, "Your mobile number has been changed", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    else {

                        String sMobno = NewMob_no.getText().toString();

                        if (TextUtils.isEmpty(sMobno) | sMobno.length() <= 10) {

                            if (TextUtils.isEmpty(sMobno)) {
                                NewMob_no.setError("Mobile number is Required");
                                return;
                            }

                            if (sMobno.length() < 10) {
                                NewMob_no.setError("Please enter valid mobile number");
                                return;
                            } else {

                                if(sMobno.equals(userMob)) {
                                    NewMob_no.setText("");
                                    btnOK.setText("update");
                                    tvmobInfo.setText("Enter New Mobile Number");
                                }
                                else
                                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();

                            }

                        }
                    }

                });

    }

}