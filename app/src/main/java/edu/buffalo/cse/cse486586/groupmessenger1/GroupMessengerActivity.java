package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import android.widget.EditText;
/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    // setting up the avd port as done in PA1.

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final int SERVER_PORT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        // code taken from PA1

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        final EditText editText = (EditText) findViewById(R.id.editText1);

        /* from android official documents */

        final Button button = (Button) findViewById(R.id.button4);

        // registering and implementing the OnClickListener

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Code taken from PA1

                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView Text_1 = (TextView) findViewById(R.id.textView1);
                Text_1.append("\t" + msg);

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }
        });
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;

    }

    /* Following the Template of PA1 code

     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String messages = null;
            int msg_seq = 0;


            try {
                while (true) {
                    Socket socket = serverSocket.accept();

                    InputStream in = socket.getInputStream();
                    DataInputStream data = new DataInputStream(in);
                    messages = data.readUTF();


                    OutputStream out = socket.getOutputStream();
                    DataOutputStream dout = new DataOutputStream(out);

                    dout.writeUTF("OK");
                    publishProgress(messages);

                    Uri build = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");

                   // Snippet as  given in the instructions in PA2 description

                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put("key", msg_seq++);
                    keyValueToInsert.put("value", messages);
                    Uri newUri = getContentResolver().insert(
                            build,
                            keyValueToInsert
                    );

                    in.close();
                    data.close();
                    out.close();
                    dout.close();
                    socket.close();
                }

            } catch (Exception e) {
                Log.e(TAG, "Server error" + e);
            }
             /*

             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }


        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
            */

           // Log.v(TAG, "Check statement");

            String strReceived = strings[0].trim();
            TextView Text_1 = (TextView) findViewById(R.id.textView1);
            Text_1.append(strReceived + "\t\n");



        }
    }

        /*client task for 5 avds
        Following the code template of PA1

         */

        private class ClientTask extends AsyncTask<String, Void, Void> {

            @Override
            protected Void doInBackground(String... msgs) {


                String remotePort[] = new String[5];
                remotePort[0] = REMOTE_PORT0;
                remotePort[1] = REMOTE_PORT1;
                remotePort[2] = REMOTE_PORT2;
                remotePort[3] = REMOTE_PORT3;
                remotePort[4] = REMOTE_PORT4;


                try {

                    for (int i = 0; i < remotePort.length; i++)
                    {

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort[i]));


                        String msgToSend = msgs[0];
                        Log.v(TAG, "ClientSocket" + remotePort[i]);

                        OutputStream out = socket.getOutputStream();
                        DataOutputStream d = new DataOutputStream(out);
                        d.writeUTF(msgToSend);

                        String ack;

                        InputStream in = socket.getInputStream();
                        DataInputStream din = new DataInputStream(in);

                        ack = din.readUTF();

                        if (ack.equals("OK"))
                            socket.close();

                        out.close();
                        d.close();
                        in.close();
                        din.close();

                    }
                }
                catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ClientTask socket IOException");
                }
                return null;
            }

        }
    }


