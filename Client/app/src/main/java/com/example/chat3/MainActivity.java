package com.example.chat3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Отправляет сообщение собеседнику, выводит на экран сообщение
     * обработчик кнопки
     */
    public void sendMsgBtn(View view){

        EditText editText = (EditText) findViewById(R.id.messageBox);
        String message = editText.getText().toString();
        editText.setText("");

        TextView msg = new TextView(this);
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
        params.setMargins(50, 50,600,0);
        layout.setLayoutParams(params);
        layout.addView(msg);
        layout.setRotation(180);
        layout.setBackgroundColor(getResources().getColor(R.color.colorSender));
        layout.setGravity(Gravity.RIGHT);
        layout.setPadding(10,10,10,10);

        LinearLayout chatbox = (LinearLayout)  findViewById(R.id.chatBox);
        chatbox.addView(layout, 0);
    }

    public void getMsg(View view){
        EditText editText = (EditText) findViewById(R.id.messageBox);
        String message = editText.getText().toString();
        editText.setText("");

        TextView msg = new TextView(this);
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
        chatbox.addView(layout, 0);
    }
}