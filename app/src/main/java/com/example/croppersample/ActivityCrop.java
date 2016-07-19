
package com.example.croppersample;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.baumann.pdfcreator.MainActivity;
import de.baumann.pdfcreator.R;

public class ActivityCrop extends AppCompatActivity {

    // Private Constants ///////////////////////////////////////////////////////////////////////////

    private final Date date = new Date();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Activity Methods ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_title_crop);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Views.
        final CropImageView cropImageView = (CropImageView) findViewById(R.id.CropImageView);
        assert cropImageView != null;
        final ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
        assert croppedImageView != null;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setImageResource(R.drawable.ic_check_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Bitmap croppedImage = cropImageView.getCroppedImage();
                croppedImageView.setImageBitmap(croppedImage);

                BitmapDrawable drawable = (BitmapDrawable) croppedImageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");

                // Encode the file as a JPEG image.
                FileOutputStream outStream;
                try {

                    outStream = new FileOutputStream(imgFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
                    outStream.flush();
                    outStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Snackbar.make(cropImageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String path = Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg";

                        Uri myUri= Uri.fromFile(new File(path));
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setClassName("de.baumann.pdfcreator", "de.baumann.pdfcreator.MainActivity");
                        sharingIntent.setType("image/*");
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, myUri);
                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(sharingIntent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }, 3000);
            }
        });

        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            cropImageView.setImageBitmap(myBitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        String title;
        if (id == R.id.action_share) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            title = sharedPref.getString("title", null);
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf");

            Uri myUri= Uri.fromFile(new File(path));
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("application/pdf");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, myUri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share_with)));

        }

        if (id == R.id.action_open) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            title = sharedPref.getString("title", null);
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf");

            File file = new File(path);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file),"application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(target);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
                LayoutInflater inflater = getLayoutInflater();

                View toastLayout = inflater.inflate(R.layout.toast,
                        (ViewGroup) findViewById(R.id.toast_root_view));

                TextView header = (TextView) toastLayout.findViewById(R.id.toast_message);
                header.setText(R.string.toast_install_pdf);

                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(toastLayout);
                toast.show();
            }

        }

        if (id == R.id.action_folder) {

            File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/de.baumann.pdf/");

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(directory), "resource/folder");

            try {
                startActivity (target);
            } catch (ActivityNotFoundException e) {
                LayoutInflater inflater = getLayoutInflater();

                View toastLayout = inflater.inflate(R.layout.toast,
                        (ViewGroup) findViewById(R.id.toast_root_view));

                TextView header = (TextView) toastLayout.findViewById(R.id.toast_message);
                header.setText(R.string.toast_install_folder);

                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(toastLayout);
                toast.show();
            }
        }

        if (id == R.id.action_settings) {

            SpannableString s;
            s = new SpannableString(Html.fromHtml(getString(R.string.about_text)));

            Linkify.addLinks(s, Linkify.WEB_URLS);

            final AlertDialog d = new AlertDialog.Builder(com.example.croppersample.ActivityCrop.this)
                    .setTitle(R.string.about_title)
                    .setMessage(s)
                    .setPositiveButton(getString(R.string.toast_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
            d.show();
            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
