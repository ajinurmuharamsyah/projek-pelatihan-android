package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.buku.Adapter.AdapterCatagory;
import com.example.buku.Model.ModelCatagory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardadminActivity extends AppCompatActivity {

    private TextView email;
    private EditText search;
    private Button addcatagory;
    private ImageButton logout;
    private RecyclerView catagoryrv;
    private FloatingActionButton pdfadd;

    //firebase auth
    private FirebaseAuth mAuth;

    //arrayList to store category
    private ArrayList<ModelCatagory> modelCatagories;
    //adapter
    private AdapterCatagory adapterCatagory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboardadmin);

        email = findViewById(R.id.subtitleTv);
        logout = findViewById(R.id.logoutBtn);
        addcatagory = findViewById(R.id.addCategoryBtn);
        catagoryrv = findViewById(R.id.catagoryrl);
        search = findViewById(R.id.searchEt);
        pdfadd = findViewById(R.id.addPdfFab);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //check type user
        checktypeuser();

        //load all categories
        loadcatagory();

        //edit text change listern, search
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type each letter
                try {
                    adapterCatagory.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle click, logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        //handle click, start catagory add screen
        addcatagory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardadminActivity.this,CategoryActivity.class));
                finish();
            }
        });

        //handle click, start pdf add screen
        pdfadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardadminActivity.this,PdfAddActivity.class));
                finish();
            }
        });
    }

    private void loadcatagory() {
        //init arraylist
        modelCatagories = new ArrayList<>();

        //get all categories form firebase > Categories
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist before adding data into it
                modelCatagories.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCatagory modelCatagory = ds.getValue(ModelCatagory.class);

                    //add to arraylist
                    modelCatagories.add(modelCatagory);
                }
                //setup adapter
                adapterCatagory = new AdapterCatagory(DashboardadminActivity.this, modelCatagories);

                //set adapter to recylerview
                catagoryrv.setAdapter(adapterCatagory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checktypeuser() {
        //get current user
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null){
            //not logged in, go to main screen
            startActivity(new Intent(this,MainActivity.class));
            finish();//finish this activity
        }
        else {
            //logged in get user info
            String email1 = firebaseUser.getEmail();
            //set in textview of toolbar
            email.setText(email1);
        }
    }

}
