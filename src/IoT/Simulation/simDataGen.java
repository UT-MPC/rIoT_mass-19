package IoT.Simulation;

import BuildingSimulator.Building;
import BuildingSimulator.CASAS;
import BuildingSimulator.Cell;
import IoT.Actor;
import IoT.Context.*;
import IoT.SmartAgent.AbstractAgent;
import IoT.SmartAgent.OneAgent;
import IoT.THING.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class simDataGen extends AbstractAgent{
    private static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private FileInputStream dataStream;
    private BufferedReader dataReader;
    private Random myRand = new Random(172849);
    ArrayList<Thing> IoTThings;
    private OneAgent myAgent;
    Building building;
    double coorHyper1 = 0.5, coorHyper2 = 0.5/ 720.0, coorHyper3 = 0.2;
    int lightIdx, speakerIdx, cameraIdx;

    private HashMap<String, EventInfo> eventMap;
    private ArrayList<String> eventCat;
    private HashSet<String> multiPlaceEvt;
    private Random rand = new Random(172849);
    private double doorBellEvtProb = 1.0/1000;
    private double childCameraProb = 0;
    ArrayList<Integer> breakPoint = new ArrayList<>(Arrays.asList(new Integer[]{}));
    ArrayList<Integer> cmdNoCnt = new ArrayList<>();
    ArrayList<String> cmdAct = new ArrayList<>();
    ArrayList<Integer> cmdDevice = new ArrayList<>();
    private boolean isInHome = true;
    private boolean isEnvChagne = false;
    private class EventInfo {
        Point2D a;
        Point2D b;
        int[] thingOpt;
        public EventInfo(Point2D a, Point2D b){
            this.a = a;
            this.b = b;
        }
        public EventInfo(int ax, int ay, int bx, int by){
            this.a = new Point2D(ax, ay);
            this.b = new Point2D(bx, by);
        }
        public EventInfo(int ax, int ay, int bx, int by, int[] thingOpt){
            this.a = new Point2D(ax, ay);
            this.b = new Point2D(bx, by);
            this.thingOpt = thingOpt;
        }
    }

    public simDataGen(Building building, FileInputStream stream){
        this.building = building;
        lightIdx = ((CASAS)building).getLightIdx();
        speakerIdx = ((CASAS)building).getSpeakerIdx();
        cameraIdx = ((CASAS)building).getCameraIdx();
        dataStream = stream;
        try {
            dataReader= new BufferedReader(new InputStreamReader(dataStream));
//            System.out.println(in.readLine());
        } catch (Exception e){
            e.printStackTrace();
        }
        multiPlaceEvt = new HashSet<>(10);
        eventMap = new HashMap<>(30);
        eventCat = new ArrayList<>();
        multiPlaceEvt.add("Toilet");
        multiPlaceEvt.add("Personal_Hygiene");
        multiPlaceEvt.add("Dress");
        multiPlaceEvt.add("Read");

        eventMap.put("Enter_Home", new EventInfo(1613,88,1778,151,new int[]{lightIdx+9}));
        eventMap.put("Leave_Home", new EventInfo(1604,7,1771,64,new int[]{}));

        eventMap.put("Toilet_M013", new EventInfo(35,91,269,352,new int[]{lightIdx+1}));
        eventMap.put("Toilet_MA020", new EventInfo(1789,91,1960,317,new int[]{lightIdx+10}));
        eventMap.put("Personal_Hygiene_MA020", new EventInfo(1801,114,1949,294,new int[]{lightIdx+10}));
        eventMap.put("Personal_Hygiene_M013", new EventInfo(35,91,269,352,new int[]{lightIdx+0,lightIdx+1}));
        eventMap.put("Bathe", new EventInfo(36,88,258,189,new int[]{lightIdx+1}));
        eventMap.put("Bed_Toilet_Transition", new EventInfo(190,156,393,364,new int[]{lightIdx+1}));


        eventMap.put("Watch_TV", new EventInfo(1272,524,1419,752,new int[]{lightIdx+8, speakerIdx+1}));
        eventMap.put("Sleep_Out_Of_Bed", new EventInfo(1272,524,1419,752,new int[]{}));

        eventMap.put("Dress_MA019", new EventInfo(1702,345,1965,583,new int[]{lightIdx+11}));
        eventMap.put("Dress_M013", new EventInfo(324,115,547,296,new int[]{lightIdx+2}));
        eventMap.put("Dress_M012", new EventInfo(59,385,418,526,new int[]{lightIdx+3}));
        eventMap.put("Dress_MA024", new EventInfo(62,394,505,721,new int[]{lightIdx+3}));

        eventMap.put("Read_M009", new EventInfo(1234,553,1310,622,new int[]{lightIdx+8}));
        eventMap.put("Read_M010", new EventInfo(1243,666,1348,722,new int[]{lightIdx+8}));
        eventMap.put("Read_M012", new EventInfo(59,385,418,526,new int[]{lightIdx+3}));
        eventMap.put("Read_M011", new EventInfo(54,637,166,716,new int[]{lightIdx+3}));
        eventMap.put("Read_MA021", new EventInfo(1234,553,1348,722,new int[]{lightIdx+8}));
        eventMap.put("Read_MA024", new EventInfo(62,394,505,721,new int[]{lightIdx+3}));

        eventMap.put("Sleep", new EventInfo(46,463,353,694,new int[]{}));
        eventMap.put("Evening_Meds", new EventInfo(647,133,791,334,new int[]{lightIdx+4}));

        eventMap.put("Work_On_Computer", new EventInfo(1732,662,1908,737,new int[]{lightIdx+11}));
        eventMap.put("Work_At_Table", new EventInfo(785,536,969,673,new int[]{lightIdx+6}));

        eventMap.put("Cook_Dinner", new EventInfo(891,157,1018,367,new int[]{lightIdx+4,lightIdx+5}));
        eventMap.put("Eat_Dinner", new EventInfo(638,549,984,735,new int[]{lightIdx+6}));
        eventMap.put("Wash_Dinner_Dishes", new EventInfo(670,153,789,326,new int[]{lightIdx+4}));
        eventMap.put("Cook_Breakfast", new EventInfo(891,157,1018,367,new int[]{lightIdx+4,lightIdx+5}));
        eventMap.put("Eat_Breakfast", new EventInfo(638,549,984,735,new int[]{lightIdx+6}));
        eventMap.put("Wash_Breakfast_Dishes", new EventInfo(670,153,789,326,new int[]{lightIdx+4}));
        eventMap.put("Cook_Lunch", new EventInfo(891,157,1018,367,new int[]{lightIdx+4,lightIdx+5}));
        eventMap.put("Eat_Lunch", new EventInfo(638,549,984,735,new int[]{lightIdx+6}));
        eventMap.put("Wash_Lunch_Dishes", new EventInfo(670,153,789,326,new int[]{lightIdx+4}));
        eventMap.put("Wash_Dishes", new EventInfo(670,153,789,326,new int[]{lightIdx+4}));
        eventMap.put("Cook", new EventInfo(891,157,1018,367,new int[]{lightIdx+4,lightIdx+5}));
        eventMap.put("Eat", new EventInfo(638,549,984,735,new int[]{lightIdx+6}));

        eventMap.put("doorBell", new EventInfo(36,86,1963,754,new int[]{cameraIdx+0}));
        eventMap.put("Evening_News", new EventInfo(36,86,1963,754,new int[]{speakerIdx+1}));
        eventMap.put("Wake_Up", new EventInfo(46,463,353,694,new int[]{speakerIdx+0}));
        eventMap.put("Child_Evt", new EventInfo(36,86,1963,754,new int[]{cameraIdx + 1}));



        eventCat.add("Enter_Home");
        eventCat.add("Leave_Home");
        eventCat.add("Toilet");
        eventCat.add("Personal_Hygiene");
        eventCat.add("Bathe");
        eventCat.add("Bed_Toilet_Transition");

        eventCat.add("Watch_TV");
        eventCat.add("Sleep_Out_Of_Bed");

        eventCat.add("Dress");

        eventCat.add("Read");

        eventCat.add("Sleep");
        eventCat.add("Evening_Meds");

        eventCat.add("Work_On_Computer");
        eventCat.add("Work_At_Table");

        eventCat.add("Cook_Dinner");
        eventCat.add("Eat_Dinner");
        eventCat.add("Wash_Dinner_Dishes");
        eventCat.add("Cook_Breakfast");
        eventCat.add("Eat_Breakfast");
        eventCat.add("Wash_Breakfast_Dishes");
        eventCat.add("Cook_Lunch");
        eventCat.add("Eat_Lunch");
        eventCat.add("Wash_Lunch_Dishes");
        eventCat.add("Wash_Dishes");
        eventCat.add("Cook");
        eventCat.add("Eat");

        eventCat.add("doorBell");
        eventCat.add("Evening_News");
        eventCat.add("Wake_Up");
        eventCat.add("Child_Evt");
    }



    private Point2D getRandomPoint(Point2D a, Point2D b){
        int rx = rand.nextInt(b.x - a.x) + a.x;
        int ry = rand.nextInt(b.y - a.y) + a.y;
//        rx = (a.x + b.x) /2 + rand.nextInt(20) -10;
//        ry = (a.y + b.y) /2+ rand.nextInt(20) -10;
        return new Point2D(rx, ry);
    }

    private List<Light> lightOnLocation(int x, int y){
        ArrayList<Thing> IoT = ((CASAS)building).getIoTThings();
        int i = ((CASAS)building).getLightIdx();
        ArrayList<Light> ret = new ArrayList<>();
        while (IoT.get(i) instanceof Light){
            Light tmpLight = (Light)IoT.get(i);
            if (tmpLight.isOn() && (tmpLight.isWithinR(x,y)) && (tmpLight.isLineOfSight(x,y, building.getCells(), building.getWidth(), building.getHeight()))){
                ret.add(tmpLight);
            }
            i += 1;
        }
        return ret;
    }

    private List<Light> getOnLight(){
        ArrayList<Thing> IoT = ((CASAS)building).getIoTThings();
        int i = ((CASAS)building).getLightIdx();
        ArrayList<Light> ret = new ArrayList<>();
        while (IoT.get(i) instanceof Light){
            Light tmpLight = (Light)IoT.get(i);
            if (tmpLight.isOn()){
                ret.add(tmpLight);
            }
            i += 1;
        }
        return ret;
    }

    public void testWithActivity()
    {
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        lightIdx = ((CASAS)building).getLightIdx();
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, 1.0,1.0}))));
        int commandCnt = 0;
        DynamicContext nowCtx;
        String lineStr="";
        int graphCnt = 0;
        ArrayList<String> evtWindow = new ArrayList<>();
        ArrayList<DynamicContext> ctxWindow = new ArrayList<>();
        ArrayList<ArrayList<Thing>> thingWindow = new ArrayList<>();
        String tmpPrevEvt = "";
        DynamicContext prevCtx = null;
        boolean isDoorBell = false;
        boolean isWatchNews = false;
        boolean isChildNoise = false;
        boolean isSleep = false;
        String ctxActivity = "";
        cmdNoCnt = new ArrayList<>();
        cmdAct = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        try {
            BufferedWriter output= new BufferedWriter(new FileWriter("exp_result/simDataset_cateCtx.txt"));
            while (true){
                dataReader.mark(1000);
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split("\\s+", 0);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalDateTime now = LocalDateTime.parse(time[0], defaultFormatter);
                String fields = arrStr[2];
                String sensor = fields.split("[0-9]")[0];
                int num = Integer.parseInt(fields.replaceAll("[^0-9]",""));
                String state = arrStr[3];
                String evt = "";
                isDoorBell = false;
                isChildNoise = false;
                if ((isInHome) && (arrStr.length <= 4)){
                    if (LocalTime.of(18, 0).isBefore(now.toLocalTime()) &&  LocalTime.of(19, 00).isAfter(now.toLocalTime()) ){
                        String[] tmp = new String[5];
                        isWatchNews = true;
                        System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                        tmp[4] = "Evening_News";
                        lineStr += " Evening_News";
                        arrStr = tmp;
                    }else{
                        isWatchNews = false;
                        double randomD = rand.nextDouble();
                        if (randomD< doorBellEvtProb){
                            String[] tmp = new String[5];
                            isDoorBell = true;
                            System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                            tmp[4] = "doorBell";
                            lineStr += " doorBell";
                            arrStr = tmp;
                        }else {
                            if (randomD < childCameraProb){
                                String[] tmp = new String[5];
                                isDoorBell = true;
                                System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                                tmp[4] = "Child_Evt";
                                lineStr += " Child_Evt";
                                arrStr = tmp;
                            }
                        }
                    }

                }
                if (arrStr.length > 4){

                    evt = arrStr[4].split("=")[0];
                    ctxActivity = evt;
                    String start = "";
                    if (arrStr[4].indexOf('=') >= 0){
                        start = arrStr[4].split("=")[1];
                    }
                    EventInfo evtInfo = null;
                    if (multiPlaceEvt.contains(evt)){
                        evt += "_" +fields;
                        if (eventMap.containsKey(evt)) {
                            evtInfo = eventMap.get(evt);
                        }
                    }else{
                        if (eventMap.containsKey(evt)){
                            evtInfo = eventMap.get(evt);
                        }
                    }
                    if (evtInfo == null){ continue;}

                    if (    !evt.equals(tmpPrevEvt) &&
                            isSleep && !evt.equals("Bed_Toilet_Transition")
                            && tmpPrevEvt.equals("Sleep") && (LocalTime.of(07, 0).isBefore(now.toLocalTime())
                            &&  LocalTime.of(10, 00).isAfter(now.toLocalTime()) )){
                        dataReader.reset();
                        isSleep = false;
                        evt = "Wake_Up";
                        ctxActivity = "Wake_Up";
                        lineStr += " Wake_Up";
                    }
                    nowCtx = new DynamicContext();
                    Point2D nowL = getRandomPoint(evtInfo.a, evtInfo.b);
                    nowCtx.addContext(new LocationContext(nowL));
                    nowCtx.addContext(new TimeContext(now.toLocalTime().toSecondOfDay()));
                    nowCtx.addContext(new BinContext(isDoorBell));
                    ArrayList<Integer> newCate = new ArrayList<>();
                    newCate.add(eventCat.indexOf(ctxActivity));
                    nowCtx.addContext(new CategoricalContext(newCate));
                    ArrayList<Action> thingActionToDo = new ArrayList<>();
                    ArrayList<Thing> thingToDo = new ArrayList<>();


                    if (evt.equals("Sleep")) {isSleep = true;}
                    // undo previous things
                    if ((!evt.equals(tmpPrevEvt)) && (tmpPrevEvt != "")){
                        //undo previous action
                        EventInfo prevInfo = eventMap.get(tmpPrevEvt);
                        int[] light = prevInfo.thingOpt;
                        int[] currthing = evtInfo.thingOpt;

                        ArrayList<Thing> thingToUndo = new ArrayList<>();
                        for (int li: light){
                            Thing newD = IoTThings.get(lightIdx + li);
                            if (newD instanceof  Light){
                                Light newL = (Light)newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Speaker){
                                Speaker newL = (Speaker) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Camera){
                                Camera newL = (Camera) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                        }
                        evtWindow.add(tmpPrevEvt);
                        thingWindow.add(thingToUndo);
                        ctxWindow.add(prevCtx);
                    }

                    int prevIdx = 0;
                    HashMap<String, DynamicContext> undoEvt = new HashMap<>();
                    HashMap<String, ArrayList<Thing>> undoThing = new HashMap<>();
                    while (evtWindow.size() > 0){
                        DynamicContext tmpCtx = ctxWindow.get(0);
                        int timePrev = ((TimeContext)tmpCtx.getContexts().get(1)).getTime();
                        int timeNow = ((TimeContext)nowCtx.getContexts().get(1)).getTime();
                        int pureSub = Math.abs(timeNow - timePrev);
                        if (pureSub > TimeContext.range){
                            pureSub = pureSub - (int)TimeContext.range;
                        }
                        if (pureSub > 600){
                            String prevEvt = evtWindow.get(0);
                            if (!prevEvt.equals(evt)){
                                undoEvt.put(evtWindow.get(0), ctxWindow.get(0));
                                undoThing.put(evtWindow.get(0), thingWindow.get(0));
                            }
                            evtWindow.remove(0);
                            ctxWindow.remove(0);
                            thingWindow.remove(0);
                        }else {
                            break;
                        }
                    }
                    for (String key:undoEvt.keySet()){
                        ArrayList<Thing> thingToUndo = undoThing.get(key);
                        ArrayList<Action> undoFilter = new ArrayList<>();
                        ArrayList<Thing> actualUndo = new ArrayList<>();
                        for (Thing li: thingToUndo){
                            if (li instanceof Light){
                                if (((Light)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new LightAction(null, false));
                                }
                            }
                            if (li instanceof Speaker){
                                if (((Speaker)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new SpeakerAction(null, false));
                                }
                            }
                            if (li instanceof Camera){
                                if (((Camera)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new CameraAction(null, false));
                                }
                            }
                        }
                        thingToUndo = actualUndo;
                        DynamicContext tmpCtx = undoEvt.get(key);

//                        tmpCtx.addContext(new (1));
                        int noBeforeCnt = 0;
                        while (thingToUndo.size() > 0){
                            ThingAction res = myAgent.getConfiguration(tmpCtx, thingToUndo.get(0).getClass(), undoFilter.get(0));
//                            ThingAction res = myAgent.getConfiguration(tmpCtx, Thing.class, null);

                            res.doAction();
                            Thing chosenL = res.getActions().get(0).getDevice();
                            if (thingToUndo.contains(chosenL) && (res.getActions().get(0).checkAction() == 1)){
                                //right
                                undoFilter.remove(thingToUndo.indexOf(chosenL));
                                thingToUndo.remove(chosenL);
//                                output.write("yes action for undo evt " + key + "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add("TurnOffDevices");
                                noBeforeCnt = 0;
                            }else{
                                noBeforeCnt += 1;
//                                output.write("wrong action for undo evt " + key+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");
                                //wrong
                                myAgent.getPunished();
                            }
                        }
                    }

                    //look for new things to do.

                    if (evt.contains("Watch_TV")){
                    }
                    if (evt.contains("Enter_Home")){
                        isInHome = true;
                    }
                    if (evt.contains("Sleep")){
                        // turn off the light in the location
                        List<Light> lightToTurnOff = lightOnLocation(nowL.x, nowL.y);
                        for (Light li: lightToTurnOff){
                            thingActionToDo.add(new LightAction(li, false));
                            thingToDo.add(li);
                        }
                    }else{
                        if (evt.equals("Leave_Home")){
                            isInHome = false;
                            // turn off all light
                            List<Light> lightToTurnOff = getOnLight();
                            for (Light li: lightToTurnOff){
                                thingActionToDo.add(new LightAction(li, false));
                                thingToDo.add(li);
                            }
                        }else{
                            // turn on the required light
                            int[] thingOpt = evtInfo.thingOpt;
                            for (int li: thingOpt){
                                Thing newD = IoTThings.get(lightIdx + li);
                                if (newD instanceof  Light){
                                    Light newL = (Light)newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new LightAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Speaker){
                                    Speaker newL = (Speaker) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new SpeakerAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Camera){
                                    Camera newL = (Camera) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new CameraAction(newL, true));
                                    }
                                }
                            }
                        }
                    }


                    // Do new Actions
                    DynamicContext newCtx = new DynamicContext(nowCtx);
//                    newCtx.addContext(new BinContext(0));
                    int noBeforeCnt = 0;
                    while (thingToDo.size() > 0){
                        ThingAction res = myAgent.getConfiguration(newCtx, thingToDo.get(0).getClass(), thingActionToDo.get(0));
//                        ThingAction res = myAgent.getConfiguration(newCtx, Thing.class, null);
                        res.doAction();
                        Thing chosenL = res.getActions().get(0).getDevice();
                        if (!thingToDo.contains(chosenL)){
//                            output.write("wrong action for evt " + lineStr+ "\n");
//                            output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");

                            //wrong
                            myAgent.getPunished();
                            noBeforeCnt += 1;

                        }else{
                            if (thingActionToDo.get(thingToDo.indexOf(chosenL)).isSame(res.getActions().get(0))){

                                //right
                                thingActionToDo.remove(thingToDo.indexOf(chosenL));
                                thingToDo.remove(chosenL);
//                                output.write("yes action for evt " + lineStr+ "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add(ctxActivity);
                                noBeforeCnt = 0;
                            }else{
//                                output.write("wrong action for evt " + lineStr+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() +'\n');

                                //wrong
                                myAgent.getPunished();
                                noBeforeCnt += 1;
                            }
                        }
                    }
                    if (commandCnt / 100  > graphCnt){
                        graphCnt += 1;
                        if (graphCnt == 20 ){
//                            eventMap.put("Evening_Meds", new EventInfo(647,133,791,334,new int[]{6}));
                            Thing light4 = IoTThings.get(lightIdx + 4);
                            IoTThings.set(lightIdx + 4, IoTThings.get(lightIdx + 6));
                            IoTThings.set(lightIdx + 6, light4);
                        }
                    }
                    tmpPrevEvt = evt;
                    prevCtx = nowCtx;
//                    evtWindow.add(evt);
//                    ctxWindow.add(nowCtx);
                }
            }
            double yesCnt_one= 0;
            double yesCnt_last= 0;
            double noTot = 0;
            output.write("id,NoCnt,Activity\n");
            for (int i = 0; i <cmdNoCnt.size(); i++){
                output.write((i+1) + ", " + cmdNoCnt.get(i) + ","+cmdAct.get(i)+"\n");
                if (cmdNoCnt.get(i) == 0){
                    yesCnt_one += 1;
                    if (i >=200){yesCnt_last += 1;}
                }
                noTot += cmdNoCnt.get(i);

            }
            System.out.println("Command satisfied by first action: " + yesCnt_one / commandCnt);
            output.close();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }

        System.out.println("finish");
    }

    public void testAllEvent(){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        lightIdx = ((CASAS)building).getLightIdx();
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, 1.0}))));
        int graphCnt = 0;
        int commandCnt = 0;
        String graph = "";
        DynamicContext nowCtx;
        String lineStr="";
        ArrayList<String> evtWindow = new ArrayList<>();
        ArrayList<DynamicContext> ctxWindow = new ArrayList<>();
        ArrayList<ArrayList<Thing>> thingWindow = new ArrayList<>();
        String tmpPrevEvt = "";
        DynamicContext prevCtx = null;
        boolean isDoorBell = false;
        boolean isWatchNews = false;
        boolean isChildNoise = false;
        boolean isSleep = false;
        String graph2= "";
        cmdNoCnt = new ArrayList<>();
        cmdAct = new ArrayList<>();
        String ctxActivity = "";
        cmdDevice = new ArrayList<>();
        try {
            BufferedWriter output= new BufferedWriter(new FileWriter("exp_result/simDataset_locationCtx.txt"));
            while (true){
                dataReader.mark(1000);
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split("\\s+", 0);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalDateTime now = LocalDateTime.parse(time[0], defaultFormatter);
                String fields = arrStr[2];
                String sensor = fields.split("[0-9]")[0];
                int num = Integer.parseInt(fields.replaceAll("[^0-9]",""));
                String state = arrStr[3];
                String evt = "";
                isDoorBell = false;
                isChildNoise = false;
                if ((isInHome) && (arrStr.length <= 4)){
                    if (LocalTime.of(18, 0).isBefore(now.toLocalTime()) &&  LocalTime.of(19, 00).isAfter(now.toLocalTime()) ){
                        String[] tmp = new String[5];
                        isWatchNews = true;
                        System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                        tmp[4] = "Evening_News";
                        lineStr += " Evening_News";
                        arrStr = tmp;
                    }else{
                        isWatchNews = false;
                        double randomD = rand.nextDouble();
                        if (randomD< doorBellEvtProb){
                            String[] tmp = new String[5];
                            isDoorBell = true;
                            System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                            tmp[4] = "doorBell";
                            lineStr += " doorBell";
                            arrStr = tmp;
                        }else {
                            if (randomD < childCameraProb){
                                String[] tmp = new String[5];
                                isDoorBell = true;
                                System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                                tmp[4] = "Child_Evt";
                                lineStr += " Child_Evt";
                                arrStr = tmp;
                            }
                        }
                    }

                }
                if (arrStr.length > 4){
                    evt = arrStr[4].split("=")[0];
                    ctxActivity = evt;
                    String start = "";
                    if (arrStr[4].indexOf('=') >= 0){
                        start = arrStr[4].split("=")[1];
                    }
                    EventInfo evtInfo = null;
                    if (multiPlaceEvt.contains(evt)){
                        evt += "_" +fields;
                        if (eventMap.containsKey(evt)) {
                            evtInfo = eventMap.get(evt);
                        }
                    }else{
                        if (eventMap.containsKey(evt)){
                            evtInfo = eventMap.get(evt);
                        }
                    }
                    if (evtInfo == null){ continue;}

                    if (    !evt.equals(tmpPrevEvt) &&
                            isSleep && !evt.equals("Bed_Toilet_Transition")
                            && tmpPrevEvt.equals("Sleep") && (LocalTime.of(07, 0).isBefore(now.toLocalTime())
                            &&  LocalTime.of(10, 00).isAfter(now.toLocalTime()) )){
                        dataReader.reset();
                        isSleep = false;
                        evt = "Wake_Up";
                        ctxActivity = "Wake_Up";
                        lineStr += " Wake_Up";
                    }
                    nowCtx = new DynamicContext();
                    Point2D nowL = getRandomPoint(evtInfo.a, evtInfo.b);
                    nowCtx.addContext(new LocationContext(nowL));
                    nowCtx.addContext(new TimeContext(now.toLocalTime().toSecondOfDay()));
                    nowCtx.addContext(new BinContext(isDoorBell));
                    ArrayList<Action> thingActionToDo = new ArrayList<>();
                    ArrayList<Thing> thingToDo = new ArrayList<>();


                    if (evt.equals("Sleep")) {isSleep = true;}
                    // undo previous things
                    if ((!evt.equals(tmpPrevEvt)) && (tmpPrevEvt != "")){
                        //undo previous action
                        EventInfo prevInfo = eventMap.get(tmpPrevEvt);
                        int[] light = prevInfo.thingOpt;
                        int[] currthing = evtInfo.thingOpt;

                        ArrayList<Thing> thingToUndo = new ArrayList<>();
                        for (int li: light){
                            Thing newD = IoTThings.get(lightIdx + li);
                            if (newD instanceof  Light){
                                Light newL = (Light)newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Speaker){
                                Speaker newL = (Speaker) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Camera){
                                Camera newL = (Camera) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                        }
                        evtWindow.add(tmpPrevEvt);
                        thingWindow.add(thingToUndo);
                        ctxWindow.add(prevCtx);
                    }

                    int prevIdx = 0;
                    HashMap<String, DynamicContext> undoEvt = new HashMap<>();
                    HashMap<String, ArrayList<Thing>> undoThing = new HashMap<>();
                    while (evtWindow.size() > 0){
                        DynamicContext tmpCtx = ctxWindow.get(0);
                        int timePrev = ((TimeContext)tmpCtx.getContexts().get(1)).getTime();
                        int timeNow = ((TimeContext)nowCtx.getContexts().get(1)).getTime();
                        int pureSub = Math.abs(timeNow - timePrev);
                        if (pureSub > TimeContext.range){
                            pureSub = pureSub - (int)TimeContext.range;
                        }
                        if (pureSub > 600){
                            String prevEvt = evtWindow.get(0);
                            if (!prevEvt.equals(evt)){
                                undoEvt.put(evtWindow.get(0), ctxWindow.get(0));
                                undoThing.put(evtWindow.get(0), thingWindow.get(0));
                            }
                            evtWindow.remove(0);
                            ctxWindow.remove(0);
                            thingWindow.remove(0);
                        }else {
                            break;
                        }
                    }
                    for (String key:undoEvt.keySet()){
                        ArrayList<Thing> thingToUndo = undoThing.get(key);
                        ArrayList<Action> undoFilter = new ArrayList<>();
                        ArrayList<Thing> actualUndo = new ArrayList<>();
                        for (Thing li: thingToUndo){
                            if (li instanceof Light){
                                if (((Light)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new LightAction(null, false));
                                }
                            }
                            if (li instanceof Speaker){
                                if (((Speaker)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new SpeakerAction(null, false));
                                }
                            }
                            if (li instanceof Camera){
                                if (((Camera)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new CameraAction(null, false));
                                }
                            }
                        }
                        thingToUndo = actualUndo;
                        DynamicContext tmpCtx = undoEvt.get(key);

//                        tmpCtx.addContext(new (1));
                        int noBeforeCnt = 0;
                        while (thingToUndo.size() > 0){
                            ThingAction res = myAgent.getConfiguration(tmpCtx, thingToUndo.get(0).getClass(), undoFilter.get(0));
//                            ThingAction res = myAgent.getConfiguration(tmpCtx, Thing.class, null);
                            res.doAction();
                            Thing chosenL = res.getActions().get(0).getDevice();
                            if (thingToUndo.contains(chosenL) && (res.getActions().get(0).checkAction() == 1)){
                                //right
                                undoFilter.remove(thingToUndo.indexOf(chosenL));
                                thingToUndo.remove(chosenL);
//                                output.write("yes action for undo evt " + key + "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add("TurnOffDevices");
                                for (int k=0; k <IoTThings.size();k++){
                                    if (IoTThings.get(k)==chosenL){
                                        cmdDevice.add(k);
                                    }
                                }
                                noBeforeCnt = 0;
                            }else{
                                noBeforeCnt += 1;
//                                output.write("wrong action for undo evt " + key+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");
                                //wrong
                                myAgent.getPunished();
                            }
                        }
                    }

                    //look for new things to do.
                    if (evt.contains("Enter_Home")){
                        isInHome = true;
                    }
                    if (evt.contains("Sleep")){
                        // turn off the light in the location
                        List<Light> lightToTurnOff = lightOnLocation(nowL.x, nowL.y);
                        for (Light li: lightToTurnOff){
                            thingActionToDo.add(new LightAction(li, false));
                            thingToDo.add(li);
                        }
                    }else{
                        if (evt.equals("Leave_Home")){
                            isInHome = false;
                            // turn off all light
                            List<Light> lightToTurnOff = getOnLight();
                            for (Light li: lightToTurnOff){
                                thingActionToDo.add(new LightAction(li, false));
                                thingToDo.add(li);
                            }
                        }else{
                            // turn on the required light
                            int[] thingOpt = evtInfo.thingOpt;
                            for (int li: thingOpt){
                                Thing newD = IoTThings.get(lightIdx + li);
                                if (newD instanceof  Light){
                                    Light newL = (Light)newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new LightAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Speaker){
                                    Speaker newL = (Speaker) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new SpeakerAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Camera){
                                    Camera newL = (Camera) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new CameraAction(newL, true));
                                    }
                                }
                            }
                        }
                    }


                    // Do new Actions
                    DynamicContext newCtx = new DynamicContext(nowCtx);
//                    newCtx.addContext(new BinContext(0));
                    int noBeforeCnt = 0;
                    while (thingToDo.size() > 0){
                        ThingAction res = myAgent.getConfiguration(newCtx, thingToDo.get(0).getClass(), thingActionToDo.get(0));
//                        ThingAction res = myAgent.getConfiguration(newCtx, Thing.class, null);
                        res.doAction();
                        Thing chosenL = res.getActions().get(0).getDevice();
                        if (!thingToDo.contains(chosenL)){
//                            output.write("wrong action for evt " + lineStr+ "\n");
//                            output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");

                            //wrong
                            myAgent.getPunished();
                            noBeforeCnt += 1;

                        }else{
                            if (thingActionToDo.get(thingToDo.indexOf(chosenL)).isSame(res.getActions().get(0))){

                                //right
                                thingActionToDo.remove(thingToDo.indexOf(chosenL));
                                thingToDo.remove(chosenL);
//                                output.write("yes action for evt " + lineStr+ "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add(evt);
                                for (int k=0; k <IoTThings.size();k++){
                                    if (IoTThings.get(k)==chosenL){
                                        cmdDevice.add(k);
                                    }
                                }
                                noBeforeCnt = 0;
                            }else{
//                                output.write("wrong action for evt " + lineStr+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() +'\n');

                                //wrong
                                myAgent.getPunished();
                                noBeforeCnt += 1;
                            }
                        }
                    }
                    if (commandCnt / 100  > graphCnt){
                        graphCnt += 1;
                        if (graphCnt == 45){
                            System.out.println(commandCnt);

//                            eventMap.put("Evening_Meds", new EventInfo(647,133,791,334,new int[]{6}));
                            Thing light6 = IoTThings.get(lightIdx + 6);
                            IoTThings.set(lightIdx + 6, IoTThings.get(lightIdx + 9));
                            IoTThings.set(lightIdx + 9, light6);
//                            IoTThings.set(lightIdx+9,new Light(80,695,70));
//                            myAgent.addDevice(IoTThings.get(lightIdx +9));
                            cmdNoCnt.add(-1);
                            cmdDevice.add(-1);
                            cmdAct.add("ChangeDevice");
//                            eventMap.put("Toilet_M013", new EventInfo(35,91,269,352,new int[]{IoTThings.size() -1 }));
//                            eventMap.put("Personal_Hygiene_M013", new EventInfo(35,91,269,352,new int[]{lightIdx+0,IoTThings.size()-1}));
//                            eventMap.put("Bathe", new EventInfo(36,88,258,189,new int[]{IoTThings.size()-1}));
//                            eventMap.put("Bed_Toilet_Transition", new EventInfo(190,156,393,364,new int[]{IoTThings.size()-1}));
                        }
                    }
                    tmpPrevEvt = evt;
                    prevCtx = nowCtx;
//                    evtWindow.add(evt);
//                    ctxWindow.add(nowCtx);
                }
            }
            double yesCnt_one = 0;
            output.write("id,NoCnt,Light,Activity\n");
            for (int i = 0; i <cmdNoCnt.size(); i++){
                output.write((i+1) + "," + cmdNoCnt.get(i)+","+cmdDevice.get(i) + ","+cmdAct.get(i)+"\n");
                if (cmdNoCnt.get(i) == 0){
                    yesCnt_one += 1;
                }
            }
            System.out.println("Command satisfied by first action: " + yesCnt_one / commandCnt);
            System.out.println("Total requests: " + cmdNoCnt.size());
            output.close();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
