package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryActivity extends AppCompatActivity {

    private EditText addcatagory;
    private Button submit;
    private ImageButton btnback;

    //firebase auth
    private FirebaseAuth mAuth;

    //progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        addcatagory = findViewById(R.id.catagoryet);
        submit = findViewById(R.id.submitBtn);
        btnback = findViewById(R.id.backBtn);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, begin upload catagory
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CategoryActivity.this,DashboardadminActivity.class));
        finish();
    }

    private String AddCatagory;
    private void validate() {
        /*Before creating account, lets do some data validation*/

        //get data
        AddCatagory = addcatagory.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(AddCatagory)){
            //validate if not empty
            Toast.makeText(this, "Please enter catagory...!", Toast.LENGTH_SHORT).show();
        } else {
            addCatagoryFirebase();
        }
    }

    private void addCatagoryFirebase() {
        //progress dialog
        progressDialog.setMessage("Saving user info...!");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered so we can get now
        String uid = mAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+ AddCatagory);
        hashMap.put("timestamp",timestamp);

        //set data db
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(CategoryActivity.this, "Category added successfully...!", Toast.LENGTH_SHORT).show();
//                        //since user account is created so start dashboard of user
//                        startActivity(new Intent(C.this,DashboarduserActivity.class));
//                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //data failed adding to db
                progressDialog.dismiss();
                Toast.makeText(CategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
