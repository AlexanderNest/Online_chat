package com.nestapp.chat3;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    /**
     * Подобные методы предназначены для того, чтобы корректно запускать asynctask
     * в зависимости от версии android
     *
     * @param task
     */
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
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_start_light_version);
        this.getPreferences(); //
    }

    /**
     * Если пользователь еще не заходил в приложение, то выводим пользовательское соглашение
     */
    private void getPreferences() {
        SharedPreferences settings = getSharedPreferences("active ", MODE_PRIVATE);
        boolean hasVisited = settings.getBoolean("hasVisited", false);

        if (!hasVisited) {
            this.showLicense();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("hasVisited", true);
            editor.commit();
        }
    }


    /**
     * пользовательское меню вверху
     * функции:
     * - написать разработчику
     * - завершить диалог
     * - в разработке
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.support:
                // Кнопка "поддержка"
                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(ServerSettings.telegram));
                startActivity(browserIntent);
                break;
            case R.id.close_dialog:
                // кнопка завершить диалог
                this.search.cancel(true);
                startAsyncTaskInParallel(new CloseChat());  // работа с бд для завершения диалога
                gm.cancel(true);  // завершаем поиск сообщений
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

    /**
     * Создание окна демонстрации пользовательского соглашения
     */
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

    /**
     * Обработчик нажатия кнопки "Начать поиск"
     *
     * @param view
     */
    public void startSearching(View view) {
        this.user_id = getId();
        this.search = new Search();
        startAsyncTaskInParallel(this.search);
    }

    /**
     * Обработчик нажания кнопки "Остановить поиск"
     *
     * @param view
     */
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
        EditText editText = findViewById(R.id.messageBox);
        String message = editText.getText().toString();

        /**
         * Класс для отправки сообщения
         */
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


    /**
     * Полчение id пользователя в чате
     *
     * @return
     */
    public String getId() {
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

    /**
     * Вывод полученного сообщения на экран
     *
     * @param message
     */
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

    /**
     * Класс для получения сообщений, имеет доступ к интерфейсу
     * Отрисовка сообщений здесь
     */

    class GetMessage extends AsyncTask<Void, String, Void> {

        private String server = ServerSettings.url;

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            getMsg(values[0]); // отрисовка полученного сообщения
        }

        /**
         * Если завершился поток => собеседник завершил диалог
         * Информирование пользователя о завершении диалога
         *
         * @param avoid
         */
        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);


            Toast toast = Toast.makeText(getApplicationContext(),
                    "Ваш собеседник завершил диалог", Toast.LENGTH_LONG);
            toast.show();
            MainActivity.this.setContentView(R.layout.activity_start_light_version);
        }


        /**
         * Проверяем, завершен ли диалог, получаем новые сообщения
         *
         * @param voids
         * @return
         */
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

        /**
         * Проверка на окончаение диалога, работа с базой данных
         *
         * @param id
         * @return
         */
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

        /**
         * Получение новых сообщений из базы данных
         *
         * @param id идентификатор пользователя текущего
         * @return
         */
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

