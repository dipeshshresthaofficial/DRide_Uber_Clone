package com.example.uber_taxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar1;

    TextView name,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

//        name = (TextView) findViewById(R.id.name1);
//        name.setText(MainActivity.name);
//
//        email = (TextView) findViewById(R.id.email1);
//        email.setText(MainActivity.email);

        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.draw_layout);

        //getSupportActionBar().hide();//Ocultar ActivityBar anterior

        toolbar1 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar1); //NO PROBLEM !!!!

        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar1,
                R.string.navigation_draw_open,R.string.navigation_draw_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //setting the nfavigation view listener
        navigationView.setNavigationItemSelectedListener(this);


        if(savedInstanceState == null) {
            //opening profile fragment immidetly at the start of program
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_profile);
        }


    }


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    //if item is pressed in the navigation view
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {//if item is selected in the menu

        switch (item.getItemId()){
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                break;
            case R.id.nav_help:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HelpFragment()).commit();
                break;
//            case R.id.nav_send:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new InformationFragment()).commit();
//                break;
            case R.id.nav_logout:
                //logging out of the app
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(Menu.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
                break;


        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
