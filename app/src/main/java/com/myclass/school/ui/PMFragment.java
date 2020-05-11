package com.myclass.school.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Message;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.ChatViewModel;
import com.myclass.school.viewmodels.UserViewModel;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

// screen for private chat with other users
public class PMFragment extends Fragment {


    // global fields used in several places
    private UserViewModel model;
    private View view;
    private User user;
    private ChatViewModel chatViewModel;
    private String userId;

    public PMFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_p_m, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }

    private void init() {
        if (getActivity() == null) return;
        // cannot enter without an argument specifying the user id
        if (getArguments() == null) return;


        // viewModels

        model = ((MainActivity) getActivity()).userVM;
        chatViewModel = ((MainActivity) getActivity()).chatVM;


        // this user id
        final String id = model.getUserId();

        PMFragmentArgs args = PMFragmentArgs.fromBundle(getArguments());

        // other user id
        userId = args.getUserId();

        // other user name
        final String name = args.getUsername();

        // profile picture and name field
        final CircleImageView profilePic = view.findViewById(R.id.profile_pic_chat);

        final AppCompatTextView nameText = view.findViewById(R.id.username_chat);

        nameText.setText(name);


        // chat id
        char[] chatIdArray = (id + userId).toCharArray();
        Arrays.sort(chatIdArray);
        final String chatId = new String(chatIdArray);


        // get this user
        model.getUser().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;
        });

        LiveData<User> userLiveData = model.getUserById(userId);
        // get other user from database!
        userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user == null || !user.getEmail().contains(userId)) return;


            // profile pic
            String photo = user.getPhotoUrl();
            if (photo != null)
                Picasso.get().load(photo).placeholder(R.drawable.ic_person).into(profilePic);

            // online status
            if (user.isOnline())
                CommonUtils.tintDrawableTextView(nameText, R.color.green);
            else
                CommonUtils.tintDrawableTextView(nameText, R.color.red);

            sendMessages(user.getName());

        });


        // refresh messages to load previous messages!
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getMessages(chatId, 100, true);
            refreshLayout.setRefreshing(false);
        });

        // set the 3 dots button action
        setUpSpinnerActions(chatId);

        // view profile
        profilePic.setOnClickListener(v -> viewProfile());
        nameText.setOnClickListener(v -> viewProfile());


        // get 30 messages for this chat
        getMessages(chatId, 30, false);

    }

    // get messages from database, and listen for changes
    private void getMessages(String chatId, int howMany, boolean isRefresh) {
        final RecyclerView rv = view.findViewById(R.id.pm_rv);
        final GroupAdapter adapter = new GroupAdapter();

        final String id = model.getUserId();


        chatViewModel.getChatMessages(chatId, howMany).observe(getViewLifecycleOwner(), messages -> {
            if (messages == null || messages.isEmpty()) return;
            adapter.clear();

            // add messages to list
            for (Message message : messages)
                adapter.add(new MessageItem(message, message.getSender().equals(id)));

            rv.setAdapter(adapter);

            // scroll to bottom if not refresh
            if (!isRefresh)
                rv.scrollToPosition(messages.size() - 1);

        });
    }


    // set the 3 dots button action
    private void setUpSpinnerActions(String chatId) {

        // get spinner and items array
        final Spinner spinner = view.findViewById(R.id.more_actions_spinner);

        final String[] items = getResources().getStringArray(R.array.chat_actions);
        ArrayAdapter adapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);

        spinner.setAdapter(adapter);

        // set actions: delete chat and viewProfile
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // 0 position (cancel) is redundant

                if (position == 1) deleteChatDialog(chatId);

                else if (position == 2) viewProfile();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Required override
            }
        });

    }


    // go to user profile
    private void viewProfile() {
        final NavOptions navOptions = new NavOptions.Builder().
                setPopUpTo(R.id.chatFragment, false).build();
        Navigation.findNavController(view).navigate(
                PMFragmentDirections.viewUserProfile().setUserId(userId), navOptions);
    }


    // show a confirmation dialog to delete chat
    private void deleteChatDialog(String chatId) {
        Runnable deleteAction = () -> {

            chatViewModel.deleteUserChat(chatId);
            Navigation.findNavController(view).navigateUp();
        };

        CommonUtils.showConfirmDialog(view.getContext(), getString(R.string.delete_chat),
                getString(R.string.delete_chat_confirm), deleteAction);

    }


    // hide keyboard if user exits this screen
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommonUtils.hideKeypad(view);
    }

    // send messages to other user
    private void sendMessages(String sendTo) {

        final String id = model.getUserId();

        final TextInputEditText messageText = view.findViewById(R.id.message_text);
        final AppCompatButton sendButton = view.findViewById(R.id.send_message_btn);

        final char[] chatId = (id + userId).toCharArray();
        Arrays.sort(chatId);

        // send message button
        sendButton.setOnClickListener(v -> {
            // validate input
            if (messageText.getText() == null) return;
            String message = messageText.getText().toString().trim();
            if (message.length() == 0) return;


            // create a message object
            final Message msg = new Message(message, id, System.currentTimeMillis());

            // send message to database
            chatViewModel.sendMessage(msg, userId, new String(chatId), user.getName(), sendTo);

            // clear text
            messageText.getText().clear();


        });

    }


    // a view holder that displays a user message
    private static class MessageItem extends Item<GroupieViewHolder> {

        // message object
        private final Message message;

        // decides if this message is sent or received
        private final boolean isSent;


        MessageItem(Message msg, boolean sent) {
            message = msg;
            isSent = sent;

        }


        // message layout
        @Override
        public int getLayout() {
            // determine color and direction of message
            if (isSent)
                return R.layout.message_item_sent;
            else
                return R.layout.message_item_received;

        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {

            final View view = viewHolder.itemView;

            // get text fields
            final AppCompatTextView contentText = view.findViewById(R.id.message_content);
            final AppCompatTextView dateText = view.findViewById(R.id.message_date_text);


            // set message content and date
            contentText.setText(message.getContent());
            dateText.setText(CommonUtils.getTimeAsString(message.getDate()));

            // show $i minutes ago when message click
            final Handler handler = new Handler();
            view.setOnClickListener(v -> {
                dateText.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    if (view.getContext() != null)
                        dateText.setVisibility(View.GONE);
                }, 2000);
            });

        }
    }

}
