package com.myclass.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

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
    private User user;
    private String classroomId;

    public ClassroomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        model = ((MainActivity) getActivity()).model;

        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();

        model.getClassById(classroomId).observe(getViewLifecycleOwner(), classroom -> {
            if (classroom == null || !classroom.getId().equals(classroomId)) return;
            Common.updateTitle(getActivity(), classroom.getName());

        });

        model.getUser().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;

            postToClass();
            model.getUser().removeObservers(this);
        });
        tabLayoutListener();
        getPosts();

    }

    private void getPosts() {

        final RecyclerView rv = view.findViewById(R.id.posts_rv);
        final GroupAdapter adapter = new GroupAdapter();

        model.getClassPosts(classroomId).observe(getViewLifecycleOwner(), classroomPosts -> {
            if (classroomPosts == null || classroomPosts.isEmpty()) return;
            adapter.clear();

            for (ClassroomPost post : classroomPosts)
                adapter.add(new PostItem(post));

            rv.setAdapter(adapter);
            rv.scrollToPosition(classroomPosts.size() - 1);
        });


    }


    private void tabLayoutListener() {

        final TabLayout tabLayout = view.findViewById(R.id.tabs_layout);
        final TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null)
            tab.select();

        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.mainFragment, false).build();

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
            post.setPhotoUrl(user.getPhotoUrl());

            model.postToClassroom(classroomId, post);
            messageText.getText().clear();
        });
    }


    private static class PostItem extends Item<GroupieViewHolder> {

        final private ClassroomPost post;

        PostItem(ClassroomPost p) {
            post = p;
        }


        @Override
        public int getLayout() {
            return R.layout.classroom_post_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            View view = viewHolder.itemView;

            AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);
            AppCompatTextView agoText = view.findViewById(R.id.post_item_date);
            AppCompatTextView authorText = view.findViewById(R.id.post_item_author);
            AppCompatTextView contentText = view.findViewById(R.id.post_item_content);
            CircleImageView profilePic = view.findViewById(R.id.profile_pic_post_item);


            if (post.getPhotoUrl() == null) {
                firstLetter.setVisibility(View.VISIBLE);
                profilePic.setVisibility(View.GONE);

            } else {
                firstLetter.setVisibility(View.GONE);
                profilePic.setVisibility(View.VISIBLE);

                Picasso.get().load(post.getPhotoUrl()).into(profilePic);

            }

            // set first letter
            final String firstLetterName = String.valueOf(post.getAuthor().charAt(0));
            firstLetter.setText(firstLetterName);

            // set random background color
            firstLetter.getBackground().setTint(Common.getRandomColor(view.getContext(), post.getAuthor().length()));

            // set time ago
            agoText.setText(Common.getTimeAgo(post.getDate()));

            // set author and content
            authorText.setText(post.getAuthor());
            contentText.setText(post.getContent());


        }
    }


}
