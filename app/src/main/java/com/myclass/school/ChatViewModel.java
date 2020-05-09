package com.myclass.school;

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


    private void addChatToUser(String id, Chat chat, String name, String otherId) {

        final boolean isTeacher = id.charAt(0) == 't';
        CollectionReference ref = repo.getStudentsRef().document(id).collection("chats");
        if (isTeacher)
            ref = repo.getTeachersRef().document(id).collection("chats");

        chat.setUserId(otherId);
        chat.setName(name);
        ref.document(chat.getId()).set(chat);

    }


    private String getUserId() {
        final String email = repo.getUser().getEmail();
        if (email == null) return null;

        return email.substring(0, email.indexOf('@'));

    }

    void deleteUserChat(String chatId) {
        final String id = getUserId();
        if (id == null) return;
        final boolean isTeacher = id.charAt(0) == 't';

        CollectionReference ref = repo.getStudentsRef().document(id).collection("chats");
        if (isTeacher)
            ref = repo.getTeachersRef().document(id).collection("chats");

        ref.document(chatId).delete();

    }


    void sendMessage(Message msg, String who, String chatId, String username, String otherName) {
        String id = getUserId();
        if (id == null) return;
        repo.getChatMessagesRef(chatId).add(msg);

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setLastMessage(msg);
        chat.setName(username);

        addChatToUser(id, chat, otherName, who);

        addChatToUser(who, chat, username, id);


    }


    LiveData<List<Chat>> getChats() {
        final String id = getUserId();
        if (id == null) return null;

        final boolean isTeacher = id.charAt(0) == 't';

        CollectionReference ref = repo.getStudentsRef().document(id).collection("chats");
        if (isTeacher)
            ref = repo.getTeachersRef().document(id).collection("chats");

        ArrayList<Chat> allChats = new ArrayList<>();

        ref.addSnapshotListener((query, e) -> {
            if (query == null || e != null) return;
            allChats.clear();
            for (DocumentSnapshot snapshot : query.getDocuments())
                allChats.add(snapshot.toObject(Chat.class));

            chats.setValue(allChats);

        });

        return chats;
    }

    LiveData<List<Message>> getChatMessages(String chatId, int howMany) {
        final MutableLiveData<List<Message>> messages = new MutableLiveData<>();

        final ArrayList<Message> allMessages = new ArrayList<>();

        repo.getChatMessagesRef(chatId).orderBy("date", Query.Direction.ASCENDING).limitToLast(howMany)
                .addSnapshotListener((query, e) -> {
                    if (query == null || e != null) return;
                    for (final DocumentSnapshot snapshot : query.getDocuments()) {
                        final Message msg = snapshot.toObject(Message.class);
                        if (!allMessages.contains(msg))
                            allMessages.add(msg);
                    }

                    messages.setValue(allMessages);

                });

        return messages;

    }

}
