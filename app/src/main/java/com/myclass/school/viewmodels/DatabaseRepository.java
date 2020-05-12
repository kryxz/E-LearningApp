package com.myclass.school.viewmodels;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


// contains methods and instances for firebase database and auth
public class DatabaseRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();


    // an empty constructor
    public DatabaseRepository() {
    }

    // returns an auth instance
    public FirebaseAuth getAuth() {
        return auth;
    }


    // returns reference to users location in database
    public CollectionReference getUsers() {
        return db.collection("users");
    }


    // returns reference to teachers location in database
    public CollectionReference getTeachersRef() {
        return db.collection("teachers");
    }

    // returns reference to students location in database
    public CollectionReference getStudentsRef() {
        return db.collection("students");
    }

    // returns reference to students location in database
    CollectionReference getClassroomsRef() {
        return db.collection("classrooms");
    }

    StorageReference getImagesRef() {
        return storage.getReference("images");
    }

    StorageReference getClassFiles(String id) {
        return storage.getReference("files").child(id);
    }


    CollectionReference getClassFilesRef(String id) {
        return getClassroomsRef().document(id).collection("files");
    }

    CollectionReference getClassPosts(String id) {
        return getClassroomsRef().document(id).collection("posts");
    }

    CollectionReference getAssignmentsRef(String id) {
        return getClassroomsRef().document(id).collection("assignments");
    }

    // get current auth user
    public FirebaseUser getUser() {
        return auth.getCurrentUser();
    }


    CollectionReference getChatMessagesRef(String chatId) {
        return db.collection("chats").document(chatId).collection("messages");

    }

    CollectionReference getNotificationsRef(String id) {
        CollectionReference ref = getStudentsRef();

        if (id.charAt(0) == 't')
            ref = getTeachersRef();

        return ref.document(id).collection("notifications");


    }


}
