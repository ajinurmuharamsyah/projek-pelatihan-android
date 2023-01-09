package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.buku.Adapter.AdapterPdfFavorite;
import com.example.buku.Convert.MyApplication;
import com.example.buku.Model.ModelPdf;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameTv,emailTv,accountTypeTv,memberDateTv,favoriteBookCountTv;
    private ImageView profil;
    private ImageButton backBtn,editBtn;
    private RecyclerView favoriteRv;

    private FirebaseAuth mAuth;

    private static final String TAG = "PROFILE_TAG";

    //arraylist to hold the books
    private ArrayList<ModelPdf> pdfArrayList;

    //adapter to set in recyclerview
    private AdapterPdfFavorite adapterPdfFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        accountTypeTv = findViewById(R.id.accountTypeTv);
        memberDateTv = findViewById(R.id.memberDateTv);
        profil = findViewById(R.id.profilTv);
        favoriteBookCountTv = findViewById(R.id.favoritesBooksBtn);
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        favoriteRv = findViewById(R.id.bookRv);

        mAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavoriteBooks();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,EditProfilActivity.class));
                finish();
            }
        });

    }

    private void loadFavoriteBooks() {
        //init list
        pdfArrayList = new ArrayList<>();

        //load favorite books from database
        //user > userId> Favorite
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(mAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //we will only get the bookId here, and we got other details in adapter using that bookId
                            String bookId = ""+ds.child("bookId").getValue();

                            //set id to model
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            //add model to list
                            pdfArrayList.add(modelPdf);
                        }

                        //set number of favorite books
                        favoriteBookCountTv.setText(""+pdfArrayList.size());//can't set int/long to textview so cancatnate with string
                        //setup adapter
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);
                        //set adapter to recyclerview
                        favoriteRv.setAdapter(adapterPdfFavorite);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user"+mAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String email =  ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profil_image = ""+snapshot.child("profil_image").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("usertype").getValue();

                        //format date to dd//MM/yyyy
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        //set data to ui
                        emailTv.setText(email);
                        nameTv.setText(name);
                        memberDateTv.setText(formattedDate);
                        accountTypeTv.setText(userType);

//                        //set Image, using glide
//                        Glide.with(ProfileActivity.this)
//                                .load(profil_image)
//                                .placeholder(R.drawable.ic_person_black_24dp)
//                                .into(profil);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
