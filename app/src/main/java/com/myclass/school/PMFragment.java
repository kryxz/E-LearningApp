package com.myclass.school;

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
import com.myclass.school.data.Message;
import com.myclass.school.data.User;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;


public class PMFragment extends Fragment {

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
        if (getArguments() == null) return;


        model = ((MainActivity) getActivity()).userVM;
        chatViewModel = ((MainActivity) getActivity()).chatVM;

        final String id = model.getUserId();

        PMFragmentArgs args = PMFragmentArgs.fromBundle(getArguments());

        userId = args.getUserId();

        final String name = args.getUsername();

        final CircleImageView profilePic = view.findViewById(R.id.profile_pic_chat);

        final AppCompatTextView nameText = view.findViewById(R.id.username_chat);


        nameText.setText(name);

        char[] chatIdArray = (id + userId).toCharArray();
        Arrays.sort(chatIdArray);
        final String chatId = new String(chatIdArray);

        model.getUser().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;
        });

        LiveData<User> userLiveData = model.getUserById(userId);
        userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user == null || !user.getEmail().contains(userId)) return;


            // profile pic
            String photo = user.getPhotoUrl();
            if (photo != null)
                Picasso.get().load(photo).placeholder(R.drawable.ic_person).into(profilePic);

            // online status
            if (user.isOnline())
                Common.tintDrawableTextView(nameText, R.color.green);
            else
                Common.tintDrawableTextView(nameText, R.color.red);

            sendMessages(user.getName());

        });


        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getMessages(chatId, 100, true);
            refreshLayout.setRefreshing(false);
        });

        setUpSpinnerActions(chatId);
        profilePic.setOnClickListener(v -> viewProfile());
        nameText.setOnClickListener(v -> viewProfile());

        getMessages(chatId, 30, false);

    }

    private void getMessages(String chatId, int howMany, boolean isRefresh) {
        final RecyclerView rv = view.findViewById(R.id.pm_rv);
        final GroupAdapter adapter = new GroupAdapter();

        final String id = model.getUserId();


        chatViewModel.getChatMessages(chatId, howMany).observe(getViewLifecycleOwner(), messages -> {
            if (messages == null || messages.isEmpty()) return;
            adapter.clear();

            for (Message message : messages)
                adapter.add(new MessageItem(message, message.getSender().equals(id)));

            rv.setAdapter(adapter);
            if (!isRefresh)
                rv.scrollToPosition(messages.size() - 1);

        });
    }


    private void setUpSpinnerActions(String chatId) {
        final Spinner spinner = view.findViewById(R.id.more_actions_spinner);

        final String[] items = getResources().getStringArray(R.array.chat_actions);
        ArrayAdapter adapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // 0 position (cancel) is redundant

                if (position == 1) deleteChatDialog(chatId);

                else if (position == 2) viewProfile();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void viewProfile() {
        final NavOptions navOptions = new NavOptions.Builder().
                setPopUpTo(R.id.chatFragment, false).build();
        Navigation.findNavController(view).navigate(
                PMFragmentDirections.viewUserProfile().setUserId(userId), navOptions);
    }


    private void deleteChatDialog(String chatId) {
        Runnable deleteAction = () -> {

            chatViewModel.deleteUserChat(chatId);
            Navigation.findNavController(view).navigateUp();
        };

        Common.showConfirmDialog(view.getContext(), getString(R.string.delete_chat),
                getString(R.string.delete_chat_confirm), deleteAction);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Common.hideKeypad(view);
    }

    private void sendMessages(String sendTo) {

        final String id = model.getUserId();

        final TextInputEditText messageText = view.findViewById(R.id.message_text);
        final AppCompatButton sendButton = view.findViewById(R.id.send_message_btn);

        final char[] chatId = (id + userId).toCharArray();
        Arrays.sort(chatId);

        // send message button
        sendButton.setOnClickListener(v -> {
            if (messageText.getText() == null) return;
            String message = messageText.getText().toString().trim();
            if (message.length() == 0) return;

            final Message msg = new Message();

            msg.setDate(System.currentTimeMillis());
            msg.setContent(message);
            msg.setSender(id);

            messageText.getText().clear();

            chatViewModel.sendMessage(msg, userId, new String(chatId), user.getName(), sendTo);

        });

    }


    private static class MessageItem extends Item<GroupieViewHolder> {
        private final Message message;
        private final boolean isSent;


        MessageItem(Message msg, boolean sent) {
            message = msg;
            isSent = sent;

        }


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
            final AppCompatTextView contentText = view.findViewById(R.id.message_content);
            final AppCompatTextView dateText = view.findViewById(R.id.message_date_text);


            // set message content and date
            contentText.setText(message.getContent());
            dateText.setText(Common.getTimeAsString(message.getDate()));

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
