package de.baumann.pdfcreator.pages;

import android.app.Activity;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.baumann.pdfcreator.R;


public class add_text extends Fragment {

    private static EditText edit;
    private String title;
    private String folder;
    private String pages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white_48dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                title = sharedPref.getString("title", null);

                backup();
                createPDF();
                deleteTemp();
            }
        });

        edit = (EditText) rootView.findViewById(R.id.editText);

        ImageButton ib_1 = (ImageButton) rootView.findViewById(R.id.imageButton_1);
        ib_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, 1);
            }
        });

        ImageButton ib_2 = (ImageButton) rootView.findViewById(R.id.imageButton_2);
        ib_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                pages = sharedPref.getString("deletePages", null);

                final EditText input = new EditText(getActivity());
                input.setHint("1-5,7,15-20");
                input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setView(input)
                        .setMessage(R.string.add_text_hint2)
                        .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                pages = input.getText().toString().trim();
                                sharedPref.edit()
                                        .putString("deletePages", pages)
                                        .apply();
                                deletePage();
                                deleteTemp();
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

        ImageButton ib_3 = (ImageButton) rootView.findViewById(R.id.imageButton_3);
        ib_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, 2);
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {

                String FilePath = data.getData().getPath();
                String FileTitle = FilePath.substring(FilePath.lastIndexOf("/")+1);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPref.edit().putString("pathPDF", FilePath).apply();
                sharedPref.edit().putString("title", FileTitle).apply();

            }

            if (requestCode == 2) {

                backup();

                String FilePath = data.getData().getPath();
                String FileTitle = FilePath.substring(FilePath.lastIndexOf("/")+1);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPref.edit().putString("pathPDF2", FilePath).apply();
                sharedPref.edit().putString("title2", FileTitle).apply();

                List<InputStream> list = new ArrayList<>();
                try {

                    // Load existing PDF
                    title = sharedPref.getString("title2", null);
                    folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                    String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                            folder + title + ".pdf");

                    String path2 = sharedPref.getString("pathPDF2", null);

                    list.add(new FileInputStream(new File(path)));
                    assert path2 != null;
                    list.add(new FileInputStream(new File(path2)));

                    // Resulting pdf
                    OutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf"));

                    mergePDF(list, out);

                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                }

                deleteTemp();
                success();

            }
        }
    }

    private void createPDF() {
        // Output file
        String outputPath = Environment.getExternalStorageDirectory() +  "/" + "123456.pdf";

        // Run conversion
        final boolean result = convertToPdf(outputPath);

        // Notify the UI
        if (result) {
            success();
        } else Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void mergePDF(List<InputStream> list, OutputStream outputStream)
            throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        for (InputStream in : list) {
            PdfReader reader = new PdfReader(in);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                document.newPage();
                //import the page from source pdf
                PdfImportedPage page = writer.getImportedPage(reader, i);
                //add the page to the destination pdf
                cb.addTemplate(page, 0, 0);
            }
        }

        outputStream.flush();
        document.close();
        outputStream.close();
    }

    private void deletePage() {
        try {

            // Load existing PDF
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            pages = sharedPref.getString("deletePages", null);
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");
            PdfReader reader = new PdfReader(path);
            reader.selectPages(pages);

            PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf"));
            pdfStamper.close();
            success();
        }
        catch (Exception e)
        {
            Snackbar.make(edit, getString(R.string.toast_successfully_not), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    private boolean convertToPdf(String outputPdfPath) {
        try {

            String paragraph = edit.getText().toString().trim();

            // Create output file if needed
            File outputFile = new File(outputPdfPath);
            if (!outputFile.exists()) outputFile.createNewFile();

            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();
            PdfContentByte cb = writer.getDirectContent();

            // Load existing PDF
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");
            PdfReader reader = new PdfReader(path);

            int n = reader.getNumberOfPages();

            for (int i = 1; i <= n; i++) {
                document.newPage();
                //import the page from source pdf
                PdfImportedPage page = writer.getImportedPage(reader, i);
                //add the page to the destination pdf
                cb.addTemplate(page, 0, 0);
            }

            // Add your new data / text here
            // for example...

            document.newPage();
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

    private void deleteTemp(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        title = sharedPref.getString("title", null);

        InputStream in;
        OutputStream out;

        try {

            title = sharedPref.getString("title", null);
            folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
            String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                    folder + title + ".pdf");

            in = new FileInputStream(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
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

        File pdfFile = new File(Environment.getExternalStorageDirectory() +  "/" + "123456.pdf");
        if(pdfFile.exists()){
            pdfFile.delete();
        }
    }

    private void success(){

        Snackbar snackbar = Snackbar
                .make(edit, getString(R.string.toast_successfully), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.toast_open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        title = sharedPref.getString("title", null);
                        folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                        String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                                folder + title + ".pdf");

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
    }

    private void backup(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (sharedPref.getBoolean ("backup", false)){

            title = sharedPref.getString("title", null);

            InputStream in;
            OutputStream out;

            try {

                title = sharedPref.getString("title", null);
                folder = sharedPref.getString("folder", "/Android/data/de.baumann.pdf/");
                String path = sharedPref.getString("pathPDF", Environment.getExternalStorageDirectory() +
                        folder + title + ".pdf");

                in = new FileInputStream(path);
                out = new FileOutputStream(Environment.getExternalStorageDirectory() +
                        folder + "backups/" + title + ".pdf");

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
        }
    }
}
