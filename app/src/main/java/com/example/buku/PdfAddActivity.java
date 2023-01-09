package com.example.buku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buku.Model.ModelCatagory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private Button upload;
    private ImageButton lampir,backbtn;
    private EditText title,description;
    private TextView category;

    //firebase auth
    private FirebaseAuth mAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //arraylist to hold pdf categories
    private ArrayList<String> modelTitleCatagories,modelIdCategories;

    //uri of picked pdf
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_add);

        backbtn = findViewById(R.id.backBtn);
        lampir = findViewById(R.id.attachBtn);
        upload = findViewById(R.id.submitBtn);
        title = findViewById(R.id.titleEt);
        description = findViewById(R.id.descriptionEt);
        category = findViewById(R.id.categoryTv);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadPdfCategories();

        //handle click, back to page
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, attach pdf
        lampir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        //handle click, pick category
        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String Title,Description;

    private void validateData() {
        //step 1: validate data
        Log.d(TAG,"validateData: Validating Data...");

        Title = title.getText().toString().trim();
        Description = description.getText().toString().trim();


        //validate
        if (TextUtils.isEmpty(Title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }else if (pdfUri==null){
            Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show();
        }else {
            //all date is validate, can upload now
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        //step 2: Upload Pdf to Firebase storage
        Log.d(TAG, "UploadPDFToStorge: Uploading to storage");

        //show progress
        progressDialog.setMessage("Uploading PDF...");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //path of pdf in firebase
        String filePathAndName = "Books/"+ timestamp;

        //storage
        StorageReference storageReference = FirebaseStorage.getInstance("gs://library-app-c3d4d.appspot.com").getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedpdfuri = ""+uriTask.getResult();

                        //upload to firebase db
                        uploadedPdfInfoToDb(uploadedpdfuri,timestamp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: PDF upload failed due to "+e.getMessage());
                Toast.makeText(PdfAddActivity.this, "PDF upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void uploadedPdfInfoToDb(String uploadedpdfuri, long timestamp) {
        //step 3: Upload Pdf info to Firebase db
        Log.d(TAG, "uploadedPdfInfoToDb: Uploading Pdf info to Firebase db");

        //show progress
        progressDialog.setMessage("Uploading PDF...");

        String uid = mAuth.getUid();

        //setup data to upload, also add view count, download count while adding pdf/book
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+Title);
        hashMap.put("description",""+Description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedpdfuri);
        hashMap.put("timestamp",timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadCount", 0);

        //db reference: DB > Books
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess: Successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploading...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf categories");
        modelTitleCatagories = new ArrayList<>();
        modelIdCategories = new ArrayList<>();

        //db reference to load categories... db > categories
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelTitleCatagories.clear();//clear before adding data
                modelIdCategories.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get id and title of category
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryType = ""+ ds.child("category").getValue();

                    //add to respective arraylist
                    modelIdCategories.add(categoryId);
                    modelTitleCatagories.add(categoryType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //selected category id and catagory title
    private String selectedCategoryId, selectedCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: showing category pick dialog");

        //get string array of categories from arraylist
        String[] categoryArray = new String[modelTitleCatagories.size()];
        for (int i = 0; i< modelTitleCatagories.size(); i++){
            categoryArray[i] = modelTitleCatagories.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        //get clicked item from list
                        selectedCategoryTitle = modelTitleCatagories.get(which);
                        selectedCategoryId = modelIdCategories.get(which);
                        //set to category textview
                        category.setText(selectedCategoryTitle);

                        Log.d(TAG,"onClick: Select Category: "+selectedCategoryId+" "+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI:" + pdfUri);
            }
        }
        else{
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PdfAddActivity.this,DashboardadminActivity.class));
        finish();
    }
}
