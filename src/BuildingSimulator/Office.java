package BuildingSimulator;


import IoT.THING.Thing;
import IoT.THING.drawableThing;

import java.awt.*;
import java.util.ArrayList;

public class Office extends Building implements buildingDrawable{
    private boolean isPaint = false;
    ArrayList<Cell> IoTThings;
    public Office(int width, int height){
        super(width, height);
    }

    @Override
    public void generate() {

    }
    @Override
    public void drawAll(Graphics2D g2d) {
        if (!isPaint){
            isPaint = true;
            IoTThings = new ArrayList<>();
            for(int i = 0; i < width; i++){
                for(int j = 0; j < height; j++){
                    if(cells[i][j] instanceof Thing){
                        IoTThings.add(cells[i][j]);
                    }
                }
            }
        }
        g2d.setStroke(new BasicStroke(5.0f));
        g2d.setColor(Color.black);
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                Cell currentCell = cells[i][j];

                if(currentCell.getType() == Cell.WALL){
                    g2d.setColor(Color.black);
                    g2d.fillRect(i, j, 1, 1);
                }
            }
        }
        for (Cell thing: IoTThings){
            if (thing instanceof drawableThing){
                ((drawableThing) thing).drawSelf(g2d, cells, width, height);
            }
        }
    }
}
