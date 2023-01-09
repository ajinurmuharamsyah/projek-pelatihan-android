package com.example.buku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.buku.Model.ModelCatagory;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboarduserActivity extends AppCompatActivity {

    private TextView email;
    private ImageButton logout,profil;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //to show in tab
    public ArrayList<ModelCatagory> catagoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    //firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboarduser);

        email = findViewById(R.id.subtitleTv);
        profil = findViewById(R.id.profilBtn);
        logout = findViewById(R.id.logoutBtn);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        //init firebase auth
        mAuth = FirebaseAuth.getInstance();
        
        //check type user
        checktypeuser();

        setupViewPagerAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        //handle click, logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(DashboarduserActivity.this,MainActivity.class));
                finish();
            }
        });
        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboarduserActivity.this,ProfileActivity.class));
                finish();
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);

        catagoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance("https://library-app-c3d4d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear before adding to list
                catagoryArrayList.clear();

                /*Load Categories - static e.g. All, Most Viewed, Most Downloaded*/
                //add data to model
                ModelCatagory modelAll = new ModelCatagory("01","All","",1);
                ModelCatagory modelMostViewed = new ModelCatagory("02","Most Viewed","",1);
                ModelCatagory modelMostDownloaded = new ModelCatagory("03","Most Downloaded","",1);
                //add models to list
                catagoryArrayList.add(modelAll);
                catagoryArrayList.add(modelMostViewed);
                catagoryArrayList.add(modelMostDownloaded);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCategory(),
                        ""+modelAll.getUid()
                ),modelAll.getCategory());

                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUid()
                ),modelMostViewed.getCategory());

                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelMostDownloaded.getId(),
                        ""+modelMostDownloaded.getCategory(),
                        ""+modelMostDownloaded.getUid()
                ),modelMostDownloaded.getCategory());

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //Load from firebase
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCatagory model = ds.getValue(ModelCatagory.class);
                    //add data to list
                    catagoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getCategory(),
                            ""+model.getUid()
                    ),model.getCategory());
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private final Context context;
        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BooksUserFragment fragment, String title){
            //add fragment passed as parameter in fragmentlist
            fragmentList.add(fragment);
            //add title passed as parameter in fragmenttitlelist
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checktypeuser() {
        //get current user
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null){
            //not logged in
            email.setText("Not Logged In");
        }
        else {
            //logged in get user info
            String email1 = firebaseUser.getEmail();
            //set in textview of toolbar
            email.setText(email1);
        }
    }
}
