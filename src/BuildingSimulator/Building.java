package BuildingSimulator;

import IoT.THING.Thing;

import java.util.ArrayList;

public abstract class Building {
    protected int width;
    protected int height;
    protected Cell[][] cells;

    public Building(int width, int height){
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        generate();
    }

    public abstract void generate();

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public void setCells(Cell[][] cells) { this.cells = cells; }

    public Cell[][] getCells(){ return cells; }

    public ArrayList<Cell> getCellsByType(String type){
        ArrayList<Cell> toReturn = new ArrayList<Cell>();
        for(int i = 0; i<width; i++){
            for(int j = 0; j<height; j++){
                if(cells[i][j].getType()==type){
                    toReturn.add(cells[i][j]);
                }
            }
        }
        return toReturn;
    }
    public ArrayList<Cell> getThingCells(){
        ArrayList<Cell> toReturn = new ArrayList<Cell>();
        for(int i = 0; i<width; i++){
            for(int j = 0; j<height; j++){
                if (Thing.class.isInstance(cells[i][j])){
                    toReturn.add(cells[i][j]);
                }
            }
        }
        return toReturn;
    }
}
