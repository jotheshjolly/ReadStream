package com.jolly.creations.readerapp;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shockwave.pdfium.PdfDocument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener,OnLoadCompleteListener {

    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    String TAG="PdfActivity";
    int position=-1;
    String fileid="";
    TextView fid;
    
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    
    private ProgressDialog mProgressDialog;
    
    Switch switchButton;
    
    String durl;
    
    DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
    
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://bingojs-default-rtdb.firebaseio.com/");
        myRef = database.getReference();
    
        pdfView= (PDFView)findViewById(R.id.pdfView);
        fid = (TextView) findViewById(R.id.fid);
        position = getIntent().getIntExtra("position",-1);
        fileid = getIntent().getStringExtra("file_code");
    
        
    
        fid.setText(fileid);
        displayFromSdcard();
    
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setMessage("Downloading Document");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        
        switchButton = (Switch) findViewById(R.id.switch1);
        switchButton.setOnClickListener(new View.OnClickListener() {
        
            @Override
            public void onClick(View v) {
                if (switchButton.isChecked()) {
                    downloadFile(durl);
                    displayFromonline();
                    
                
                    Toast.makeText(getApplicationContext(), "Switch is in ON State", Toast.LENGTH_LONG).show();
                } else {
    
                    displayFromSdcard();
                
                    Toast.makeText(getApplicationContext(), "Switch is in OFF State", Toast.LENGTH_LONG).show();
                }
            }
        });
        
    }
    
    private void startDownload(String url) {
        new DownloadFileAsync().execute(url);
    }
    
    @Override
    public void onDetachedFromWindow() {
        myRef.child(fileid+"").removeValue();
        super.onDetachedFromWindow();
    }
    
    @Override
    protected void onDestroy() {
        myRef.child(fileid+"").removeValue();
        super.onDestroy();
    }
    
    private void displayFromonline() {
    
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                durl=dataSnapshot.child(fileid).child("filelink").getValue().toString();
            }
        
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        
        //startDownload(durl);
        
        File file = new File(Environment.getExternalStorageDirectory().toString()+"/"+ "Rtemp.pdf");
    
        //Toast.makeText(getApplicationContext(), Environment.getExternalStorageDirectory().toString()+"/"+ file.toString(), Toast.LENGTH_LONG).show();
        
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(PdfActivity.this)
                .enableAnnotationRendering(true)
                .onLoad(PdfActivity.this)
                .scrollHandle(new DefaultScrollHandle(PdfActivity.this))
                .load();
        
    }
    
    private void displayFromSdcard() {
        pdfFileName = MainActivity.fileList.get(position).getName();
    
        Toast.makeText(getApplicationContext(), MainActivity.fileList.get(position).toString(), Toast.LENGTH_LONG).show();

        pdfView.fromFile(MainActivity.fileList.get(position))
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }
    
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
    
    boolean isFile_Exists(String filename){
        
        File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder1.exists();
        
        
    }
    
    boolean delete_File( String filename) {
    
        File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder1.delete();
        
    }
    
    
    public void downloadFile(String Url) {
        String DownloadUrl = Url;
        
        /*DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request1.setDescription("Sample Music File");   //appears the same in Notification bar while downloading
        request1.setTitle("File1.pdf");
        request1.setVisibleInDownloadsUi(false);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner();
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }
        request1.setDestinationInExternalFilesDir(getApplicationContext(), "/File", "temp.pdf");
        
        DownloadManager manager1 = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "temp.pdf");
            if(file.exists()){
                Toast.makeText(getApplicationContext(), "file exists", Toast.LENGTH_LONG).show();
    
            }
        }*/
    
        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(DownloadUrl);
    
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("My File");
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationUri(Uri.parse("file:///Rtemp.pdf"));
    
        downloadmanager.enqueue(request);
    }
    
    
    class DownloadFileAsync extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
           
        }
        
        @Override
        protected String doInBackground(String... aurl) {
            int count;
            
            try {
    
                String DownloadUrl = aurl[0];
                DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
                request1.setDescription("Sample Music File");   //appears the same in Notification bar while downloading
                request1.setTitle("File1.pdf");
                request1.setVisibleInDownloadsUi(false);
    
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request1.allowScanningByMediaScanner();
                    request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                }
                request1.setDestinationInExternalFilesDir(getApplicationContext(), "/File", "Rtemp.pdf");
    
                DownloadManager manager1 = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Objects.requireNonNull(manager1).enqueue(request1);
                if (DownloadManager.STATUS_SUCCESSFUL == 8) {
                    File file = new File( "Rtemp.pdf");
                    if(file.exists()){
                        Toast.makeText(getApplicationContext(), "file exists", Toast.LENGTH_LONG).show();
            
                    }
                }
                
                
                /*URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
    
                
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream("Rtemp.pdf",true);
                
                byte data[] = new byte[1024];
                
                long total = 0;
                
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }
                
                output.flush();
                output.close();
                input.close();*/
            } catch (Exception e) {}
            return null;
            
        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }
        
        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            
        }
    }
    
}