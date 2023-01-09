package com.example.buku.Filter;

import android.widget.Filter;

import com.example.buku.Adapter.AdapterCatagory;
import com.example.buku.Model.ModelCatagory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelCatagory> filterList;
    //adapter in which filter need to implemented
    AdapterCatagory adapterCatagory;

    //constructor
    public FilterCategory(ArrayList<ModelCatagory> filterList, AdapterCatagory adapterCatagory) {
        this.filterList = filterList;
        this.adapterCatagory = adapterCatagory;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (constraint != null && constraint.length() > 0){
            //change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCatagory>filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adapterCatagory.modelCatagories = (ArrayList<ModelCatagory>)results.values;

        //notify changes
        adapterCatagory.notifyDataSetChanged();
    }
}
