package com.example.buku.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buku.Convert.MyApplication;
import com.example.buku.EditPdfAdminActivity;
import com.example.buku.Filter.FilterCategory;
import com.example.buku.Filter.FilterPdfAdmin;
import com.example.buku.Model.ModelCatagory;
import com.example.buku.Model.ModelPdf;
import com.example.buku.PdfDetailActivity;
import com.example.buku.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.buku.Constants.Constants.MAX_BYTES_PDF;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    //context
    private Context context;
    //arratlist to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> modelPdfArrayList,filterList;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //instance of our filter class
    private FilterPdfAdmin filter;

    //progress
    private ProgressDialog progressDialog;
    //constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> modelPdfArrayList) {
        this.context = context;
        this.modelPdfArrayList = modelPdfArrayList;
        this.filterList = modelPdfArrayList;

        //setup progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.ro_pdf_admin,parent,false);
        return new AdapterPdfAdmin.HolderPdfAdmin(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
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

        //handle click, show dialog with option 1) Edit, 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(modelPdf, holder);
            }
        });

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

    private void moreOptionsDialog(ModelPdf modelPdf, HolderPdfAdmin holder) {
        String bookId = modelPdf.getId();
        String bookTitle = modelPdf.getTitle();
        String bookUrl = modelPdf.getUrl();
        //options to show in dialog
        String[] options = {"Edit","Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //Edit clicked, Open new activity to edit the book info
                            Intent intent = new Intent(context, EditPdfAdminActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        } else {
                            //Delete clicked
                            MyApplication.deleteBooks(context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                            //deleteBooks(modelPdf,holder);
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return modelPdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    /*View Holder class for row_Pdf_admin.xml*/
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        private PDFView pdfView;
        private ProgressBar progressBar;
        private TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        private ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = itemView.findViewById(R.id.pdfView);
            progressBar = itemView.findViewById(R.id.progressBar);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            categoryTv = itemView.findViewById(R.id.categoryTv);
            sizeTv = itemView.findViewById(R.id.sizeTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            dateTv = itemView.findViewById(R.id.dateTv);
        }
    }
}
