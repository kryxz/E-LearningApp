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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Notification;
import com.myclass.school.data.NotificationType;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.DatabaseRepository;
import com.myclass.school.viewmodels.UserViewModel;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    private View view;

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

        UserViewModel model = ((MainActivity) getActivity()).userVM;

        showBottomNavigation();

        CommonUtils.setDrawerIcon(getActivity());


        model.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            setUpDrawer(user);
        });
        final AppCompatTextView noNotificationsText = view.findViewById(R.id.no_notifications);


        // Recycler view for the list of messages
        final RecyclerView rv = view.findViewById(R.id.notifications_rv);

        final GroupAdapter adapter = new GroupAdapter();


        model.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null) return;
            if (notifications.isEmpty()) {
                noNotificationsText.setVisibility(View.VISIBLE);
                return;
            }
            noNotificationsText.setVisibility(View.GONE);

            adapter.clear();

            for (Notification notification : notifications)
                adapter.add(new NotificationItem(notification));

            rv.setAdapter(adapter);


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


    private static class NotificationItem extends Item<GroupieViewHolder> {
        private final Notification notification;


        NotificationItem(Notification n) {
            notification = n;
        }


        @Override
        public int getLayout() {
            return R.layout.notification_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            final CircleImageView iconView = view.findViewById(R.id.icon_pic);
            final AppCompatTextView titleText = view.findViewById(R.id.notification_item_title);
            final AppCompatTextView messageText = view.findViewById(R.id.notification_item_description);
            final AppCompatTextView dateText = view.findViewById(R.id.notification_item_date);


            titleText.setText(notification.getTitle());
            messageText.setText(notification.getMessage());
            dateText.setText(CommonUtils.getTimeAsString(notification.getDate()));

            int icon = R.drawable.ic_assignment;
            if (notification.getType() == NotificationType.NEW_FILE)
                icon = R.drawable.ic_file;
            else if (notification.getType() == NotificationType.MENTION)
                icon = R.drawable.ic_person;

            iconView.setImageDrawable(CommonUtils.tintDrawable(view.getContext(), icon,
                    CommonUtils.getRandomColor(view.getContext(), position)));

            view.setOnClickListener(v -> {
                NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.mainFragment, false).build();
                Navigation.findNavController(view).navigate(MainFragmentDirections.goToClassroom(notification.getClassroomId()), navOptions);

            });
        }
    }


}
