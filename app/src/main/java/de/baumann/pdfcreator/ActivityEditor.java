package de.baumann.pdfcreator;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;


public class ActivityEditor extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener, GPUImageView.OnPictureSavedListener {

    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;

    private final Date date = new Date();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final GPUImage.ScaleType mScaleTyp = GPUImage.ScaleType.CENTER_INSIDE;

    private int h;
    private int w;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_title_edit);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setImageResource(R.drawable.ic_check_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();

                Snackbar.make(mGPUImageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE)
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

        Bitmap myBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");

        h = myBitmap.getHeight();
        w = myBitmap.getWidth();

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        assert seekBar != null;
        seekBar.setOnSeekBarChangeListener(this);

        ImageButton chooseFilter = (ImageButton) findViewById(R.id.imageButton_filter);
        assert chooseFilter != null;
        chooseFilter.setOnClickListener(this);

        ImageButton save = (ImageButton) findViewById(R.id.imageButton_check);
        assert save != null;
        save.setOnClickListener(this);

        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
        assert mGPUImageView != null;
        mGPUImageView.setScaleType(mScaleTyp);
        mGPUImageView.setImage(imgFile);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.imageButton_filter:
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
                assert mGPUImageView != null;
                mGPUImageView.setImage(imgFile);

                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImageView.requestRender();
                    }

                });
                break;

            case R.id.imageButton_check:
                saveImage();
                break;

            default:
                break;
        }

    }

    @Override
    public void onPictureSaved(final Uri uri) {
        Snackbar.make(mGPUImageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_LONG)
                .setAction("Action", null);
    }

    private void saveImage() {

        Snackbar.make(mGPUImageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        String folder = "/";
        String fileName = "img_1.jpg";

        mGPUImageView.saveToPictures(folder, fileName, w, h, this);
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
        }
        mGPUImageView.requestRender();
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
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

            final AlertDialog d = new AlertDialog.Builder(ActivityEditor.this)
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
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