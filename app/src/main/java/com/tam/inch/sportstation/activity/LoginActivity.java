package com.tam.inch.sportstation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.tam.inch.sportstation.R;
import com.tam.inch.sportstation.common.Constant;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.edtEmail)
    EditText edtEmail;
    @Bind(R.id.edtPass)
    EditText edtPass;
    @Bind(R.id.btnLogin)
    Button btnLogin;
    @Bind(R.id.btnRegister)
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
    public void onEvent(String event) {
        if (event.equals(Constant.KEY_CLOSE)){
            LoginActivity.this.finish();
        }
    }
    @OnClick(R.id.btnLogin)
    public void btnLogin() {
        String email = edtEmail.getText().toString();
        String pass = edtPass.getText().toString();
        if (email.isEmpty() || pass.isEmpty()) {

        } else {
            Firebase authenticateUser = new Firebase(Constant.FIREBASE_CHAT_URL);
            authenticateUser.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Toast.makeText(LoginActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @OnClick(R.id.btnRegister)
    public void setBtnRegister(){
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }
}
