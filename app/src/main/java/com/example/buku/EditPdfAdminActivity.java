package com.example.buku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditPdfAdminActivity extends AppCompatActivity {
    private Button update;
    private ImageButton backbtn;
    private EditText titletv,descriptiontv;
    private TextView categorytv;


    //firebase auth
    private FirebaseAuth mAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //category id  get from intent started from AdapterPdfAdmin
    private String bookId;

    //arraylist to hold pdf categories
    private ArrayList<String> modelTitleCatagories,modelIdCategories;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pdf_admin);
        backbtn = findViewById(R.id.backBtn);
        update = findViewById(R.id.submitBtn);
        titletv = findViewById(R.id.titleEt);
        descriptiontv = findViewById(R.id.descriptionEt);
        categorytv = findViewById(R.id.categoryTv);

        bookId = getIntent().getStringExtra("bookId");

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();

        loadCategories();
        loadBookInfo();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, pick category
        categorytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        //handle click, back to page
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        //handle click, update data book
        update.setOnClickListener(new View.OnClickListener() {
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

        Title = titletv.getText().toString().trim();
        Description = descriptiontv.getText().toString().trim();


        //validate
        if (TextUtils.isEmpty(Title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        } else {
            //all date is validate, can update now
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting Update Pdf info to Firebase db");

        //show progress
        progressDialog.setMessage("Updatign Book Info...");
        progressDialog.show();

        //setup data to upload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+Title);
        hashMap.put("description",""+Description);
        hashMap.put("categoryId",""+selectedCategoryId);

        //db reference: DB > Books
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");
        reference.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess: Book Update...");
                        Toast.makeText(EditPdfAdminActivity.this, "Book info updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: Failed to update due to "+e.getMessage());
                        Toast.makeText(EditPdfAdminActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");

        //db reference to load book info
        DatabaseReference refbooks = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");
        refbooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();

                        //set to view
                        titletv.setText(title);
                        descriptiontv.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();

                                        //set to view
                                        categorytv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId="",selectedCategoryTitle="";

    private void categoryDialog(){
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
                        categorytv.setText(selectedCategoryTitle);

                        Log.d(TAG,"onClick: Select Category: "+selectedCategoryId+" "+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: ");

        modelIdCategories = new ArrayList<>();
        modelTitleCatagories = new ArrayList<>();

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

                    Log.d(TAG, "onDataChange: ID: "+categoryId);
                    Log.d(TAG, "onDataChange: category: "+categoryType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditPdfAdminActivity.this,PdfListAdminActivity.class));
        finish();
    }
}
