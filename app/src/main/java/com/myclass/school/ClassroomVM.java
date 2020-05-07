package com.myclass.school;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomPost;

import java.util.ArrayList;
import java.util.List;


public class ClassroomVM extends ViewModel {
    private final DatabaseRepository repo = new DatabaseRepository();

    private final MutableLiveData<Classroom> classroom = new MutableLiveData<>();


    LiveData<Classroom> getClassById(String id) {

        repo.getClassroomsRef().document(id).
                get().addOnSuccessListener(query ->
                classroom.setValue(query.toObject(Classroom.class))
        );


        return classroom;
    }

    void postToClassroom(String classId, ClassroomPost post) {
        repo.getClassPosts(classId).document(post.getId()).set(post);

    }

    LiveData<List<ClassroomPost>> getClassPosts(String id, int howMany) {
        MutableLiveData<List<ClassroomPost>> posts = new MutableLiveData<>();

        repo.getClassPosts(id).orderBy("date", Query.Direction.ASCENDING).limitToLast(howMany).
                addSnapshotListener((query, exception) -> {
                    if (exception != null || query == null) return;
                    ArrayList<ClassroomPost> allPosts = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ClassroomPost post = doc.toObject(ClassroomPost.class);
                        allPosts.add(post);
                    }

                    posts.setValue(allPosts);

                });

        return posts;
    }


}
