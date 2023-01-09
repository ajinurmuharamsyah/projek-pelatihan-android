package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.core.text.TextUtilsCompat;

import android.app.ProgressDialog;
import android.text.TextUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    private ImageButton btnback;
    private Button register;
    private EditText name,email,password,cpassword;

    //progress dialog
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.nameid);
        email = findViewById(R.id.emailid);
        password = findViewById(R.id.passwordet);
        cpassword = findViewById(R.id.cPpassword);
        btnback = findViewById(R.id.backBtn);
        register = findViewById(R.id.registerBtn);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, begin register
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatedate();
            }
        });

    }

    private String Name,Email,Password,CPassword;
    private void validatedate() {
        /*Before creating account, lets do some data validation*/

        //get data
        Name = name.getText().toString().trim();
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();
        CPassword = cpassword.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(Name)){
            //name edit text is empty, must enter name
            Toast.makeText(this, "Enter your name...!", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            //email is either no entered or email pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Invalid email pattern...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Password)){
            //password edit text is empty, must enter password
            Toast.makeText(this, "Enter your password...!", Toast.LENGTH_SHORT).show();
        }
        else if (Password.length()<8){
            //passwor is minimum 8 character or password pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Password minimum 8 Character..!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(CPassword)){
            //confirm password edit text is empty,must enter confirm password
            Toast.makeText(this, "Confirm Password...!", Toast.LENGTH_SHORT).show();
        }
        else if (!Password.equals(CPassword)){
            //password and confirm password doesn't match, don't allow to continue in that case,both password must match
            Toast.makeText(this, "Password doesn't match...!", Toast.LENGTH_SHORT).show();
        }
        else {
            //all data is validated, begin creating account
            createaccount();
        }

    }

    private void createaccount() {
        //show progress
        progressDialog.setMessage("Creating account library");
        progressDialog.show();

        //create user in firebase auth
        mAuth.createUserWithEmailAndPassword(Email,Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account creation success, now add in firebase realtime database
                inputUserInfo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //account creating failed
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inputUserInfo() {
        progressDialog.setMessage("Saving user info...!");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered so we can get now
        String uid = mAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("name",""+Name);
        hashMap.put("email",""+Email);
        hashMap.put("profil_image","");//add empty, we do later
        hashMap.put("usertype","user");//possible values are user, admin: will make manually admin in firebase realtime database by changing this values
        hashMap.put("timestamp",""+timestamp);

        //set data db
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created...", Toast.LENGTH_SHORT).show();
                        //since user account is created so start dashboard of user
                        startActivity(new Intent(RegisterActivity.this,DashboarduserActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //data failed adding to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        finish();
    }
}
