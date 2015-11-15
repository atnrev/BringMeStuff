package com.bringmestuff.bringmestuff;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

public class Upload_photo extends Activity {

    ProgressDialog prgDialog;
    String encodedString;
    RequestParams paramsUpload = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 1;
    private SendPost send = null;
    String extension;
    String email;
    String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_photo);
        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        Intent myIntent = getIntent(); // gets the previously created intent
        email = myIntent.getStringExtra("email");
        password = myIntent.getStringExtra("password");

        try {
            String paramsDownload = ("email="+ email);
            send = (SendPost)new SendPost().execute("http://192.168.0.3/download_image.php", paramsDownload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
             // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgPath));
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Php web app
                //params.put("filename", fileName);


                // si l'utilisateur a entré un point
                if (fileName.lastIndexOf(".") > 0) {
                    // On récupère l'extension du fichier
                    extension = fileName.substring(fileName.lastIndexOf("."));
                } else {
                    // sinon c'est que l'utilisateur n'a pas entré d'extension donc on met en .txt
                    fileName += ".jpg";
                }


                paramsUpload.put("filename", email+"#photo"+extension);
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(imgPath,
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                paramsUpload.put("image", encodedString);

                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall("http://192.168.0.3/upload_image.php",paramsUpload);
    }

    // Make Http call to upload Image to Php server
    public String makeHTTPCall(String phpFileToInvoke, RequestParams params) {
        final String[] response = new String[1];
        prgDialog.setMessage("Transfert en cours...");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(phpFileToInvoke, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                prgDialog.hide();

                try {
                    response[0] = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), response[0], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), "Error : "+statusCode,Toast.LENGTH_LONG).show();

            }
        });
        return response[0];
    }
    public static void getPhoto(String host) {
        InputStream input = null;
        FileOutputStream writeFile = null;

        try
        {
            URL url = new URL(host);
            URLConnection connection = url.openConnection();
            int fileLength = connection.getContentLength();

            if (fileLength == -1)
            {
                System.out.println("Invalide URL or file.");
                return;
            }

            input = connection.getInputStream();
            String fileName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
            writeFile = new FileOutputStream(fileName);
            byte[] buffer = new byte[1024];
            int read;

            while ((read = input.read(buffer)) > 0)
                writeFile.write(buffer, 0, read);
            writeFile.flush();
        }
        catch (IOException e)
        {
            System.out.println("Error while trying to download the file.");
            e.printStackTrace();
        } finally
        {
            try
            {
                writeFile.close();
                input.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }
    public void deconnexion (View view){
        super.onResume();
        Toast.makeText(getApplicationContext(), "Déconnexion...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Upload_photo.this, LoginActivity.class);
        startActivity(intent);
    }

    public class SendPost extends AsyncTask<String, Void, String> {

        private final String USER_AGENT = "Mozilla/5.0";
        public String reponse;

        @Override
        protected String doInBackground(String... params) {
            String phpUrl = params[0];
            String urlParameters = params[1];
            URL obj = null;
            try {
                obj = new URL(phpUrl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + phpUrl);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer resp = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    resp.append(inputLine);
                }

                in.close();

                //print result
                System.out.println(resp.toString());
                reponse = resp.toString();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            return reponse;
        }

        @Override
        protected void onPostExecute(String reponse) {
            super.onPostExecute(reponse);
            //sendClient(reponse);
        }

        public String getReponse() {
            return reponse;
        }

        public void setReponse(String reponse) {
            this.reponse = reponse;
        }

        private void sendClient(String reponse) {

            String reponse1 = "Vous êtes connectés.";
            String reponse2 = "Mot de passe incorrect!";
            String reponse3 = "Vous faîtes désormais parti du réseau.";

            if (reponse.contains(reponse1)) {
                //System.out.println("if Vous êtes connectés.");
                //on annonce au client s'il vient de se connecter
                Intent intent = new Intent(Upload_photo.this, Upload_photo.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            } else if (reponse.contains(reponse2)) {
                //System.out.println("if Mot de passe incorrect.");
                //on annonce au client que le mdp est incorrect
                Intent intent = new Intent(Upload_photo.this, LoginActivity.class);
                startActivity(intent);
            } else if (reponse.contains(reponse3)) {
                //System.out.println("if Vous faîtes désormais parti du réseau.");
                //on annonce au client qu'il est le bienvenu.
                Intent intent = new Intent(Upload_photo.this, Upload_photo.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            } else {
                //System.out.println("if on ne sait pas.");
            }
        }
    }
}