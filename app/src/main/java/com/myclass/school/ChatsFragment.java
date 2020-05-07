package com.myclass.school;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.data.Chat;
import com.myclass.school.data.User;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private View view;
    private UserViewModel model;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }

    private void init() {
        if (getActivity() == null) return;
        model = ((MainActivity) getActivity()).userVM;
        ChatViewModel chatViewModel = ((MainActivity) getActivity()).chatVM;

        final RecyclerView rv = view.findViewById(R.id.chats_rv);
        final GroupAdapter adapter = new GroupAdapter();


        chatViewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            if (chats == null || chats.isEmpty()) return;
            adapter.clear();
            for (Chat chat : chats)
                adapter.add(new ChatItem(chat, chat.getName()));


            rv.setAdapter(adapter);

        });

    }

    private class ChatItem extends Item<GroupieViewHolder> {
        private final Chat chat;

        private final String name;

        ChatItem(Chat c, String n) {
            chat = c;
            name = n;
        }


        @Override
        public int getLayout() {
            return R.layout.chat_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            final AppCompatTextView firstLetterText = view.findViewById(R.id.first_letter_name_item);
            final CircleImageView profilePic = view.findViewById(R.id.profile_pic_chat);

            final AppCompatTextView userNameText = view.findViewById(R.id.chat_item_name);
            final AppCompatTextView lastMessage = view.findViewById(R.id.chat_item_description);
            final AppCompatTextView lastMessageDate = view.findViewById(R.id.chat_item_last);


            if (chat.getLastMessage() != null) {
                lastMessage.setText(chat.getLastMessage().getContent());
                lastMessageDate.setText(Common.getTimeAgo(chat.getLastMessage().getDate()));
            }

            LiveData<User> userLiveData = model.getUserById(chat.getUserId());


            userLiveData.observe(getViewLifecycleOwner(), user -> {
                if (user == null || !user.getEmail().contains(chat.getUserId())) return;
                String photoUrl = user.getPhotoUrl();
                firstLetterText.setText(String.valueOf(user.getName().charAt(0)));
                if (photoUrl != null) {
                    Drawable textBackground = Common.getDrawableFromView(firstLetterText);

                    Picasso.get().load(photoUrl).placeholder(textBackground).into(profilePic);
                    profilePic.setVisibility(View.VISIBLE);
                    firstLetterText.setVisibility(View.GONE);
                } else {
                    profilePic.setVisibility(View.GONE);
                    firstLetterText.setVisibility(View.VISIBLE);

                }

                userNameText.setText(user.getName());
                userLiveData.removeObservers(getViewLifecycleOwner());
            });


            view.setOnClickListener(v -> Navigation.findNavController(view).navigate(
                    ChatsFragmentDirections.goToThisChat(chat.getUserId(), name, chat.getId())));
        }
    }

}
