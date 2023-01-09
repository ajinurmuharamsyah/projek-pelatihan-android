package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextView signup,forgot;
    private Button login;
    private EditText email,password;

    //firebase auth
    private FirebaseAuth mAuth;

    //progressDialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailid);
        password = findViewById(R.id.passwordet);
        signup = findViewById(R.id.noAccountTv);
        login = findViewById(R.id.loginBtn);
        forgot = findViewById(R.id.forgotid);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, begin login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    validatedata();
            }
        });

        //handle click, begin register
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        //handle click. open forgot password activity
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
                finish();
            }
        });


    }

    private String Email,Password;
    private void validatedata() {
        /*Before creating account, lets do some data validation*/

        //get data
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            //email is either no entered or email pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Invalid email pattern...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Password)){
            //password edit text is empty, must enter password
            Toast.makeText(this, "Enter your password...!", Toast.LENGTH_SHORT).show();
        }
        else {
            //all data is validated, begin creating account
            loginUser();
        }
    }

    private void loginUser() {
        //show progress
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        //login user
        mAuth.signInWithEmailAndPassword(Email,Password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login success, check if user is user or admin
                        checkTypeUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //login failed
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTypeUser() {
        //check if user is user or admin from realtime database
        //get current user
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        //check in db
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get user type
                String userType = ""+snapshot.child("usertype").getValue();
                //check user type
                if (userType.equals("user")){
                    //this is simple user, open user dashboard
                    startActivity(new Intent(LoginActivity.this,DashboarduserActivity.class));
                    finish();
                }
                else if (userType.equals("admin")){
                    //this is admin,open admin dashboard
                    startActivity(new Intent(LoginActivity.this,DashboardadminActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
