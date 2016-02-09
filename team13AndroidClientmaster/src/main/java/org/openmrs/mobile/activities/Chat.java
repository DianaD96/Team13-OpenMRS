package org.openmrs.mobile.activities;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.fragments.ApiAuthRest;



import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

public class Chat extends Activity {

    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = false;
    String comment = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        Button input = (Button) findViewById(R.id.chat_input);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Chat.this, DashboardActivity.class);
                startActivity(i);
            }
        });

        Button graph = (Button) findViewById(R.id.chat_graph);
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Chat.this, Graph.class);
                startActivity(i);
            }
        });

        Button chat = (Button) findViewById(R.id.chat_chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Chat.this, Chat.class);
                startActivity(i);
            }
        });

        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
                sendData();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        try {
            getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        comment=chatText.getText().toString();
        chatText.setText("");
        side = !side;
        return true;
    }

    private boolean getChatMessage(String s) {
        chatArrayAdapter.add(new ChatMessage(side, s));
        //comment=chatText.getText().toString();
        chatText.setText("");
        side = !side;
        return true;
    }

    public void sendData()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ApiAuthRest.setURLBase(Container.URLBase);
        ApiAuthRest.setUsername(Container.username);
        ApiAuthRest.setPassword(Container.password);

        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" +c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

        final String JSONComment= "{\"obsDatetime\": \"" + date + "\"" +
                ", \"concept\": \"" + Container.chat_uuid + "\"" +
                ", \"value\": \"" + comment + "\"" +
                ", \"person\": \"" + Container.user_uuid + "\"}";

        StringEntity inputComment = null;
        try {
            inputComment = new StringEntity(JSONComment);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        inputComment.setContentType("application/json");
        try {
            Log.i("OpenMRS response", "Comment Added = " + ApiAuthRest.getRequestPost("obs", inputComment));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getData() throws Exception {
        /*
	 * SET VALUE FOR CONNECT TO OPENMRS
	 */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ApiAuthRest.setURLBase("http://bupaopenmrs.cloudapp.net/openmrs/ws/rest/v1/");
        ApiAuthRest.setUsername("diana");
        ApiAuthRest.setPassword("Admin123");


 	/*
 	 * Example how parse json return session
 	 */

        String request = "obs?patient=" + Container.user_uuid + "&concept=" + Container.chat_uuid;
        System.out.println("########################");
        System.out.println("Search the persons that have name  JOHN");
        Object obj = ApiAuthRest.getRequestGet(request);
        JSONObject jsonObject = new JSONObject ((String) obj);
        JSONArray arrayResult = (JSONArray) jsonObject.get("results");

        System.out.println("########################");
        int itemArray = arrayResult.length();
        int iterator;
        ArrayList<String> array = new ArrayList<String>();
        for (iterator = 0; iterator < itemArray; iterator++) {
            JSONObject data = (JSONObject) arrayResult.get(iterator);
            String uuid = (String) data.get("uuid");
            String display = (String) data.get("display");
            System.out.println("Rows " + iterator + " => Result OBS UUID:" + uuid + " Display:" + display.substring(7));
            if(array.size()<15) {
                array.add(display.substring(7));
                getChatMessage(display.substring(7));
            }


            /*/
            //Show ROWS LINKS
            JSONArray arrayResultLinks = (JSONArray) data.get("links");
            int largoArrayLinks = arrayResultLinks.length();
            int contadorLinks;
            for (contadorLinks = 0; contadorLinks < largoArrayLinks; contadorLinks++) {
                JSONObject registroLink = (JSONObject) arrayResultLinks.get(contadorLinks);
                String uri = (String) registroLink.get("uri");
                String rel = (String) registroLink.get("rel");
                System.out.println("==>Record Row " + iterator + "." + contadorLinks
                        + " =>  URI:" + uri + " REL:" + rel);

            }
            /*/
            System.out.println("########################");
        }
    }
}

