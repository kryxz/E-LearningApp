package com.myclass.school.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.DatabaseRepository;
import com.myclass.school.viewmodels.UserViewModel;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    private View view;
    private UserViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    private void init() {
        if (checkUserAuth() == null) return;
        if (getActivity() == null) return;

        model = ((MainActivity) getActivity()).userVM;

        showBottomNavigation();

        CommonUtils.setDrawerIcon(getActivity());


        model.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            setUpDrawer(user);
        });
    }

    private FirebaseUser checkUserAuth() {
        final DatabaseRepository repo = new DatabaseRepository();
        final FirebaseUser user = repo.getUser();

        NavOptions navOptions = new
                NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build();
        NavController navController = Navigation.findNavController(view);

        // no user logged in -> go to login screen
        if (user == null) {
            navController.navigate(MainFragmentDirections.loginNow(), navOptions);
            return null;
        }
        // user logged in and is admin -> admin screen
        else if (user.getEmail() != null && user.getEmail().contains("admin")) {
            navController.navigate(MainFragmentDirections.goToAdmin(), navOptions);
            return null;
        }

        return user;

    }


    private void showBottomNavigation() {

        final MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        final BottomNavigationView bottomNavigation = activity.bottomNavigationView;

        bottomNavigation.setVisibility(View.VISIBLE);

        // show fab button
        CommonUtils.fabVisibility(getActivity(), View.VISIBLE);

    }

    private void setUpDrawer(final User user) {
        final MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        final DrawerLayout drawer = activity.drawer;

        final NavigationView navView = activity.findViewById(R.id.nav_view);
        final View headerView = navView.getHeaderView(0);

        final AppCompatTextView usernameTv = headerView.findViewById(R.id.header_username);
        final AppCompatTextView userType = headerView.findViewById(R.id.header_user_type);


        // set username and user type (Teacher or Student)
        usernameTv.setText(user.getName());
        String type = getString(R.string.teacher);

        if (user.getEmail().charAt(0) == 's')
            type = getString(R.string.student);

        userType.setText(type);


        final CircleImageView profilePic = headerView.findViewById(R.id.header_profile_pic);
        final String photo = user.getPhotoUrl();
        if (photo != null)
            Picasso.get().load(photo).placeholder(R.drawable.ic_person).into(profilePic);



        NavController navController = Navigation.findNavController(view);
        // go to profile when click on name or picture
        headerView.setOnClickListener(v -> {
            navController.navigate(MainFragmentDirections.goToProfile());
            drawer.closeDrawer(GravityCompat.START);
        });


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // open drawer on click
        if (item.getItemId() == android.R.id.home)
            CommonUtils.openDrawer(getActivity());
        return super.onOptionsItemSelected(item);
    }


}