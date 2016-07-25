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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.baumann.pdfcreator.*;

@SuppressWarnings("UnusedParameters")
public final class CropResultActivity extends AppCompatActivity {

    /**
     * The image to show in the activity.
     */
    static Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_result);

        setTitle(R.string.app_title_2);

        ImageView imageView;
        TextView textview;
        imageView = ((ImageView) findViewById(R.id.resultImageView));
        textview = ((TextView) findViewById(R.id.resultImageText));

        if (mImage != null) {

            assert imageView != null;
            imageView.setImageBitmap(mImage);

            Snackbar.make(imageView, getString(R.string.toast_savedImage), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();

            double ratio = ((int) (10 * mImage.getWidth() / (double) mImage.getHeight())) / 10d;
            int byteCount = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
                byteCount = mImage.getByteCount() / 1024;
            }
            String desc = "(" + mImage.getWidth() + ", " + mImage.getHeight() + "), Ratio: " + ratio + ", Bytes: " + byteCount + "K";
            assert textview != null;
            textview.setText(desc);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/.pdf_temp/pdf_temp.jpg");

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

            Intent intent = new Intent(CropResultActivity.this, de.baumann.pdfcreator.MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            if (mImage != null) {
                mImage.recycle();
                mImage = null;
            }
            finish();

        } else {
            Intent intent = getIntent();
            Uri imageUri = intent.getParcelableExtra("URI");
            if (imageUri != null) {
                assert imageView != null;
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image is set to show", Toast.LENGTH_LONG).show();
            }
        }
    }
}
