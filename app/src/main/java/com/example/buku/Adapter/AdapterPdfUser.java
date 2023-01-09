package com.example.buku.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buku.Convert.MyApplication;
import com.example.buku.Filter.FilterPdfAdmin;
import com.example.buku.Filter.FilterPdfUser;
import com.example.buku.Model.ModelPdf;
import com.example.buku.PdfDetailActivity;
import com.example.buku.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    //context
    private Context context;
    //arratlist to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> modelPdfArrayList,filterList;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //instance of our filter class
    private FilterPdfUser filter;

//    //progress
//    private ProgressDialog progressDialog;
    //constructor
    public AdapterPdfUser(Context context, ArrayList<ModelPdf> modelPdfArrayList) {
        this.context = context;
        this.modelPdfArrayList = modelPdfArrayList;
        this.filterList = modelPdfArrayList;

//        //setup progress dialog
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setTitle("Please wait");
//        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.ro_pdf_user,parent,false);
        return new AdapterPdfUser.HolderPdfUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        /*Get Data, set data, handle click*/

        //get data
        ModelPdf modelPdf = modelPdfArrayList.get(position);
        String pdfId = modelPdf.getId();
        String categoryId = modelPdf.getCategoryId();
        String title = modelPdf.getTitle();
        String description = modelPdf.getDescription();
        String pdfUrl = modelPdf.getUrl();
        long timestamp = modelPdf.getTimestamp();

        //we need to convert timestamp to dd/MM/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //we will need these functions many time, to insteady of writing again and again move them to myAplication class and make static to use later
        //load further details like category, pdf from url, pdf size in seprate functions
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        //we don't need page number here, pass null
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        //handle book/pdf click, open pdf detail page, pass pdf/book id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelPdfArrayList.size(); //return list size || number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterList,this);
        }
        return filter;
    }

    public class HolderPdfUser extends RecyclerView.ViewHolder {
        private PDFView pdfView;
        private ProgressBar progressBar;
        private TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
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
