package com.myclass.school;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.myclass.school.viewmodels.ChatViewModel;
import com.myclass.school.viewmodels.ClassroomVM;
import com.myclass.school.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;
    public DrawerLayout drawer;


    public UserViewModel userVM;
    public ChatViewModel chatVM;
    public ClassroomVM classroomVM;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // right to left layout!
        // getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(R.layout.activity_main);

        setupNavigation();
        FirebaseApp.initializeApp(this);
        userVM = new ViewModelProvider(this).get(UserViewModel.class);
        chatVM = new ViewModelProvider(this).get(ChatViewModel.class);
        classroomVM = new ViewModelProvider(this).get(ClassroomVM.class);
    }


    // sets up navigation between screens
    private void setupNavigation() {

        bottomNavigationView = findViewById(R.id.bottom_nav);
        // a navController manages navigation between pages.
        navController = Navigation.findNavController(this, R.id.nav_host);


        // title in the app bar will automatically change when user goes to another screen
        NavigationUI.setupActionBarWithNavController(this, navController);


        // navigate with bottom navigation bar
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


        // setup drawer navigation
        NavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
        drawer = findViewById(R.id.drawer_layout);


        final View fab = findViewById(R.id.open_drawer_fab);

        ArrayList<Integer> noFabScreens = new ArrayList<>(
                Arrays.asList(R.id.PMFragment,
                        R.id.viewUsersFragment,
                        R.id.classroomFragment,
                        R.id.createAssignmentsFragment
                )
        );


        // drawer only available in main screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.adminFragment)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            else
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            if (noFabScreens.contains(destination.getId()))
                fab.setVisibility(View.GONE);
            else
                fab.setVisibility(View.VISIBLE);

        });

        fab.setOnClickListener(v -> {
            navController.navigate(R.id.mainFragment);
            // close if open
            if (drawer.isOpen())
                drawer.closeDrawer(GravityCompat.START);
            else
                drawer.openDrawer(GravityCompat.START);
        });

        // logout using fab
        findViewById(R.id.open_drawer_fab).setOnLongClickListener(v -> {
            logout();
            return false;
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

    // todo remove
    private void logout() {

        // code that is executed upon confirmation (when user says yes to logout)
        Runnable logout = () -> {
            userVM.logout();

            CommonUtils.restartApp(this);
        };

        CommonUtils.showConfirmDialog(this, getString(R.string.logout),
                getString(R.string.logout_confirm), logout);

    }


    @Override
    protected void onPause() {
        CommonUtils.setOnline(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        CommonUtils.setOnline(true);
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
