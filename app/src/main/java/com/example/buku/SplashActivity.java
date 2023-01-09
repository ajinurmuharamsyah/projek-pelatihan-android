package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        //start main screen after 2 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checktypeuser();
//                startActivity(new Intent(SplashActivity.this,MainActivity.class));
//                finish(); //finish this activity
            }
        },2000); //2000 means 2 seconds
    }

    private void checktypeuser() {
        //get current user, if logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null){
            //user not logged in
            //start main screen
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();//finish this activity
        }
        else {
            //check in db
            DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
            reference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //get user type
                    String userType = ""+snapshot.child("usertype").getValue();
                    //check user type
                    if (userType.equals("user")){
                        //this is simple user, open user dashboard
                        startActivity(new Intent(SplashActivity.this,DashboarduserActivity.class));
                        finish();
                    }
                    else if (userType.equals("admin")){
                        //this is admin,open admin dashboard
                        startActivity(new Intent(SplashActivity.this,DashboardadminActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
