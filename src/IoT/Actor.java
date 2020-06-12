package IoT;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

public class Actor {
    private class Snapshot{
        public int x,y;
        public LocalTime time;
        public Snapshot(int x, int y, LocalTime time){
            this.x = x;
            this.y = y;
            this.time = time;
        }
        public void clone(Snapshot B){
            this.x = B.x;
            this.y = B.y;
            this.time = B.time;
        }
    }
    BufferedImage image;
    private int x;
    private int y;
    private double dirX;
    private double dirY;
    private int dx;
    private int dy;
    private ArrayList<Snapshot> window;
    private static long windowSize = 5;          //The duration of the window in seconds.
    private static long timeThreshold = 500;         //Record the actor every timeThreshold millisecond.
    public Actor(int x, int y, int maxX, int maxY){
        try {
            image = ImageIO.read(new File("src/SwingContent/person.png"));
        } catch (IOException e){
            image = null;
        }
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
        dirX = 0;
        dirY = 0;
        window = new ArrayList<>();
    }

    private void updateDir(){
        if (window.size() <= 1) return;
        int inter = (window.size() + 1) / 2;
        double avgX = 0;
        double avgY = 0;
        for (int i = 0; i < window.size(); i++){
            if (i + inter >= window.size()){
                break;
            }
            Snapshot oldS = window.get(i);
            Snapshot newS = window.get(i + inter);

            long time = window.get(i).time.until(window.get(i + inter).time, MILLIS);
            time = time / timeThreshold;
            avgX = (newS.x - oldS.x) / (double)time;
            avgY = (newS.y - oldS.y) / (double)time;
        }
        avgX /= window.size() / 2;
        avgY /= window.size() / 2;
        dirX = avgX;
        dirY = avgY;
//        System.out.println("walking at the direction of (" + dirX + ", "+ dirY + ")");
//        System.out.println("walking at the direction of (" + (((int)Math.toDegrees(Math.atan2((double)dirY,(double)dirX)) + 360) % 360) + ")");
    }

    public void updateWindow(LocalTime now){
        Snapshot snapNow = new Snapshot(x,y,now);
        if (window.size() == 0){
            window.add(snapNow);
        }else{
            Snapshot lastTime = window.get(window.size()-1);
            long diff = lastTime.time.until(now, MILLIS);
            if (diff > timeThreshold){
                window.add(snapNow);
            }else{
                lastTime.x = x;
                lastTime.y = y;
            }
            Snapshot firstTime = window.get(0);
            if (firstTime.time.until(now, SECONDS) > windowSize){
                window.remove(0);
            }
        }
        updateDir();
    }

    public boolean move(LocalTime now){
        boolean result = false;
        if ((dx != 0) || (dy != 0)){
            result = true;
        }
        x += dx;
        y += dy;
        updateWindow(now);
        return result;
        //System.out.println(window.size());
    }

    public void reset(){
        x -= dx;
        y -= dy;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getTopLeftX() {
        return x;
    }

    public int getTopLeftY() {
        return y;
    }

    public double getDirX() { return dirX; }

    public double getDirY() { return dirY; }

    public void jumpTo(int x, int y, LocalTime now){
        this.x = x;
        this.y = y;
        updateWindow(now);
    }

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            dx = -2;
        }

        if (key == KeyEvent.VK_RIGHT) {
            dx = 2;
        }

        if (key == KeyEvent.VK_UP) {
            dy = -2;
        }

        if (key == KeyEvent.VK_DOWN) {
            dy = 2;
        }

    }

    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
        else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            dy = 0;
        }
    }
}
