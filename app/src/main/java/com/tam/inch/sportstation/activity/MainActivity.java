package com.tam.inch.sportstation.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.tam.inch.sportstation.R;
import com.tam.inch.sportstation.common.Constant;
import com.tam.inch.sportstation.object.User;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private Firebase rootUrl;
    private Firebase urlCurrenUser;
    private Firebase urlAllUser;
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;
    private String currenUserId;
    private String currenUserEmail;
    private ArrayList<User> arrUser;
    private AllUserAdapter allUserAdapter;
    private ArrayList<String> arrStringEmail;
    private ValueEventListener valueEventListenerUserConnected;
    private User currenUser;
    @Bind(R.id.btnLogout)
    Button btnLogout;
    @Bind(R.id.lvUser)
    ListView lvUser;
    @Bind(R.id.tvUsserName)
    TextView tvUsserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        arrStringEmail = new ArrayList<>();
        arrUser = new ArrayList<User>();
        allUserAdapter = new AllUserAdapter(MainActivity.this, 0, arrUser);
        lvUser.setAdapter(allUserAdapter);
        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this,ChatRoomActivity.class);
                User user=arrUser.get(position);
                Gson gson=new Gson();
                intent.putExtra(Constant.KEY_SEND_USER,gson.toJson(user).toString()+"---"+gson.toJson(currenUser).toString());
                startActivity(intent);
            }
        });
        rootUrl = new Firebase(Constant.FIREBASE_CHAT_URL);
        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                setAuthenticatedUser(authData);
            }
        };
        rootUrl.addAuthStateListener(mAuthStateListener);
    }

    private void setAuthenticatedUser(AuthData authData) {
        mAuthData = authData;
        if (authData != null) {
            currenUserId = authData.getUid();
            currenUserEmail = (String) authData.getProviderData().get(Constant.KEY_EMAIL);
            getCurrenUser(authData);
            getAllUser(authData);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    public void getCurrenUser(AuthData authData) {
        urlCurrenUser = new Firebase(Constant.FIREBASE_CHAT_URL).child(Constant.CHILD_USERS).child(authData.getUid());
        urlCurrenUser.addValueEventListener(valueEventListenerCurrenUser);
        valueEventListenerUserConnected=rootUrl.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    urlCurrenUser.child(Constant.CHILD_CONNECTION).setValue(Constant.KEY_ONLINE);
                    urlCurrenUser.child(Constant.CHILD_CONNECTION).onDisconnect().setValue(Constant.KEY_OFFLINE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    private ValueEventListener valueEventListenerCurrenUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            tvUsserName.setText("Hello "+user.name);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };
    public void getAllUser(AuthData authData) {
        urlAllUser = new Firebase(Constant.FIREBASE_CHAT_URL).child(Constant.CHILD_USERS);
        urlAllUser.addChildEventListener(childEventListenerAllUser);
    }
    private ChildEventListener childEventListenerAllUser = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            User user = dataSnapshot.getValue(User.class);
            if (!dataSnapshot.getKey().equals(currenUserId)){
                arrStringEmail.add(user.email);
                arrUser.add(user);
                allUserAdapter.notifyDataSetChanged();
            }else {
                currenUser=user;
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (!dataSnapshot.getKey().equals(currenUserId)){
                User user = dataSnapshot.getValue(User.class);
                int index = arrStringEmail.indexOf(user.email);
                arrUser.set(index, user);
                allUserAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            rootUrl.removeAuthStateListener(mAuthStateListener);
        } catch (Exception e) {
        }
        try {
            urlCurrenUser.removeEventListener(valueEventListenerCurrenUser);
        } catch (Exception e) {
        }
        try {
            urlAllUser.removeEventListener(childEventListenerAllUser);
        } catch (Exception e) {
        }
        try {
            rootUrl.getRoot().child(".info/connected").removeEventListener(valueEventListenerUserConnected);
        }catch (Exception e){}
    }

    @OnClick(R.id.btnLogout)
    public void btnLogout() {
        if (this.mAuthData != null) {
            urlCurrenUser.child(Constant.CHILD_CONNECTION).setValue(Constant.KEY_OFFLINE);
            rootUrl.removeAuthStateListener(mAuthStateListener);
            rootUrl.unauth();
            setAuthenticatedUser(null);
        }
    }

    public class AllUserAdapter extends ArrayAdapter<User> {
        private Activity mActivity;
        private ArrayList<User> mArrUser;
        @Bind(R.id.tvNameUser)
        TextView tvNameUser;
        @Bind(R.id.tvStatus)
        TextView tvStatus;

        public AllUserAdapter(Activity mActivity, int resource, ArrayList<User> mArrUser) {
            super(mActivity, resource, mArrUser);
            this.mActivity = mActivity;
            this.mArrUser = mArrUser;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = mActivity.getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.item_list_user, null);
            }
            ButterKnife.bind(this, convertView);
            tvNameUser.setText(mArrUser.get(position).name);
            tvStatus.setText(mArrUser.get(position).connecttion);
            if (mArrUser.get(position).connecttion.equals(Constant.KEY_ONLINE)){
                tvStatus.setTextColor(Color.parseColor("#00FF00"));
            }else {
                tvStatus.setTextColor(Color.parseColor("#FF0000"));
            }
            return convertView;
        }
    }

}
