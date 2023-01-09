package com.example.buku.Filter;

import android.widget.Filter;

import com.example.buku.Adapter.AdapterPdfAdmin;
import com.example.buku.Adapter.AdapterPdfUser;
import com.example.buku.Model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to implemented
    AdapterPdfUser adapterPdfUser;

    //constructor
    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (constraint != null && constraint.length() > 0){
            //change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf>filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count =filteredModels.size();
            results.values = filteredModels;
        }else {
            results.count =filterList.size();
            results.values = filterList;
        }
        return results; //dont miss it
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterPdfUser.modelPdfArrayList = (ArrayList<ModelPdf>)results.values;

        //notify changes
        adapterPdfUser.notifyDataSetChanged();
    }
}
