package com.nareshkatta99.java_compressor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import id.zelory.compressor.constraint.DestinationConstraint;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private File actual, compress;
    private ImageView i1, i2;
    private TextView size1, size2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPerm();
        i1 = findViewById(R.id.i1);
        i2 = findViewById(R.id.i2);
        size1 = findViewById(R.id.size1);
        size2 = findViewById(R.id.size2);
        i1.setOnClickListener(this);
        i2.setOnClickListener(this);
    }

    private void checkPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permisson denied", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK) {
            fetchFile(data);
        }
    }

    private void fetchFile(Intent data) {
        if (data == null || data.getData() == null)
            return;
        Uri response = data.getData();
        String filePath = FileHelper.getRealPathFromURI_API19(this, response);
        if (filePath == null || filePath.trim().length() == 0) {
            String name = getDisplayName(response);
            if (name == null)
                name = String.valueOf(System.currentTimeMillis());
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(response));
            if (ext != null && !name.endsWith(ext))
                name = name + ext;
            File file = new File(getCacheDir(), "images");
            file.mkdirs();
            file = new File(file, name);
            try {
                if (!file.exists())
                    file.createNewFile();
                String fileData = readTextFromUri(response);
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(fileData.getBytes());
                outputStream.close();
                actual = new File(file.getAbsolutePath());
            } catch (Exception e) {
            }
        } else
            actual = new File(filePath);
        onActualReady();
    }

    private void onActualReady() {
        if (actual != null && actual.exists()) {
            Picasso.get().load(actual).into(i1);
            size1.setText(getReadableFileSize(actual.length()));
        }
    }

    private void onCompressReady() {
        if (compress != null && compress.exists()) {
            Picasso.get().load(compress).into(i2);
            size2.setText(getReadableFileSize(compress.length()));
        }
    }

    protected String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    String getDisplayName(Uri uri) {
        String displayName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
        try {
            if (cursor.moveToFirst())
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (cursor.moveToFirst())
                    displayName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
            } catch (Exception inner) {
                inner.printStackTrace();
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.i1:
                openFileChooser();
                break;
            case R.id.i2:
                startCompression();
                break;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(mimeTypeString());
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Open"), 102);
    }

    String mimeTypeString() {
        StringBuilder builder = new StringBuilder();
        for (String s : mimeTypes) {
            if (builder.length() > 0)
                builder.append("|");
            builder.append(s);
        }
        return builder.toString();
    }

    String[] mimeTypes = {
            "image/jpeg", "image/jpg", "image/png", "image/*"// images
    };

    private void startCompression() {
        if (actual != null && actual.exists()) {
            //add implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0" to dependencies
            final CircularProgressDrawable drawable = new CircularProgressDrawable(this);
            drawable.setStrokeWidth(5f);
            drawable.setCenterRadius(25f);
            drawable.setColorSchemeColors(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            drawable.start();
            i2.setImageDrawable(drawable);
            JavaCompressor.compress(this, actual, new Callback() {
                @Override
                public void onComplete(boolean status, @Nullable File file) {
                    drawable.stop();
                    if (status) {
                        compress = file;
                        onCompressReady();
                    }
                }
            }, new DestinationConstraint(new File(getCacheDir(), "compressed.jpeg")));
        }
    }

    private static String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

    private String getReadableFileSize(long size) {
        if (size <= 0L) {
            return "0 B";
        } else {
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024.0D));
            return new DecimalFormat("#,##0.#").format(((double) size) / Math.pow(1024.0D, (double) digitGroups)) + " " + units[digitGroups];
        }
    }
}