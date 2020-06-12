package IoT.Simulation;

import BuildingSimulator.Building;
import BuildingSimulator.CASAS;
import BuildingSimulator.Cell;
import IoT.Actor;
import IoT.Context.*;
import IoT.SmartAgent.AbstractAgent;
import IoT.SmartAgent.OneAgent;
import IoT.THING.*;
import SampleHomes.CASAS_HH118_ann;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.CookieHandler;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayAgent extends AbstractAgent {
    private static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private FileInputStream dataStream;
    private BufferedReader dataReader;
    private Random myRand = new Random(172849);
    ArrayList<Thing> IoTThings;
    int mSensorIdx;
    int actorSensor = 0;
    int previousActorSensor = 0;
    private OneAgent myAgent;
    Point2D previousL = new Point2D(0,0);
    ArrayList<Integer> motionStack;
    Building building;
    double binHyper1 = 0.09, binHyper2 = 0.02, binHyper3 = 1.0/720.0;
    double catHyper1 = 0.4, catHyper2 = 0.05, catHyper3 = 0.05;
    double coorHyper1 = 0.7, coorHyper2 = 0.5, coorHyper3 = 0.3;
    ArrayList<Integer> breakPoint = new ArrayList<>(Arrays.asList(new Integer[]{10,30,70,120}));
    ArrayList<Integer> cmdNoCnt = new ArrayList<>();
    ArrayList<Integer> cmdDevice = new ArrayList<>();
    LocalDateTime actorTime = LocalDateTime.MIN;

    public PlayAgent(Building building, FileInputStream stream){
        this.building = building;
        dataStream = stream;
        try {
            dataReader= new BufferedReader(new InputStreamReader(dataStream));
//            System.out.println(in.readLine());
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e){ }

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

    private void findNextMotion(Actor actor) throws IOException{
        for (int i = 0; i <10; i++){
            String lineStr = dataReader.readLine();
            if (lineStr == null) { break; }
            String[] arrStr = lineStr.split(" ", 3);
            String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
            LocalTime now = LocalTime.parse(time[0], defaultFormatter);
            String[] fields = arrStr[2].split(" ");
            String sensor = fields[0].split("[0-9]")[0];
            int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
            String state = fields[1];

            // get actor location
            if (sensor.equals("M") || sensor.equals("MA")){
                MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                if (state.equals("ON")) {
                    ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                    int x = myRand.nextInt(pixels.size() / 2);
//                    actor.jumpTo(pixels.get(2 * x), pixels.get( 2 * x + 1), now);
                    actor.jumpTo(mx.getScope().getX(), mx.getScope().getY(), now);
                    actorSensor = num;
                    break;

                }else{
                    ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                    int x = myRand.nextInt(pixels.size() / 2);
//                    previousL.x = pixels.get(2 * x);
//                    previousL.y = pixels.get( 2 * x + 1);
                    previousL.x = mx.getScope().getX();
                    previousL.y = mx.getScope().getY();
                    previousActorSensor = num;
                }

            }
        }
    }

    int handleRequest(LightAction truth, DynamicContext ctx, OneAgent agent){
        int noBefore = 0;
        while (true){
            ThingAction res = myAgent.getConfiguration(ctx, Light.class, truth);
            res.doAction();
            if (res.getActions().get(0).isSame(truth)){
                myAgent.getPraised();
                break;
            }else {
                myAgent.getPunished();
                noBefore += 1;
            }
        }
        return noBefore;
    }
    public double readDataset(Actor actor, int ctxCombination){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        mSensorIdx = ((CASAS)building).getmSensorIdx();
        int lightIdx = ((CASAS)building).getLightIdx();
        switch (ctxCombination){
            case 0:
                myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, coorHyper3}))));
                break;
            case 1:
                myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{catHyper1, catHyper3, catHyper2}))));
                break;
            case 2:
                Double[] newAlpha = new Double[37];
                newAlpha[0] = binHyper3;
                for (int i = 0; i < 18; i++){
                    newAlpha[i + 1] = binHyper1;
                }
                for (int i = 0; i < 18; i++){
                    newAlpha[i + 19] = binHyper2;
                }
                myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(newAlpha))));
                break;
            default:
                myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, coorHyper3}))));
        }


        int commandCnt = 0;
        actorSensor = 0;
        String lineStr="";
        previousL = new Point2D(0,0);
        motionStack = new ArrayList<>();
        cmdNoCnt = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        double noTotLast = 0;
        int noBefore = 0;
        LightAction cacheAction=null;
        LocalDateTime cacheTime=null;
        int cacheNum = 0;
        boolean flag=false;
        try{
            BufferedWriter output = null;
            BufferedWriter pOutput = null;
            switch (ctxCombination){
                case 0:
                    output= new BufferedWriter(new FileWriter("exp_result/RealDataset_locTimeCtx.txt"));
                    pOutput= new BufferedWriter(new FileWriter("exp_result/python_data_location.csv"));
                    pOutput.write("time,x,y,px,py");
                    break;
                case 1:
                    output= new BufferedWriter(new FileWriter("exp_result/RealDataset_cateCtx.txt"));
                    pOutput= new BufferedWriter(new FileWriter("exp_result/python_data_cate.csv"));
                    pOutput.write("Cate1, time, cate2");
                    break;
                case 2:
                    output= new BufferedWriter(new FileWriter("exp_result/RealDataset_binCtx.txt"));
                    pOutput= new BufferedWriter(new FileWriter("exp_result/python_data_bin.csv"));
                    pOutput.write("time,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30,s31,s32,s33,s34,s35");
                    break;
                default:
                    output= new BufferedWriter(new FileWriter("exp_result/RealDataset_locTimeCtx.txt"));
                    pOutput= new BufferedWriter(new FileWriter("exp_result/python_data_location.csv"));
                    pOutput.write("time,x,y,px,py");
                    break;
            }
            for (int i = lightIdx; i <mSensorIdx;i++){pOutput.write(",light" + (i-lightIdx));}
            pOutput.write(",req,target\n");
            while (true){
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalDateTime now = LocalDateTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON") ) {
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
//                        actor.jumpTo(pixels.get(2 * x), pixels.get( 2 * x + 1), now.toLocalTime());
                        actor.jumpTo(mx.getScope().getX(), mx.getScope().getY(), now.toLocalTime());
                        actorSensor = num;
                        actorTime = now;
                        if (!motionStack.contains(num)){
                            motionStack.add(0,num);
                        }

                    }else{
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
//                        previousL.x = pixels.get(2 * x);
//                        previousL.y = pixels.get( 2 * x + 1);
                        previousL.x = mx.getScope().getX();
                        previousL.y = mx.getScope().getY();
                        previousActorSensor = num;
                        if (motionStack.contains(num)){
                            motionStack.remove(motionStack.indexOf(num));
                        }
                    }

                }
                //get a command
                if (sensor.equals("L")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    LightAction newAction = null;
                    if (state.equals("ON")){
                        if (mx.isOn()){
                            continue;
                        }
                        newAction = new LightAction(mx, true);
                    }
                    if (state.equals("OFF")){
                        if (!mx.isOn()){
                            continue;
                        }
                        newAction = new LightAction(mx, false);
                    }
                    if (newAction == null) {
                        continue;
                    }

                    if (motionStack.size() == 0){
                        //read new Location
                        dataReader.mark(10000);
                        findNextMotion(actor);
                        dataReader.reset();
                    }
                    DynamicContext nowCtx = null;
                    String outStr = "";
                    switch (ctxCombination){
                        case 0:
                            nowCtx = new DynamicContext(actor.getTopLeftX(), actor.getTopLeftY(), now.toLocalTime().toSecondOfDay());
                            nowCtx.addContext(new LocationContext(previousL));
                            outStr = now.toLocalTime().toSecondOfDay() + "," + actor.getTopLeftX() + "," + actor.getTopLeftY() + "," + previousL.x + "," + previousL.y;
                            break;
                        case 1:
                            nowCtx = new DynamicContext();
                            ArrayList<Integer> newCate = new ArrayList<>();
                            newCate.add(actorSensor);
                            nowCtx.addContext(new CategoricalContext(newCate));
                            nowCtx.addContext(new TimeContext(now.toLocalTime().toSecondOfDay()));
                            newCate = new ArrayList<>();
                            newCate.add(previousActorSensor);
                            nowCtx.addContext(new CategoricalContext(newCate));
                            outStr = actorSensor + "," +  now.toLocalTime().toSecondOfDay()  + "," + previousActorSensor;
                            break;
                        case 2:
                            nowCtx = new DynamicContext();
                            nowCtx.addContext(new TimeContext(now.toLocalTime().toSecondOfDay()));
                            outStr = now.toLocalTime().toSecondOfDay()+",";

                            for (int i = 0; i < 18; i++){
                                if (i == actorSensor - 1){
                                    nowCtx.addContext(new BinContext(1));
                                    outStr += "1,";
                                }else{
                                    nowCtx.addContext(new BinContext(0));
                                    outStr += "0,";
                                }
                            }
                            for (int i = 0; i < 18; i++){
                                if (i == previousActorSensor - 1){
                                    nowCtx.addContext(new BinContext(1));
                                    outStr += "1,";
                                }else{
                                    nowCtx.addContext(new BinContext(0));
                                    outStr += "0,";
                                }
                            }
                            outStr += (num -1) + "\n";
                            break;
                        default:
                            nowCtx = new DynamicContext(actor.getTopLeftX(), actor.getTopLeftY(), now.toLocalTime().toSecondOfDay());
                            nowCtx.addContext(new LocationContext(previousL));
                            outStr = now.toLocalTime().toSecondOfDay() + "," + actor.getTopLeftX() + "," + actor.getTopLeftY() + "," + previousL.x + "," + previousL.y;
                            break;

                    }
                    for (int i = lightIdx; i < mSensorIdx; i++){
                        outStr += "," + (((Light)IoTThings.get(i)).isOn()?1:0);
                    }
                    outStr += "," +newAction.checkAction();
//                    outStr += ","+(num -1)+"\n";
                    outStr += ","+((num -1)*2+(newAction.checkAction()==0?0:1)) + "\n";
                    pOutput.write(outStr);
                    cmdNoCnt.add(handleRequest(newAction, nowCtx, myAgent));
                    cmdDevice.add(num-1);
                }
            }
            output.write("id,NoCnt,Light\n");
            for (int i = 0; i <cmdNoCnt.size(); i++) {
                output.write((i + 1) + ", " + cmdNoCnt.get(i) + ", " + cmdDevice.get(i) + "\n");
                if (cmdNoCnt.get(i)==0){noTotLast += 1;}
            }
            output.close();
            pOutput.close();
            System.out.println("First guess accuracy: " + (noTotLast / cmdNoCnt.size())+ "\n");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
        return (noTotLast / cmdNoCnt.size());
    }
    public double playAll(Actor actor){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        mSensorIdx = ((CASAS)building).getmSensorIdx();
        int lightIdx = ((CASAS)building).getLightIdx();
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{coorHyper1, coorHyper2, coorHyper3}))));


        int commandCnt = 0;
        actorSensor = 0;
        String lineStr="";
        String graph = "";
        previousL = new Point2D(0,0);
        motionStack = new ArrayList<>();
        cmdNoCnt = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        double noTotLast = 0;
        int noBefore = 0;
        try{
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_locTimeCtx.txt"));
            while (true){
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalTime now = LocalTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];

                // get actor location
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON") ) {
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
                        actor.jumpTo(pixels.get(2 * x), pixels.get( 2 * x + 1), now);
//                        actor.jumpTo(mx.getScope().getX(), mx.getScope().getY(), now);
                        actorSensor = num;
                        if (!motionStack.contains(num)){
                            motionStack.add(0,num);
                        }

                    }else{
                        previousL.x = mx.getScope().getX();
                        previousL.y = mx.getScope().getY();
                        if (motionStack.contains(num)){
                            motionStack.remove(motionStack.indexOf(num));
                        }
//                        for (int i = 0; i < motionStack.size(); i ++){
//                            if (motionStack.get(i) == num){
//                                motionStack.remove(i);
//                            }
//                        }
                    }

                }

                //get a command
                if (sensor.equals("L") || sensor.equals("LL")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    LightAction newAction = null;
                    if (state.equals("ON")){
                        if (mx.isOn()){
                            continue;
                        }
                        newAction = new LightAction(mx, true);
                    }
                    if (state.equals("OFF")){
                        if (!mx.isOn()){
                            continue;
                        }
                        newAction = new LightAction(mx, false);
                    }
                    if (newAction == null) continue;

                    if (motionStack.size() == 0){
                        //read new Location
                        dataReader.mark(10000);
                        findNextMotion(actor);
                        dataReader.reset();
                    }

                    DynamicContext nowCtx = new DynamicContext(actor.getTopLeftX(), actor.getTopLeftY(), now.toSecondOfDay());
                    nowCtx.addContext(new LocationContext(previousL));
                    while (true){
                        ThingAction res = myAgent.getConfiguration(nowCtx, Light.class, null);
                        res.doAction();
                        if (res.getActions().get(0).isSame(newAction)){
                            myAgent.getPraised();
                            cmdNoCnt.add(noBefore);
                            cmdDevice.add(num -1);
                            noBefore = 0;
                            break;
                        }else {
                            myAgent.getPunished();
                            noBefore += 1;
                        }
                    }
                }
            }
            output.write("id, NoCnt\n");
            for (int i = 0; i <cmdNoCnt.size(); i++) {
                output.write((i + 1) + ", " + cmdNoCnt.get(i) + ", " + cmdDevice.get(i) + "\n");
                if (i >= 500) {noTotLast += cmdNoCnt.get(i);}
            }
            output.close();
            System.out.println("Decision accuracy after first 500 requests: " + ((double)cmdNoCnt.size() - 500.0) / ((double)cmdNoCnt.size() - 500.0 + noTotLast) + "\n");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
        return (double)(cmdNoCnt.size() - 500) / (cmdNoCnt.size() - 500 + noTotLast);
    }

    public double playAllBin(Actor actor){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        mSensorIdx = ((CASAS)building).getmSensorIdx();
        int lightIdx = ((CASAS)building).getLightIdx();
        Double[] newAlpha = new Double[37];
        newAlpha[0] = coorHyper2;
        for (int i = 0; i < 18; i++){
            newAlpha[i + 1] = binHyper1;
        }
        for (int i = 0; i < 18; i++){
            newAlpha[i + 19] = binHyper2;
        }
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(newAlpha))));
        int commandCnt = 0;
        actorSensor = 0;
        String lineStr="";
        String graph = "";
        previousL = new Point2D(0,0);
        motionStack = new ArrayList<>();
        previousActorSensor = 0;
        cmdNoCnt = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        double noTotLast = 0;
        int noBefore = 0;
        try{
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_binCtx.txt"));
            while (true){
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalTime now = LocalTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];

                // get actor location
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON") ) {
                        actorSensor = num;
                        if (!motionStack.contains(num)){
                            motionStack.add(0,num);
                        }

                    }else{
                        previousL.x = mx.getScope().getX();
                        previousL.y = mx.getScope().getY();
                        previousActorSensor = num;
                        if (motionStack.contains(num)){
                            motionStack.remove(motionStack.indexOf(num));
                        }
                    }

                }

                //get a command
                if (sensor.equals("L") || sensor.equals("LL")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    LightAction newAction = null;
                    if (state.equals("ON")){
                        if (mx.isOn()){
                            //System.out.println("Invalid state " + lineStr);
                             continue;
                        }
                        newAction = new LightAction(mx, true);
                    }
                    if (state.equals("OFF")){
                        if (!mx.isOn()){
//                            System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        newAction = new LightAction(mx, false);
                    }
                    if (newAction == null) continue;

                    if (motionStack.size() == 0){
                        //read new Location
                        dataReader.mark(10000);
                        findNextMotion(actor);
                        dataReader.reset();
                    }

                    DynamicContext nowCtx = new DynamicContext();
                    nowCtx.addContext(new TimeContext(now.toSecondOfDay()));
                    for (int i = 0; i < 18; i++){
                        if (i == actorSensor - 1){
                            nowCtx.addContext(new BinContext(1));
                        }else{
                            nowCtx.addContext(new BinContext(0));
                        }
                    }
                    for (int i = 0; i < 18; i++){
                        if (i == previousActorSensor - 1){
                            nowCtx.addContext(new BinContext(1));
                        }else{
                            nowCtx.addContext(new BinContext(0));
                        }
                    }
                    boolean yesFlag = true;
                    while (true){
                        ThingAction res = myAgent.getConfiguration(nowCtx, Light.class, null);
                        res.doAction();
                        if (res.getActions().get(0).isSame(newAction)){
                            myAgent.getPraised();
                            cmdNoCnt.add(noBefore);
                            cmdDevice.add(num -1);
                            noBefore = 0;
                            break;
                        }else {
                            myAgent.getPunished();
                            noBefore += 1;
                        }
                    }
                    commandCnt += 1;
                }
            }
            output.write("id, NoCnt\n");
            for (int i = 0; i <cmdNoCnt.size(); i++) {
                output.write((i + 1) + ", " + cmdNoCnt.get(i) + ", " + cmdDevice.get(i) + "\n");
                if (i >= 500) {noTotLast += cmdNoCnt.get(i);}
            }
            output.close();
            System.out.println("Decision accuracy after first 500 requests: " + (double)(cmdNoCnt.size() - 500) / (cmdNoCnt.size() - 500 + noTotLast) + "\n");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
        return (double)(cmdNoCnt.size() - 500) / (cmdNoCnt.size() - 500 + noTotLast);
    }

    public double playAllCat(Actor actor){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        mSensorIdx = ((CASAS)building).getmSensorIdx();
        int lightIdx = ((CASAS)building).getLightIdx();
        Double[] newAlpha = new Double[37];
        newAlpha[0] = 1.0/720;
        for (int i = 0; i < 18; i++){
            newAlpha[i + 1] = binHyper1;
        }
        for (int i = 0; i < 18; i++){
            newAlpha[i + 19] = binHyper2;
        }
        myAgent = new OneAgent(building.getThingCells(), new DynamicContext.Alpha(new ArrayList<Double>(Arrays.asList(new Double[]{catHyper1, coorHyper2, catHyper2}))));
        int commandCnt = 0;
        actorSensor = 0;
        String lineStr="";
        String graph = "";
        previousL = new Point2D(0,0);
        motionStack = new ArrayList<>();
        previousActorSensor = 0;
        cmdNoCnt = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        double noTotLast = 0;
        int noBefore = 0;
        try{
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_cateCtx.txt"));
            while (true){
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalTime now = LocalTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];

                // get actor location
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON") ) {
                        actorSensor = num;
                        if (!motionStack.contains(num)){
                            motionStack.add(0,num);
                        }

                    }else{
                        previousActorSensor = num;
                        if (motionStack.contains(num)){
                            motionStack.remove(motionStack.indexOf(num));
                        }
                    }

                }

                //get a command
                if (sensor.equals("L") || sensor.equals("LL")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    LightAction newAction = null;
                    if (state.equals("ON")){
                        if (mx.isOn()){
                            //System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        newAction = new LightAction(mx, true);
                    }
                    if (state.equals("OFF")){
                        if (!mx.isOn()){
//                            System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        newAction = new LightAction(mx, false);
                    }
                    if (newAction == null) continue;

                    if (motionStack.size() == 0){
                        //read new Location
                        dataReader.mark(10000);
                        for (int i = 0; i <10; i++){
                            String nextStr = dataReader.readLine();
                            if (nextStr == null) { break; }
                            arrStr = nextStr.split(" ", 3);
                            fields = arrStr[2].split(" ");
                            sensor = fields[0].split("[0-9]")[0];
                            int numN = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                            state = fields[1];

                            // get actor location
                            if (sensor.equals("M") || sensor.equals("MA")){
                                if (state.equals("ON")){
                                    actorSensor = numN;
                                    break;
                                }else{
                                    previousActorSensor = numN;
                                }
                            }
                        }
//                        findNextMotion(actor);

                        dataReader.reset();
                    }

                    DynamicContext nowCtx = new DynamicContext();
                    ArrayList<Integer> newCate = new ArrayList<>();
                    newCate.add(actorSensor);
                    nowCtx.addContext(new CategoricalContext(newCate));
                    nowCtx.addContext(new TimeContext(now.toSecondOfDay()));
                    newCate = new ArrayList<>();
                    newCate.add(previousActorSensor);
                    nowCtx.addContext(new CategoricalContext(newCate));
                    boolean yesFlag = true;
                    while (true){
                        ThingAction res = myAgent.getConfiguration(nowCtx, Light.class, null);
                        res.doAction();
                        if (res.getActions().get(0).isSame(newAction)){
                            myAgent.getPraised();
                            cmdNoCnt.add(noBefore);
                            cmdDevice.add(num -1);
                            noBefore = 0;
                            break;
                        }else {
                            myAgent.getPunished();
                            noBefore += 1;
                        }
                    }
                    commandCnt += 1;
                }
            }
            output.write("id, NoCnt\n");
            for (int i = 0; i <cmdNoCnt.size(); i++) {
                output.write((i + 1) + ", " + cmdNoCnt.get(i) + ", " + cmdDevice.get(i) + "\n");
                if (i >= 500) {noTotLast += cmdNoCnt.get(i);}

            }

            output.close();
            System.out.println("Decision accuracy after first 500 requests: " + ((double)(cmdNoCnt.size() - 500)) / (cmdNoCnt.size() - 500 + noTotLast) + "\n");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
        return (double)(cmdNoCnt.size() - 500) / (cmdNoCnt.size() - 500 + noTotLast);
    }


    public void GridCV(Actor actor){
        double [] binS1 = new double[]{0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2};
        double [] binS2 = new double[]{0.5,0.1,0.05,0.01,0.005,5.0/720.0,2.0/720,1.0/720, 0.5/720};
        double [] binS3 = new double[]{0.02,0.05,0.08,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};

        try {
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_GridCVLocation.txt"));
            double max = 0;
            double ss1 = 0, ss2 = 0, ss3 = 0;
            for (double s1: binS1){
                for (double s2: binS2){
                    for (double s3: binS3) {
                        coorHyper1 = s1;
                        coorHyper2 = s2;
                        coorHyper3 = s3;
                        double ret = readDataset(actor,0);
                        output.write(ret+ "  "+ s1 + " "+ s2 + " " + s3 + "\n");
                        if (ret > max){
                            max = ret;
                            ss1 = s1;
                            ss2 = s2;
                            ss3 = s3;
                        }
                    }
                }
            }
            output.write("Best result" + max + "  "+ ss1 + " "+ ss2 + " " + ss3 + "\n");
            System.out.println("Best result" + max + "  "+ ss1 + " "+ ss2 + " " + ss3);
            output.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void catGridCV(Actor actor){
//        double [] binS1 = new double[]{0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        double [] binS1 = new double[]{0.05, 0.06, 0.07, 0.08, 0.09,0.1,0.2,0.3};
        double [] binS2 = new double[]{ 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07,0.08,0.09};
        double [] binS3 = new double[]{0.5,0.1,0.05,0.01,0.005,5.0/720.0,2.0/720.0,1.0/720.0, 0.5/720.0};
        double max = 0;
        double ss1 = 0, ss2 = 0;
        try {
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_GridCVCat.txt"));
            for (double s1: binS1){
                for (double s2: binS2){
                    for (double s3: binS3) {
                        catHyper1 = s1;
                        catHyper2 = s2;
                        catHyper3 = s3;
                        double ret = readDataset(actor, 1);
                        output.write(ret + "  " + s1 + " " + s2 + "\n");
                        if (ret > max) {
                            max = ret;
                            ss1 = s1;
                            ss2 = s2;
                        }
                    }
                }
            }
            output.write("Best result" + max + "  "+ ss1 + " "+ ss2  + "\n");
            System.out.println("Best result" + max + "  "+ ss1 + " "+ ss2);
            output.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void binGridCV(Actor actor){
//        double [] binS1 = new double[]{0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        double [] binS1 = new double[]{0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09};
        double [] binS2 = new double[]{0.007, 0.008, 0.009, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07,0.08,0.09};
        double max = 0;
        double ss1 = 0, ss2 = 0;
        try {
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_GridCVBin.txt"));
            for (double s1: binS1){
                for (double s2: binS2){
                    binHyper1 = s1;
                    binHyper2 = s2;
                    double ret = readDataset(actor,2);
                    output.write(ret+ "  "+ s1 + " "+ s2 + "\n");
                    if (ret > max){
                        max = ret;
                        ss1 = s1;
                        ss2 = s2;
                    }
                }
            }
            output.write("Best result" + max + "  "+ ss1 + " "+ ss2  + "\n");
            System.out.println("Best result" + max + "  "+ ss1 + " "+ ss2);
            output.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pythonDataLocation(String inputFile){
        try {
            BufferedReader input= new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            BufferedWriter output= new BufferedWriter(new FileWriter("pData_raw_withPrev.csv"));
            output.write("time,x,y,px,py,target\n");
            ArrayList<Thing> IoTThings = ((CASAS)building).getIoTThings();
            int nowX = 0;
            int nowY = 0;
            int nowTime = 0;
            int mSensorIdx = ((CASAS)building).getmSensorIdx();
            int lightIdx = ((CASAS)building).getLightIdx();
            motionStack = new ArrayList<>();
            previousL = new Point2D(0,0);
            while (true){
                String lineStr = input.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalTime now = LocalTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];

                // get actor location
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON")){
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
                        nowX = pixels.get(2 * x);
                        nowY = pixels.get( 2 * x + 1);
                        motionStack.add(0,num);
                    }else{
                        previousL.x = mx.getScope().getX();
                        previousL.y = mx.getScope().getY();
                        for (int i = 0; i < motionStack.size(); i ++){
                            if (motionStack.get(i) == num){
                                motionStack.remove(i);
                            }
                        }
                    }
                }

                //get a command
                if (sensor.equals("L") || sensor.equals("LL")) {
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    int action= -1;
                    if (state.equals("ON")) {
                        if (mx.isOn()) {
//                            System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        mx.turnOn();
                        action = 0;
                    }
                    if (state.equals("OFF")) {
                        if (!mx.isOn()) {
//                          System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        mx.turnOff();
                        action = 1;
                    }
                    if (action == -1) continue;
                    if (motionStack.size() == 0) {
                        //read new Location
                        dataReader.mark(10000);
                        for (int i = 0; i <10; i++){
                            String nextStr = dataReader.readLine();
                            if (lineStr == null) { break; }
                            arrStr = lineStr.split(" ", 3);
                            fields = arrStr[2].split(" ");
                            sensor = fields[0].split("[0-9]")[0];
                            int numN = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                            state = fields[1];
                            // get actor location
                            if (sensor.equals("M") || sensor.equals("MA")){
                                MotionSensor ms = (MotionSensor) IoTThings.get(mSensorIdx + numN - 1);
                                if (state.equals("ON")) {
                                    ArrayList<Integer> pixels = ms.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                                    int x = myRand.nextInt(pixels.size() / 2);
                                    nowX = pixels.get(2 * x);
                                    nowY = pixels.get( 2 * x + 1);
                                    break;

                                }else{
                                    previousL.x = ms.getScope().getX();
                                    previousL.y = ms.getScope().getY();
                                }
                            }
                        }
                        dataReader.reset();
                    }
                    String outStr = now.toSecondOfDay() + "," + nowX + "," + nowY + "," + previousL.x + "," + previousL.y + "," +(num -1) +"\n";
                    output.write(outStr);
                }
            }
            output.close();
            System.out.println("Python file generated");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void sortFromLocation (ArrayList<Thing> arr, int x, int y){
        Collections.sort(arr, new Comparator<Thing>() {
            @Override
            public int compare(Thing thing, Thing t1) {
                int x1 = thing.getScope().getX();
                int y1 = thing.getScope().getY();
                int x2 = t1.getScope().getX();
                int y2 = t1.getScope().getY();
                if (new Point2D(x,y).distance(new Point2D(x1,y1)) < new Point2D(x,y).distance(new Point2D(x2,y2)) ){
                    return -1;
                }else{
                    return 1;
                }
            }
        });
    }
    public double testClosetAll(Actor actor){
        resetAll();
        IoTThings = ((CASAS)building).getIoTThings();
        mSensorIdx = ((CASAS)building).getmSensorIdx();
        int lightIdx = ((CASAS)building).getLightIdx();
        actorSensor = 0;
        String lineStr="";
        String graph = "";
        previousL = new Point2D(0,0);
        motionStack = new ArrayList<>();
        cmdNoCnt = new ArrayList<>();
        cmdDevice = new ArrayList<>();
        double noTotLast = 0;
        int noBefore = 0;
        try{
            BufferedWriter output= new BufferedWriter(new FileWriter("RealDataset_closest.txt"));
            while (true){
                lineStr = dataReader.readLine();
                if (lineStr == null) { break; }
                String[] arrStr = lineStr.split(" ", 3);
                String[] time = (arrStr[0] + " " + arrStr[1]).split("\\.");
                LocalTime now = LocalTime.parse(time[0], defaultFormatter);
                String[] fields = arrStr[2].split(" ");
                String sensor = fields[0].split("[0-9]")[0];
                int num = Integer.parseInt(fields[0].replaceAll("[^0-9]",""));
                String state = fields[1];

                // get actor location
                if (sensor.equals("M") || sensor.equals("MA")){
                    MotionSensor mx = (MotionSensor) IoTThings.get(mSensorIdx + num - 1);
                    if (state.equals("ON")) {
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
                        actor.jumpTo(pixels.get(2 * x), pixels.get( 2 * x + 1), now);
                        actorSensor = num;
                        motionStack.add(0,num);
                    }else{
                        ArrayList<Integer> pixels = mx.getScopePixel(building.getCells(), building.getWidth(), building.getHeight());
                        int x = myRand.nextInt(pixels.size() / 2);
                        previousL.x = pixels.get(2 * x);
                        previousL.y = pixels.get( 2 * x + 1);
                        for (int i = 0; i < motionStack.size(); i ++){
                            if (motionStack.get(i) == num){
                                motionStack.remove(i);
                            }
                        }
                    }

                }

                //get a command
                if (sensor.equals("L") || sensor.equals("LL")){
                    Light mx = (Light) IoTThings.get(lightIdx + num - 1);
                    LightAction newAction = null;
                    if (state.equals("ON")){
                        if (mx.isOn()){
//                            System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        newAction = new LightAction(mx, true);
                    }
                    if (state.equals("OFF")){
                        if (!mx.isOn()){
//                          System.out.println("Invalid state " + lineStr);
                            continue;
                        }
                        newAction = new LightAction(mx, false);
                    }
                    if (newAction == null) continue;

                    if (motionStack.size() == 0){
                        //read new Location
                        dataReader.mark(10000);
                        findNextMotion(actor);
                        dataReader.reset();
                    }

                    boolean yesFlag = true;
                    ArrayList<Thing> tmpThing = new ArrayList(IoTThings.subList(lightIdx,mSensorIdx));
                    sortFromLocation(tmpThing, actor.getTopLeftX(), actor.getTopLeftY());
                    for (Thing thing: tmpThing){
                        LightAction chosenAction = null;
                        if (((Light)thing).isOn()){
                            chosenAction = new LightAction((Light)thing, false);
                        }else {
                            chosenAction = new LightAction((Light)thing, true);
                        }
                        if (chosenAction.isSame(newAction)){
//                            System.out.println("Yes!" + lineStr + " motion sensor (" + actorSensor + ") with score " + res.longRet );
                            newAction.doAction();
                            cmdNoCnt.add(noBefore);
                            cmdDevice.add(num -1);
                            noBefore = 0;
                            break;
                        }else {
//                            System.out.println("No!" + lineStr + " motion sensor (" + actorSensor + ") Wrong light at " + res.getActions().get(0).getDevice().toString() + " with score " + res.longRet);
                            noBefore += 1;
                        }
                    }

                }
            }
            output.write("id,NoCnt,Light\n");
            for (int i = 0; i <cmdNoCnt.size(); i++) {
                output.write((i + 1) + ", " + cmdNoCnt.get(i) + ", " + cmdDevice.get(i) + "\n");
            }
            System.out.println("Decision accuracy after first 500 requests: " + ((double)(cmdNoCnt.size() - 500)) / (cmdNoCnt.size() - 500 + noTotLast) + "\n");

            output.close();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(lineStr);
        }
        return ((double)(cmdNoCnt.size() - 500)) / (cmdNoCnt.size() - 500 + noTotLast);
    }



    @Override
    public void getPunished(){  }

    @Override
    public void getPraised(){  }

    @Override
    public void getSmallPunished(){  }

    @Override
    public void mouseCallback(MouseEvent e){
        if (myAgent != null){
            myAgent.mouseCallback(e);
        }

    }

    @Override
    public void paintNeed(Graphics g){
        if (myAgent != null){
            myAgent.paintNeed(g);
        }
    }
}
