package com.myclass.school;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.DateUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.myclass.school.data.ClassroomFile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


/*
A class which holds some common methods that are used in other places
 */

public class Common {
    // some default values
    public static final String EMAIL_SUFFIX = "@elearn.jo";
    static final String DEFAULT_PASSWORD = "elearn123";


    static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private static int[] colors = new int[]{
            R.color.colorAccent,
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.red,
            R.color.green,
            R.color.pico_void,
            R.color.maz_blue,
            R.color.wild_green,
            R.color.aqua_velvet,
            R.color.steel_pink,
            R.color.orange_light,

    };


    static int getRandomColor(Context c, int pos) {
        return ContextCompat.getColor(c, colors[pos % colors.length]);
    }


    /*
    a simple confirm/cancel dialog
    performs an action upon confirmation
     */

    public static void showConfirmDialog(Context context, String title, String message,
                                         Runnable confirmAction) {


        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        final View layout = View.inflate(context, R.layout.confirm_dialog, null);
        dialog.setView(layout);


        // title and message
        final AppCompatTextView titleEd = layout.findViewById(R.id.dialog_title);
        final AppCompatTextView messageEd = layout.findViewById(R.id.dialog_message);
        titleEd.setText(title);
        messageEd.setText(message);

        // buttons confirm and cancel
        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        // dismiss dialog on cancel
        cancel.setOnClickListener(v -> dialog.dismiss());

        // run action on confirm
        confirm.setOnClickListener(v -> {
            confirmAction.run();

            dialog.dismiss();
        });


        dialog.show();
    }


    // shows a simple toast message
    static void showMessage(Context context, int id) {
        if (context != null)
            Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show();
    }


    // copies text to device clipboard
    public static void copyText(Context context, String text) {

        // a clipData object to hold the text data
        final ClipData copied = ClipData.newPlainText("code", text);

        // get the ClipboardManager from context
        final ClipboardManager clipboardManager = ((ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE));

        // set text data to clipboard
        if (clipboardManager != null)
            clipboardManager.setPrimaryClip(copied);


        showMessage(context, R.string.copied);
    }


    static void setDrawerIcon(Activity ac) {
        if (ac == null) return;

        Drawable drawable = ContextCompat.getDrawable(ac, R.drawable.ic_format_list);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(ac, R.color.lynx_white));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

