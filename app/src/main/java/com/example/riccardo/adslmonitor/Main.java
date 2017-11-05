package com.example.riccardo.adslmonitor;

import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Main extends AppCompatActivity {

    DecimalFormat decimalFormat = new DecimalFormat("#0.00#");

    private Handler mHandler = new Handler();
    TextView statusText;

    private boolean loop = false;
    String result = "";
    myAsyncTask readSpeed;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;

    //chart stuffs
    LineChart chartDownSpeed;
    LineChart chartUpSpeed;
    LineChart chartHLog;
    LineChart chartSNR;
    LineChart chartQLN;
    LineData dataHlog;
    LineData dataSNR;
    LineData dataQLN;

    //workaround for "glitch on SNR QLN HLog graphs
    int update=0;

    float downValue = 0;
    float upValue = 0;
    int time = 0;
    float interval=1000;
    boolean drawHlogAndStuffs=false;
    boolean HlogGraphUpdate=false;
    boolean SNRGraphUpdate=false;
    boolean QLNGraphUpdate=false;
    final int NOTIFICATION_ID = 1;

    //Credential stuffs
    private SharedPreferences mPrefs;
    Credential c = new Credential();
    private String address;
    private String user;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //credential
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        address = mPrefs.getString("address", "null");
        user = mPrefs.getString("user", "null");
        password = mPrefs.getString("password", "null");

        chartDownSpeed = (LineChart) findViewById(R.id.chartDownSpeed);
        LineData dataD = new LineData();
        dataD.setValueTextColor(Color.WHITE);
        chartDownSpeed.setData(dataD);
        chartDownSpeed.setDrawBorders(false);
        chartDownSpeed.setDescription("");
        chartDownSpeed.setAutoScaleMinMaxEnabled(true);
        chartDownSpeed.setTouchEnabled(false);
        chartDownSpeed.getAxisLeft().setAxisMinValue(0.0f);
        chartDownSpeed.getAxisRight().setAxisMinValue(0.0f);

        chartUpSpeed = (LineChart) findViewById(R.id.chartUpSpeed);
        dataD = new LineData();
        dataD.setValueTextColor(Color.WHITE);
        chartUpSpeed.setData(dataD);
        chartUpSpeed.setDrawBorders(false);
        chartUpSpeed.setDescription("");
        chartUpSpeed.setAutoScaleMinMaxEnabled(true);
        chartUpSpeed.setTouchEnabled(false);
        chartUpSpeed.getAxisLeft().setAxisMinValue(0.0f);
        chartUpSpeed.getAxisRight().setAxisMinValue(0.0f);

        chartHLog = (LineChart) findViewById(R.id.chartHLog);
        dataD = new LineData();
        dataD.setValueTextColor(Color.WHITE);
        chartHLog.setData(dataD);
        chartHLog.setDrawBorders(false);
        chartHLog.setDescription("");
        chartHLog.setAutoScaleMinMaxEnabled(true);
        chartHLog.setTouchEnabled(false);

        chartSNR = (LineChart) findViewById(R.id.chartSNR);
        dataD = new LineData();
        dataD.setValueTextColor(Color.WHITE);
        chartSNR.setData(dataD);
        chartSNR.setDrawBorders(false);
        chartSNR.setDescription("");
        chartSNR.setAutoScaleMinMaxEnabled(true);
        chartSNR.setTouchEnabled(false);

        chartQLN = (LineChart) findViewById(R.id.chartQLN);
        dataD = new LineData();
        dataD.setValueTextColor(Color.WHITE);
        chartQLN.setData(dataD);
        chartQLN.setDrawBorders(false);
        chartQLN.setDescription("");
        chartQLN.setAutoScaleMinMaxEnabled(true);
        chartQLN.setTouchEnabled(false);

        notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        statusText = (TextView) findViewById(R.id.textView);

        final Button button = (Button) findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(c!=null) {
                    address = c.getAddress();
                    user = c.getUser();
                    password = c.getPassword();
                }

                if (!loop) {
                    button.setText(R.string.buttontext);
                    builder = new NotificationCompat.Builder(v.getContext())
                            .setAutoCancel(true);
                    builder.setSmallIcon(R.drawable.ic_launcher);
                    builder.setContentTitle("ADSL speed");
                    builder.setContentText("");

                    notificationManager.notify(NOTIFICATION_ID, builder.build());

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

        final Button buttonUpdate = (Button) findViewById(R.id.buttonUpdateGraph);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //on the update speed loop this will draw the graph on time
                drawHlogAndStuffs = true;
                update = 2;
            }
        });
        final EditText et1 = (EditText) findViewById(R.id.editTextUpdateInterval);
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

        final Button credential = (Button) findViewById(R.id.credential);
        credential.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                c = new Credential();
                c.setAddress(address);
                c.setUser(user);
                c.setPassword(password);
                c.show(ft, "w");
            }
        });

        if (address.compareTo("null") == 0 || address.compareTo("null") == 0) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            c = new Credential();
            c.setAddress(address);
            c.setUser(user);
            c.setPassword(password);
            c.show(ft, "w");
        }
        else
        {
            c=null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("address",address);
        ed.putString("user",user);
        ed.putString("password",password);
        ed.apply();
    }

    private class myAsyncTask extends AsyncTask<Void, Void, Void> {

        InputStream in;
        PrintStream out;
        String prompt = " >";

        float oldRXValue = -1, oldTXValue = -1;

        @Override
        protected Void doInBackground(Void... params) {
            onProgressUpdate("L");

            TelnetClient telnet = new TelnetClient();
            try {
                telnet.connect(address, 23);
                in = telnet.getInputStream();
                out = new PrintStream(telnet.getOutputStream());

                readUntil("Login:");
                write(user);
                readUntil("Password:");
                write(password);

                //login success

                readUntil(prompt);
                write("sh");
                readUntil("#");

                float timeMillis;
                long prevTime = 0;

                //used for compensating time consuming operation in the while(true) loop.
                long loopTimeStart;

                //reading cumulative data
                while (loop) {
                    loopTimeStart = System.currentTimeMillis();

                    write("ifconfig pppoa0");

                    timeMillis = (float) ((System.currentTimeMillis() - prevTime)) / 1000F;
                    prevTime = System.currentTimeMillis();
                    readUntil("RX bytes:");

                    float RXValue = Float.parseFloat(readUntil(" ")) / 1000000;
                    readUntil(":");
                    float TXValue = Float.parseFloat(readUntil(" ")) / 1000000;
                    readUntil("#");

                    downValue = (RXValue - oldRXValue) / timeMillis;
                    upValue = (TXValue - oldTXValue) / timeMillis;

                    if (oldRXValue > 0) {
                        result = "↓" + decimalFormat.format(downValue) + " MiB/s \t↑"
                                + decimalFormat.format(upValue) + " MiB/s";
                        onProgressUpdate("P");
                    }

                    oldRXValue = RXValue;
                    oldTXValue = TXValue;

                    if(drawHlogAndStuffs) {
                        drawHlog();drawSNR();drawQLN();
                        drawHlogAndStuffs=!drawHlogAndStuffs;
                    }
                    waitPeriod(System.currentTimeMillis() - loopTimeStart);
                }
                write("exit");
                write("exit");
            } catch (Exception e) {
                e.printStackTrace();
                onProgressUpdate("E");
            }
            return null;
        }
        private synchronized void drawHlog() {
            write("adsl info --Hlog");
            readUntil("Tone number      Hlog");
            String[] strVect=readUntil("#").split("\\r?\\n");

            ArrayList<Entry> values = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<>();

            for(int i=1;i<strVect.length-1;i++) {
                String[] line=strVect[i].split("          ");
                int tone=Integer.parseInt(line[0].replaceAll(" ",""));
                float value=Float.parseFloat(line[1].replaceAll(" ",""));
                Entry point = new Entry(value, tone); // 0 == quarter 1
                values.add(point);
                xVals.add(""+tone);
            }
            LineDataSet setHLog = new LineDataSet(values, "HLog");
            setHLog.setAxisDependency(YAxis.AxisDependency.LEFT);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setHLog);
            setHLog.setColor(Color.BLUE);
            setHLog.setCircleColor(Color.BLUE);
            setHLog.setCircleRadius(0.2f);
            setHLog.setLineWidth(2f);
            setHLog.setCircleColor(Color.BLUE);
            setHLog.setCircleColorHole(Color.BLUE);
            setHLog.setFillAlpha(65);
            setHLog.setFillColor(ColorTemplate.getHoloBlue());
            setHLog.setDrawValues(false);
            setHLog.setDrawFilled(false);
            setHLog.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            dataHlog = new LineData(xVals, dataSets);
            HlogGraphUpdate=false;
        }
        private synchronized void drawSNR() {
            write("adsl info --SNR");
            readUntil("Tone number      SNR");
            String[] strVect=readUntil("#").split("\\r?\\n");

            ArrayList<Entry> values = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<>();

            for(int i=1;i<strVect.length-1;i++) {
                String[] line=strVect[i].split("          ");
                int tone=Integer.parseInt(line[0].replaceAll(" ",""));
                float value=Float.parseFloat(line[1].replaceAll(" ",""));
                Entry point = new Entry(value, tone); // 0 == quarter 1
                values.add(point);
                xVals.add(""+tone);
            }
            LineDataSet setSNR = new LineDataSet(values, "SNR");
            setSNR.setAxisDependency(YAxis.AxisDependency.LEFT);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setSNR);
            setSNR.setColor(Color.BLUE);
            setSNR.setCircleColor(Color.BLUE);
            setSNR.setCircleRadius(0.2f);
            setSNR.setLineWidth(2f);
            setSNR.setCircleColor(Color.BLUE);
            setSNR.setCircleColorHole(Color.BLUE);
            setSNR.setFillAlpha(65);
            setSNR.setFillColor(ColorTemplate.getHoloBlue());
            setSNR.setDrawValues(false);
            setSNR.setDrawFilled(false);
            setSNR.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            dataSNR = new LineData(xVals, dataSets);
            SNRGraphUpdate=false;
        }
        private synchronized void drawQLN() {
            write("adsl info --QLN");
            readUntil("Tone number      QLN");
            String[] strVect=readUntil("#").split("\\r?\\n");

            ArrayList<Entry> values = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<>();

            for(int i=1;i<strVect.length-1;i++) {
                String[] line=strVect[i].split("          ");
                int tone=Integer.parseInt(line[0].replaceAll(" ",""));
                float value=Float.parseFloat(line[1].replaceAll(" ",""));
                Entry point = new Entry(value, tone); // 0 == quarter 1
                values.add(point);
                xVals.add(""+tone);
            }
            LineDataSet setQLN = new LineDataSet(values, "QLN");
            setQLN.setAxisDependency(YAxis.AxisDependency.LEFT);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setQLN);
            setQLN.setColor(Color.BLUE);
            setQLN.setCircleColor(Color.BLUE);
            setQLN.setCircleRadius(0.2f);
            setQLN.setLineWidth(2f);
            setQLN.setCircleColor(Color.BLUE);
            setQLN.setCircleColorHole(Color.BLUE);
            setQLN.setFillAlpha(65);
            setQLN.setFillColor(ColorTemplate.getHoloBlue());
            setQLN.setDrawValues(false);
            setQLN.setDrawFilled(false);
            setQLN.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            dataQLN = new LineData(xVals, dataSets);
            QLNGraphUpdate=false;
        }
        private synchronized void waitPeriod(long correction) {
            try {
                if (correction < interval)
                    this.wait((long)(interval) - correction);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String readUntil(String pattern) {

            StringBuilder sb = new StringBuilder();
            try {
                char lastChar = pattern.charAt(pattern.length() - 1);
                char ch = (char) in.read();
                while (true) {
                    sb.append(ch);
                    if (ch == lastChar) {
                        if (sb.toString().endsWith(pattern)) {
                            return sb.toString();
                        }
                    }
                    ch = (char) in.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        public void write(String value) {
            try {
                out.println(value);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void onProgressUpdate(String... params) {
            char c = params[0].charAt(0);
            switch (c) {
                case 'L':
                    break;
                case 'P':
                    builder.setContentText(result);
                    notificationManager.notify(
                            NOTIFICATION_ID,
                            builder.build());

                    break;
                default:
                    result = "what!?";
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // This gets executed on the UI thread so it can safely modify Views
                    final TextView statusText = (TextView) findViewById(R.id.textView);
                    statusText.setText(result);

                    LineData dataD = chartDownSpeed.getData();
                    LineData dataU = chartUpSpeed.getData();
                    if (dataD != null) {
                        ILineDataSet set = dataD.getDataSetByIndex(0);
                        if (set == null) {
                            set = createSetDown();
                            dataD.addDataSet(set);
                        }
                        dataD.addXValue("" + time);
                        dataD.addEntry(new Entry(downValue * 1000f, set.getEntryCount()), 0);

                        chartDownSpeed.setVisibleXRangeMaximum(60);
                        chartDownSpeed.moveViewToX(dataD.getXValCount() - 61);
                        chartDownSpeed.notifyDataSetChanged();
                    }
                    if (dataU != null) {
                        ILineDataSet set = dataU.getDataSetByIndex(0);

                        if (set == null) {
                            set = createSetUp();
                            dataU.addDataSet(set);
                        }
                        dataU.addXValue("" + time);
                        dataU.addEntry(new Entry(upValue * 1000f, set.getEntryCount()), 0);

                        chartUpSpeed.notifyDataSetChanged();
                        chartUpSpeed.setVisibleXRangeMaximum(60);
                        chartUpSpeed.moveViewToX(dataU.getXValCount() - 61);
                    }
                    time++;
                    if (time > 60) {
                        time = 0;
                    }
                    if(dataHlog!=null && !HlogGraphUpdate){
                        chartHLog.setData(dataHlog);
                        HlogGraphUpdate=true;
                    }
                    if(dataSNR!=null && !SNRGraphUpdate){
                        chartSNR.setData(dataSNR);
                        SNRGraphUpdate=true;
                    }
                    if(dataQLN!=null && !QLNGraphUpdate){
                        chartQLN.setData(dataQLN);
                        QLNGraphUpdate=true;
                    }
                    if(update>0)
                    {
                        chartHLog.invalidate();
                        chartSNR.invalidate();
                        chartQLN.invalidate();
                        update--;
                    }
                }
            });
        }
    }

    private LineDataSet createSetDown() {

        LineDataSet set = new LineDataSet(null, "Download Speed (KiB/s)");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.GREEN);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setCircleColor(Color.GREEN);
        set.setCircleColorHole(Color.GREEN);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setDrawValues(false);
        set.setDrawFilled(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private LineDataSet createSetUp() {

        LineDataSet set = new LineDataSet(null, "Upload Speed (KiB/s)");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setCircleColor(Color.RED);
        set.setCircleColorHole(Color.RED);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setDrawValues(false);
        set.setDrawFilled(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

}