//        System.out.println("Command satisfied by first action: " + yesCnt_one / commandCnt);
//        System.out.println("Command satisfied by first action after first 1000 requests: " + yesCnt_last / (commandCnt - 500));
        System.out.println("finish");
    }

    public void resetAll(){
        ArrayList<Cell> devices = building.getThingCells();
        for (Cell device: devices){
            ((Thing) device).reset();
        }
        try{
            dataStream.getChannel().position(0);
            dataReader= new BufferedReader(new InputStreamReader(dataStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void testLightRange(){
        for (String key: eventMap.keySet()){
            EventInfo locationPool = eventMap.get(key);
            ArrayList<Thing> IoT = ((CASAS)building).getIoTThings();

            for (int x = locationPool.a.x; x <= locationPool.b.x; x++){
                for (int y = locationPool.a.y; y <= locationPool.b.y; y++) {
                    boolean flag = false;
                    int i = ((CASAS)building).getLightIdx();
                    while (IoT.get(i) instanceof Light){
                        Light tmpLight = (Light)IoT.get(i);
                        if (tmpLight.isWithinR(x,y)){
                            if (tmpLight.isLineOfSight(x,y, building.getCells(), building.getWidth(), building.getHeight())){
                                flag = true;
                                break;
                            }
                            i += 1;
                        }else{
                            i += 1;
                        }
                    }
                    if (!flag){
                        System.out.println("Not in light " + x + ", " + y + "For action " + key);
                        break;
                    }
                }
            }
        }
    }

    private void writeCtxTo_change(BufferedWriter output, DynamicContext ctx, List<Thing> things, Action target, String act) throws IOException{
        output.write(((LocationContext)ctx.getContexts().get(0)).getLocation().x + "," +((LocationContext)ctx.getContexts().get(0)).getLocation().y +","
                + ((TimeContext)ctx.getContexts().get(1)).getTime()+"," +((BinContext)ctx.getContexts().get(2)).getBit()+",");
        int deviceNum = 0;
        target.undoAction();
        if (isEnvChagne){
            for (int i =0; i <things.size();i++){
                int tmpI = i;
                if (i == lightIdx + 6) {
                    tmpI = lightIdx + 9;
                }
                if (i == lightIdx + 9) {
                    tmpI = lightIdx + 6;
                }
                if ( things.get(i) == target.getDevice()){
                    deviceNum = tmpI;
                }
                output.write(things.get(tmpI).getDeviceState()+",");
            }
            output.write(things.get(lightIdx+6).getDeviceState()+",");
        }else{
            for (int i =0; i <things.size();i++){
                output.write(things.get(i).getDeviceState()+",");
                if (things.get(i) == target.getDevice()){
                    deviceNum = i;
                }
            }
            output.write(0+",");
        }
        target.doAction();
        output.write(target.checkAction() + "," + (deviceNum*2 + target.checkAction())+ ","+act+"\n" );
    }
    private void writeCtxTo_new(BufferedWriter output, DynamicContext ctx, List<Thing> things, Action target, String act) throws IOException{
        output.write(((LocationContext)ctx.getContexts().get(0)).getLocation().x + "," +((LocationContext)ctx.getContexts().get(0)).getLocation().y +","
                + ((TimeContext)ctx.getContexts().get(1)).getTime()+"," +((BinContext)ctx.getContexts().get(2)).getBit()+",");
        int deviceNum = 0;
        target.undoAction();
        if (isEnvChagne){
            for (int i =0; i <things.size();i++){
                if (i == lightIdx + 6){
                    if ( things.get(i) == target.getDevice()){
                        deviceNum = things.size();
                    }
                    output.write(0+",");
                }else{
                    if (things.get(i) == target.getDevice()){
                        deviceNum = i;
                    }
                    output.write(things.get(i).getDeviceState()+",");
                }
            }
            output.write(things.get(lightIdx+6).getDeviceState()+",");
        }else{
            for (int i =0; i <things.size();i++){
                output.write(things.get(i).getDeviceState()+",");
                if (things.get(i) == target.getDevice()){
                    deviceNum = i;
                }
            }
            output.write(0+",");
        }
        target.doAction();
        output.write(target.checkAction() + "," + (deviceNum*2 + target.checkAction())+ ","+act+"\n" );
    }

    public void pythonData(){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        lightIdx = ((CASAS)building).getLightIdx();
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, 1.0}))));
        int graphCnt = 0;
        int commandCnt = 0;
        String graph = "";
        DynamicContext nowCtx;
        String lineStr="";
        ArrayList<String> evtWindow = new ArrayList<>();
        ArrayList<DynamicContext> ctxWindow = new ArrayList<>();
        ArrayList<ArrayList<Thing>> thingWindow = new ArrayList<>();
        String tmpPrevEvt = "";
        DynamicContext prevCtx = null;
        boolean isDoorBell = false;
        boolean isWatchNews = false;
        boolean isChildNoise = false;
        boolean isSleep = false;
        String graph2= "";
        cmdNoCnt = new ArrayList<>();
        cmdAct = new ArrayList<>();
        String ctxActivity = "";
        cmdDevice = new ArrayList<>();
        try {
            BufferedWriter pOutput= new BufferedWriter(new FileWriter("exp_result/python_data_sim.csv"));
            pOutput.write("x,y,time,bin1");
            for (int i = 0; i <IoTThings.size() +1;i++){pOutput.write(",device" + i);}
            pOutput.write(",req,target,Activity\n");
            while (true){
                dataReader.mark(1000);
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split("\\s+", 0);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalDateTime now = LocalDateTime.parse(time[0], defaultFormatter);
                String fields = arrStr[2];
                String sensor = fields.split("[0-9]")[0];
                int num = Integer.parseInt(fields.replaceAll("[^0-9]",""));
                String state = arrStr[3];
                String evt = "";
                isDoorBell = false;
                isChildNoise = false;
                if ((isInHome) && (arrStr.length <= 4)){
                    if (LocalTime.of(18, 0).isBefore(now.toLocalTime()) &&  LocalTime.of(19, 00).isAfter(now.toLocalTime()) ){
                        String[] tmp = new String[5];
                        isWatchNews = true;
                        System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                        tmp[4] = "Evening_News";
                        lineStr += " Evening_News";
                        arrStr = tmp;
                    }else{
                        isWatchNews = false;
                        double randomD = rand.nextDouble();
                        if (randomD< doorBellEvtProb){
                            String[] tmp = new String[5];
                            isDoorBell = true;
                            System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                            tmp[4] = "doorBell";
                            lineStr += " doorBell";
                            arrStr = tmp;
                        }else {
                            if (randomD < childCameraProb){
                                String[] tmp = new String[5];
                                isDoorBell = true;
                                System.arraycopy(arrStr, 0, tmp, 0, arrStr.length);
                                tmp[4] = "Child_Evt";
                                lineStr += " Child_Evt";
                                arrStr = tmp;
                            }
                        }
                    }

                }
                if (arrStr.length > 4){
                    evt = arrStr[4].split("=")[0];
                    ctxActivity = evt;
                    String start = "";
                    if (arrStr[4].indexOf('=') >= 0){
                        start = arrStr[4].split("=")[1];
                    }
                    EventInfo evtInfo = null;
                    if (multiPlaceEvt.contains(evt)){
                        evt += "_" +fields;
                        if (eventMap.containsKey(evt)) {
                            evtInfo = eventMap.get(evt);
                        }
                    }else{
                        if (eventMap.containsKey(evt)){
                            evtInfo = eventMap.get(evt);
                        }
                    }
                    if (evtInfo == null){ continue;}

                    if (    !evt.equals(tmpPrevEvt) &&
                            isSleep && !evt.equals("Bed_Toilet_Transition")
                            && tmpPrevEvt.equals("Sleep") && (LocalTime.of(07, 0).isBefore(now.toLocalTime())
                            &&  LocalTime.of(10, 00).isAfter(now.toLocalTime()) )){
                        dataReader.reset();
                        isSleep = false;
                        evt = "Wake_Up";
                        ctxActivity = "Wake_Up";
                        lineStr += " Wake_Up";
                    }
                    nowCtx = new DynamicContext();
                    Point2D nowL = getRandomPoint(evtInfo.a, evtInfo.b);
                    nowCtx.addContext(new LocationContext(nowL));
                    nowCtx.addContext(new TimeContext(now.toLocalTime().toSecondOfDay()));
                    nowCtx.addContext(new BinContext(isDoorBell));
                    ArrayList<Action> thingActionToDo = new ArrayList<>();
                    ArrayList<Thing> thingToDo = new ArrayList<>();


                    if (evt.equals("Sleep")) {isSleep = true;}
                    // undo previous things
                    if ((!evt.equals(tmpPrevEvt)) && (tmpPrevEvt != "")){
                        //undo previous action
                        EventInfo prevInfo = eventMap.get(tmpPrevEvt);
                        int[] light = prevInfo.thingOpt;
                        int[] currthing = evtInfo.thingOpt;

                        ArrayList<Thing> thingToUndo = new ArrayList<>();
                        for (int li: light){
                            Thing newD = IoTThings.get(lightIdx + li);
                            if (newD instanceof  Light){
                                Light newL = (Light)newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Speaker){
                                Speaker newL = (Speaker) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                            if (newD instanceof  Camera){
                                Camera newL = (Camera) newD;
                                boolean flag = true;
                                for (int idx: currthing) {if (idx == li) {flag = false; break;}}
                                if ((flag) && (newL.isOn())){
                                    thingToUndo.add(newL);
                                }
                            }
                        }
                        evtWindow.add(tmpPrevEvt);
                        thingWindow.add(thingToUndo);
                        ctxWindow.add(prevCtx);
                    }

                    int prevIdx = 0;
                    HashMap<String, DynamicContext> undoEvt = new HashMap<>();
                    HashMap<String, ArrayList<Thing>> undoThing = new HashMap<>();
                    while (evtWindow.size() > 0){
                        DynamicContext tmpCtx = ctxWindow.get(0);
                        int timePrev = ((TimeContext)tmpCtx.getContexts().get(1)).getTime();
                        int timeNow = ((TimeContext)nowCtx.getContexts().get(1)).getTime();
                        int pureSub = Math.abs(timeNow - timePrev);
                        if (pureSub > TimeContext.range){
                            pureSub = pureSub - (int)TimeContext.range;
                        }
                        if (pureSub > 600){
                            String prevEvt = evtWindow.get(0);
                            if (!prevEvt.equals(evt)){
                                undoEvt.put(evtWindow.get(0), ctxWindow.get(0));
                                undoThing.put(evtWindow.get(0), thingWindow.get(0));
                            }
                            evtWindow.remove(0);
                            ctxWindow.remove(0);
                            thingWindow.remove(0);
                        }else {
                            break;
                        }
                    }
                    for (String key:undoEvt.keySet()){
                        ArrayList<Thing> thingToUndo = undoThing.get(key);
                        ArrayList<Action> undoFilter = new ArrayList<>();
                        ArrayList<Thing> actualUndo = new ArrayList<>();
                        for (Thing li: thingToUndo){
                            if (li instanceof Light){
                                if (((Light)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new LightAction(null, false));
                                }
                            }
                            if (li instanceof Speaker){
                                if (((Speaker)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new SpeakerAction(null, false));
                                }
                            }
                            if (li instanceof Camera){
                                if (((Camera)li).isOn()){
                                    actualUndo.add(li);
                                    undoFilter.add(new CameraAction(null, false));
                                }
                            }
                        }
                        thingToUndo = actualUndo;
                        DynamicContext tmpCtx = undoEvt.get(key);

//                        tmpCtx.addContext(new (1));
                        int noBeforeCnt = 0;
                        while (thingToUndo.size() > 0){
                            ThingAction res = myAgent.getConfiguration(tmpCtx, thingToUndo.get(0).getClass(), undoFilter.get(0));
//                            ThingAction res = myAgent.getConfiguration(tmpCtx, Thing.class, null);
                            res.doAction();
                            Thing chosenL = res.getActions().get(0).getDevice();
                            if (thingToUndo.contains(chosenL) && (res.getActions().get(0).checkAction() == 1)){
                                //right
                                undoFilter.remove(thingToUndo.indexOf(chosenL));
                                thingToUndo.remove(chosenL);
//                                output.write("yes action for undo evt " + key + "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add("TurnOffDevices");
                                for (int k=0; k <IoTThings.size();k++){
                                    if (IoTThings.get(k)==chosenL){
                                        cmdDevice.add(k);
                                    }
                                }
                                writeCtxTo_change(pOutput,tmpCtx,IoTThings,res.getActions().get(0),"TurnOffDevices");
                                noBeforeCnt = 0;
                            }else{
                                noBeforeCnt += 1;
//                                output.write("wrong action for undo evt " + key+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");
                                //wrong
                                myAgent.getPunished();
                            }
                        }
                    }

                    //look for new things to do.
                    if (evt.contains("Enter_Home")){
                        isInHome = true;
                    }
                    if (evt.contains("Sleep")){
                        // turn off the light in the location
                        List<Light> lightToTurnOff = lightOnLocation(nowL.x, nowL.y);
                        for (Light li: lightToTurnOff){
                            thingActionToDo.add(new LightAction(li, false));
                            thingToDo.add(li);
                        }
                    }else{
                        if (evt.equals("Leave_Home")){
                            isInHome = false;
                            // turn off all light
                            List<Light> lightToTurnOff = getOnLight();
                            for (Light li: lightToTurnOff){
                                thingActionToDo.add(new LightAction(li, false));
                                thingToDo.add(li);
                            }
                        }else{
                            // turn on the required light
                            int[] thingOpt = evtInfo.thingOpt;
                            for (int li: thingOpt){
                                Thing newD = IoTThings.get(lightIdx + li);
                                if (newD instanceof  Light){
                                    Light newL = (Light)newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new LightAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Speaker){
                                    Speaker newL = (Speaker) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new SpeakerAction(newL, true));
                                    }
                                }
                                if (newD instanceof  Camera){
                                    Camera newL = (Camera) newD;
                                    boolean flag = true;
                                    if (!(newL.isOn())){
                                        thingToDo.add(newL);
                                        thingActionToDo.add(new CameraAction(newL, true));
                                    }
                                }
                            }
                        }
                    }


                    // Do new Actions
                    DynamicContext newCtx = new DynamicContext(nowCtx);
//                    newCtx.addContext(new BinContext(0));
                    int noBeforeCnt = 0;
                    while (thingToDo.size() > 0){
                        ThingAction res = myAgent.getConfiguration(newCtx, thingToDo.get(0).getClass(), thingActionToDo.get(0));
//                        ThingAction res = myAgent.getConfiguration(newCtx, Thing.class, null);
                        res.doAction();
                        Thing chosenL = res.getActions().get(0).getDevice();
                        if (!thingToDo.contains(chosenL)){
//                            output.write("wrong action for evt " + lineStr+ "\n");
//                            output.write("chosen wrong device " + res.getActions().get(0).getDevice() + " with score " + res.longRet + "\n");

                            //wrong
                            myAgent.getPunished();
                            noBeforeCnt += 1;

                        }else{
                            if (thingActionToDo.get(thingToDo.indexOf(chosenL)).isSame(res.getActions().get(0))){

                                //right
                                thingActionToDo.remove(thingToDo.indexOf(chosenL));
                                thingToDo.remove(chosenL);
//                                output.write("yes action for evt " + lineStr+ "\n");
                                myAgent.getPraised();
                                commandCnt += 1;
                                cmdNoCnt.add(noBeforeCnt);
                                cmdAct.add(ctxActivity);
                                for (int k=0; k <IoTThings.size();k++){
                                    if (IoTThings.get(k)==chosenL){
                                        cmdDevice.add(k);
                                    }
                                }
                                writeCtxTo_change(pOutput,newCtx, IoTThings,res.getActions().get(0),evt);
                                noBeforeCnt = 0;
                            }else{
//                                output.write("wrong action for evt " + lineStr+ "\n");
//                                output.write("chosen wrong device " + res.getActions().get(0).getDevice() +'\n');

                                //wrong
                                myAgent.getPunished();
                                noBeforeCnt += 1;
                            }
                        }
                    }
                    if (commandCnt / 100  > graphCnt){
                        graphCnt += 1;
                        if (graphCnt == 45 ){
//                            eventMap.put("Evening_Meds", new EventInfo(647,133,791,334,new int[]{6}));
                            Thing light6 = IoTThings.get(lightIdx + 6);
                            IoTThings.set(lightIdx + 6, IoTThings.get(lightIdx + 9));
                            IoTThings.set(lightIdx + 9, light6);
                            isEnvChagne = true;
//                            IoTThings.set(lightIdx+6,new Light(80,695,70));
//                            myAgent.addDevice(IoTThings.get(lightIdx +6));
//                            eventMap.put("Toilet_M013", new EventInfo(35,91,269,352,new int[]{IoTThings.size() -1 }));
//                            eventMap.put("Personal_Hygiene_M013", new EventInfo(35,91,269,352,new int[]{lightIdx+0,IoTThings.size()-1}));
//                            eventMap.put("Bathe", new EventInfo(36,88,258,189,new int[]{IoTThings.size()-1}));
//                            eventMap.put("Bed_Toilet_Transition", new EventInfo(190,156,393,364,new int[]{IoTThings.size()-1}));
                        }
                    }
                    tmpPrevEvt = evt;
                    prevCtx = nowCtx;
//                    evtWindow.add(evt);
//                    ctxWindow.add(nowCtx);
                }
            }
            double yesCnt_one = 0;
            for (int i = 0; i <cmdNoCnt.size(); i++){
                if (cmdNoCnt.get(i) == 0){
                    yesCnt_one += 1;
                }
            }
            System.out.println("Command satisfied by first action: " + yesCnt_one / commandCnt);
            pOutput.close();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
