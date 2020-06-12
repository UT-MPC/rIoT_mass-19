package SampleHomes;

import BuildingSimulator.CASAS;
import BuildingSimulator.Cell;
import IoT.THING.Camera;
import IoT.THING.Light;
import IoT.THING.MotionSensor;
import IoT.THING.Speaker;

import java.util.ArrayList;

public class CASAS_HH107 extends CASAS{
    private static int[][] floorPlan = new int[][]
           {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
            {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,},
            {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,},
            {0,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,},
            {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,}};

    private static int[][] light = new int[][]
            {{144,323,50},                      //0
            {149,219,125},
            {486,168,100},
            {280,567,210},
            {819,234,150},
            {1037,240,75},
            {808,600,150},
            {1226,233,100},
            {1352,569,200},
            {1580,222,100},
            {1886,196,100},
            {1833,534,175},
            {80,695,70},
            {73,434,70},};                   //13

    private static int[][] camera = new int[][]
            {
            {1785,81,150,180,270},              //door camera   12
            {1960,640,300,100,177},             //baby camera   13
            };

    private static int[][] speaker = new int[][]
            {
            {188,450,225},              //bedroom speaker       14
            {1100,600,400},             //living room speaker   15
            };

    public CASAS_HH107(int width, int height) {
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
    public void generate(){
        String cellType;
        int[][] floorPlan = getFloorPlan();
        int[][] lightPlan = getLightPlan();
        int[][] sensorPlan = getMotionSensorPan();
        int dy = floorPlan.length;
        int dx = floorPlan[0].length;
        walls = new ArrayList<>();
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                int d1 = j * dy / height;
                int d2 = i * dx / width;
                int p1 = (j - 1) * dy / height;
                int p2 = (i - 1) * dx / width;
                int type = floorPlan[d1][d2];
                cellType = Cell.ROOM;
                if (type == 1){
                    if ((p1 != d1) && (p2 != d2)) {
                        cellType = Cell.WALL;
                    }else {
                        try{
                            if ((floorPlan[d1][d2 + 1] == 1) && (floorPlan[d1 + 1][d2] == 1) && (floorPlan[d1 + 1][d2 + 1] == 1)){
                                cellType = Cell.WALL;
                            }
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                        try{
                            if ((floorPlan[d1 + 1][d2] == 1) && (p2 != d2)){
                                cellType = Cell.WALL;
                            }
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                        try{
                            if ((floorPlan[d1][d2 + 1] == 1) && (p1 != d1)){
                                cellType = Cell.WALL;
                            }
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                    }
                }
                cells[i][j] = new Cell(cellType);
                if (cellType == Cell.WALL){
                    walls.add(i);
                    walls.add(j);
                }
            }
        }
        int numD = lightPlan.length;
        IoTThings = new ArrayList<>();
        lightIdx = 0;
        for (int i = 0; i < numD; i++){
            int lightX = lightPlan[i][0] * width / 2000;
            int lightY = lightPlan[i][1] * height / 800;
            Light newLight = new Light(lightX, lightY,lightPlan[i][2] * width / 1400);
            cells [lightX][lightY] = newLight;
            IoTThings.add(newLight);
        }
        cameraIdx = IoTThings.size();
        numD = camera.length;
        for (int i = 0; i < numD; i++){
            int thingX = camera[i][0] * width / 2000;
            int thingY = camera[i][1] * height / 800;
            Camera newCamera = new Camera(thingX, thingY, camera[i][2]  * width / 2000, camera[i][3],camera[i][4]);
            cells [thingX][thingY] = newCamera;
            IoTThings.add(newCamera);
        }
        speakerIdx = IoTThings.size();
        numD = speaker.length;
        for (int i = 0; i < numD; i++){
            int thingX = speaker[i][0] * width / 2000;
            int thingY = speaker[i][1] * height / 800;
            Speaker newSpeaker = new Speaker(thingX, thingY, speaker[i][2] * width / 2000);
            cells [thingX][thingY] = newSpeaker;
            IoTThings.add(newSpeaker);
        }
    }
}
