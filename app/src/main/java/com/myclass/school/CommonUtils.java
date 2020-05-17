package com.myclass.school;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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
import com.myclass.school.viewmodels.DatabaseRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/*
A class which holds some common methods that are used in other places
 */

public class CommonUtils {
    // some default values
    public static final String EMAIL_SUFFIX = "@elearn.jo";
    public static final String DEFAULT_PASSWORD = "elearn123";
    // an object that contains references to database locations
    private static final DatabaseRepository repo = new DatabaseRepository();


    // language codes (delete app data if changed)
    private static final String ARABIC = "AR";
    private static final String ENGLISH = "EN";

    // random colors for list icons
    private static List<Integer> colors = new ArrayList<>(
            Arrays.asList(
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
                    R.color.ba_red,
                    R.color.magenta_purple,
                    R.color.sea,
                    R.color.radiant_yellow,
                    R.color.pico_pink,
                    R.color.asphalt
            )
    );

    // shuffle colors
    static {
        Collections.shuffle(colors);
    }


    // get file name from uri
    public static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


    /*
    a simple confirm/cancel dialog
    performs an action upon confirmation
     */
    public static void showConfirmDialog(Context context, String title, String message,
                                         Runnable confirmAction) {


        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setPositiveButton(R.string.confirm, (d, which) -> {

            confirmAction.run();
            d.dismiss();
        });

        dialog.setNegativeButton(R.string.cancel, (d, which) -> d.dismiss());

        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.show();
    }

    // returns a random color from the colors array
    public static int getRandomColor(Context c, int pos) {
        return ContextCompat.getColor(c, colors.get(pos % colors.size()));
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

    // shows a simple toast message
    public static void showMessage(Context context, int id) {
        if (context != null)
            Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show();
    }

    // set drawer icon in main screen
    public static void setDrawerIcon(Activity ac) {
        if (ac == null) return;

        Drawable drawable = ContextCompat.getDrawable(ac, R.drawable.ic_format_list);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(ac, R.color.lynx_white));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

        ActionBar bar = ((MainActivity) ac).getSupportActionBar();
        if (bar == null) return;

        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(drawable);

    }

    // get drawer from activity and open it
    public static void openDrawer(Activity activity) {
        if (activity == null) return;
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);

