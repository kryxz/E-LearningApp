package com.myclass.school;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.ClassroomPost;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// must be public
public class UserViewModel extends ViewModel {
    private final DatabaseRepository repo = new DatabaseRepository();

    private final MutableLiveData<User> user = new MutableLiveData<>();

    private final MutableLiveData<List<Classroom>> classes = new MutableLiveData<>();


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


    LiveData<User> getUserById(String id) {

        if (id.equals("null")) {
            final String email = getAuthUser().getEmail();

            if (email == null) return null;
            id = email.substring(0, email.indexOf('@'));
        }


        final boolean isTeacher = id.charAt(0) == 't';

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

    private final MutableLiveData<Classroom> classroom = new MutableLiveData<>();


    void updateUser(User user) {
        String id = user.getEmail().substring(0, user.getEmail().indexOf('@'));

        if (user instanceof Teacher)
            repo.getTeachersRef().document(id).set(user);
        else
            repo.getStudentsRef().document(id).set(user);

    }


    void logout() {
        repo.getAuth().signOut();
    }




    private void updatePicUrl(String email, String picUrl) {
        String id = email.substring(0, email.indexOf('@'));

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("photoUrl", picUrl);
        else
            repo.getStudentsRef().document(id).update("photoUrl", picUrl);


    }

    void uploadFile(Uri file, String classroomId, String fileName, String description,
                    String fileType, Runnable completeAction) {

        String email = getAuthUser().getEmail();
        if (email == null) return;

        String userId = email.substring(0, email.indexOf('@'));

        String fileId = UUID.randomUUID().toString().substring(0, 10);
        StorageReference fileRef = repo.getClassFiles(classroomId).child(fileId);

        ClassroomFile classroomFile = new ClassroomFile();

        classroomFile.setAuthor(userId);
        classroomFile.setId(fileId);

        classroomFile.setName(fileName);
        classroomFile.setDate(System.currentTimeMillis());
        classroomFile.setDescription(description);
        classroomFile.setType(fileType);

        UploadTask uploadTask = fileRef.putFile(file);
        uploadTask.addOnSuccessListener(result ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    classroomFile.setDownloadUrl(uri.toString());

                    repo.getClassFilesRef(classroomId).document(fileId).set(classroomFile);
                    completeAction.run();
                }));


    }

    LiveData<String> getNameById(String id) {

        MutableLiveData<String> name = new MutableLiveData<>();

        CollectionReference ref = repo.getTeachersRef();

        if (id.charAt(0) == 's')
            ref = repo.getStudentsRef();

        ref.document(id).get().addOnSuccessListener(query ->
                name.setValue(query.get("name", String.class)));
        return name;
    }

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


    LiveData<List<ClassroomFile>> getClassFiles(String id) {
        MutableLiveData<List<ClassroomFile>> files = new MutableLiveData<>();

        repo.getClassFilesRef(id).orderBy("date", Query.Direction.DESCENDING).
                addSnapshotListener((query, exception) -> {
                    if (exception != null || query == null) return;
                    ArrayList<ClassroomFile> allFiles = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ClassroomFile file = doc.toObject(ClassroomFile.class);
                        allFiles.add(file);
                    }

                    files.setValue(allFiles);

                });

        return files;
    }

}
