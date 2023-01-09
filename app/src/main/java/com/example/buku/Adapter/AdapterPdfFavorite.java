package com.example.buku.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buku.Convert.MyApplication;
import com.example.buku.Filter.FilterPdfUser;
import com.example.buku.Model.ModelPdf;
import com.example.buku.PdfDetailActivity;
import com.example.buku.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfUser>{

    //context
    private Context context;
    //arratlist to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> modelPdfArrayList,filterList;

    private static final String TAG = "PDF_FAVORITE_TAG";

    //instance of our filter class
//    private FilterPdfUser filter;

//    //progress
//    private ProgressDialog progressDialog;
    //constructor
    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> modelPdfArrayList) {
        this.context = context;
        this.modelPdfArrayList = modelPdfArrayList;
//        this.filterList = modelPdfArrayList;

//        //setup progress dialog
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setTitle("Please wait");
//        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_pdf_favorite,parent,false);
        return new AdapterPdfFavorite.HolderPdfUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        /*Get Data, set data, handle click*/

        //get data
        ModelPdf modelPdf = modelPdfArrayList.get(position);

        loadBooksDetail(modelPdf, holder);

        //handle book/pdf click, open pdf detail page, pass pdf/book id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",modelPdf.getId());//pass book id not category id
                context.startActivity(intent);
            }
        });

        //handle click, remove from favorite
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, modelPdf.getId());//pass book id not category id
            }
        });

    }

    private void loadBooksDetail(ModelPdf modelPdf, HolderPdfUser holder) {
        String bookId = modelPdf.getId();
        Log.d(TAG, "loadBooksDetail: Book Detail of Book ID: "+bookId);

        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");
        reference.child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //GET book info
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                        //set to model
                        modelPdf.setFavorite(true);
                        modelPdf.setTitle(bookTitle);
                        modelPdf.setDescription(description);
                        modelPdf.setCategoryId(categoryId);
                        modelPdf.setUid(uid);
                        modelPdf.setUrl(bookUrl);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                holder.categoryTv
                        );
                        //we don't need page number here, pass null
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                holder.pdfView,
                                holder.progressBar,
                                null
                        );
                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                holder.sizeTv
                        );

                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelPdfArrayList.size(); //return list size || number of records
    }

    public class HolderPdfUser extends RecyclerView.ViewHolder {
        private PDFView pdfView;
        private ImageButton removeBtn;
        private ProgressBar progressBar;
        private TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
            removeBtn = itemView.findViewById(R.id.removeFavoriteBtn);
            pdfView = itemView.findViewById(R.id.pdfView);
            progressBar = itemView.findViewById(R.id.progressBar);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            categoryTv = itemView.findViewById(R.id.categoryTv);
            sizeTv = itemView.findViewById(R.id.sizeTv);
            dateTv = itemView.findViewById(R.id.dateTv);
        }
    }
}
