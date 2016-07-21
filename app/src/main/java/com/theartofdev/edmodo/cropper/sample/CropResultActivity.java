// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.edmodo.cropper.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.baumann.pdfcreator.R;

public final class CropResultActivity extends AppCompatActivity {

    /**
     * The image to show in the activity.
     */
    static Bitmap mImage;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_result);

        setTitle(R.string.app_title_2);


        imageView = ((ImageView) findViewById(R.id.resultImageView));

        if (mImage != null) {
            imageView.setImageBitmap(mImage);
            double ratio = ((int) (10 * mImage.getWidth() / (double) mImage.getHeight())) / 10d;
            int byteCount = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
                byteCount = mImage.getByteCount() / 1024;
            }
            String desc = "(" + mImage.getWidth() + ", " + mImage.getHeight() + "), Ratio: " + ratio + ", Bytes: " + byteCount + "K";
            ((TextView) findViewById(R.id.resultImageText)).setText(desc);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/pdf_temp/pdf_temp.jpg");

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

            Snackbar.make(imageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String path = Environment.getExternalStorageDirectory() + "/Pictures/pdf_temp/pdf_temp.jpg";

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

        } else {
            Intent intent = getIntent();
            Uri imageUri = intent.getParcelableExtra("URI");
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image is set to show", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        releaseBitmap();
        super.onBackPressed();
    }

    public void onImageViewClicked(View view) {
        releaseBitmap();
        finish();
    }

    private void releaseBitmap() {
        if (mImage != null) {
            mImage.recycle();
            mImage = null;
        }
    }
}
