package SwingContent;

import BuildingSimulator.*;
import IoT.*;
import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.Simulation.PlayAgent;
import IoT.Simulation.simDataGen;
import IoT.SmartAgent.*;
import IoT.PhysicalScopeQuery;
import IoT.THING.Thing;
import IoT.THING.drawableThing;
import SampleHomes.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public class Simulator extends JPanel implements ActionListener {
    private final String modeSIM = "Simulator_mode";
    private final String modePLAY = "Play_mode";
    private final String modeSIMDATA = "Simulation dataset";

    private int width;
    private int height;
    private Building building;
    private Timer timer;
    private final int delay = 10;
    private Actor actor;
    private AbstractAgent myAgent;
    private JPanel timePanel;
    private JPanel instPanel;
    private JPanel rightPanel;
    private JTextField[] timeTextFields = new JTextField[3];
    private String mode;
    private boolean isTimeGoing = false;
    private int deltaTime = 20;                             //Time speed in millisecond
    private int playCount;
    private LocalTime showTime;
    private DateTimeFormatter showFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean isHide = false;


    private void setTimePanel(){
        showTime = LocalTime.of(12,10,59);
        timePanel = new JPanel();
        timeTextFields[0] = new JTextField(String.valueOf(showTime.getHour()),3);
        timeTextFields[1] = new JTextField(String.valueOf(showTime.getMinute()),3);
        timeTextFields[2] = new JTextField(String.valueOf(showTime.getSecond()),3);
        timeTextFields[0].setFocusable(false);
        timeTextFields[1].setFocusable(false);
        timeTextFields[2].setFocusable(false);

        timePanel.add(timeTextFields[0]);
        timePanel.add(new JLabel(":"));
        timePanel.add(timeTextFields[1]);
        timePanel.add(new JLabel(":"));
        timePanel.add(timeTextFields[2]);
        timePanel.setMaximumSize(new Dimension(400, 100));
        timePanel.setBorder(BorderFactory.createMatteBorder(0,0,2,0,Color.black));
        timePanel.setFocusable(false);
    }

    private void simulatorUI(){
        mode = modeSIM;
        isTimeGoing = true;
        setTimePanel();

        instPanel = new JPanel();
        instPanel.setLayout(new BoxLayout(instPanel, BoxLayout.Y_AXIS));
        instPanel.add(new JLabel("\"I\": Request a camera"));
        instPanel.add(new JLabel("\"O\": Request a light"));
        instPanel.add(new JLabel("\"P\": Punish agent selection"));
        instPanel.add(new JLabel("\"[\": Praise agent selection"));
        instPanel.add(new JLabel("\"R\": Turn off all lights"));
        instPanel.add(new JLabel("(space): Resume or pause timer"));
        instPanel.add(new JLabel("\"-\": Slow the timer"));
        instPanel.add(new JLabel("\"=\": Speedup the timer"));
        instPanel.setPreferredSize(new Dimension(400,300));
        instPanel.setFocusable(false);

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(timePanel);
        rightPanel.add(instPanel);
        rightPanel.setPreferredSize(new Dimension(400,550));
        rightPanel.setFocusable(false);

        myAgent = new OneAgent(building.getThingCells());

        actor = new Actor(10, 10, this.getHeight(), this.getWidth());

        JFrame frame = new JFrame("IoT Simulator");
        frame.add(this, BorderLayout.WEST);
        frame.add(rightPanel, BoxLayout.X_AXIS);
        frame.setPreferredSize(new Dimension(width +250, Math.max(height + 50, 500)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        // paint the actual house on the frame
        repaint();

        addKeyListener(new simAdapter());
        addMouseListener(new MouseWatcher());
        timer = new Timer(delay, this);
        timer.start();

    }

    private void CASAS_Play(String filename){
        mode = modePLAY;
        isTimeGoing = false;
        setTimePanel();
        instPanel = new JPanel();
        instPanel.setLayout(new BoxLayout(instPanel, BoxLayout.Y_AXIS));
        instPanel.add(new JLabel("\"R\": Run a simulation"));
        instPanel.add(new JLabel("\"H\": Hide motion sensors"));
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(timePanel);
        rightPanel.add(instPanel);
        rightPanel.setPreferredSize(new Dimension(400,550));
        rightPanel.setFocusable(false);


        actor = new Actor(10, 10, this.getHeight(), this.getWidth());

        JFrame frame = new JFrame("IoT Simulator");
        frame.add(this, BorderLayout.WEST);
        frame.add(rightPanel, BoxLayout.X_AXIS);
        frame.setPreferredSize(new Dimension(width +250, Math.max(height + 50, 500)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        // paint the actual house on the frame
        repaint();
        addKeyListener(new simAdapter());
        addMouseListener(new MouseWatcher());
        try{
            myAgent = new PlayAgent(building, new FileInputStream(filename + "/input.txt"));
        } catch (FileNotFoundException e){
            myAgent = null;
            System.out.println("Cannot find file at dir" + System.getProperty("user.dir"));
        }


//        addKeyListener(new simAdapter());
//        addMouseListener(new MouseWatcher());
        timer = new Timer(delay, this);
        timer.start();
    }

    private void simDataset(String filename){
        mode = modeSIMDATA;
        isTimeGoing = false;
        setTimePanel();
        instPanel = new JPanel();
        instPanel.setLayout(new BoxLayout(instPanel, BoxLayout.Y_AXIS));
        instPanel.add(new JLabel("\"R\": Run a simulation"));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(timePanel);
        rightPanel.add(instPanel);
        rightPanel.setPreferredSize(new Dimension(400,550));
        rightPanel.setFocusable(false);


        actor = new Actor(10, 10, this.getHeight(), this.getWidth());

        JFrame frame = new JFrame("IoT Simulator");
        frame.add(this, BorderLayout.WEST);
        frame.add(rightPanel, BoxLayout.X_AXIS);
        frame.setPreferredSize(new Dimension(width +250, Math.max(height + 50, 500)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        // paint the actual house on the frame
        repaint();
        addKeyListener(new simAdapter());
        addMouseListener(new MouseWatcher());
        try{
            myAgent = new simDataGen(building, new FileInputStream(filename + "/ann.txt"));
        } catch (FileNotFoundException e){
            myAgent = null;
            System.out.println("Cannot find file at dir" + System.getProperty("user.dir"));
        }


//        addKeyListener(new simAdapter());
//        addMouseListener(new MouseWatcher());
        timer = new Timer(delay, this);
        timer.start();
    }
    public Simulator(int picked, int minLights, int maxLights){

        super();

        switch(picked){
//            case 0:
//                width = 600;
//                height = 800;
//                building = new RandomHouse(width, height);
//                break;
//            case 1:
//                width = 300;
//                height = 400;
//                building = new Example1(width, height);
//                break;
//            case 2:
//                width = 600;
//                height = 800;
//                building = new Example2(width, height);
//                break;
            case 0:
                width = 1400;
                height = 800;
                building = new CASAS_HH118(width, height);
                break;
            case 1:
                width = 1400;
                height = 800;
                building = new CASAS_HH118_ann(width, height);
                break;
            case 2:
                width = 2000;
                height = 800;
                building = new CASAS_HH107(width, height);
                break;
            default:
                width = 600;
                height = 800;
                building = new RandomHouse(width, height);
                break;
        }


        this.setPreferredSize(new Dimension(width, height));
        this.setLocation(10, 10);
        this.setFocusable(true);
        // construct the frame that holds both panels


        switch (picked){
            case 4:
                CASAS_Play("data/hh118");
                break;
            case 5:
                simDataset("data/hh107");
                break;
            default:
                simulatorUI();
                break;
        }
        //instPanel.setBorder(BorderFactory.createLineBorder(Color.black));


        //rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));


        // these lines are needed for actor movement


    }


    @Override
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintHouse(g);
        paintActor(g);
        if (myAgent != null){
            myAgent.paintNeed(g);
        }

    }

    private void paintActor(Graphics g){
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(5.0f));

        if (actor != null) {
            g2d.drawImage(actor.getImage(), actor.getTopLeftX() - 17, actor.getTopLeftY() - 10, null);  //move the actor's head to the actural place of the actor
        }
//        g2d.setColor(Color.red);
//        g2d.fill(new Ellipse2D.Double(actor.getTopLeftX()-5, actor.getTopLeftY()-5, 10, 10));
    }

    private void paintHouse(Graphics g){
        Graphics2D g2d = (Graphics2D) g.create();
        if (building instanceof buildingDrawable){
            ((buildingDrawable) building).drawAll(g2d);
        }else{
            Cell[][]cells = building.getCells();
            g2d.setStroke(new BasicStroke(5.0f));
            for(int i = 0; i < width; i++){
                for(int j = 0; j < height; j++){
                    Cell currentCell = cells[i][j];

                    if(currentCell.getType() == Cell.WALL){
                        g2d.setColor(Color.black);
                        g2d.fillRect(i, j, 1, 1);
                    }
                    else if(currentCell.getType()==Cell.ROOM){
                    }
                    else {
                        // instance of Thing
                        if (currentCell instanceof drawableThing){
                            ((drawableThing)currentCell).drawSelf(g2d, cells, width, height);
                        }
                    }
                }
            }
        }

    }

    private void performQuery(){
        Thing closestLight = new ClosestLocationQuery(building).makeQuery(Cell.LIGHT, actor.getTopLeftX(), actor.getTopLeftY());
        System.out.println(closestLight);

        Thing coveringLight = new PhysicalScopeQuery(building).makeQuery(Cell.LIGHT, actor.getTopLeftX(), actor.getTopLeftY());
        System.out.println(coveringLight);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isTimeGoing && (mode == modeSIM)){
            if (deltaTime > 1000){
                showTime = showTime.plusSeconds(deltaTime / 1000);
            }else{
                showTime = showTime.plusNanos(deltaTime * 1000000);
            }
            actor.move(showTime);
            //rightPanel.repaint();
        }
        timeTextFields[0].setText(String.valueOf(showTime.getHour()));
        timeTextFields[1].setText(String.valueOf(showTime.getMinute()));
        timeTextFields[2].setText(String.valueOf(showTime.getSecond()));
        repaint();
    }

    private void toggleTimeGoing(){
        isTimeGoing = !isTimeGoing;
    }
    private void pauseTime(){
        isTimeGoing = false;
    }
    private class MouseWatcher extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e){
            myAgent.mouseCallback(e);
        }
    }
    private class simAdapter extends KeyAdapter {
        Set<Integer> agentKeys = new HashSet<Integer>();
        public simAdapter() {
            super();
            agentKeys.addAll(Arrays.asList(new Integer[] {KeyEvent.VK_I, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_R}));
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                actor.keyReleased(e);
            }
            else if(key == KeyEvent.VK_ENTER){
                performQuery();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (mode == modeSIM){
                if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN){
                    actor.keyPressed(e);
                }
                if (agentKeys.contains(key)) {
                    AbstractContextArr nowContext = new DynamicContext(actor.getTopLeftX(), actor.getTopLeftY(),showTime.toSecondOfDay());
                    myAgent.interaction(nowContext, e);
                }
                if(key == KeyEvent.VK_SPACE){
                    toggleTimeGoing();
                }
                if(key == KeyEvent.VK_MINUS){
                    deltaTime = Math.max(5, deltaTime - Math.max(2, deltaTime / 20));
                }
                if(key == KeyEvent.VK_EQUALS){
                    deltaTime = deltaTime + Math.max(2, deltaTime / 20);
                }
            }
            if (mode == modePLAY){
                if (key == KeyEvent.VK_T){
                    pauseTime();
                    ((PlayAgent)myAgent).testClosetAll(actor);
                }
                if (key == KeyEvent.VK_C){
                    pauseTime();
                    ((PlayAgent)myAgent).readDataset(actor,1);
                }
                if (key == KeyEvent.VK_B){
                    pauseTime();
                    ((PlayAgent)myAgent).readDataset(actor,2);
                }
                if (key == KeyEvent.VK_G){
                    pauseTime();
                    ((PlayAgent)myAgent).catGridCV(actor);
                }

                if (key == KeyEvent.VK_V){
                    pauseTime();
                    ((PlayAgent)myAgent).GridCV(actor);
                }

                if (key == KeyEvent.VK_R){
                    pauseTime();
                    ((PlayAgent)myAgent).readDataset(actor,0);
                }
                if(key == KeyEvent.VK_SPACE){
                    toggleTimeGoing();
                }
                if(key == KeyEvent.VK_MINUS){
                    deltaTime = Math.max(5, deltaTime - Math.max(2, deltaTime / 20));
                }
                if(key == KeyEvent.VK_EQUALS){
                    deltaTime = deltaTime + Math.max(2, deltaTime / 20);
                }
                if(key == KeyEvent.VK_H){
                    if (building instanceof CASAS){
                        ((CASAS)building).toggleMotion();
                    }
                }
                if (key==KeyEvent.VK_P){
                    System.out.println("Generate dataset for python");
                    ((PlayAgent)myAgent).pythonDataLocation("data/hh118/input.txt");
                }
            }
            if (mode == modeSIMDATA){
                if (key == KeyEvent.VK_P){
                    ((simDataGen)myAgent).pythonData();
                }
                if (key == KeyEvent.VK_L){
                    ((simDataGen)myAgent).testLightRange();
                }
                if (key == KeyEvent.VK_R){
                    ((simDataGen)myAgent).testAllEvent();
                }
                if (key == KeyEvent.VK_C){
                    ((simDataGen)myAgent).testWithActivity();
                }
            }
        }

    }
}