        ActionBar bar = ((MainActivity) ac).getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeAsUpIndicator(drawable);
/*            if (bar.getTitle() == null) return;

            String title = bar.getTitle().toString();
            if (!title.contains("  "))
                bar.setTitle("  " + title);*/

        }

    }

    static void openDrawer(Activity activity) {
        if (activity == null) return;
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);

        // close if open
        if (drawer.isOpen())
            drawer.closeDrawer(GravityCompat.START);
        else
            drawer.openDrawer(GravityCompat.START);

    }

    static void fabVisibility(Activity activity, int visibility) {
        if (activity == null) return;
        activity.findViewById(R.id.open_drawer_fab).setVisibility(visibility);

    }

    static void downloadFile(Activity activity, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
        Common.showMessage(activity, R.string.file_download);
    }

    static String getTimeAsString(long time) {
        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
        ).toString();
    }

    @SuppressLint("ClickableViewAccessibility") // disable an IDE warning
    static void passwordView(final AppCompatEditText editText) {
        // hides and shows password when user clicks at the 'eye' icon.
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[0].getBounds().width())) {
                    if (editText.getTransformationMethod() == null) {
                        // Hides password.
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility_off, 0);
                        editText.setTransformationMethod(new PasswordTransformationMethod());
                    } else {
                        // Shows password
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility, 0);
                        editText.setTransformationMethod(null);
                    }
                    return true;
                }
            }
            return false;
        });
    }


    static void hideKeypad(View v) {
        Context context = v.getContext();
        ((InputMethodManager)
                Objects.requireNonNull(context.getSystemService(Activity.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    static void tintDrawableTextView(AppCompatTextView tv, int color) {
        Drawable drawable = ContextCompat.getDrawable(tv.getContext(), R.drawable.colored_circle);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(tv.getContext(), color));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    static Drawable tintDrawable(Context context, int id, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        if (drawable == null) return null;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }


/*
    static void recreateFragment(View view, int id) {
        Navigation.findNavController(view).navigate(id,
                null,
                new NavOptions.Builder().setPopUpTo(id, true).build());

    }
*/


    private static final DatabaseRepository repo = new DatabaseRepository();

    static void setOnline(boolean online) {
        FirebaseUser user = repo.getUser();
        if (user == null) return;
        final String email = user.getEmail();
        if (email == null || email.contains("admin")) return;

        final String id = email.substring(0, email.indexOf('@'));

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("online", online);
        else
            repo.getStudentsRef().document(id).update("online", online);
    }

    static void restartApp(Activity activity) {
        if (activity == null) return;
        TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainActivity.class))
                .addNextIntent(activity.getIntent())
                .startActivities();
    }


    static void updateTitle(Activity ac, int id) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(ac.getString(id));
        }

    }

    static void updateTitle(Activity ac, String text) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(text);
        }

    }

    static void hideActionBar(Activity ac) {
        if (ac != null) {
            ActionBar bar = ((AppCompatActivity) ac).getSupportActionBar();
            if (bar != null) bar.hide();
        }

    }

    static void showActionBar(Activity ac) {
        if (ac != null) {
            ActionBar bar = ((AppCompatActivity) ac).getSupportActionBar();
            if (bar != null) bar.show();
        }

    }

    // shows a datePick dialog when clicked on the editText.
    // needs a fragmentManager to display the dialog.
    static void showDatePickerDialog(TextInputEditText editText, final FragmentManager manager) {

        final Common.DatePickerFragment datePickerFragment = new DatePickerFragment(
                // minimum date -> Now
                // maximum date -> a month from now
                editText, new Date().getTime(),
                new Date().getTime() + 2629746000L);


        editText.setFocusableInTouchMode(false);


        editText.setOnClickListener(v -> {
                    hideKeypad(v);
                    datePickerFragment.show(manager, "datePicker");
                }
        );

    }

    static Drawable getDrawableFromView(View v) {
        Bitmap returnedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = v.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);

        v.draw(canvas);
        //return the bitmap
        return new BitmapDrawable(v.getResources(),
                returnedBitmap);
    }

    static String getDateFormatted(long date) {
        SimpleDateFormat ft = new SimpleDateFormat("MMM dd, hh:mm a", Locale.US);
        return ft.format(date);
    }

    // inner class to hold some temp data
    static class Temp {

        // for creating an assignment
        static long assignmentDate = 0L;
        static long assignmentDueDate = 0L;
        static ClassroomFile tempFile = null;

        // for submitting to an assignment
        static Uri fileUri;
        static String fileType;

    }

    // Public and static: Android requires the class to be so.
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        final AppCompatEditText ed; // set picked date in this
        final private Long minDate; // minimum date that can be chosen by user
        final private Long maxDate; // maximum date that can be chosen by user


        // Constructor
        DatePickerFragment(AppCompatEditText editText,
                           Long minimumDate, Long maximumDate) {

            ed = editText;
            minDate = minimumDate;
            maxDate = maximumDate;
        }


        @NonNull
        @Override
        //Creates a dialog, customizes a few things and returns it
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use date(maxDate) as the default date in the picker
            // by making a Calendar instance and setting time to maxDate.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(minDate);

            //Getting fields from calendar instance.
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);

            // Create a new instance of DatePickerDialog and return it
            assert getContext() != null;
            //instantiating a Date picker dialog
            DatePickerDialog dialog = new DatePickerDialog(getContext(), this, year, month, day);

            //Setting max and min date.
            dialog.getDatePicker().setMinDate(minDate);
            dialog.getDatePicker().setMaxDate(maxDate);

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //set new date to the EditText we got from the Constructor.
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);


            // Since January is 0. December is 11. So we should increment month by 1.
            String dateString = year + "-" + (month + 1) + "-" + day + " ";

            if (ed.getId() == R.id.assignment_due_date) {
                Temp.assignmentDueDate = calendar.getTimeInMillis();
                ed.setText(getString(R.string.due_date_arg, dateString, Common.getDateFormatted(calendar.getTimeInMillis())));

            } else if (ed.getId() == R.id.assignment_date) {
                Temp.assignmentDate = calendar.getTimeInMillis();
                ed.setText(getString(R.string.open_date_arg, dateString));

            } else
                ed.setText(dateString);

        }


    }
}

