package com.utracx.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.utracx.R;
import com.utracx.database.AppDatabase;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.util.helper.SharedPreferencesHelper;

import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;

public class ForgetPassword_A extends AppCompatActivity {

    LinearLayout ll_Forget_Username, ll_forget_update;
    Button btnNext, btnUpdate;
    EditText etNewPass, etCPss, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        ll_Forget_Username = findViewById(R.id.ll_forgot_username);
        ll_forget_update = findViewById(R.id.ll_forgot_pass);

        etNewPass = findViewById(R.id.et_forget_pass);
        etCPss = findViewById(R.id.et_forget_Cpass);
        username = findViewById(R.id.et_forget_username);

        btnNext = findViewById(R.id.btn_forgot_next);
        btnUpdate = findViewById(R.id.btn_forgot_update);

        ll_Forget_Username.setVisibility(View.VISIBLE);
        ll_forget_update.setVisibility(View.GONE);

        btnNext.setOnClickListener(v -> {
            checkUsername();
        });

        btnUpdate.setOnClickListener(v -> setChangePassword());
    }

    private void checkUsername() {

        String sUser = username.getText().toString().trim();

        if(sUser.length() > 4) {

            ll_Forget_Username.setVisibility(View.GONE);
            ll_forget_update.setVisibility(View.VISIBLE);
        }
        else{
            username.setError("Invalid username");
            return;
        }

    }

    private void setChangePassword() {

        String NewPassword = etNewPass.getText().toString().trim();
        String NewCPassword = etCPss.getText().toString().trim();

        if (TextUtils.isEmpty(NewPassword) | NewPassword.length() < 6 ) {

            if (TextUtils.isEmpty(NewPassword)) {
                etNewPass.setError("Password is Required");
                return;
            }

            if (NewPassword.length() < 6) {
                etNewPass.setError("Password must contain minimum 6 characters");
                return;
            }
        }

        if (TextUtils.isEmpty(NewCPassword) | NewCPassword.length() < 6 ) {
            if (TextUtils.isEmpty(NewCPassword)) {
                etCPss.setError("Password is Required");
                return;
            }

            if (NewCPassword.length() < 6) {
                etCPss.setError("Password must contain minimum 6 characters");
                return;
            }
        }

            if (NewCPassword.equals(NewPassword)) {

                etNewPass.setText("");
                etCPss.setText("");

                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password not matching", Toast.LENGTH_SHORT).show();
            }


    }

    public void ShowForgetHidePass(View view) {

        if (view.getId() == R.id.show_pass_newFpass) {
            if (etNewPass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setBackground(getDrawable(R.drawable.show_pass));

                //Show Password
                etNewPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setBackground(getDrawable(R.drawable.pass_hide));

                //Hide Password
                etNewPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
        if (view.getId() == R.id.show_pass_CFpass) {
            if(etCPss.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setBackground(getDrawable(R.drawable.show_pass));

                //Show Password
                etCPss.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            }else {
                ((ImageView) (view)).setBackground(getDrawable(R.drawable.pass_hide));

                //Hide Password
                etCPss.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }

        }
    }

}