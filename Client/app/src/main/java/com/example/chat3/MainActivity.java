package com.example.chat3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    int convertDpToPixels(Context context, float dp) {
        float a = dp * context.getResources().getDisplayMetrics().density;
        int b = (int) a;
        return b;
    }

    private String user_id = "1";
    private String opponent_id = "1";

    GetMessage gm;
    Search search;


    private String your_nickname = null;
    private String opponent_nickname = null;
    private String your_sex = null;
    private String opponent_sex = null;

    private boolean first;
    private boolean second;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(GetMessage task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(Search task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute(this.user_id);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(CloseChat task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute(this.user_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.user_id = getId();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_start_light_version);

        SharedPreferences sp = getSharedPreferences("my_settings",
                Context.MODE_PRIVATE);
        // проверяем, первый ли раз открывается программа
        boolean hasVisited = sp.getBoolean("hasVisited", false);

        if (hasVisited) {
            showLicense();
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("hasVisited", true);
            e.commit();
        }



       /* this.first = false;
        this.second = false;*/


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.support:
                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(ServerSettings.telegram));
                startActivity(browserIntent);
                break;
            case R.id.close_dialog:
                this.search.cancel(true);
                startAsyncTaskInParallel(new CloseChat());
                gm.cancel(true);
                setContentView(R.layout.activity_start_light_version);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void printId() {
        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        fl.setRotation(180);

        TextView tv = new TextView(this);

        tv.setText("Ваш идентификатор: " + this.user_id);
        tv.setTextSize(20);
        tv.setBackgroundResource(R.drawable.your_message_background);
        tv.setPadding(20, 20, 20, 20);

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.RIGHT;

        p.setMargins(40, 20, 40, 20);
        tv.setLayoutParams(p);

        fl.addView(tv);

        LinearLayout chatbox = findViewById(R.id.chatBox);
        chatbox.addView(fl, 0);
    }

    public void showLicense() {
        String license = getString(R.string.license);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Условия использования приложения")
                .setMessage(license)
                .setCancelable(false)
                .setPositiveButton("Закрыть приложение",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                .setNegativeButton("Я прочитал(-а) условия использования и " +
                                "согласен(-на) с условиями",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void startSearching(View view) {
        this.search = new Search();
        startAsyncTaskInParallel(this.search);
    }
    /*public void startSearching(View view) {
        EditText nickname = findViewById(R.id.nickname_edittext);
        EditText age = findViewById(R.id.age_edittext);
        RadioGroup yoursex_rg = findViewById(R.id.your_sex_radioGroup);
        RadioGroup opponentsex_rg = findViewById(R.id.opponent_sex_radioGroup);


        int yoursex = yoursex_rg.getCheckedRadioButtonId();
        int opponentsex = opponentsex_rg.getCheckedRadioButtonId();

        switch (yoursex) {
            case R.id.your_sex_male:
                //ваш пол мужской
                this.your_sex = "m";
                break;
            case R.id.your_sex_female:
                //ваш пол женский
                this.your_sex = "f";
                break;
            default:
                Toast.makeText(getApplicationContext(), "Выберите ваш пол",
                        Toast.LENGTH_LONG).show();
                return;
        }


        switch (opponentsex) {
            case R.id.opponent_sex_male_radio_button:
                //ищем мужской
                this.opponent_sex = "m";
                break;
            case R.id.opponent_sex_female_radio_button:
                // ищем женский
                this.opponent_sex = "f";
                break;
            case R.id.opponent_sex_all_radio_button:
                //неважно кого искать
                this.opponent_sex = "all";
            default:
                Toast toast = Toast.makeText(getApplicationContext(), "Выберите пол собеседника",
                        Toast.LENGTH_LONG);
                toast.show();
                return;

        }

        this.your_nickname = nickname.getText().toString();

        if (this.your_nickname.length() <= 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Введите ваше имя",
                    Toast.LENGTH_LONG);
            toast.show();
            return;
        }
    }*/

    public void stopSearching(View view) {
        setContentView(R.layout.activity_start_light_version);
        this.search.cancel(true);
        startAsyncTaskInParallel(new CloseChat());
    }

    /**
     * Отправляет сообщение собеседнику, выводит на экран сообщение
     * обработчик кнопки
     */
    public void sendMsgBtn(View view) {
        /*if (!this.first) {
            EditText editText = findViewById(R.id.messageBox);
            String message = editText.getText().toString();
            this.first = true;
            this.user_id = message;
            editText.setText("");
            return;
        }
        if (!this.second) {
            EditText editText = findViewById(R.id.messageBox);
            String message = editText.getText().toString();
            this.second = true;
            this.opponent_id = message;
            editText.setText("");
            return;
        }*/

        EditText editText = findViewById(R.id.messageBox);
        String message = editText.getText().toString();

        SendMessage n = new SendMessage();
        n.execute("sendMessage", this.user_id, this.opponent_id, message);

        editText.setText("");

        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        fl.setRotation(180);

        TextView tv = new TextView(this);

        tv.setText(message);
        tv.setTextSize(20);
        tv.setBackgroundResource(R.drawable.your_message_background);
        tv.setPadding(20, 20, 20, 20);

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.RIGHT;

        p.setMargins(40, 20, 40, 20);
        tv.setLayoutParams(p);

        fl.addView(tv);

        LinearLayout chatbox = findViewById(R.id.chatBox);
        chatbox.addView(fl, 0);
    }

    public String getId(){
        SendMessage n = new SendMessage();
        n.execute("getId");
        try {
            return n.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void getMsg(String message) {
        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        fl.setRotation(180);

        TextView tv = new TextView(this);

        tv.setText(message);
        tv.setTextSize(20);
        tv.setBackgroundResource(R.drawable.opponent_message_background);
        tv.setPadding(20, 20, 20, 20);

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.LEFT;

        p.setMargins(40, 20, 40, 20);
        tv.setLayoutParams(p);

        fl.addView(tv);

        LinearLayout chatbox = findViewById(R.id.chatBox);
        chatbox.addView(fl, 0);
    }

    class GetMessage extends AsyncTask<Void, String, Void> {

        private String server = ServerSettings.url;

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            getMsg(values[0]);
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);

// Java
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Ваш собеседник завершил диалог", Toast.LENGTH_LONG);
            toast.show();
            MainActivity.this.setContentView(R.layout.activity_start_light_version);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (dialogIsOver(MainActivity.this.user_id)) {
                    break;
                }
                String message = this.getMessage(user_id);
                if (message.length() > 0) {
                    publishProgress(message);
                }
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private boolean dialogIsOver(String id) {
            String url = this.server + "?action=isClosed&user_id=" + id;
            final String USER_AGENT = "Mozilla/5.0";


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return response.toString().equals("1");

        }

        private String getMessage(String id) {
            String url = this.server + "?action=getMessage&user=" + id;
            final String USER_AGENT = "Mozilla/5.0";


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return response.toString();
        }
    }

    class Search extends AsyncTask<String, Void, Void> {

        final private String server = ServerSettings.url;
        private String opponent_id;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.setContentView(R.layout.activity_opponent_searching);


                   /* Toast toast = Toast.makeText(getApplicationContext(),
                            "id - " + MainActivity.this.user_id, Toast.LENGTH_SHORT);
                    toast.show();*/

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity.this.setContentView(R.layout.activity_chat);

            MainActivity.this.gm = new GetMessage();
            startAsyncTaskInParallel(gm);

            /*Toast toast = Toast.makeText(getApplicationContext(),
                    "opp - " + MainActivity.this.opponent_id, Toast.LENGTH_SHORT);
            toast.show();*/
        }

        @Override
        protected Void doInBackground(String... parameter) {

            String inQueue_response = "null";
            String user_id = MainActivity.this.user_id;
            inQueue_response = getInLine(user_id);


            String opponent = "";

            while (opponent.length() <= 0) {
                opponent = check_pair(user_id);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            MainActivity.this.opponent_id = opponent;

            return null;
        }

        private String accept(String id) {
            String url = this.server + "?action=accept&user_id=" + id;
            final String USER_AGENT = "Mozilla/5.0";


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        private String check_pair(String id) {
            String url = this.server + "?action=checkPair&user_id=" + id;
            final String USER_AGENT = "Mozilla/5.0";


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        private String getInLine(String id) {

            String url = this.server + "?action=getInLine&user_id=" + id;
            final String USER_AGENT = "Mozilla/5.0";


            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.toString();
        }

    }


    class CloseChat extends AsyncTask<String, Void, Void> {

        final private String server = ServerSettings.url;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... parameter) {
            closeDialog(MainActivity.this.user_id);
            return null;
        }

        private void closeDialog(String id) {
            String url = this.server + "?action=closeDialog&user_id=" + id;
            final String USER_AGENT = "Mozilla/5.0";

            URL obj = null;
            try {
                obj = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = "";
            StringBuffer response = new StringBuffer();

            while (true) {
                try {
                    if (in != null)
                        if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.append(inputLine);
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

