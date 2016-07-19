package de.baumann.pdfcreator.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.baumann.pdfcreator.ActivityEditor;
import de.baumann.pdfcreator.R;


public class create_image extends Fragment {

    private final Date date = new Date();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String title;

    private static ImageView img;
    private int img_int = 0;

    private ImageButton ib_2;
    private ImageButton ib_4;
    private ImageButton ib_6;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                if(imgFile.exists()){
                    ib_2.setVisibility(View.GONE);
                    ib_4.setVisibility(View.GONE);
                    ib_6.setVisibility(View.GONE);

                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    final EditText input = new EditText(getActivity());
                    input.setHint(R.string.app_hint);
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                            .setView(input)
                            .setMessage(R.string.app_title)
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    title = input.getText().toString().trim();
                                    sharedPref.edit()
                                            .putString("title", title)
                                            .putString("pathPDF", Environment.getExternalStorageDirectory() +  "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf")
                                            .apply();
                                    img_1();

                                    InputStream in;
                                    OutputStream out;

                                    try {

                                        title = sharedPref.getString("title", null);
                                        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                                "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf");

                                        in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + dateFormat.format(date) + "_" + title + ".pdf");
                                        out = new FileOutputStream(path);

                                        byte[] buffer = new byte[1024];
                                        int read;
                                        while ((read = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, read);
                                        }
                                        in.close();

                                        // write the output file
                                        out.flush();
                                        out.close();
                                    } catch (Exception e) {
                                        Log.e("tag", e.getMessage());
                                    }

                                    img.setRotation(0);
                                    img.setImageResource(R.drawable.image);
                                    img_int = 0;

                                    File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + dateFormat.format(date) + "_" + title + ".pdf");
                                    if(pdfFile.exists()){
                                        pdfFile.delete();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });
                    dialog.show();
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        img=(ImageView)rootView.findViewById(R.id.imageView);

        ImageButton ib_1 = (ImageButton) rootView.findViewById(R.id.imageButton_1);
        ib_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setRotation(0);
                img_int = 0;

                ib_2.setVisibility(View.VISIBLE);
                ib_4.setVisibility(View.VISIBLE);
                ib_6.setVisibility(View.VISIBLE);

                selectImage_1();
            }
        });


        ib_2 = (ImageButton) rootView.findViewById(R.id.imageButton_2);
        ib_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                if(imgFile.exists()){
                    img.setRotation(90);
                    img_int = 270;
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        ImageButton ib_3 = (ImageButton) rootView.findViewById(R.id.imageButton_3);
        ib_3.setVisibility(View.GONE);

        ib_4 = (ImageButton) rootView.findViewById(R.id.imageButton_4);
        ib_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                if(imgFile.exists()){
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPref.edit()
                            .putInt("startFragment", 0)
                            .putBoolean("appStarted", false)
                            .apply();

                    Intent intent = new Intent(getActivity(), ActivityEditor.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finish();
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        ib_6 = (ImageButton) rootView.findViewById(R.id.imageButton_6);
        ib_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                if(imgFile.exists()){
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPref.edit()
                            .putInt("startFragment", 0)
                            .putBoolean("appStarted", false)
                            .apply();

                    Intent intent = new Intent(getActivity(), com.example.croppersample.ActivityCrop.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    getActivity().finish();
                } else {
                    Snackbar.make(img, getString(R.string.toast_noImage), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        // Get intent, action and MIME type
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }

        img=(ImageView)rootView.findViewById(R.id.imageView);
        File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
        if(imgFile.exists()){
            ib_2.setVisibility(View.VISIBLE);
            ib_4.setVisibility(View.VISIBLE);
            ib_6.setVisibility(View.VISIBLE);
            img.setRotation(0);
            img_int = 0;
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        } else {
            ib_2.setVisibility(View.GONE);
            ib_4.setVisibility(View.GONE);
            ib_6.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            img.setImageURI(imageUri);

            BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
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
        }
    }

    private void img_1() {
        // Input file
        String inputPath = Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg";

        // Output file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        String outputPath = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf");

        // Run conversion
        final boolean result = convertToPdf1(inputPath, outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(img, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
                                Snackbar.make(img, getString(R.string.toast_install_pdf), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
            snackbar.show();
        } else Snackbar.make(img, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean convertToPdf1(String jpgFilePath, String outputPdfPath) {
        try {
            // Check if Jpg file exists or not

            File inputFile = new File(jpgFilePath);
            if (!inputFile.exists()) throw new Exception("File '" + jpgFilePath + "' doesn't exist.");

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            Image image = Image.getInstance(jpgFilePath);
            image.setRotationDegrees(img_int);
            if (PageSize.A4.getWidth() - image.getWidth() < 0) {
                image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin(),
                PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin());
            }
            image.setAlignment(Element.ALIGN_CENTER);

            document.add(image);
            document.close();

            File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
            if(imgFile.exists()){
                imgFile.delete();
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private void selectImage_1() {

        final CharSequence[] options = {getString(R.string.goal_camera),getString(R.string.goal_gallery),getString(R.string.goal_gallery2), getString(R.string.goal_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.goal_camera))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);

                } else if (options[item].equals(getString(R.string.goal_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals(getString(R.string.goal_gallery2))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 3);

                } else if (options[item].equals(getString(R.string.goal_cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");
                if(imgFile.exists()){

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    img.setImageBitmap(myBitmap);

                    BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();

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
                    img.setImageBitmap(bitmap);
                }

            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);

                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
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
            } else if (requestCode == 3) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);

                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");

                // Encode the file as a PNG image.
                FileOutputStream outStream;
                try {

                    outStream = new FileOutputStream(imgFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                    outStream.flush();
                    outStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int PIC_CROP = 1;
            if (requestCode == PIC_CROP) {
                if (data != null) {
                    // get the returned data
                    Bundle extras = data.getExtras();
                    // get the cropped bitmap
                    Bitmap selectedBitmap = extras.getParcelable("data");

                    img.setImageBitmap(selectedBitmap);

                    BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();

                    File imgFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/img_1.jpg");

                    // Encode the file as a PNG image.
                    FileOutputStream outStream;
                    try {

                        outStream = new FileOutputStream(imgFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                        outStream.flush();
                        outStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
