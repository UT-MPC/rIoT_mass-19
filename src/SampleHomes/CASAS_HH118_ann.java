package SampleHomes;

import BuildingSimulator.CASAS;
import IoT.Actor;
import IoT.THING.Light;
import IoT.THING.MotionSensor;
import jdk.internal.util.xml.impl.Input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CASAS_HH118_ann extends CASAS {
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private MotionSensor prevSensor = null;
    private static int[][] floorPlan = new int[][]
            {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
             {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
             {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,1,0,0,0,0,0,1,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,1,0,0,0,0,0,1,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,1,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1},
             {0,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,1,1,1,1,1,0,0,0,0,0,1,0,0,1,1,1},
             {0,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,1,0,0,0,0,0,1,0,0,1,1,1},
             {0,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,0,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
             {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}};
    // The lights are in order with the dataset.
    private static int[][] light = new int[][]
            {{680,330,150},
            {250,550,300},
            {151,200,150},
            {400,200,150},
            {400,260,50},
            {420,100,50},
            {750,620,250},
            {1150,550,270},
            {1140,200,150},
            {1110,260,50},};
    // The motion sensors are in order with the dataset.
    private static int[][] motionSensor = new int[][]
            {{1160,540,230,230},
            {1275,190,60,120},
            {894,160,80,85},
            {430,205,100,50},
            {150,200,120,140},
            {1130,180,80,110},
            {325,155,50,50},
            {857,600,110,160},
            {166,160,70,70},
            {807,370,50,50},
            {611,105,40,40},
            {580,362,50,50},
            {510,183,50,50},
            {622,240,50,50},
            {1165,650,200,110},
            {160,530,135,230},
            {1165,418,200,110},
            {430,530,135,230},
            };

    public CASAS_HH118_ann(int width, int height) {
        super(width, height);
    }
    @Override
    protected int[][] getFloorPlan() {
        return floorPlan;
    }
    @Override
    protected int[][] getLightPlan() {
        return light;
    }
    @Override
    protected int[][] getMotionSensorPan(){
        return motionSensor;
    }

    @Override
    public void generate(){
        super.generate();
    }

    public LocalTime readLine(BufferedReader reader, Actor actor){
        LocalTime now;
        while (true) {
            try{
                String lineStr = reader.readLine();
                if (lineStr == null) { return null;}
                String[] arrStr = lineStr.split("\\.", 2);
                now = LocalTime.parse(arrStr[0], timeFormatter);
                String[] fields = arrStr[1].split(" ");
                String sensor = fields[1].split("[0-9]")[0];
                int num = Integer.parseInt(fields[1].replaceAll("[^0-9]",""));
                String state = fields[2];
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON")){
                        if (prevSensor != null) {prevSensor.turnOff();}
                        mx.turnOn();
                        prevSensor = mx;
//                        break;

                    }
//                    if (state.equals("OFF")){
//                        mx.turnOff();
//                    }
                }
                if (sensor.equals("L") || sensor.equals("LL")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    if (state.equals("ON")){
                        mx.turnOn();
                    }
                    if (state.equals("OFF")){
                        mx.turnOff();
                    }
                    break;
                }
            } catch (IOException e){
                e.printStackTrace();
                return null;
            }

        }
        return now;

    }


}
