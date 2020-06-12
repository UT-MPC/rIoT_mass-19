package IoT.SmartAgent;

import BuildingSimulator.Cell;
import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.Context.SimpleDirContext;
import IoT.THING.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class OneAgent extends AbstractAgent{
    private class AgentActionTuple implements Comparable<AgentActionTuple> {
        public ThingAction action;
        public AbstractAgent agent;

        public AgentActionTuple (AbstractAgent newAgent, ThingAction newAction) {
            action = newAction;
            agent = newAgent;
        }

        @Override
        public int compareTo(AgentActionTuple bTuple) {
            return action.compareTo(bTuple.action);
        }
    }
    ArrayList<OneAgent> myAgents;
    ArrayList<DeviceAgent> myDevices;

    AgentActionTuple lastAgentAction = null;

    PriorityQueue<AgentActionTuple> lastCandidate;


    private int maxDeviceNum = 100;
    private final int longRetPraise = 10;
    private final int longRetPunish = -5;
    protected DeviceAgent paintAgent = null;
    private ThingAction undoAction;
    AbstractContextArr lastCtx;

    private Random epsilon = new Random(172849);

    public OneAgent(ArrayList<Cell> input) {
        DynamicContext.Alpha defaultAlpha = DynamicContext.Alpha.defaultAlpha();
        ArrayList<Cell> deC = input;
        int num = deC.size();
        ArrayList<Thing> devices = new ArrayList<>(num);
        for (Cell d: deC){
            devices.add((Thing)d);
        }
        myAgents = new ArrayList<>();
        myDevices = new ArrayList<>();
        if ((num > maxDeviceNum) && (maxDeviceNum > 0)){
            int numA = num / maxDeviceNum;
            for (int i = 0; i < numA; i++){
                myAgents.add(new OneAgent(devices.subList(i * maxDeviceNum, (i + 1) * maxDeviceNum), new ArrayList<>(), defaultAlpha));
            }
            List<Thing> tmp = devices.subList(numA * maxDeviceNum, devices.size());
            for (Thing t: tmp){
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            }
        }else{
            for (Thing t: devices){
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            }
        }
    }
    public OneAgent(ArrayList<Cell> input, DynamicContext.Alpha defaultAlpha) {
        ArrayList<Cell> deC = input;
        int num = deC.size();
        ArrayList<Thing> devices = new ArrayList<>(num);
        for (Cell d: deC){
            devices.add((Thing)d);
        }
        myAgents = new ArrayList<>();
        myDevices = new ArrayList<>();
        if ((num > maxDeviceNum) && (maxDeviceNum > 0)){
            int numA = num / maxDeviceNum;
            for (int i = 0; i < numA; i++){
                myAgents.add(new OneAgent(devices.subList(i * maxDeviceNum, (i + 1) * maxDeviceNum), new ArrayList<>(), defaultAlpha));
            }
            List<Thing> tmp = devices.subList(numA * maxDeviceNum, devices.size());
            for (Thing t: tmp){
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            }
        }else{
            for (Thing t: devices){
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            }
        }
    }

    public OneAgent(List<Thing> devices, ArrayList<OneAgent> agents) {
        myAgents = agents;
        myDevices = new ArrayList<>();
        int num = devices.size();
        if ((num > maxDeviceNum) && (maxDeviceNum > 0)){
            int numA = num / maxDeviceNum;
            for (int i = 0; i < numA; i++){
                myAgents.add(new OneAgent(devices.subList(i * maxDeviceNum, (i + 1) * maxDeviceNum), null));
            }
            List<Thing> tmp = devices.subList(numA * maxDeviceNum, devices.size());
            for (Thing t: tmp){
                myDevices.add(new DeviceAgent(t));
            }
        }else {
            for (Thing t : devices) {
                myDevices.add(new DeviceAgent(t));
            }
        }
    }
    public OneAgent(List<Thing> devices, ArrayList<OneAgent> agents, DynamicContext.Alpha defaultAlpha) {
        myAgents = agents;
        myDevices = new ArrayList<>();
        int num = devices.size();
        if ((num > maxDeviceNum) && (maxDeviceNum > 0)){
            int numA = num / maxDeviceNum;
            for (int i = 0; i < numA; i++){
                myAgents.add(new OneAgent(devices.subList(i * maxDeviceNum, (i + 1) * maxDeviceNum), null, defaultAlpha));
            }
            List<Thing> tmp = devices.subList(numA * maxDeviceNum, devices.size());
            for (Thing t: tmp){
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            }
        }else {
            for (Thing t : devices) {
                myDevices.add(new DeviceAgent(t));
                myDevices.get(myDevices.size() - 1).setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));

            }
        }
    }
    public void addDevice(Thing device){
        myDevices.add(new DeviceAgent(device));
    }
    public ThingAction getConfiguration(AbstractContextArr currContext, Class thingType, Action actionFilter) {
        //System.out.println(((SimpleSpaceTimeContext)currContext).x + " " + ((SimpleSpaceTimeContext)currContext).y);
//        lastAgentAction = null;
        if ((lastCtx == null) || (currContext.distanceTo(lastCtx) > 5)) {
            lastCandidate = new PriorityQueue<>(myDevices.size() + myAgents.size(), Collections.reverseOrder());
            for (DeviceAgent agent: myDevices){
                ThingAction newAction = agent.getConfiguration(currContext, thingType, actionFilter);
                if (newAction != null){
                    lastCandidate.add(new AgentActionTuple(agent, newAction));
                }
            }
            for (OneAgent agent: myAgents){
                ThingAction newAction = agent.getConfiguration(currContext, thingType, actionFilter);
                if (newAction != null){
                    lastCandidate.add(new AgentActionTuple(agent, newAction));
                }
            }
        }
        lastCtx = currContext;
        AgentActionTuple newAction = lastCandidate.poll();
//        if ((epsilon.nextDouble() < 0.01) && (lastCandidate.size() >0)){
//            AgentActionTuple nextAction = lastCandidate.peek();
//            if (nextAction.action.longRet > -0.01){
//                nextAction = lastCandidate.poll();
//                lastCandidate.add(newAction);
//                newAction = nextAction;
//            }
//        }
        lastAgentAction = newAction;
        if (lastAgentAction == null){
            return null;
        }
//        System.out.println("chosen agent action device " + lastAgentAction.action.getActions().get(0).getDevice() + " chosen action = " + lastAgentAction.action.getActions().get(0).checkAction());
//        lastAgentAction.action.doAction();
        return lastAgentAction.action;
    }
    public void resetAll(){
        for (OneAgent agent: myAgents){
            agent.resetAll();
        }

        for (DeviceAgent device: myDevices){
            device.getDevice().reset();
        }
    }

    @Override
    public void getPunished() {
        if (lastAgentAction != null){
            lastAgentAction.agent.getPunished();
//            lastAgentAction = null;
        }
    }
    @Override
    public void getPraised() {
        if (lastAgentAction != null){
            lastAgentAction.agent.getPraised();
            lastCtx = null;
        }
    }

    @Override
    public void getSmallPunished(){
        if (lastAgentAction != null){
            lastAgentAction.agent.getSmallPunished();
            lastCtx = null;
        }
    }

    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_O){
            undoAction = getConfiguration(currContext, Light.class, null);
            undoAction.doAction();
        }
        if (key == KeyEvent.VK_P){
            getPunished();
        }
        if (key == KeyEvent.VK_OPEN_BRACKET){
            getPraised();
        }
        if (key == KeyEvent.VK_I){
            undoAction = getConfiguration(currContext, Camera.class, null);
            undoAction.doAction();
        }
        if (key == KeyEvent.VK_R){
            resetAll();
        }
    }

    @Override
    public void mouseCallback(MouseEvent e){
//        System.out.println(e.getX()+ " " + e.getY());
        for (OneAgent agent: myAgents){
            agent.mouseCallback(e);
            if (agent.paintAgent != null){
                paintAgent = agent.paintAgent;
                break;
            }
        }
        paintAgent = null;
        for (DeviceAgent agent: myDevices){
            Thing thisLight = agent.getDevice();
            if (thisLight instanceof Light){
                if (thisLight.getScope().contains(e.getX(),e.getY())){
                    paintAgent = agent;
                }
            }
        }
    }

    @Override
    public void paintNeed(Graphics g){
        if (paintAgent == null) return;
        paintAgent.paintNeed(g);
    }
}
