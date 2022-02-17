package com.utracx.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utracx.R;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.SharedPreferencesHelper;

public class ChangePasswordActivity extends AppCompatActivity {

    Button btnChangePass;
    EditText nPass, CPass, oldPass;
    String uPass, uID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        UserDataEntity data = AppDatabase.getDatabase(this).userDetailsDao().getUserDataByEmail(
                SharedPreferencesHelper.getUserEmail(this)
        );
        uPass = data.getPassword();
        uID   = data.getUserID();

        initView();
        onClick();


    }

    private void initView() {
        oldPass = findViewById(R.id.et_pass_old);
        nPass = findViewById(R.id.et_pass_new);
        CPass = findViewById(R.id.et_npass_conf);
    }

    private void onClick() {
        findViewById(R.id.btn_ChangePass).setOnClickListener(v -> setChangePassword());
        findViewById(R.id.backView).setOnClickListener(backView -> finish());
    }

    private void setChangePassword() {

        String sOldPassword = oldPass.getText().toString().trim();
        String NewPassword = nPass.getText().toString().trim();
        String NewCPassword = CPass.getText().toString().trim();

        if(sOldPassword.isEmpty() | sOldPassword.length() < 6){
            if (TextUtils.isEmpty(sOldPassword)) {
                oldPass.setError("Password is Required");
                return;
            }

        }

        if (TextUtils.isEmpty(NewPassword) | NewPassword.length() < 6 ) {

            if (TextUtils.isEmpty(NewPassword)) {
                nPass.setError("Password is Required");
                return;
            }

            if (NewPassword.length() < 6) {
                nPass.setError("Password must contain minimum 6 characters");
                return;
            }
        }

        if (TextUtils.isEmpty(NewCPassword) | NewCPassword.length() < 6 ) {
            if (TextUtils.isEmpty(NewCPassword)) {
                CPass.setError("Password is Required");
                return;
            }

            if (NewCPassword.length() < 6) {
                CPass.setError("Password must contain minimum 6 characters");
                return;
            }
        }

        if(sOldPassword.equals(uPass)) {

            if (NewCPassword.equals(NewPassword)) {

                nPass.setText("");
                CPass.setText("");
                oldPass.setText("");

                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password Miss Matched", Toast.LENGTH_SHORT).show();
            }
        }
            else {
                oldPass.setError("Invalid password");
                return;
            }


    }


    public void ShowHidePass(View view) {

        if (view.getId() == R.id.show_pass_old) {
            if (oldPass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setImageResource(R.drawable.show_pass);

                //Show Password
                oldPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }

        if (view.getId() == R.id.show_pass_new) {
            if (nPass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setImageResource(R.drawable.show_pass);

                //Show Password
                nPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                nPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
        if (view.getId() == R.id.show_npass_conf) {
            if(CPass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView)(view)).setImageResource(R.drawable.show_pass);

                //Show Password
                CPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            }else {
                ((ImageView) (view)).setImageResource(R.drawable.pass_hide);

                //Hide Password
                CPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }

        }
    }

}