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
import com.tam.inch.sportstation.object.User;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class RegisterActivity extends AppCompatActivity {
    @Bind(R.id.edtName)EditText edtName;
    @Bind(R.id.edtEmail)EditText edtEmail;
    @Bind(R.id.edtPass)EditText edtPass;
    @Bind(R.id.btnRegister)Button btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.btnRegister)
    public void setBtnRegister(){
        final Firebase rootUrl=new Firebase(Constant.FIREBASE_CHAT_URL);
        final String userFirstName=edtName.getText().toString();
        final String userEmail=edtEmail.getText().toString();
        final String userPassword=edtPass.getText().toString();
        if(userFirstName.isEmpty()||userEmail.isEmpty()||userPassword.isEmpty()){

        }else{
            rootUrl.createUser(userEmail, userPassword, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    rootUrl.authWithPassword(userEmail, userPassword, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            long createTime = new Date().getTime();
                            rootUrl.child(Constant.CHILD_USERS).child(authData.getUid()).setValue(new User(userFirstName,userEmail,Constant.KEY_ONLINE, String.valueOf(createTime)), new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    EventBus.getDefault().post(Constant.KEY_CLOSE);
                                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {

                        }
                    });
                }

                @Override
                public void onError(FirebaseError firebaseError) {
                    Toast.makeText(RegisterActivity.this,firebaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}
