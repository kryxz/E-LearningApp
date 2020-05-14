package com.myclass.school.viewmodels;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myclass.school.CommonUtils;
import com.myclass.school.data.Assignment;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomPost;
import com.myclass.school.data.Notification;
import com.myclass.school.data.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// must be public
public class ClassroomVM extends ViewModel {

    private final DatabaseRepository repo = new DatabaseRepository();

    private final MutableLiveData<Classroom> classroom = new MutableLiveData<>();


    // get classroom object from database!
    public LiveData<Classroom> getClassById(String id) {

        repo.getClassroomsRef().document(id).
                get().addOnSuccessListener(query ->
                classroom.setValue(query.toObject(Classroom.class))
        );


        return classroom;
    }

    // send message to classroom
    public void postToClassroom(String classId, ClassroomPost post, Notification notification) {

        post.setMention(CommonUtils.Temp.isMention);
        CommonUtils.Temp.isMention = false; // reset to default
        repo.getClassPosts(classId).document(post.getId()).set(post);

        // if mention, send notification
        if (CommonUtils.Temp.mentionWho != null && post.isMention())
            notifyMention(notification);


    }


    private void notifyMention(Notification notification) {
        repo.getNotificationsRef(CommonUtils.Temp.mentionWho).add(notification);
        CommonUtils.Temp.mentionWho = null;

    }

    // get classroom messages
    public LiveData<List<ClassroomPost>> getClassPosts(String id, int howMany) {

        MutableLiveData<List<ClassroomPost>> posts = new MutableLiveData<>();
        // query order by date, and get only a specific amount of messages
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

    // get classroom assignments
    public LiveData<List<Assignment>> getAssignments(String id) {
        final MutableLiveData<List<Assignment>> assignments = new MutableLiveData<>();

        repo.getAssignmentsRef(id).addSnapshotListener((query, exception) -> {
            if (exception != null || query == null) return;
            final ArrayList<Assignment> all = new ArrayList<>();

            for (DocumentSnapshot doc : query.getDocuments()) {
                Assignment post = doc.toObject(Assignment.class);
                all.add(post);
            }

            assignments.setValue(all);
        });

        return assignments;
    }

    // add a new assignment to class
    public void addAssignment(String id, Assignment assignment, Notification notification) {

        assignment.setId(UUID.randomUUID().toString().substring(0, 10));
        assignment.setClassroomId(id);

        repo.getClassroomsRef().document(id).get().addOnSuccessListener(query -> {
            if (query == null) return;
            Classroom classroom = query.toObject(Classroom.class);
            if (classroom == null) return;
            assignment.setClassroomName(classroom.getName());

            notifyStudents(classroom.getMembers(), notification);

            repo.getAssignmentsRef(id).document(assignment.getId()).set(assignment);
        });


    }

    public String getUserId() {
        String email = repo.getUser().getEmail();
        if (email == null) return null;

        return email.substring(0, email.indexOf('@'));

    }

    private void notifyStudents(List<String> members, Notification notification) {

        final String teacherId = getUserId();
        for (String id : members)
            if (!id.equals(teacherId))
                repo.getNotificationsRef(id).add(notification);

    }

    // remove assignment from database
    public void deleteAssignment(String classId, String assignmentId) {
        repo.getAssignmentsRef(classId).document(assignmentId).delete();

    }


    public void sendNotificationInstructor(Notification n, String classroomId) {

        repo.getClassroomsRef().document(classroomId).get().addOnSuccessListener(query -> {
            final String id = query.get("instructor", String.class);
            if (id != null)
                repo.getNotificationsRef(id).add(n);

        });
    }


    // student submission upload!
    // upload a file, and send a data object to database
    public void uploadSubmission(Submission submission, Assignment assignment, ProgressBar progressBar) {


        // files reference
        final StorageReference fileRef = repo.getClassFiles(assignment.getClassroomId())
                .child(submission.getSenderId() + assignment.getId());


        // upload task to track progress
        final UploadTask uploadTask = fileRef.putFile(CommonUtils.Temp.fileUri);


        // update progress bar as the upload task is working
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred())
                    / taskSnapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
            progressBar.setSecondaryProgress((int) progress + 10);

        });

        // add data to database when the upload process is complete!
        uploadTask.addOnCompleteListener(task -> fileRef.getDownloadUrl().addOnSuccessListener(result -> {
            submission.getFile().setDownloadUrl(result.toString());
            repo.getAssignmentsRef(assignment.getClassroomId())
                    .document(assignment.getId()).update("submissions", FieldValue.arrayUnion(submission));

            // hide progress bar on complete
            new Handler().postDelayed(() ->
                    progressBar.setVisibility(View.GONE), 250);
        }));

        // null temp data
        CommonUtils.Temp.fileUri = null;
        CommonUtils.Temp.fileType = null;

    }
}
