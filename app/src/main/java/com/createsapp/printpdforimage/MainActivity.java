package com.createsapp.printpdforimage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.createsapp.printpdforimage.Utils.PDFUtils;
import com.createsapp.printpdforimage.Utils.PdfDocumentAdapter;
import com.createsapp.printpdforimage.model.SuperHeroModel;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_PRINT = "last_file_print.pdf";
    private AlertDialog dialog;

    List<SuperHeroModel> superHeroModelsList = new ArrayList<SuperHeroModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new AlertDialog.Builder(this).setCancelable(false).setMessage("Please wait").create();

        addSuperHeroes();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        findViewById(R.id.btn_create_pdf)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        createPDFFile(new StringBuilder(getAppPath())
                                                .append(FILE_PRINT).toString());
                                    }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "" + response.getPermissionName() + " need enable", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();


    }

    private void createPDFFile(String path) {
        if (new File(path).exists())
            new File(path).delete();

        try {
            final Document document = new Document();
            //Save
            PdfWriter.getInstance(document, new FileOutputStream(path));
            //OPen
            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Prasad");
            document.addCreator("CreatesApp");

            //font Setting
            BaseColor colorAcent = new BaseColor(0, 153, 204, 255);
            float fontSize = 20.0f;
            //Custome font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);

            //Create title of document
            Font titleFont = new Font(fontName, 36.0f, Font.NORMAL, BaseColor.BLACK);
            PDFUtils.addNewItem(document, "CREATE APPS", Element.ALIGN_CENTER, titleFont);

            //Add More information
            Font textFont = new Font(fontName, fontSize, Font.NORMAL, colorAcent);
            PDFUtils.addNewItem(document, "Create By:", Element.ALIGN_LEFT, titleFont);
            PDFUtils.addNewItem(document, "Prasad ", Element.ALIGN_LEFT, textFont);

            PDFUtils.addLinesSeperator(document);

            //Add detail
            PDFUtils.addLinesSeperator(document);
            PDFUtils.addNewItem(document, "DETAIL ", Element.ALIGN_CENTER, titleFont);
            PDFUtils.addLinesSeperator(document);

            //Use Rxjava, fetch Image from  URL and add to PDF
            Observable.fromIterable(superHeroModelsList)
                    .flatMap(model -> getBitmapFromUrl(this, model, document))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(model -> {
                        //On Next
                        //Tech item, we will add detail
                        PDFUtils.addNewItemWithLeftAndRight(document, model.getName(), "", titleFont, textFont);

                        PDFUtils.addLinesSeperator(document);
                        PDFUtils.addNewItem(document, model.getDescription(), Element.ALIGN_LEFT, textFont);
                        PDFUtils.addLinesSeperator(document);

                    }, throwable -> {
                        dialog.dismiss();
                        Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }, () -> {
                        //On Complete
//                        When  completed, close document
                        document.close();
                        dialog.dismiss();
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

                        printPDF();
                    });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dialog.dismiss();
        }
    }

    private void printPDF() {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this, new StringBuilder(getAppPath())
            .append(FILE_PRINT).toString(),FILE_PRINT);
            printManager.print("Document",printDocumentAdapter, new PrintAttributes.Builder().build());
        }catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private ObservableSource<SuperHeroModel> getBitmapFromUrl(Context context, SuperHeroModel model, Document document) {
        return Observable.fromCallable(() -> {
            Bitmap bitmap = Glide.with(context)
                    .asBitmap()
                    .load(model.getImage())
                    .submit().get();

            Image image = Image.getInstance(bitmapToByteArray(bitmap));
            image.scaleAbsolute(100, 100);
            document.add(image);

            return model;
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private String getAppPath() {
        File dir = new File(android.os.Environment.getExternalStorageDirectory() +
                File.separator + getResources().getString(R.string.app_name)
                + File.separator);

        if (!dir.exists())
            dir.mkdir();
        return dir.getPath() + File.separator;
    }

    private void addSuperHeroes() {
        SuperHeroModel superHeroModel = new SuperHeroModel("Lorem ipsum",
                "https://miro.medium.com/max/1200/1*mk1-6aYaf_Bes1E3Imhc0A.jpeg",
                "Lorem ipsum is a pseudo-Latin text used in web design, typography, layout, and printing in place of English to emphasise design elements over content.");

        superHeroModelsList.add(superHeroModel);
        superHeroModel = superHeroModel = new SuperHeroModel("Lorem ipsum",
                "https://miro.medium.com/max/1200/1*mk1-6aYaf_Bes1E3Imhc0A.jpeg",
                "Lorem ipsum is a pseudo-Latin text used in web design, typography, layout, and printing in place of English to emphasise design elements over content.");

        superHeroModelsList.add(superHeroModel);
    }
}