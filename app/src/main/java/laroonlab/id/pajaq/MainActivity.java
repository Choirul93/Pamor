package laroonlab.id.pajaq;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    CardView mCardView;
    Button btnCek;
    EditText etKode;
    EditText etNomer;
    EditText etSeri;

    TextView tvNomer;
    TextView tvMerek;
    TextView tvTipe;
    TextView tvWarna;
    TextView tvWilayah;
    TextView tvTahunRakit;
    TextView tvPkb;
    TextView tvJatuhTempo;

    String kode=" ";
    String nomer=" ";
    String seri=" ";
    String query=" ";

    private static final String PAJAK_REQUEST_URL = "http://ibacor.com/api/pajak-kendaraan?";

    ProgressDialog progressDialog;
    private  final String LOG = getClass().getSimpleName().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        initUi();



        btnCek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kode= etKode.getText().toString().trim().toLowerCase();
                nomer= etNomer.getText().toString().trim().toLowerCase();
                seri= etSeri.getText().toString().trim().toLowerCase();
                mCardView.setVisibility(View.GONE);


                if(isNetworkAvailable()){
                    if(kode.length()<1|| nomer.length()<1 || seri.length()<1){
                        Toast.makeText(MainActivity.this, "Data belum lengkap", Toast.LENGTH_SHORT).show();

                    }else {
                        progressDialog.show();
                        query="kode="+kode+"&nomor="+nomer+"&seri="+seri;
                        PajakAsyncTask pajak = new PajakAsyncTask();
                        pajak.execute();


                    }

                }else{
                    Toast.makeText(MainActivity.this, "No internet Acces", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public  void initUi(){
        //input
        mCardView=(CardView)findViewById(R.id.card_view);
        btnCek=(Button)findViewById(R.id.btnCek);
        etKode=(EditText)findViewById(R.id.etKOde);
        etNomer=(EditText)findViewById(R.id.etNomer);
        etSeri=(EditText)findViewById(R.id.etSeri);

        //output
        tvNomer=(TextView)findViewById(R.id.tvNomer);
        tvMerek=(TextView)findViewById(R.id.tvMerek);
        tvTipe=(TextView)findViewById(R.id.tvTipe);
        tvTahunRakit=(TextView)findViewById(R.id.tvTahunRakit);
        tvWarna=(TextView)findViewById(R.id.tvWarna);
        tvWilayah=(TextView)findViewById(R.id.tvWilayah);
        tvPkb=(TextView)findViewById(R.id.tvPkb);
        tvJatuhTempo=(TextView)findViewById(R.id.tvJatuhTempo);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void setEnableButton(Boolean enable) {
        if (enable) {
            btnCek.setEnabled(true);
        } else {
            btnCek.setEnabled(false);
        }

    }

    private class PajakAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {

            URL url = createUrl(PAJAK_REQUEST_URL + query);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
                // the function will be declared later.
                // `makeHttpRequest()` return String formated JSON.
            } catch (IOException e) {
                Log.e("MainActivity", "IOException", e);
                // if error happened, we log the error
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {

            progressDialog.dismiss();
            try {
                JSONObject jsonPajak =  new JSONObject(jsonResponse);
                String status = jsonPajak.getString("status");


                if(status.toLowerCase().equals("success") ){
                    String provinsi = jsonPajak.getString("provinsi");
                    if(provinsi.toLowerCase().equals("jateng")){
                        JSONObject data = jsonPajak.getJSONObject("data");
                        String nopol = data.getString("nopol");

                        JSONObject kendaraan = data.getJSONObject("kendaraan");
                        String merk = kendaraan.getString("merk");
                        String type = kendaraan.getString("type");
                        String tahun_pembuatan = kendaraan.getString("tahun_pembuatan");
                        String warna = kendaraan.getString("warna");
                        String wilayah = kendaraan.getString("wilayah");

                        JSONObject pkb = data.getJSONObject("pkb");
                        String jumlah = pkb.getString("jumlah");
                        String jatuh_tempo = pkb.getString("jatuh_tempo");


                        tvNomer.setText(nopol.toUpperCase());
                        tvMerek.setText(merk);
                        tvTipe.setText(" "+type.toUpperCase());
                        tvTahunRakit.setText(tahun_pembuatan.toUpperCase());
                        tvWarna.setText(warna);
                        tvWilayah.setText(" "+wilayah);
                        tvPkb.setText(jumlah);
                        tvJatuhTempo.setText(jatuh_tempo);

                        mCardView.setVisibility(View.VISIBLE);

                        Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);
                        mCardView.startAnimation(slide_up);

                    } else {
                        Toast.makeText(MainActivity.this, "Bukan Plat Jateng", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(MainActivity.this, "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private URL createUrl(String stringUrl) {
        Log.e("CREATE url","TRUE");
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e("MainActivity", "Error with creating URL", exception);
            return null;
        }

        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                // read the response data and make it as a single string
            } else {
                Log.e("MainActivity", "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    //
    private String readFromStream(InputStream inputStream) throws IOException {

        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
