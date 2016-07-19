package de.baumann.pdfcreator.pages;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.baumann.pdfcreator.R;


public class create_text extends Fragment {

    private final Date date = new Date();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String title;
    private static EditText edit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

            }
        });

        edit = (EditText) rootView.findViewById(R.id.editText);

        ImageButton ib_1 = (ImageButton) rootView.findViewById(R.id.imageButton_1);
        ib_1.setVisibility(View.GONE);

        ImageButton ib_2 = (ImageButton) rootView.findViewById(R.id.imageButton_2);
        ib_2.setVisibility(View.GONE);

        ImageButton ib_3 = (ImageButton) rootView.findViewById(R.id.imageButton_3);
        ib_3.setVisibility(View.GONE);

        // Get intent, action and MIME type
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText(intent); // Handle single image being sent
            }
        }

        return rootView;
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            edit.setText(sharedText);
        }
    }

    private void img_1() {

        // Output file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);
        String outputPath = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                "/Android/data/de.baumann.pdf/" + dateFormat.format(date) + "_" + title + ".pdf");

        // Run conversion
        final boolean result = convertToPdf1(outputPath);

        // Notify the UI
        if (result) {
            Snackbar snackbar = Snackbar
                    .make(edit, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
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
                                Snackbar.make(edit, getString(R.string.toast_install_pdf), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
            snackbar.show();
        } else Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean convertToPdf1(String outputPdfPath) {
        try {

            String paragraph = edit.getText().toString().trim();

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            document.add (new Paragraph(paragraph));

            document.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
