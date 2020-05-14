package com.myclass.school.viewmodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myclass.school.CommonUtils;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.Notification;
import com.myclass.school.data.NotificationType;
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
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();


    public LiveData<List<Classroom>> getMyClasses() {

        String id = getUserId();

        final ArrayList<Classroom> myClasses = new ArrayList<>();


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


    public LiveData<User> getUserById(String id) {

        final MutableLiveData<User> otherUser = new MutableLiveData<>();

        if (id.equals("null"))
            id = getUserId();


        final boolean isTeacher = id.charAt(0) == 't';

        DocumentReference ref = repo.getStudentsRef().document(id);

        if (isTeacher)
            ref = repo.getTeachersRef().document(id);

        ref.addSnapshotListener((query, exception) -> {
            if (exception != null || query == null) return;
            User u;
            if (isTeacher)
                u = query.toObject(Teacher.class);
            else u = query.toObject(Student.class);

            otherUser.setValue(u);

        });

        return otherUser;

    }


    public LiveData<User> getUser() {
        final String id = getUserId();
        if (id == null) return null;

        final boolean isTeacher = id.charAt(0) == 't';

        DocumentReference ref = repo.getStudentsRef().document(id);

        if (isTeacher)
            ref = repo.getTeachersRef().document(id);

        ref.addSnapshotListener((query, exception) -> {
            if (exception != null || query == null) return;
            User u;
            if (isTeacher)
                u = query.toObject(Teacher.class);
            else u = query.toObject(Student.class);

            user.setValue(u);

        });


        return user;
    }

    private FirebaseUser getAuthUser() {
        return repo.getUser();
    }


    public void updateProfilePic(Bitmap pic) {
        final String id = getUserId();
        if (id == null) return;


        // bitmap to byte array
        final ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 100, bAOS);
        final byte[] img = bAOS.toByteArray();

        UserProfileChangeRequest.Builder changeBuilder = new UserProfileChangeRequest.Builder();


        final StorageReference imageRef = repo.getImagesRef().child(id + ".png");

        imageRef.putBytes(img).addOnSuccessListener(result ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    changeBuilder.setPhotoUri(uri);
                    repo.getUser().updateProfile(changeBuilder.build());
                    updatePicUrl(uri.toString());
                }));

    }


    public void updateUser(User user) {
        final String id = user.getEmail().substring(0, user.getEmail().indexOf('@'));

        if (user instanceof Teacher)
            repo.getTeachersRef().document(id).set(user);
        else
            repo.getStudentsRef().document(id).set(user);

    }


    public void logout() {
        repo.getAuth().signOut();
    }


    private void updatePicUrl(String picUrl) {
        String id = getUserId();

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("photoUrl", picUrl);
        else
            repo.getStudentsRef().document(id).update("photoUrl", picUrl);


    }


    public void uploadFile(Uri file, String classroomId, String fileName, String description,
                           String fileType, Context context) {


        final String userId = getUserId();
        if (userId == null) return;

        final String fileId = UUID.randomUUID().toString().substring(0, 10);
        final StorageReference fileRef = repo.getClassFiles(classroomId).child(fileId);

        final ClassroomFile classroomFile = new ClassroomFile();

        classroomFile.setAuthor(userId);
        classroomFile.setId(fileId);

        classroomFile.setName(fileName);
        classroomFile.setDate(System.currentTimeMillis());
        classroomFile.setDescription(description);
        classroomFile.setType(fileType);

        final UploadTask uploadTask = fileRef.putFile(file);
        uploadTask.addOnSuccessListener(result ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    classroomFile.setDownloadUrl(uri.toString());

                    repo.getClassFilesRef(classroomId).document(fileId).set(classroomFile);
                    notifyStudents(classroomId, context);
                    CommonUtils.showMessage(context, R.string.file_sent);
                }));


    }

    private void notifyStudents(String classroomId, Context context) {

        repo.getClassroomsRef().document(classroomId).get().addOnSuccessListener(query -> {
            final Classroom classroom = query.toObject(Classroom.class);
            if (classroom == null) return;
            Notification notification = new Notification(
                    context.getString(R.string.new_file_title),
                    context.getString(R.string.new_file, classroom.getName()),
                    System.currentTimeMillis(), classroomId,
                    NotificationType.NEW_FILE

            );
            for (String id : classroom.getMembers())
                repo.getNotificationsRef(id).add(notification);
        });
    }

    public LiveData<String> getNameById(String id) {

        final MutableLiveData<String> name = new MutableLiveData<>();

        CollectionReference ref = repo.getTeachersRef();

        if (id.charAt(0) == 's')
            ref = repo.getStudentsRef();

        ref.document(id).get().addOnSuccessListener(query ->
                name.setValue(query.get("name", String.class)));
        return name;
    }

    public LiveData<String> getPhotoUrl(String id) {

        final MutableLiveData<String> photoUrl = new MutableLiveData<>();

        CollectionReference ref = repo.getTeachersRef();

        if (id.charAt(0) == 's')
            ref = repo.getStudentsRef();

        ref.document(id).get().addOnSuccessListener(query ->
                photoUrl.setValue(query.get("photoUrl", String.class)));
        return photoUrl;
    }


    public LiveData<List<ClassroomFile>> getClassFiles(String id) {
        final MutableLiveData<List<ClassroomFile>> files = new MutableLiveData<>();

        repo.getClassFilesRef(id).orderBy("date", Query.Direction.DESCENDING).
                addSnapshotListener((query, exception) -> {
                    if (exception != null || query == null) return;
                    final ArrayList<ClassroomFile> allFiles = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ClassroomFile file = doc.toObject(ClassroomFile.class);
                        allFiles.add(file);
                    }

                    files.setValue(allFiles);

                });

        return files;
    }

    public String getUserId() {
        String email = getAuthUser().getEmail();
        if (email == null) return null;

        return email.substring(0, email.indexOf('@'));

    }


    public LiveData<List<Notification>> getNotifications() {
        final String id = getUserId();

        repo.getNotificationsRef(id).orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((query, exception) -> {
                    if (exception != null || query == null) return;
                    final ArrayList<Notification> allNotifications = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments())
                        allNotifications.add(doc.toObject(Notification.class));


                    notifications.setValue(allNotifications);

                });

        return notifications;
    }

    public void deleteNotification(String id) {
        repo.getNotificationsRef(getUserId()).document(id).delete();
    }

    public void changePassword(String old, String newPassword, Runnable doneAction) {
        String email = getAuthUser().getEmail();
        if (email == null) return;
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, old);
        repo.getUser().reauthenticate(authCredential).addOnSuccessListener(task ->
                repo.getUser().updatePassword(newPassword).addOnSuccessListener(taskTwo -> {

                    repo.getUserRefById(getUserId()).update("password", newPassword);
                    doneAction.run();
                }));

    }
}