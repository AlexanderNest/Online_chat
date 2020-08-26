package com.example.chat3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_settings);
        showLicense();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.first = false;
        this.second = false;

        GetMessage gm = new GetMessage();
        startAsyncTaskInParallel(gm);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.support:
                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(ServerSettings.telegram));
                startActivity(browserIntent);

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
                Toast toast = Toast.makeText(getApplicationContext(), "Выберите ваш пол",
                        Toast.LENGTH_LONG);
                toast.show();
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
        setContentView(R.layout.activity_opponent_searching);
    }

    public void stopSearching(View view) {
        setContentView(R.layout.activity_start_settings);
    }

    /**
     * Отправляет сообщение собеседнику, выводит на экран сообщение
     * обработчик кнопки
     */
    public void sendMsgBtn(View view) {
        if (!this.first) {
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
        }
        if (((EditText) findViewById(R.id.messageBox)).getText().toString().equals("image")) {
            FrameLayout fl = new FrameLayout(this);
            fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            fl.setRotation(180);

            ImageView image = new ImageView(this);
            image.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            image.setImageResource(R.drawable.messenger_button_white_bg_selector);
        }

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

        SendMessage n = new SendMessage();


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

        //editText.setText("");

        /*TextView msg = new TextView(this);
        msg.setText(message);
        msg.setLayoutParams( new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        msg.setSingleLine(false);

        TextView status = new TextView(this);


        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params=  new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(600, 50,50,0);
        layout.setLayoutParams(params);
        layout.addView(msg);
        layout.setRotation(180);
        layout.setBackgroundColor(getResources().getColor(R.color.colorGetter));
        layout.setGravity(Gravity.LEFT);
        layout.setPadding(10,10,10,10);

        LinearLayout chatbox = (LinearLayout)  findViewById(R.id.chatBox);
        chatbox.addView(layout, 0);*/


    }

    class GetMessage extends AsyncTask<Void, String, Void> {  // получение погоды в фоне

        private String server = ServerSettings.url;

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            getMsg(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                String message = this.getMessage(user_id);
                if (message.length() > 0) {
                    publishProgress(message);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
}

