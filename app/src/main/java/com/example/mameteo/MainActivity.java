package com.example.mameteo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    ImageButton chercher;
    TextInputEditText ville;
    TextView results;
    String nom;
    Toolbar toolbar;
    private ProgressDialog dialog;
    Drawable drawable;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ville=findViewById(R.id.city);
        dialog=new ProgressDialog(this);
        chercher=findViewById(R.id.cherche);
        chercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearchCity();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void doSearchCity(){
        results=findViewById(R.id.resultat);
        nom=ville.getText().toString();
        new RequestTask().execute(nom);

    }
    private String getWeatherForCity(String city) {

        String response = "";

        try {

            String urlString = "https://api.openweathermap.org/data/2.5/weather?q="+URLEncoder.encode(city,"UTF-8")+",fr&units=metric&lang=fr&appid=0a73790ec47f53b9e1f2e33088a0f7d0";
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();
            int code = httpConnection.getResponseCode();
            if(code == HttpURLConnection.HTTP_OK) {
                String json = inputStreamToString(httpConnection.getInputStream());
                response = json;
            } else {
                response = "Le serveur a répondu avec le code " + code;
            }
            httpConnection.disconnect();
        } catch (Exception e) {
            response = "Exception détectée : " + e;
        }
        return response;
    }


    private static String inputStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
    private class RequestTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String...params){
            try {
                String reponse=getWeatherForCity(params[0]);
                Log.d("reponse", "doInBackground: "+reponse);
                Bitmap bitmap=getpicture(reponse);
                drawable=new BitmapDrawable(getResources(),bitmap);
                Log.d("drawable", "doInBackground: "+drawable);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return getWeatherForCity(params[0]);
        }
        @Override
        protected void onPreExecute(){

            dialog.setMessage("Veuillez patientez...");
            dialog.show();
        }
        @Override
        protected void onPostExecute(String result){
            dialog.dismiss();

            try {
                Log.d("drawable1", "onPostExecute: "+drawable);
                toolbar.setLogo(drawable);
                results.setText(decodeJSON(result));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String decodeJSON(String jsonStr) throws JSONException {
        JSONObject jso=new JSONObject(jsonStr);
        JSONObject coord=jso.getJSONObject("coord");
        String time="(lon: "+coord.getString("lon")+", lat: "+coord.getString("lat")+")";
        String temperature=jso.getJSONObject("main").getString("temp")+"˚C";
        String huimidty=jso.getJSONObject("main").getString("humidity");
        String temps=jso.getJSONArray("weather").getJSONObject(0).getString("description");
        String chaine="Voici le temps a "+nom+time+"\n"+"Temperature ="+temperature+"\n"+"Humidity = "+huimidty+"%"+"\n"+"Temps = "+temps;
        return chaine;
    }
    private Bitmap loadBitmapFromURL(URL url) throws Exception {
        URLConnection connection = url.openConnection();
        return BitmapFactory.decodeStream(connection.getInputStream());
    }
    private Bitmap getpicture(String jsonStr) throws Exception{
        JSONObject jso=new JSONObject(jsonStr);
        String iconurl="https://openweathermap.org/img/wn/"+jso.getJSONArray("weather").getJSONObject(0).getString("icon")+"@2x.png";
        Log.d("iconurl", "getpicture: "+iconurl);
        URL url=new URL(iconurl);
        bitmap=loadBitmapFromURL(url);
        Log.d("bitmap", "getpicture: "+bitmap);
        return bitmap;


    }
    

    }
