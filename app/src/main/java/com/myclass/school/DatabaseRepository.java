package com.myclass.school;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


// contains methods and instances for firebase database and auth
class DatabaseRepository {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    // an empty constructor
    DatabaseRepository() {
    }

    // returns an auth instance
    FirebaseAuth getAuth() {
        return auth;
    }



    // returns reference to users location in database
    CollectionReference getUsers() {
        return db.collection("users");
    }


    // returns reference to teachers location in database
    CollectionReference getTeachersRef() {
        return db.collection("teachers");
    }

    // returns reference to students location in database
    CollectionReference getStudentsRef() {
        return db.collection("students");
    }

    // returns reference to students location in database
    CollectionReference getClassroomsRef() {
        return db.collection("classrooms");
    }

    StorageReference getImagesRef() {
        return storage.getReference("images");
    }

    // get current auth user
    FirebaseUser getUser() {
        return auth.getCurrentUser();
    }


}
