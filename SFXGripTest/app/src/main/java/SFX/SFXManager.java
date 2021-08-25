package SFX;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;

import java.time.LocalDateTime; // import the LocalDateTime class
import java.time.temporal.ChronoUnit;


public class SFXManager implements SensorEventListener {

    LocalDateTime t = LocalDateTime.now();
    LocalDateTime tstart = LocalDateTime.now();

    float[] xim1 = {0, 0, 0};
    float[] xi = {0, 0, 0};
    float[] xi_g = {0, 0, 0};
    float[] xim1_g = {0, 0, 0};
    float[] yi_g = {0, 0, 0};
    float[] yim1_g = {0, 0, 0};

    float[] gyro_buff = new float[50];
    int ibuff = 0;

    float[] yim1 = {0, 0, 0};
    float[] yi = {0, 0, 0};
    float alpha = (float) 0.5;
    float alpha_g = (float) 0.9;
    float threshSum = 0;
    float maxNorm = 0;
    float zeroThresh = (float) (5 * 5);
    float tapThresh = (float) (9 * 9);

    // mag tap varibles
    Boolean checkMagTap = false;
    Boolean magTapCheckStarted = false;
    LocalDateTime dateAtMagTapCheck = LocalDateTime.now();
    LocalDateTime dateAtLastTap = LocalDateTime.now();
    LocalDateTime zeroStartDate = LocalDateTime.now();

    float maxSpikeDuration_ms = (float) 150;
    float betweenTapsDuration_ms = (float) 200;


    private SensorManager sensorManager;
    private ISFXSensorEventHandler handler;
    private int LastSensor = 0;

    public SFXManager(SensorManager sensorManager, ISFXSensorEventHandler handler) {
        this.sensorManager = sensorManager;
        this.handler = handler;

        System.out.println("HI");

        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyro != null) {
            sensorManager.registerListener(this, gyro,
                    SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            System.out.println("gyrro null");
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            System.out.println("magnetic null");
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {


            // gyro callback...

            // push values back
            System.arraycopy(xi_g, 0, xim1_g, 0, xim1_g.length);
            System.arraycopy(yi_g, 0, yim1_g, 0, yim1_g.length);

            // read new values
            System.arraycopy(event.values, 0, xi_g, 0, xi_g.length);

            // compute new high-pass magnitudes
            float sum = 0;
            float norm = 0;
            for (int i = 0; i < 3; i++) {
                yi_g[i] = alpha_g * yim1_g[i] + alpha_g * (xi_g[i] - xim1_g[i]);
                sum += yi_g[i];
                norm += yi_g[i] * yi_g[i];
            }



            // add to buffer
            gyro_buff[ibuff] = norm;



            ibuff = (ibuff + 1) % gyro_buff.length;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {



            // magnetometer callback...

            // get times
            LocalDateTime tnew = LocalDateTime.now();
            long dt = ChronoUnit.MILLIS.between(t, tnew);
            long trel = ChronoUnit.MILLIS.between(tstart, t);
            t = tnew;

            // push values back
            System.arraycopy(xi, 0, xim1, 0, xim1.length);
            System.arraycopy(yi, 0, yim1, 0, yim1.length);

            // read new values
            System.arraycopy(event.values, 0, xi, 0, xi.length);

            // compute new high-pass magnitudes
            float sum = 0;
            float norm = 0;
            for (int i = 0; i < 3; i++) {
//                yi[i] = alpha * yim1[i] + alpha * (xi[i] - xim1[i]);
                yi[i] = xi[i] - xim1[i];
                sum += yi[i];
                norm += yi[i] * yi[i];
            }

//            System.out.println(norm);

            // run SFX algorithm
            if (magTapCheckStarted) {
                threshSum += yi[2];
            }

            System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" + event.values[0]);
            float norm1 = event.values[0];
            if (LastSensor == 0) {

                if (norm1 > -25) {
                    this.LastSensor = 2;
                    this.handler.onMagnetValueChanged(norm1);
                } else {
                    this.LastSensor = 1;
                    this.handler.onMagnetValueChanged(norm1);
                }
            } else if (this.LastSensor == 1) {

                if (norm1 > -25) {
                    this.LastSensor = 2;
                    this.handler.onMagnetValueChanged(norm1);
                } else {
                    ;
                }

            } else if (this.LastSensor == 2) {

                if (norm1 > -25) {
                    ;
                } else {
                    this.LastSensor = 1;
                    this.handler.onMagnetValueChanged(norm1);
                }
            }


            if (checkMagTap) {
                if (magTapCheckStarted) {
                    long dtTap = ChronoUnit.MILLIS.between(dateAtMagTapCheck, t);
                    if (dtTap < maxSpikeDuration_ms) {
                        // we're in the time window...
                        if (norm > maxNorm) {
                            maxNorm = norm;
                        }
                        if (norm < zeroThresh) {
                            // mag went down within the window --> tap detected
                            //Console.WriteLine($"Tap Detected!");
                            long dtLastTap = ChronoUnit.MILLIS.between(dateAtLastTap, t);
                            Boolean gyroOK = gyroBuffInRange(0.00008, 15);
                            if (dtLastTap > 100 && gyroOK) {

                                // SFX TRIGGERED!
                                System.out.print("PLAY SFX ");
                                if (threshSum > 0) System.out.println("(collapsed)");
                                else System.out.println("(expanded)");

                            } else {
                                if (!gyroOK) {
                                    System.out.println("Gyro out of bounds");
                                } else {
                                    System.out.println("SFX too fast");
                                }
                            }


                            // stop checking
                            magTapCheckStarted = false;
                            checkMagTap = false;
                            dateAtLastTap = LocalDateTime.now();
                        }

                    } else {
                        // mag hasn't gone low enough --> no tap detected
                        magTapCheckStarted = false;
                        checkMagTap = false;
                    }
                } else {
                    // see if it's over the thresh
                    if (norm > tapThresh) {
                        // begin checking
                        //Console.WriteLine($"Mag Thresh");
                        dateAtMagTapCheck = LocalDateTime.now();
                        magTapCheckStarted = true;
                        threshSum = yi[2];
                        maxNorm = norm;
                    } else if (norm < zeroThresh) {
                        zeroStartDate = t;
                    } else if (ChronoUnit.MILLIS.between(zeroStartDate, t) > 150) {
                        // toook too long to reach peak
                        checkMagTap = false;
                    }
                }
            } else {
                // only start checking for a tap when mag value is low enough
                float dtLastTap = ChronoUnit.MILLIS.between(dateAtLastTap, t);
                if (norm < zeroThresh && dtLastTap > betweenTapsDuration_ms) {
                    checkMagTap = true;
                    zeroStartDate = t;
                }
            }

        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private Boolean gyroBuffInRange(double min, double max) {
        int j = 0;
        int n = gyro_buff.length;
        float maxg = 0;
        //while (j < n && gyro_buff[j] >= min && gyro_buff[j] <= max)
        while (j < n) {
            if (gyro_buff[j] > maxg) {
                maxg = gyro_buff[j];
            }
            j++;
        }
        return (maxg >= min && maxg <= max);
    }

}
