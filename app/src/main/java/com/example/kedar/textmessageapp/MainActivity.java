package com.example.kedar.textmessageapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    SmsManager smsManager = SmsManager.getDefault();
    SmsMessage[] messages;

    TextView display;
    TextView stateView;

    String[] greetings = {"hi","hello","hey","yo"};
    String[] endings = {"bye","cya","goodbye","ttly"};
    String[] breakups = {"it isn't you, it's me. But, I think we need to take a break","I think we should see other people",
            "my parents don't think you're a good influence on me, so I have to break up with you"};
    String[] confuseds = {"what?","why","????","idk what to say"};
    String[] friendly = {"we can still be friends", "I hope there's no hard feelings", "I understand if you're mad at me"};
    ArrayList<String> greetingList = new ArrayList<>(Arrays.asList(greetings));
    ArrayList<String> endingList = new ArrayList<>(Arrays.asList(endings));
    ArrayList<String> breakupList = new ArrayList<>(Arrays.asList(breakups));
    ArrayList<String> confusedList = new ArrayList<>(Arrays.asList(confuseds));
    ArrayList<String> friendlyList = new ArrayList<>(Arrays.asList(friendly));
    BroadcastReceiver receiver;
    IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_PHONE_STATE};
                requestPermissions(permissions, 1);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.RECEIVE_SMS};
                requestPermissions(permissions, 1);
            }
        }


        Log.d("TAG",Boolean.toString(checkSelfPermission(Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED));
        Log.d("TAG1",Boolean.toString(checkSelfPermission(Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED));

        display = (TextView)findViewById(R.id.display);
        stateView = (TextView)findViewById(R.id.stateView);
        receiver = new textReceiver();
        registerReceiver(receiver, filter);
    }

    public class textReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[])bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for(int i=0;i<pdus.length;i++){
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i],bundle.getString("format"));
            }

            final String originatingAddress = messages[0].getOriginatingAddress();
            final String messageBody = messages[0].getMessageBody().toLowerCase();
            display.setText("Last Message: "+messageBody);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(state==0 && greetingList.contains(messageBody)){
                        stateView.setText("Greeting");
                        String response = greetingList.get((int) (Math.random()*greetingList.size()));
                        smsManager.sendTextMessage(originatingAddress,null,response,null,null);
                        response = breakupList.get((int) (Math.random()*breakupList.size()));
                        smsManager.sendTextMessage(originatingAddress,null,response,null,null);
                        state++;
                    }

                    else if(state==1 && confusedList.contains(messageBody)){
                        stateView.setText("Breakup");
                        String response = friendlyList.get((int) (Math.random()*friendlyList.size()));
                        smsManager.sendTextMessage(originatingAddress,null,response,null,null);
                        response = "Anyway, I gotta go now";
                        smsManager.sendTextMessage(originatingAddress,null,response,null,null);
                        state++;
                    }
                    else if(state==2 && endingList.contains(messageBody)){
                        stateView.setText("Ending");
                        String response = endingList.get((int) (Math.random()*endingList.size()));
                        smsManager.sendTextMessage(originatingAddress,null,response,null,null);
                        state++;
                    }
                    else{
                        smsManager.sendTextMessage(originatingAddress,null,"I don't understand",null,null);
                    }
                }
            },3000);
        }
    }
}
