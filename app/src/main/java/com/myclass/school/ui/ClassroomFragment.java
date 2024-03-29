package com.myclass.school.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
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
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomPost;
import com.myclass.school.data.Notification;
import com.myclass.school.data.NotificationType;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.ClassroomVM;
import com.myclass.school.viewmodels.UserViewModel;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


/*
    a screen that allows users to interact with a classroom
    users can send messages, and visit other members profiles!
 */
public class ClassroomFragment extends Fragment {

    // global fields used in several places
    private View view;
    private UserViewModel model;
    private ClassroomVM classroomVM;
    private User user;
    private String classroomId;
    private Classroom classroom;

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


    /*
    initializes class fields
    gets user data from database
    get classroom messages from database
     */
    private void init() {
        if (getActivity() == null) return;
        // cannot enter without an argument!
        if (getArguments() == null) return;

        // viewModels
        model = ((MainActivity) getActivity()).userVM;
        classroomVM = ((MainActivity) getActivity()).classroomVM;


        // current classroom id
        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();

        // tab layout allows user to go to files page
        tabLayoutListener();


        // get classroom object from database to update title
        classroomVM.getClassById(classroomId).observe(getViewLifecycleOwner(), classroom -> {
            if (classroom == null || !classroom.getId().equals(classroomId)) return;
            CommonUtils.updateTitle(getActivity(), classroom.getName());
            this.classroom = classroom;
            if (!classroom.getMembers().contains(model.getUserId())) {
                Navigation.findNavController(view).navigateUp();
            }
            mentionUser();
        });
        // get user data from database
        LiveData<User> userLiveData = model.getUser();
        userLiveData.observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;
            postToClass();
            // don't observe data changes for user! (no actual need for that)
            userLiveData.removeObservers(this);
        });


        // get more messages if user swipes up!
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getPosts(50, true);
            refreshLayout.setRefreshing(false);
        });

        // get classroom messages
        getPosts(20, false);

    }


    /*
        gets a specific amount of messages,
        and decides if recycler view should scroll down or stay top
     */
    private void getPosts(final int howMany, final boolean isRefresh) {

        // Recycler view for the list of messages
        final RecyclerView rv = view.findViewById(R.id.posts_rv);

        final GroupAdapter adapter = new GroupAdapter();

        // Shows a hint for user if no posts found!
        final AppCompatTextView noPostsText = view.findViewById(R.id.no_posts_yet);

        // Get classroom posts from database!
        classroomVM.getClassPosts(classroomId, howMany).observe(getViewLifecycleOwner(), classroomPosts -> {

            // Show hint if no posts found
            if (classroomPosts == null || classroomPosts.isEmpty()) {
                noPostsText.setVisibility(View.VISIBLE);
                return;
            }

            noPostsText.setVisibility(View.GONE);
            adapter.clear();

            // Add posts to the list
            for (ClassroomPost post : classroomPosts)
                adapter.add(new PostItem(post));


            rv.setAdapter(adapter);
            // Scroll to bottom if no refresh!
            if (!isRefresh)
                rv.scrollToPosition(classroomPosts.size() - 1);
        });


    }


    // A tabLayout to navigate between the classroom screen and files screen
    private void tabLayoutListener() {

        final TabLayout tabLayout = view.findViewById(R.id.tabs_layout);

        // Select classroom tab
        final TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null)
            tab.select();


        /*
            Navigation options to easily navigate between
            the two screens without messing up the navigation stack
         */
        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.classesFragment, false).build();

        // Send user to files fragment when click on files tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1)
                    Navigation.findNavController(view).
                            navigate(ClassroomFragmentDirections.goToFiles(classroomId), navOptions);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Required override
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Required override
            }
        });

    }


    private void mentionUser() {

        final List<String> names = new ArrayList<>();

        final List<String> membersIds = classroom.getMembers();
        final HashMap<String, String> nameId = new HashMap<>();
        final AppCompatAutoCompleteTextView messageText = view.findViewById(R.id.message_text);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),
                R.layout.select_mention_message, names);
        messageText.setAdapter(adapter);

        messageText.setThreshold(1);
        for (String id : membersIds) {
            LiveData<String> nameLiveData = model.getNameById(id);
            nameLiveData.observe(getViewLifecycleOwner(), s -> {
                if (s == null || s.equals(user.getName())) return;
                names.add(s);
                nameId.put(s, id);
                nameLiveData.removeObservers(getViewLifecycleOwner());
            });

        }
        messageText.setOnItemClickListener((parent, view1, position, id) -> {
            CommonUtils.Temp.isMention = true;
            CommonUtils.Temp.mentionWho = nameId.get(messageText.getText().toString().trim());

        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) return;
                final String text = s.toString();
                if (StringUtils.indexOfAny(text, names.toArray(new String[0])) == -1) {
                    CommonUtils.Temp.isMention = false;
                    CommonUtils.Temp.mentionWho = null;
                }


            }
        };
        messageText.addTextChangedListener(textWatcher);

    }


    //  enables user to send messages to the classroom
    private void postToClass() {
        final AppCompatAutoCompleteTextView messageText = view.findViewById(R.id.message_text);
        final AppCompatButton sendButton = view.findViewById(R.id.send_message_btn);


        // show send button when click on text field
        messageText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                sendButton.setVisibility(View.VISIBLE);

        });


        // send message button
        sendButton.setOnClickListener(v -> {
            // validate input
            if (messageText.getText() == null) return;
            String message = messageText.getText().toString().trim();
            if (message.length() == 0) return;

            final String randomId = UUID.randomUUID().toString().substring(0, 10);


            // a classroom message object
            final ClassroomPost post = new ClassroomPost(
                    randomId, // identifier for this message
                    model.getUserId(), // identifier who sent it
                    message, // actual message content
                    user.getName(), // username of sender
                    System.currentTimeMillis() // sent at date
            );


            final Notification notification = new Notification(
                    getString(R.string.new_mention_title),
                    getString(R.string.new_mention, classroom.getName()),
                    System.currentTimeMillis(),
                    classroomId,
                    NotificationType.MENTION
            );
            // Send to database
            // Notification is only sent if post contains a mention to a user
            classroomVM.postToClassroom(classroomId, post, notification);
            // Clear text field
            messageText.getText().clear();
        });
    }


    // options menu in action bar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (model.getUserId().charAt(0) == 't')
            inflater.inflate(R.menu.classroom_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.assignments)
            goToAssignments(); // go to assignments when click
        return super.onOptionsItemSelected(item);
    }


    private void goToAssignments() {

        // A navController manages navigation
        final NavController controller = Navigation.findNavController(view);

        controller.navigate(ClassroomFragmentDirections.viewAssignments(classroomId));

    }

    // a view holder for classroom messages
    private class PostItem extends Item<GroupieViewHolder> {

        final private ClassroomPost post;

        PostItem(ClassroomPost p) {
            post = p;
        }


        // message layout
        @Override
        public int getLayout() {
            return R.layout.classroom_post_item;
        }


        // send user to message sender profile!
        private void goToProfile(View view) {
            Navigation.findNavController(view).
                    navigate(ClassroomFragmentDirections.viewUserProfile().setUserId(post.getSenderId()));
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            // get fields
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
            firstLetter.getBackground().setTint(CommonUtils.getRandomColor(view.getContext(), senderName.length()));


            // Get profile picture from database, and set first letter of name if no picture found!
            LiveData<String> stringLiveData = model.getPhotoUrl(post.getSenderId());
            stringLiveData.observe(getViewLifecycleOwner(), url -> {
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
                stringLiveData.removeObservers(getViewLifecycleOwner());

            });

            // set time ago
            agoText.setText(CommonUtils.getTimeAsString(post.getDate()));
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
