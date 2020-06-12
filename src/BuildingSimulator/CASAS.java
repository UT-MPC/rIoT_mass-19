package BuildingSimulator;

import IoT.THING.Light;
import IoT.THING.MotionSensor;
import IoT.THING.Thing;
import IoT.THING.drawableThing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class CASAS extends Building implements buildingDrawable{

    protected ArrayList<Thing> IoTThings;
    protected ArrayList<Integer> walls;
    protected boolean isPaint = false;
    public CASAS(int width, int height){
        super(width, height);
    }
    protected abstract int[][] getFloorPlan();
    protected int[][] getLightPlan(){
        return new int[][]{};
    }

    protected int[][] getMotionSensorPan(){
        return new int[][] {};
    }

    protected int lightIdx;
    protected int mSensorIdx;
    protected int cameraIdx;
    protected int speakerIdx;

    private BufferedImage wallImage;

    private boolean isHideMotion = false;
    @Override
    public void generate() {
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
        int numLight = lightPlan.length;
        IoTThings = new ArrayList<>();
        lightIdx = 0;
        for (int i = 0; i < numLight; i++){
            int lightX = lightPlan[i][0] * width / 1400;
            int lightY = lightPlan[i][1] * height / 800;
            Light newLight = new Light(lightX, lightY,lightPlan[i][2] * width / 1400);
            cells [lightX][lightY] = newLight;
            IoTThings.add(newLight);
        }
        mSensorIdx = IoTThings.size();
        int numSensor = sensorPlan.length;
        for (int i = 0; i < numSensor; i++){
            int sensorX = sensorPlan[i][0] * width / 1400;
            int sensorY = sensorPlan[i][1] * height / 800;
            int rx = sensorPlan[i][2] * width / 1400;
            int ry = sensorPlan[i][3] * width / 1400;
            MotionSensor newSensor = new MotionSensor(sensorX, sensorY,rx, ry);
            cells [sensorX][sensorY] = newSensor;
            IoTThings.add(newSensor);
        }
    }


    @Override
    public void drawAll(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(5.0f));
        g2d.setColor(Color.black);
        for (int i = 0; i < walls.size(); i+=2){
            g2d.fillRect(walls.get(i), walls.get(i + 1), 1, 1);
        }
        for (Thing thing: IoTThings){
            if (thing instanceof drawableThing){
                if ((thing instanceof MotionSensor)){
                    ((MotionSensor)thing).isHide(isHideMotion);
                }
                ((drawableThing) thing).drawSelf(g2d, cells, width, height);
            }
        }
    }

    public ArrayList<Thing> getIoTThings() {
        return IoTThings;
    }

    public int getLightIdx() {
        return lightIdx;
    }

    public int getmSensorIdx() {
        return mSensorIdx;
    }
    public int getCameraIdx() {
        return cameraIdx;
    }
    public int getSpeakerIdx() {
        return speakerIdx;
    }
    @Override
    public ArrayList<Cell> getThingCells() {
        ArrayList<Cell> ret = new ArrayList<>();
        for (Thing thing: IoTThings){
            ret.add(thing);
        }
        return ret;
    }

    public void toggleMotion(){
        isHideMotion = !isHideMotion;
    }
}
