package BuildingSimulator;

public class Cell {

    public static String ROOM = "room";
    public static String LIGHT = "light";
    public static String WALL = "wall";
    public static String THING = "thing";
    public static String Camera = "camera";
    public static String Speaker = "speaker";
    public static String MotionSensor = "MotionS";

    String type;

    public Cell(){
        this.type = ROOM;
    }

    public Cell(String type){
        this.type = type;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
