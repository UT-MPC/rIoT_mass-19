package IoT.SmartAgent;

import IoT.Context.AbstractContextArr;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Random;

/*
 * AbstractAgent is the smart control of the system.

 */
public abstract class AbstractAgent {
    public AbstractAgent(){    }
    protected int chooseRand(int max){
        Random rand = new Random(System.currentTimeMillis());
        return rand.nextInt(max);
    }
    public abstract void interaction(AbstractContextArr currContext, KeyEvent e);
    public abstract void getPunished();
    public abstract void getPraised();
    public abstract void getSmallPunished();
    public abstract void mouseCallback(MouseEvent e);
    public abstract void paintNeed(Graphics g);
}

