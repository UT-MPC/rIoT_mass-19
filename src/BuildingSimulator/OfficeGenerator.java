package BuildingSimulator;

import IoT.THING.Camera;
import IoT.THING.Light;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class OfficeGenerator{
    private int width;
    private int height;
    private int minLights;
    private int maxLights;
    private Random generator;
    private ArrayList<Room> rooms;
    private Cell[][] cells;
    private int doorSize = 25;
    private int lightRadius = 40;
    private int roomDimension = 250;

    public OfficeGenerator(int width, int height, int minLights, int maxLights){
        this.width = width;
        this.height = height;
        this.minLights = minLights;
        this.maxLights = maxLights;
        generator = new Random();
        rooms = new ArrayList<>();
        cells = new Cell[width + 2][height + 2];
        generate();
    }

    private void generate(){
        initialize();
        generateRooms();
        generateGrid();
        placeDoors();
        placeGridLights();
        placeRandomLights();
    }

    private void placeDoors() {
    }

    private void generateRooms(){

        int[] coordX = {0, 350, 700, 0, 350, 700};
        int[] coordY = {50, 50, 50, 400, 400, 400};
        boolean splitVertical = true;
        int numChunks = 6;

        Queue<Room> chunksOfRooms = new PriorityQueue<>();

        for(int i = 0; i < numChunks; i++){
           Room r = new Room();
           r.setWidth(roomDimension);
           r.setHeight(roomDimension);
           r.setLeftCornerX(coordX[i]);
           r.setLeftCornerY(coordY[i]);

            chunksOfRooms.add(r);
        }

        while(!chunksOfRooms.isEmpty()){

            Room r = chunksOfRooms.poll();
            boolean wantSplit = generator.nextBoolean() || generator.nextBoolean();

            // decide whether or not to split space once
            if(wantSplit){

                splitVertical = !splitVertical;
                Room r1 = new Room();
                Room r2 = new Room();

                // split the space vertically
                if(splitVertical){
                    r1.setHeight(roomDimension);
                    r1.setWidth(roomDimension / 2);
                    r1.setLeftCornerX(r.getLeftCornerX());
                    r1.setLeftCornerY(r.getLeftCornerY());

                    r2.setHeight(roomDimension);
                    r2.setWidth(roomDimension / 2);
                    r2.setLeftCornerX(r.getLeftCornerX() + roomDimension / 2);
                    r2.setLeftCornerY(r.getLeftCornerY());
                }

                // split the space horizontally
                else{
                    r1.setHeight(roomDimension / 2);
                    r1.setWidth(roomDimension);
                    r1.setLeftCornerX(r.getLeftCornerX());
                    r1.setLeftCornerY(r.getLeftCornerY());

                    r2.setHeight(roomDimension / 2);
                    r2.setWidth(roomDimension);
                    r2.setLeftCornerX(r.getLeftCornerX());
                    r2.setLeftCornerY(r.getLeftCornerY() + roomDimension / 2);
                }
                rooms.add(r1);
                rooms.add(r2);
            }
            else {
                rooms.add(r);
            }
        }
    }

    private void generateGrid(){
        for(Room r : rooms){
            int x = r.getLeftCornerX();
            int w = r.getWidth();
            int y = r.getLeftCornerY();
            int h = r.getHeight();

            for(int i = 0; i < w; i++){

                // make space for doors
                if(i < (w/2 - doorSize) || i > (w/2 + doorSize)) {
                    cells[x + i][y] = new Cell(Cell.WALL);
                    cells[x + i][y + h] = new Cell(Cell.WALL);

                }
            }
            for(int i = 0; i < h; i++){

                // make space for doors
                if(i < (h/2 - doorSize) || i > (h/2 + doorSize)) {
                    cells[x][y + i] = new Cell(Cell.WALL);
                    cells[x + w][y + i] = new Cell(Cell.WALL);
                }
            }
        }
    }

    private void placeGridLights(){
        // place a grid of lights along the building's hallways
        for(int i = 125; i < width; i+= 175){
            cells[i][10] = new Light(i, 10, lightRadius);
            cells[i][350] = new Light(i, 350, lightRadius);
            cells[i][690] = new Light(i, 690, lightRadius);
        }
        //cells[300][170] = new Light(300, 170, 2 * lightRadius);
        cells[350][200] = new Camera(350, 200, 3 * lightRadius, 100,200);
        cells[650][170] = new Light(650, 170, 2 * lightRadius);
        cells[300][580] = new Light(300, 515, 2 * lightRadius);
        cells[650][580] = new Light(650, 515, 2 * lightRadius);
    }

    private void placeRandomLights(){
        for(Room r : rooms){
            // place the minimum number of lights in each room, if min is nonzero
            if(minLights != 0) {
                for (int i = 0; i < minLights; i++) {
                    generateLight(r);
                }
            }
            // place the rest of the lights up to max lights at random
            for(int i = 0; i < maxLights; i++){
                // 1/2 chance to place light
                boolean wantPlace = generator.nextBoolean();
                if(wantPlace){
                    generateLight(r);
                }
            }
        }
    }

    private void generateLight(Room r){
        int lightSize = lightRadius;
        // randomly increase the light's radius
        if(generator.nextBoolean()){
            lightSize *= 2;
        }
        int minX = r.getLeftCornerX();
        int minY = r.getLeftCornerY();
        int randomX = generator.nextInt(roomDimension) + minX;
        int randomY = generator.nextInt(roomDimension) + minY;
        // keep generating random numbers until both are in range
        while (randomX > width || randomY > height){
            randomX = generator.nextInt(roomDimension) + minX;
            randomY = generator.nextInt(roomDimension) + minY;
        }
        cells[randomX][randomY] = new Light(randomX, randomY, lightSize);
    }

    private void initialize(){
        String cellType;

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                if(i == 0 || i == width - 1){
                    cellType = Cell.WALL;
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
    }

    public Cell[][] getCells() { return cells; }
}
