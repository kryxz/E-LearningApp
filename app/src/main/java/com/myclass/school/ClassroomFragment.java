package com.myclass.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.data.ClassroomPost;
import com.myclass.school.data.User;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class ClassroomFragment extends Fragment {

    private View view;
    private UserViewModel model;
    private ClassroomVM classroomVM;
    private User user;
    private String classroomId;

    public ClassroomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_classroom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    private void init() {
        if (getActivity() == null) return;
        if (getArguments() == null) return;
        model = ((MainActivity) getActivity()).userVM;
        classroomVM = ((MainActivity) getActivity()).classroomVM;

        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();

        classroomVM.getClassById(classroomId).observe(getViewLifecycleOwner(), classroom -> {
            if (classroom == null || !classroom.getId().equals(classroomId)) return;
            Common.updateTitle(getActivity(), classroom.getName());

        });

        LiveData<User> userLiveData = model.getUser();
        userLiveData.observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;
            postToClass();
            userLiveData.removeObservers(this);
        });
        tabLayoutListener();

        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getPosts(50, true);
            refreshLayout.setRefreshing(false);
        });

        getPosts(20, false);

    }

    private void getPosts(final int howMany, final boolean isRefresh) {

        final RecyclerView rv = view.findViewById(R.id.posts_rv);

        final GroupAdapter adapter = new GroupAdapter();
        final AppCompatTextView noPostsText = view.findViewById(R.id.no_posts_yet);

        classroomVM.getClassPosts(classroomId, howMany).observe(getViewLifecycleOwner(), classroomPosts -> {

            if (classroomPosts == null || classroomPosts.isEmpty()) {
                noPostsText.setVisibility(View.VISIBLE);
                return;
            }

            noPostsText.setVisibility(View.GONE);
            adapter.clear();

            for (ClassroomPost post : classroomPosts)
                adapter.add(new PostItem(post));


            rv.setAdapter(adapter);
            if (!isRefresh)
                rv.scrollToPosition(classroomPosts.size() - 1);
        });


    }


    private void tabLayoutListener() {

        final TabLayout tabLayout = view.findViewById(R.id.tabs_layout);
        final TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null)
            tab.select();

        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.classesFragment, false).build();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1)
                    Navigation.findNavController(view).
                            navigate(ClassroomFragmentDirections.goToFiles(classroomId), navOptions);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void postToClass() {
        TextInputEditText messageText = view.findViewById(R.id.message_text);
        AppCompatButton sendButton = view.findViewById(R.id.send_message_btn);


        messageText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                sendButton.setVisibility(View.VISIBLE);

        });

        // send message button
        sendButton.setOnClickListener(v -> {
            if (messageText.getText() == null) return;
            String message = messageText.getText().toString().trim();
            if (message.length() == 0) return;

            ClassroomPost post = new ClassroomPost();


            post.setDate(System.currentTimeMillis());
            post.setContent(message);
            post.setSenderId(user.getEmail());
            post.setAuthor(user.getName());
            post.setId(UUID.randomUUID().toString().substring(0, 10));

            classroomVM.postToClassroom(classroomId, post);
            messageText.getText().clear();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.classroom_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.assignments)
            goToAssignments();
        return super.onOptionsItemSelected(item);
    }


    private void goToAssignments() {

        final NavController controller = Navigation.findNavController(view);
        if (model.getUserId().charAt(0) == 't')
            controller.navigate(ClassroomFragmentDirections.viewAssignments(classroomId));
        else
            controller.navigate(ClassroomFragmentDirections.studentAssignments(classroomId));


    }

    private class PostItem extends Item<GroupieViewHolder> {

        final private ClassroomPost post;

        PostItem(ClassroomPost p) {
            post = p;
        }


        @Override
        public int getLayout() {
            return R.layout.classroom_post_item;
        }

        private void goToProfile(View view) {
            Navigation.findNavController(view).
                    navigate(ClassroomFragmentDirections.viewUserProfile().setUserId(post.getSenderId()));
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            final AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);
            final AppCompatTextView agoText = view.findViewById(R.id.post_item_date);
            final AppCompatTextView authorText = view.findViewById(R.id.post_item_author);
            final AppCompatTextView contentText = view.findViewById(R.id.post_item_content);
            final CircleImageView profilePic = view.findViewById(R.id.profile_pic_post_item);


            // set first letter
            final String senderName = post.getAuthor();
            final String firstLetterName = String.valueOf(senderName.charAt(0));
            firstLetter.setText(firstLetterName);

            // set random background color
            firstLetter.getBackground().setTint(Common.getRandomColor(view.getContext(), senderName.length()));


            model.getPhotoUrl(post.getSenderId()).observe(getViewLifecycleOwner(), url -> {
                if (url == null) {
                    firstLetter.setVisibility(View.VISIBLE);
                    profilePic.setVisibility(View.GONE);
                    firstLetter.setOnClickListener(v -> goToProfile(view));

                } else {
                    Picasso.get().load(url).placeholder(R.drawable.ic_person).into(profilePic);
                    firstLetter.setVisibility(View.GONE);
                    profilePic.setVisibility(View.VISIBLE);
                    profilePic.setOnClickListener(v -> goToProfile(view));

                }

            });

            // set time ago
            agoText.setText(Common.getTimeAsString(post.getDate()));
            // set author and content
            authorText.setText(senderName);
            contentText.setText(post.getContent());


            /*
            get new name for sender
            if a user changes their name, new name should be in old posts too
             */
            LiveData<String> userNameLiveData =
                    model.getNameById(post.getSenderId());
            userNameLiveData.observe(getViewLifecycleOwner(), name -> {
                if (name == null) return;
                authorText.setText(senderName);
                userNameLiveData.removeObservers(getViewLifecycleOwner());

            });


        }
    }


}