//        System.out.println("Command satisfied by first action: " + yesCnt_one / commandCnt);
//        System.out.println("Command satisfied by first action after first 1000 requests: " + yesCnt_last / (commandCnt - 500));
        System.out.println("finish");
    }

    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e){ }

    @Override
    public void getPunished(){  }

    @Override
    public void getPraised(){  }

    @Override
    public void getSmallPunished(){  }

    @Override
    public void mouseCallback(MouseEvent e){
        System.out.println(e.getX()+ " " + e.getY());
        if (myAgent != null){
            myAgent.mouseCallback(e);
        }

        ArrayList<Thing> IoT = ((CASAS)building).getIoTThings();
        int i = ((CASAS)building).getLightIdx();
        while (IoT.get(i) instanceof Light){
            Light tmpLight = (Light)IoT.get(i);
            if (tmpLight.isWithinR(e.getX(),e.getY())){
                if (tmpLight.isLineOfSight(e.getX(),e.getY(), building.getCells(), building.getWidth(), building.getHeight())){
                    System.out.println(i - ((CASAS)building).getLightIdx());
                }
                i += 1;
            }else{
                i += 1;
            }
        }
    }

    @Override
    public void paintNeed(Graphics g){
        if (myAgent != null){
            myAgent.paintNeed(g);
        }
    }
}
