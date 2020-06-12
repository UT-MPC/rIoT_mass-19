package SampleHomes;
import BuildingSimulator.*;
import IoT.THING.Light;

public class Example1 extends Office {

    public Example1(int width, int height) {
        super(width, height);
    }

    public void generate(){

        /*
         here's what we're going for:
         X X X X X X X X X X X X X
         X           X           X
         X           X           X
         X     L           L     X
         X           X           X
         X           X           X
         X X X X X X X X X X X X X
         */

        String cellType;

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                if(i == 0 || i == width - 1 || j == height / 2){
                    if (j == height / 2 && i > width / 2 - 25 && i < width / 2 + 25){
                        cellType = Cell.ROOM;
                    }
                    else {
                        cellType = Cell.WALL;
                    }
                }
                else if(j == 0 || j == height - 1){
                    cellType = Cell.WALL;
                }
                else{
                    cellType = Cell.ROOM;
                }
                cells[i][j] = new Cell(cellType);
            }
        }

        // add lights
        cells[width / 2][height / 4] =  new Light(width / 2, height / 4, 150);
        cells[width / 2][3 * height / 4] =  new Light(width / 2, 3 * height/ 4, 10);

    }

}