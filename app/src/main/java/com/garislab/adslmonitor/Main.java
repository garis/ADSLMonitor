package com.garislab.adslmonitor;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {


    DecimalFormat decimalFormat = new DecimalFormat("#0.00#");

    private Handler mHandler = new Handler();

    private boolean loop = false;
    String result = "";
    myAsyncTask readSpeed;

    //chart stuffs
    LineChart chartDownSpeed;
    LineChart chartUpSpeed;
    LineChart chartHLog;
    LineChart chartSNR;
    LineChart chartQLN;
    LineChart chartBIT;

    List<Entry> entriesSpeedDown = new ArrayList<>();
    List<Entry> entriesSpeedUp = new ArrayList<>();
    List<Entry> entriesHLog = new ArrayList<>();
    List<Entry> entriesSNR = new ArrayList<>();
    List<Entry> entriesQLN = new ArrayList<>();
    List<Entry> entriesBIT = new ArrayList<>();

    List<ILineDataSet> dataSetDown;
    List<ILineDataSet> dataSetUp;
    List<ILineDataSet> dataSetHlog;
    List<ILineDataSet> dataSetSNR;
    List<ILineDataSet> dataSetQLN;
    List<ILineDataSet> dataSetBIT;

    LineData dataDown;
    LineData dataUp;
    LineData dataHlog;
    LineData dataSNR;
    LineData dataQLN;
    LineData dataBIT;

    LineDataSet setCompDown;
    LineDataSet setCompUp;
    LineDataSet setCompHlog;
    LineDataSet setCompSNR;
    LineDataSet setCompQLN;
    LineDataSet setCompBIT;

    //workaround for "glitch on SNR QLN HLog graphs
    int update = 0;

    float downValue = 0;
    float upValue = 0;
    int time = 0;
    float interval = 1000;
    boolean drawHlogAndStuffs = false;
    boolean HlogGraphUpdate = false;
    boolean SNRGraphUpdate = false;
    boolean QLNGraphUpdate = false;
    boolean BITGraphUpdate = false;


    final int GRAPH_MAX_TIME = 60;

    //Credential stuffs
    private SharedPreferences mPrefs;
    Credential c = new Credential();
    private String address;
    private String userSSH;
    private String passwordSSH;
    private String userSH;
    private String passwordSH;

    BufferedReader fromServer;
    OutputStream toServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullscreen();

        //credential
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        address = mPrefs.getString("address", "null");
        userSSH = mPrefs.getString("userSSH", "null");
        passwordSSH = mPrefs.getString("passwordSSH", "null");
        userSH = mPrefs.getString("userSH", "null");
        passwordSH = mPrefs.getString("passwordSH", "null");

        //######################################################################### DOWN
        chartDownSpeed = findViewById(R.id.chartDownSpeed);
        entriesSpeedDown.add(new Entry(0, 0));
        setCompDown = new LineDataSet(entriesSpeedDown, "Down speed");
        setCompDown.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetDown = new ArrayList<>();
        dataSetDown.add(setCompDown);
        dataDown = new LineData(dataSetDown);
        chartDownSpeed.setData(dataDown);
        themeLineSpeedDown(setCompDown);
        chartDownSpeed.invalidate(); // refresh

        //######################################################################### UP
        chartUpSpeed = findViewById(R.id.chartUpSpeed);
        entriesSpeedUp.add(new Entry(0, 0));
        setCompUp = new LineDataSet(entriesSpeedUp, "Up speed");
        setCompUp.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetUp = new ArrayList<>();
        dataSetUp.add(setCompUp);
        dataUp = new LineData(dataSetUp);
        chartUpSpeed.setData(dataUp);
        themeLineSpeedUp(setCompUp);
        chartUpSpeed.invalidate(); // refresh

        //######################################################################### HLOG
        chartHLog = findViewById(R.id.chartHLog);
        entriesHLog.add(new Entry(0, 0));
        setCompHlog = new LineDataSet(entriesHLog, "H log");
        setCompHlog.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetHlog = new ArrayList<>();
        dataSetHlog.add(setCompHlog);
        dataHlog = new LineData(dataSetHlog);
        chartHLog.setData(dataHlog);
        chartHLog.invalidate(); // refresh

        //######################################################################### SNR
        chartSNR = findViewById(R.id.chartSNR);
        entriesSNR.add(new Entry(0, 0));
        setCompSNR = new LineDataSet(entriesSNR, "SNR");
        setCompSNR.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetSNR = new ArrayList<>();
        dataSetSNR.add(setCompSNR);
        dataSNR = new LineData(dataSetSNR);
        chartSNR.setData(dataSNR);
        chartSNR.invalidate(); // refresh

        //######################################################################### QLN
        chartQLN = findViewById(R.id.chartQLN);
        entriesQLN.add(new Entry(0, 0));
        setCompQLN = new LineDataSet(entriesQLN, "QLN");
        setCompQLN.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetQLN = new ArrayList<>();
        dataSetQLN.add(setCompQLN);
        dataQLN = new LineData(dataSetQLN);
        chartQLN.setData(dataQLN);
        chartQLN.invalidate(); // refresh

        //######################################################################### Bit alloc
        chartBIT = findViewById(R.id.chartBIT);
        entriesBIT.add(new Entry(0, 0));
        setCompBIT = new LineDataSet(entriesBIT, "BIT");
        setCompBIT.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetBIT = new ArrayList<>();
        dataSetBIT.add(setCompBIT);
        dataBIT = new LineData(dataSetBIT);
        chartBIT.setData(dataBIT);
        chartBIT.invalidate(); // refresh

        final Button button = findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (c != null) {
                    address = c.getAddress();
                    userSSH = c.getUserSSH();
                    passwordSSH = c.getPasswordSSH();
                    userSH = c.getUserSH();
                    passwordSH = c.getPasswordSH();
                }

                if (!loop) {
                    button.setText(R.string.buttontext);

                    //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                    //notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

                    readSpeed = new myAsyncTask();
                    loop = true;
                    readSpeed.execute();
                } else {
                    button.setText(R.string.buttontextstart);
                    loop = false;
                    readSpeed.cancel(false);
                }
            }
        });

        final Button buttonUpdate = findViewById(R.id.buttonUpdateGraph);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //on the update speed loop this will draw the graph on time
                drawHlogAndStuffs = true;
                update = 2;
            }
        });
        final EditText et1 = findViewById(R.id.editTextUpdateInterval);
        et1.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        interval = Float.parseFloat(s.toString()) * 1000;
                    } catch (Exception e) {
                        interval = 1000;
                    }
                }
            }
        });

        final Button credential = findViewById(R.id.credential);
        credential.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                c = new Credential();
                c.setAddress(address);
                c.setUserSSH(userSSH);
                c.setPasswordSSH(passwordSSH);
                c.setUserSH(userSH);
                c.setPasswordSH(passwordSH);
                c.show(ft, "w");
            }
        });

        if (address.compareTo("null") == 0 || address.compareTo("null") == 0) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            c = new Credential();
            c.setAddress(address);
            c.setUserSSH(userSSH);
            c.setPasswordSSH(passwordSSH);
            c.setUserSH(userSH);
            c.setPasswordSH(passwordSH);
            c.show(ft, "w");
        } else {
            c = null;
        }
    }

    public void onResume(){
        super.onResume();
        //readSpeed = new myAsyncTask();
        //loop = true;
        //readSpeed.execute();
    }

    public void onStop() {
        super.onStop();
        loop = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("address", address);
        ed.putString("userSSH", userSSH);
        ed.putString("passwordSSH", passwordSSH);
        ed.putString("userSH", userSH);
        ed.putString("passwordSH", passwordSH);
        ed.apply();
    }

    @SuppressLint("StaticFieldLeak")
    protected class myAsyncTask extends AsyncTask<Void, Void, Void> {

        float oldRXValue = -1, oldTXValue = -1;

        @Override
        protected Void doInBackground(Void... params) {
            onProgressUpdate("L");

            try {

                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                Session session = jsch.getSession(userSSH, address, 22);
                session.setPassword(passwordSSH);
                session.setConfig(config);
                session.connect();
                System.out.println("Connected");

                Channel channel = session.openChannel("shell");

                fromServer = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                toServer = channel.getOutputStream();
                channel.connect();
                loginShell(fromServer, toServer, userSH, passwordSH);

                float timeMillis;
                long prevTime = 0;

                //used for compensating time consuming operation in the while(true) loop.
                long loopTimeStart;
                String line;
                float RXValue, TXValue;
                //reading cumulative data
                while (loop) {
                    loopTimeStart = System.currentTimeMillis();

                    //ip -s link show ptm0
                    toServer.write(("ip -s link show ptm0" + "\r\n").getBytes());
                    toServer.flush();

                    line = fromServer.readLine();
                    while (!line.contains("RX"))
                        line = fromServer.readLine();
                    line = fromServer.readLine();


                    timeMillis = (float) ((System.currentTimeMillis() - prevTime)) / 1000F;
                    prevTime = System.currentTimeMillis();

                    RXValue = Float.parseFloat(line.split(" ")[4]) / 1000000;
                    fromServer.readLine();
                    line = fromServer.readLine();
                    TXValue = Float.parseFloat(line.split(" ")[4]) / 1000000;

                    downValue = (RXValue - oldRXValue) / timeMillis;
                    upValue = (TXValue - oldTXValue) / timeMillis;

                    if (oldRXValue > 0) {
                        result = "↓" + decimalFormat.format(downValue) + " MiB/s \t↑"
                                + decimalFormat.format(upValue) + " MiB/s";
                        onProgressUpdate("P");
                    }
                    oldRXValue = RXValue;
                    oldTXValue = TXValue;

                    if (drawHlogAndStuffs) {
                        drawHlog(fromServer, toServer);
                        drawSNR(fromServer, toServer);
                        drawQLN(fromServer, toServer);
                        drawBIT(fromServer, toServer);
                        drawHlogAndStuffs = !drawHlogAndStuffs;
                    }
                    waitPeriod(System.currentTimeMillis() - loopTimeStart);
                }
                onProgressUpdate("S");
                toServer.write(("exit" + "\r\n").getBytes());
                toServer.flush();
                fromServer.readLine();
                toServer.write(("exit" + "\r\n").getBytes());
                toServer.flush();
                fromServer.readLine();
            } catch (Exception e) {
                e.printStackTrace();
                onProgressUpdate("E");
            }
            return null;
        }

        private synchronized void drawHlog(BufferedReader fromServer, OutputStream toServer) throws IOException {
            toServer.write(("xdslctl info --Hlog" + "\r\n").getBytes());
            toServer.flush();
            String retLine = fromServer.readLine();
            while (!retLine.contains("Tone"))
                retLine = fromServer.readLine();
            StringBuilder strb = new StringBuilder();
            while (!retLine.contains("8191")) {
                retLine = fromServer.readLine();
                strb.append(retLine);
            }

            String[] strVect = strb.toString().split(getString(R.string.separator));

            LineData data = chartHLog.getData();
            data.clearValues();

            List<Entry> valsComp1 = new ArrayList<>();

            for (int i = 1; i < strVect.length - 1; i++) {
                String[] line = strVect[i].split("\t\t");
                int tone = Integer.parseInt(line[0].replaceAll(" ", ""));
                float value = Float.parseFloat(line[1].replaceAll(" ", ""));
                valsComp1.add(new Entry(tone, value));
            }
            LineDataSet setComp1 = new LineDataSet(valsComp1, "Hlog");
            themeLineaParams(setComp1);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);
            data = new LineData(dataSets);
            chartHLog.setData(data);
            chartHLog.invalidate(); // refresh
            HlogGraphUpdate = false;

        }

        private synchronized void drawSNR(BufferedReader fromServer, OutputStream toServer) throws IOException {
            toServer.write(("xdslctl info --SNR" + "\r\n").getBytes());
            toServer.flush();
            String retLine = fromServer.readLine();
            while (!retLine.contains("Tone"))
                retLine = fromServer.readLine();
            StringBuilder strb = new StringBuilder();
            while (!retLine.contains("8191")) {
                retLine = fromServer.readLine();
                strb.append(retLine);
            }

            String[] strVect = strb.toString().split(getString(R.string.separator));

            LineData data = chartSNR.getData();
            data.clearValues();

            List<Entry> valsComp1 = new ArrayList<>();

            for (int i = 1; i < strVect.length - 1; i++) {
                String[] line = strVect[i].split("\t\t");
                int tone = Integer.parseInt(line[0].replaceAll(" ", ""));
                float value = Float.parseFloat(line[1].replaceAll(" ", ""));
                valsComp1.add(new Entry(tone, value));
            }
            LineDataSet setComp1 = new LineDataSet(valsComp1, "SNR");
            themeLineaParams(setComp1);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);
            data = new LineData(dataSets);
            chartSNR.setData(data);
            chartSNR.invalidate(); // refresh
            SNRGraphUpdate = false;
        }

        private synchronized void drawQLN(BufferedReader fromServer, OutputStream toServer) throws IOException {
            toServer.write(("xdslctl info --QLN" + "\r\n").getBytes());
            toServer.flush();
            String retLine = fromServer.readLine();
            while (!retLine.contains("Tone"))
                retLine = fromServer.readLine();
            StringBuilder strb = new StringBuilder();
            while (!retLine.contains("8191")) {
                retLine = fromServer.readLine();
                strb.append(retLine);
            }

            String[] strVect = strb.toString().split(getString(R.string.separator));

            LineData data = chartQLN.getData();
            data.clearValues();

            List<Entry> valsComp1 = new ArrayList<>();

            for (int i = 1; i < strVect.length - 1; i++) {
                String[] line = strVect[i].split("\t\t");
                int tone = Integer.parseInt(line[0].replaceAll(" ", ""));
                float value = Float.parseFloat(line[1].replaceAll(" ", ""));
                valsComp1.add(new Entry(tone, value));
            }
            LineDataSet setComp1 = new LineDataSet(valsComp1, "QLN");

            themeLineaParams(setComp1);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);
            data = new LineData(dataSets);
            chartQLN.setData(data);
            chartQLN.invalidate(); // refresh
            QLNGraphUpdate = false;
        }

        private synchronized void drawBIT(BufferedReader fromServer, OutputStream toServer) throws IOException {
            toServer.write(("xdslctl info --Bits" + "\r\n").getBytes());
            toServer.flush();
            String retLine = fromServer.readLine();
            while (!retLine.contains("Tone"))
                retLine = fromServer.readLine();
            StringBuilder strb = new StringBuilder();
            while (!retLine.contains("8191")) {
                retLine = fromServer.readLine();
                strb.append(retLine);
            }

            String[] strVect = strb.toString().split(getString(R.string.separator));

            LineData data = chartBIT.getData();
            data.clearValues();

            List<Entry> valsComp1 = new ArrayList<>();

            for (int i = 1; i < strVect.length - 1; i++) {
                String[] line = strVect[i].split("\t\t");
                int tone = Integer.parseInt(line[0].replaceAll(" ", ""));
                float value = Float.parseFloat(line[1].replaceAll(" ", ""));
                valsComp1.add(new Entry(tone, value));
            }
            LineDataSet setComp1 = new LineDataSet(valsComp1, "BIT");

            themeLineaParams(setComp1);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);
            data = new LineData(dataSets);
            chartBIT.setData(data);
            chartBIT.invalidate(); // refresh
            BITGraphUpdate = false;
        }

        private void themeLineaParams(LineDataSet setComp1) {
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setColor(Color.BLUE);
            setComp1.setCircleColor(Color.WHITE);
            setComp1.setLineWidth(1f);
            setComp1.setCircleRadius(0);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setCircleColorHole(Color.BLUE);
            setComp1.setDrawCircles(false);
            setComp1.setDrawCircleHole(false);
            setComp1.setHighlightEnabled(false);
            setComp1.setDrawValues(false);
            setComp1.setDrawFilled(false);
            setComp1.setMode(LineDataSet.Mode.LINEAR);
        }


        String loginShell(BufferedReader fromServer, OutputStream toServer, String user, String pass) {

            try {
                toServer.write(("sh" + "\r\n").getBytes());
                toServer.flush();
                fromServer.readLine();

                Thread.sleep(40);

                toServer.write((user + "\n").getBytes());
                toServer.flush();
                fromServer.readLine();

                Thread.sleep(40);

                toServer.write((pass + "\n").getBytes());
                toServer.flush();
                fromServer.readLine();

                Thread.sleep(40);

                toServer.write(("date" + "\n").getBytes());
                toServer.flush();
                fromServer.readLine();

                Thread.sleep(40);

                String line;
                StringBuilder builder = new StringBuilder();

                while (true) {
                    line = fromServer.readLine();
                    builder.append(line);
                    if (line.contains("#"))
                        break;
                }
                return builder.toString();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        private synchronized void waitPeriod(long correction) {
            try {
                if (correction < interval)
                    this.wait((long) (interval) - correction);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void onProgressUpdate(String... params) {
            char c = params[0].charAt(0);
            switch (c) {
                case 'L':
                    break;
                case 'P'://update

                    break;
                case 'S'://stop
                    break;
                default:
                    result = "what!?";
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ((EditText) findViewById(R.id.textViewSpeed)).setText(result);

                    LineData data = chartDownSpeed.getData();
                    if (data != null) {
                        data.addEntry(new Entry(time, downValue), 0);

                        if (time > GRAPH_MAX_TIME)
                            data.removeEntry(0, 0);

                        chartDownSpeed.moveViewToX(data.getEntryCount());
                        chartDownSpeed.notifyDataSetChanged();
                    }

                    data = chartUpSpeed.getData();
                    if (data != null) {
                        data.addEntry(new Entry(time, upValue), 0);

                        if (time > GRAPH_MAX_TIME)
                            data.removeEntry(0, 0);

                        chartUpSpeed.moveViewToX(data.getEntryCount());
                        chartUpSpeed.notifyDataSetChanged();
                    }

                    if (dataHlog != null && !HlogGraphUpdate) {
                        HlogGraphUpdate = true;
                    }
                    if (dataSNR != null && !SNRGraphUpdate) {
                        SNRGraphUpdate = true;
                    }
                    if (dataQLN != null && !QLNGraphUpdate) {
                        QLNGraphUpdate = true;
                    }
                    if (update > 0)
                        update--;
                    time++;
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void themeLineSpeedDown(LineDataSet setComp1) {
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.GREEN);
        setComp1.setCircleColor(Color.GREEN);
        setComp1.setLineWidth(2f);
        setComp1.setCircleRadius(0);
        setComp1.setCircleColor(Color.GREEN);
        setComp1.setCircleColorHole(Color.GREEN);
        setComp1.setDrawCircles(false);
        setComp1.setDrawCircleHole(false);
        setComp1.setHighlightEnabled(false);
        setComp1.setFillColor(ColorTemplate.getHoloBlue());
        setComp1.setDrawValues(false);
        setComp1.setDrawFilled(false);
        setComp1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setComp1.setCubicIntensity(0.2f);
    }

    private void themeLineSpeedUp(LineDataSet setComp1) {

        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.RED);
        setComp1.setCircleColor(Color.RED);
        setComp1.setLineWidth(2f);
        setComp1.setCircleRadius(0);
        setComp1.setCircleColor(Color.RED);
        setComp1.setCircleColorHole(Color.RED);
        setComp1.setDrawCircles(false);
        setComp1.setDrawCircleHole(false);
        setComp1.setHighlightEnabled(false);
        setComp1.setFillColor(ColorTemplate.getHoloBlue());
        setComp1.setDrawValues(false);
        setComp1.setDrawFilled(false);
        setComp1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setComp1.setCubicIntensity(0.2f);
    }

    private void fullscreen() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
