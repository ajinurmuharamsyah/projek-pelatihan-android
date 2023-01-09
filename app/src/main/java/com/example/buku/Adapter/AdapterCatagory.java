package com.example.buku.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buku.Filter.FilterCategory;
import com.example.buku.Model.ModelCatagory;
import com.example.buku.PdfListAdminActivity;
import com.example.buku.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCatagory extends RecyclerView.Adapter<AdapterCatagory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCatagory> modelCatagories,filterList;

    //instance of our filter class
    private FilterCategory filter;

    public AdapterCatagory(Context context, ArrayList<ModelCatagory> modelCatagories) {
        this.context = context;
        this.modelCatagories = modelCatagories;
        this.filterList = modelCatagories;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.catagory_list,parent,false);
        return new AdapterCatagory.HolderCategory(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        final ModelCatagory modelCatagory = modelCatagories.get(position);
        String Id = modelCatagory.getId();
        String Uid = modelCatagory.getUid();
        String category = modelCatagory.getCategory();
        long timestamp = modelCatagory.getTimestamp();

        //set data
        holder.catagoryTv.setText(category);

        //handle click, delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //begin delete
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCategory(modelCatagory,holder);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        //handle item click, goto PdfListAdminActivity, also pass pdf category and categoryId
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", Id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCatagory modelCatagory, HolderCategory holder) {
        //get id of category to delete
        String id = modelCatagory.getId();
        //Firebase DB > Categories > categoryId
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Delete successfully
                        Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failde to delete
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelCatagories.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCategory(filterList,this);
        }
        return filter;
    }

    /*View holder class to hold UI views for category_list.xml*/
    public class HolderCategory extends RecyclerView.ViewHolder {
        //ui view category_list.xml
        TextView catagoryTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            //init ui view
            catagoryTv = itemView.findViewById(R.id.titlecatagory);
            deleteBtn = itemView.findViewById(R.id.hapusBtn);

        }
    }
}
