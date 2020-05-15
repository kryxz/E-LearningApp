package com.myclass.school.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.myclass.school.data.Chat;
import com.myclass.school.data.Message;

import java.util.ArrayList;
import java.util.List;


// a viewModel that controls chat data, contains methods for getting chats, deleting chats, and sending messages
public class ChatViewModel extends ViewModel {

    private final DatabaseRepository repo = new DatabaseRepository();

    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>();


    // updates or adds chat data to user
    private void addChatToUser(String id, Chat chat, String name, String otherId) {


        // reference in database
        CollectionReference ref = repo.getUserRefById(id).collection("chats");


        // set name and the other user id
        chat.setUserId(otherId);
        chat.setName(name);
        // update document!
        ref.document(chat.getId()).set(chat);

    }


    // returns user id
    private String getUserId() {
        // get email from auth
        final String email = repo.getUser().getEmail();
        if (email == null) return null;

        return email.substring(0, email.indexOf('@'));

    }

    // removes chat for user
    public void deleteUserChat(String chatId) {
        final String id = getUserId();
        if (id == null) return;


        CollectionReference ref = repo.getUserRefById(id).collection("chats");


        // delete chat
        ref.document(chatId).delete();

    }

    // send message to a chat!
    public void sendMessage(Message msg, String who, String chatId, String username, String otherName) {
        String id = getUserId();
        if (id == null) return;

        // add message to chat
        repo.getChatMessagesRef(chatId).add(msg);

        // chat object with last message and chat Id
        Chat chat = new Chat(chatId, msg);


        // update chat for the two users!
        addChatToUser(id, chat, otherName, who);
        addChatToUser(who, chat, username, id);


    }

    // get a user chats
    public LiveData<List<Chat>> getChats() {
        final String id = getUserId();
        if (id == null) return null;


        CollectionReference ref = repo.getUserRefById(id).collection("chats");


        // array to contains all chats
        ArrayList<Chat> allChats = new ArrayList<>();


        // query database
        ref.addSnapshotListener((query, e) -> {
            if (query == null || e != null) return;
            allChats.clear();

            // add all chats to array
            for (DocumentSnapshot snapshot : query.getDocuments())
                allChats.add(snapshot.toObject(Chat.class));

            // update live data
            chats.setValue(allChats);

        });

        return chats;
    }

    /*
         get some messages sorted by date!
         parameter *howMany*  decides how many messages to  query
     */
    public LiveData<List<Message>> getChatMessages(String chatId, int howMany) {

        // init live data and an array for messages
        final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
        final ArrayList<Message> allMessages = new ArrayList<>();

        repo.getChatMessagesRef(chatId).orderBy("date", Query.Direction.ASCENDING).limitToLast(howMany)
                .addSnapshotListener((query, e) -> {
                    if (query == null || e != null) return;
                    // add messages to array
                    for (final DocumentSnapshot snapshot : query.getDocuments()) {
                        final Message msg = snapshot.toObject(Message.class);
                        if (!allMessages.contains(msg))
                            allMessages.add(msg);
                    }
                    // update live data
                    messages.setValue(allMessages);

                });

        return messages;

    }

}
