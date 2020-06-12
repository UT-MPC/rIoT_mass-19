package BuildingSimulator;

public class HouseGenerator{

    int width;
    int height;
    private Cell[][] cells;

    public HouseGenerator(int width, int height){
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
    }

    public Cell[][] getCells() { return cells;}

}