        // close if open
        if (drawer.isOpen())
            drawer.closeDrawer(GravityCompat.START);
        else
            drawer.openDrawer(GravityCompat.START);

    }

    // show or hide the fab button!
    public static void fabVisibility(Activity activity, int visibility) {
        if (activity == null) return;
        activity.findViewById(R.id.open_drawer_fab).setVisibility(visibility);

    }

    // send user to internet browser to download a file with its url
    public static void downloadFile(Activity activity, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
        CommonUtils.showMessage(activity, R.string.file_download);
    }

    // returns a string saying if the time was in the past (5 hours ago)
    // or the is in the future (in 2 days)
    public static String getTimeAsString(long time) {
        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
        ).toString();
    }


    // hide keyboard
    public static void hideKeypad(View v) {
        Context context = v.getContext();
        ((InputMethodManager)
                Objects.requireNonNull(context.getSystemService(Activity.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    // set a color for the drawable in a text view
    // used to show if a user is online or offline
    public static void tintDrawableTextView(AppCompatTextView tv, int color) {
        Drawable drawable = ContextCompat.getDrawable(tv.getContext(), R.drawable.colored_circle);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(tv.getContext(), color));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    // color the specified drawable with a specific color
    public static Drawable tintDrawable(Context context, int id, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        if (drawable == null) return null;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }


    // set user online or offline
    // used in OnPause and OnResume in MainActivity
    public static void setOnline(boolean online) {
        final FirebaseUser user = repo.getUser();
        if (user == null) return;
        final String email = user.getEmail();
        if (email == null || email.contains("admin")) return;

        final String id = email.substring(0, email.indexOf('@'));

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("online", online);
        else
            repo.getStudentsRef().document(id).update("online", online);
    }


    // restart app
    public static void restartApp(Activity activity) {
        if (activity == null) return;
        TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainActivity.class))
                .addNextIntent(activity.getIntent())
                .startActivities();
    }


    // update action bar title with a text from strings.xml
    public static void updateTitle(Activity ac, int id) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(ac.getString(id));
        }

    }


    // update action bar title with a string
    public static void updateTitle(Activity ac, String text) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(text);
        }

    }


    // shows a datePick dialog when clicked on the editText.
    // needs a fragmentManager to display the dialog.
    public static void showDatePickerDialog(TextInputEditText editText, final FragmentManager manager) {

        final CommonUtils.DatePickerFragment datePickerFragment = new DatePickerFragment(
                // minimum date -> Now
                // maximum date -> a month from now
                editText, new Date().getTime(),
                new Date().getTime() + 2629746000L);


        // cannot type in this field
        editText.setFocusableInTouchMode(false);


        editText.setOnClickListener(v -> {
                    hideKeypad(v);
                    datePickerFragment.show(manager, "datePicker");
                }
        );

    }


    // get view as a drawable
    public static Drawable getDrawableFromView(View v) {
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
        // return the bitmap
        return new BitmapDrawable(v.getResources(),
                returnedBitmap);
    }

    // get a string of the format MMM dd, hh:mm a
    public static String getDateFormatted(long date) {
        SimpleDateFormat ft = new SimpleDateFormat("MMM dd, hh:mm a", Locale.ENGLISH);
        return ft.format(date);
    }

    // inner class to hold some temp data
    public static class Temp {

        // for creating an assignment
        public static long assignmentDate = 0L;
        public static long assignmentDueDate = 0L;
        public static ClassroomFile tempFile = null;

        // for submitting to an assignment
        public static Uri fileUri;
        public static String fileType;

        public static boolean isMention;
        public static String mentionWho;

    }

    public static String getDayMonthYear(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DATE);

        return year + "-" + (month + 1) + "-" + day + " ";

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
        // Creates a dialog, customizes a few things and returns it
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use date(maxDate) as the default date in the picker
            // by making a Calendar instance and setting time to maxDate.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(minDate);

            // Getting fields from calendar instance.
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);

            // Create a new instance of DatePickerDialog and return it
            assert getContext() != null;
            // instantiating a Date picker dialog
            DatePickerDialog dialog = new DatePickerDialog(getContext(), this, year, month, day);

            // Setting max and min date.
            dialog.getDatePicker().setMinDate(minDate);
            dialog.getDatePicker().setMaxDate(maxDate);

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // set new date to the EditText we got from the Constructor.
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);


            // Since January is 0. December is 11. So we should increment month by 1.
            String dateString = year + "-" + (month + 1) + "-" + day + " ";

            if (ed.getId() == R.id.assignment_due_date) {
                Temp.assignmentDueDate = calendar.getTimeInMillis();
                ed.setText(getString(R.string.due_date_arg, dateString, CommonUtils.getDateFormatted(calendar.getTimeInMillis())));

            } else if (ed.getId() == R.id.assignment_date) {
                Temp.assignmentDate = calendar.getTimeInMillis();
                ed.setText(getString(R.string.open_date_arg, dateString));

            } else
                ed.setText(dateString);

        }


    }


    static void setLocale(String langCode, Activity activity) {

        Configuration config = activity.getResources().getConfiguration();
        config.locale = new Locale(langCode);
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

    }


    private static void changeLanguagePref(String langCode, Activity activity) {
        final SharedPreferences.Editor editor = activity.getSharedPreferences("PREFS", 0).edit();
        editor.putString("LANG_PREF", langCode);
        editor.apply();
        setLocale(langCode, activity);
        restartApp(activity);
    }

    public static void changeLanguage(Activity activity) {
        final String currentLang = getLanguage(activity);
        if (currentLang.equals(ENGLISH))
            changeLanguagePref(ARABIC, activity);
        else
            changeLanguagePref(ENGLISH, activity);

    }

    static String getLanguage(Activity activity) {
        final SharedPreferences sharedPreferences = activity.getSharedPreferences("PREFS", 0);
        return sharedPreferences.getString("LANG_PREF", ENGLISH);
    }
}

