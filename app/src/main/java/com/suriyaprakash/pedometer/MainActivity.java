
package com.suriyaprakash.pedometer;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // GUI Elements.
    Button startButton;
    Button resetButton;
    Button stopButton;
    TextView steps;
    // Sensor related variables.
    SensorManager sensorManager;
    Sensor accelerometer;

    // Graph related variables.
    XYPlot plot;
    private SimpleXYSeries xSeries = null;
    private SimpleXYSeries ySeries = null;
    private SimpleXYSeries zSeries = null;
    private SimpleXYSeries nSeries = null;
    public Redrawer redrawer;
    private static final int HISTORY_SIZE = 300;

    // Environment Controlling variables.
    public int stepCountingVariable = 0;
    public static boolean start = false;
    public static boolean oneGraph = true;

    // Total number of steps.
    public int stepNumbers = 0;

    // Variables to store accelerometer data to process.
    private static List<Double> accelDataX = new ArrayList<Double>();
    private static List<Double> accelDataY = new ArrayList<Double>();
    private static List<Double> accelDataZ = new ArrayList<Double>();
    private static List<Double> peakArray = new ArrayList<Double>();
    private int listIndex = 0;
    private int peakIndex = 0;
    private double previous = 9.81;

    // Length of dataPoints - Window.
    private int window = 30;

    /**
     * drawGraph() function is used to plot the graph
     */
    public void drawGraph() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        xSeries = new SimpleXYSeries("X");
        xSeries.useImplicitXVals();
        ySeries = new SimpleXYSeries("Y");
        ySeries.useImplicitXVals();
        zSeries = new SimpleXYSeries("Z");
        zSeries.useImplicitXVals();
        nSeries = new SimpleXYSeries("A");
        nSeries.useImplicitXVals();

        plot.setRangeBoundaries(-16, 18, BoundaryMode.FIXED);
        // Sets the X axis boundaries
        plot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        // Plots the X, Y and Z accelerometer values

        redrawer = new Redrawer(plot, 50, true);
    }

    /**
     * startGraph() function is used to start plotting
     */
    public void startGraph() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        xSeries = new SimpleXYSeries("X");
        xSeries.useImplicitXVals();
        ySeries = new SimpleXYSeries("Y");
        ySeries.useImplicitXVals();
        zSeries = new SimpleXYSeries("Z");
        zSeries.useImplicitXVals();
        nSeries = new SimpleXYSeries(("A"));
        nSeries.useImplicitXVals();

        plot.setRangeBoundaries(-16, 18, BoundaryMode.FIXED);
        // Sets the X axis boundaries
        plot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        // Plots the X, Y and Z accelerometer values
        plot.addSeries(xSeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));
        plot.addSeries(ySeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 200, 100), null, null, null));
        plot.addSeries(zSeries,
                new LineAndPointFormatter(
                        Color.rgb(200, 100, 100), null, null, null));
        plot.addSeries(nSeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 100), null, null, null));
        plot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        plot.setDomainStepValue(HISTORY_SIZE / 20);
        plot.setLinesPerRangeLabel(4);
        plot.setDomainLabel("Time");
        plot.getDomainTitle().pack();
        plot.setRangeLabel("Accelerometer Values");
        plot.getRangeTitle().pack();
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));

        redrawer = new Redrawer(plot, 50, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, String.format("Previous Number of Steps: " + stepCountingVariable), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Initializing data structures which processes data of acceleration values.
        for (int i = 0; i < window; i++) {
            accelDataX.add((double) 0.0);
        }
        for (int i = 0; i < window; i++) {
            accelDataY.add((double) 0.0);
        }
        for (int i = 0; i < window; i++) {
            accelDataZ.add((double) 9.8);
        }

        for (int i = 0; i < window; i++) {
            peakArray.add((double) 9.8);
        }

        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        stopButton = findViewById(R.id.stopButton);
        steps = findViewById(R.id.stepCount);
        plot = findViewById(R.id.graph);
        drawGraph();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Log.d("Fitness","Start");
        listeners();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void listeners() {
//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("Fitness", "Resume");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d("Fitness", "Pause");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * The app will not work in locked mode or when not on screen because it will be stopped and reseted.
     */
    @Override
    protected void onStop() {
        super.onStop();
        //Log.d("Fitness", "Stop");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Most important function as it is responsible for sending accelerometer values for the graph to run.
     * Signal processing will be applied here.
     *
     * @param event
     */
    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        // Condition checks if start was clicked or not.
        if (start == true) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            /**
             * The following if code is responsible for the moving the graph frame with respect to time.
             */
            if (xSeries.size() > HISTORY_SIZE) {
                xSeries.removeFirst();
                ySeries.removeFirst();
                zSeries.removeFirst();
                nSeries.removeFirst();
            }
            /**
             * The following code adds the current event on the graph.
             */
            xSeries.addLast(null, (event.values[0]));
            ySeries.addLast(null, (event.values[1]));
            zSeries.addLast(null, (event.values[2]));

            // Scalar Sum of the X, Y and Z values of accelerometer data.
            double accelSum = Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]);

            // MOVING AVERAGE FILTER BEGINS.
            accelDataX.add(listIndex, (double) event.values[0]);
            accelDataY.add(listIndex, (double) event.values[1]);
            accelDataZ.add(listIndex, (double) event.values[2]);

            // To control Moving window.
            if (listIndex < window) {
                listIndex++;
            } else {
                listIndex = 0;
            }

            double sumX = 0;
            double sumY = 0;
            double sumZ = 0;
            double averageX;
            double averageY;
            double averageZ;

            for (int i = 0; i < window; i++) {
                sumX += accelDataX.get(i);
                sumY += accelDataY.get(i);
                sumZ += accelDataZ.get(i);
            }
            averageX = (sumX / window);
            averageY = (sumY / window);
            averageZ = (sumZ / window);
            // MOVING AVERAGE FILTER ENDS.

            // Vector Sum of the X, Y and Z values of accelerometer data after output from moving average filter.
            double acceleration = Math.sqrt(Math.pow(averageX, 2) + Math.pow(averageY, 2) + Math.pow(averageZ, 2));

            /**
             * 14.3 and 10.00925 and 14.22 were values selected after data collection of accelerometer values
             * and experimentation. The difference between previous and current value to get DELTA(Acceleration)
             * was selected after experimentation.
             */
            if (((acceleration < 14.3) && ((acceleration > 10.00925) || (accelSum > 14.22)))) {
                /** High and low limits on filtered and smooth acceleration to protect from low frequency and
                 *  high frequency noise. accelSum helps to make the phone orientation independent.
                 */
                if (Math.abs(previous - acceleration) > 0.0395) {
                    // Checking for the change in values between acceleration.
                    if (peakIndex < window) {
                        // The moving average is checked for peaks if and only if it crosses the threshold.
                        peakArray.add(peakIndex, acceleration);
                        peakIndex++;
                    } else {
                        for (int i = 1; i < window - 1; i += 2) {
                            if ((peakArray.get(i) > peakArray.get(i - 1) && peakArray.get(i) > peakArray.get(i + 1)) ||
                                    (peakArray.get(i) < peakArray.get(i - 1) && peakArray.get(i) < peakArray.get(i + 1))) {
                                // To detect the crest and troughs.
                                stepNumbers += 1;
                                i += 2;
                                steps.setText("" + stepNumbers);
                            }
                        }
                        peakIndex = 0;
                    }
                }
            }
            previous = acceleration;

            // Add data to the graph.
            nSeries.addLast(null, acceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        System.out.println(accuracy);
        // Will not be used
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startCount(View view) {
        start = true;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (oneGraph) {
            startGraph();
            oneGraph = false;
        }
    }

    public void stopCount(View view) {
        sensorManager.unregisterListener(this, accelerometer);
        String temp = String.format((String) steps.getText());
        stepCountingVariable = Integer.parseInt(temp);
    }

    public void resetCount(View view) {
        steps.setText("0");
        sensorManager.unregisterListener(this, accelerometer);
        start = false;
        plot.clear();
        oneGraph = true;
        stepNumbers = 0;
    }
    
}
