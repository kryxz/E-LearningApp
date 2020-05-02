package com.myclass.school;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


// must be public
public class UserViewModel extends ViewModel {
    private final DatabaseRepository repo = new DatabaseRepository();

    private MutableLiveData<User> user = new MutableLiveData<>();

    private MutableLiveData<List<Classroom>> classes = new MutableLiveData<>();


    LiveData<List<Classroom>> getMyClasses() {

        String email = getAuthUser().getEmail();
        if (email == null) return classes;

        String id = email.substring(0, email.indexOf('@'));

        ArrayList<Classroom> myClasses = new ArrayList<>();


        repo.getClassroomsRef().whereArrayContains("members", id)
                .get().addOnSuccessListener(query -> {
            myClasses.clear();
            for (DocumentSnapshot doc : query.getDocuments()) {
                Classroom c = doc.toObject(Classroom.class);
                myClasses.add(c);
            }

            classes.setValue(myClasses);

        });


        return classes;
    }

    LiveData<User> getUser() {
        String email = getAuthUser().getEmail();

        if (email == null) return null;

        boolean isTeacher = email.charAt(0) == 't';

        String id = email.substring(0, email.indexOf('@'));

        if (isTeacher)
            repo.getTeachersRef().document(id).addSnapshotListener((query, exception) -> {
                if (exception != null || query == null) return;
                Teacher t = query.toObject(Teacher.class);
                user.setValue(t);
            });

        else
            repo.getStudentsRef().document(id).addSnapshotListener((query, exception) -> {
                if (exception != null || query == null) return;
                Student s = query.toObject(Student.class);
                user.setValue(s);

            });


        return user;
    }

    private FirebaseUser getAuthUser() {
        return repo.getUser();
    }


    void updateProfilePic(Bitmap pic) {
        String email = getAuthUser().getEmail();
        if (email == null) return;
        String id = email.substring(0, email.indexOf('@'));


        // bitmap to byte array
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 100, bAOS);
        byte[] img = bAOS.toByteArray();

        UserProfileChangeRequest.Builder changeBuilder = new UserProfileChangeRequest.Builder();


        StorageReference imageRef = repo.getImagesRef().child(id + ".png");

        imageRef.putBytes(img).addOnSuccessListener(result ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    changeBuilder.setPhotoUri(uri);
                    repo.getUser().updateProfile(changeBuilder.build());
                    updatePicUrl(email, uri.toString());
                }));

    }


    void updateUser(User user) {
        String id = user.getEmail().substring(0, user.getEmail().indexOf('@'));

        if (user instanceof Teacher)
            repo.getTeachersRef().document(id).set(user);
        else
            repo.getStudentsRef().document(id).set(user);

    }

/*
    void logout() {
        repo.getAuth().signOut();
    }

*/


    private void updatePicUrl(String email, String picUrl) {
        String id = email.substring(0, email.indexOf('@'));

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("photoUrl", picUrl);
        else
            repo.getStudentsRef().document(id).update("photoUrl", picUrl);


    }


}
