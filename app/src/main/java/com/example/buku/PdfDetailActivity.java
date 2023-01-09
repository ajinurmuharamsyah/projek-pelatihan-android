package com.example.buku;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buku.Adapter.AdapterPdfFavorite;
import com.example.buku.Convert.MyApplication;
import com.example.buku.Model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfDetailActivity extends AppCompatActivity {
    private TextView titleTv,categoryTv,dateTv,sizeTv,viewTv,downloadTv,descriptionTv,pageTv;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private Button readBookBtn,downloadBookBtn,favoriteBookBtn;
    private PDFView pdfView;

    boolean isInMyFavorite = false;

    private FirebaseAuth mAuth;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    String bookId, bookTitle, bookUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_detail);

        titleTv = findViewById(R.id.titleTv);
        descriptionTv = findViewById(R.id.descriptionEt);
        categoryTv = findViewById(R.id.categoryTv);
        dateTv = findViewById(R.id.dateTv);
        sizeTv = findViewById(R.id.sizeTv);
        viewTv = findViewById(R.id.viewTv);
        downloadTv = findViewById(R.id.downloadsTv);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.backBtn);
        pdfView = findViewById(R.id.pdfView);
        readBookBtn = findViewById(R.id.readBooksBtn);
        downloadBookBtn = findViewById(R.id.downloadBooksBtn);
        pageTv = findViewById(R.id.pagesTv);
        favoriteBookBtn = findViewById(R.id.favoritesBooksBtn);

        //get bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //at start hide download button, because we need book url that we will load later in function loadBookDetail();
        downloadBookBtn.setVisibility(View.GONE);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        //load book detail
        loadBookDetail();

        //increment book view count, whenever this page
        MyApplication.incrementBookViewCount(bookId);



        //handle backbutton click, go back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, read pdf
        readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent1 = new Intent(PdfDetailActivity.this,PdfViewActivity.class);
                    intent1.putExtra("bookId",bookId);
                    startActivity(intent1);
                }
            }
        });

        //handle click, download pdf
        downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }else {
                    Log.d(TAG_DOWNLOAD, "onClick: Checking Permission");
                    if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                        MyApplication.downloadBook(PdfDetailActivity.this,""+bookId,""+bookTitle,""+bookUrl);
                    }
                    else {
                        Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission...");
                        resultPermisionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
            }
        });

        //handle click, add/remove favorite
        favoriteBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null){
                    //in favorite, remove from firebase
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isInMyFavorite){
                        //in favorite, add to favorite
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    } else {
                        //not in favorite, add to favorite
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    //request storage permission
    private ActivityResultLauncher<String> resultPermisionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    Log.d(TAG_DOWNLOAD, ": Permission Granted");
                    MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
                }else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied...: ");
                    Toast.makeText(this, "Permision was denied...", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetail() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //required data is loaded, show download button
                        downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                categoryTv
                        );

                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                pdfView,
                                progressBar,
                                pageTv
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                sizeTv
                        );

//                        MyApplication.loadPdfPageCount(
//                                PdfDetailActivity.this,
//                                ""+bookUrl,
//                                pageTv
//                        );

                        titleTv.setText(bookTitle);
                        descriptionTv.setText(description);
                        viewTv.setText(viewsCount.replace("null","N/A"));
                        downloadTv.setText(downloadCount.replace("null","N/A"));
                        dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        //logged in check if its in favorite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(mAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite){
                            //exits in favorite
                            favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.ic_favorite_black_24dp,0,0);
                            favoriteBookBtn.setText("Remove Favorite");
                        }
                        else {
                            //not exists in favorite
                            favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.ic_favorite_border_black_24dp,0,0);
                            favoriteBookBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
