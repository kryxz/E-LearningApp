package com.myclass.school;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    DrawerLayout drawer;


    UserViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // right to left layout!
        // getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(R.layout.activity_main);

        setupNavigation();
        model = new ViewModelProvider(this).get(UserViewModel.class);

    }


    // sets up navigation between screens
    private void setupNavigation() {

        bottomNavigationView = findViewById(R.id.bottom_nav);
        // a navController manages navigation between pages.
        NavController navController = Navigation.findNavController(this, R.id.nav_host);


        // title in the app bar will automatically change when user goes to another screen
        NavigationUI.setupActionBarWithNavController(this, navController);


        // navigate with bottom navigation bar
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


        // setup drawer navigation
        NavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
        drawer = findViewById(R.id.drawer_layout);


        // drawer only available in main screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.adminFragment)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            else
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);


        });


        // fragments that has no back button
        int[] noBackButtonSet = new int[]{R.id.mainFragment, R.id.loginFragment,
                R.id.adminFragment, R.id.chatFragment,
                R.id.assignmentsFragment, R.id.classesFragment};

        AppBarConfiguration appBarConfiguration
                = new AppBarConfiguration.Builder(noBackButtonSet).build();

        // hides the back button if the user is either in main or login screen.
        NavigationUI.setupActionBarWithNavController(this,
                navController, appBarConfiguration);


    }

    @Override
    protected void onPause() {
        Common.setOnline(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Common.setOnline(true);
        super.onResume();
    }

    /*
    this method is called whenever the user goes back in the app!
    The navController decides where the user should go
     */

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host).navigateUp()
                || super.onSupportNavigateUp();
    }


}
