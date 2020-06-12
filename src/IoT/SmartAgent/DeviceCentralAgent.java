package IoT.SmartAgent;

import BuildingSimulator.Building;
import BuildingSimulator.Cell;
import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.Context.SimpleDirContext;
import IoT.THING.ThingAction;
import IoT.THING.Camera;
import IoT.THING.Light;
import IoT.THING.Thing;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class DeviceCentralAgent extends AbstractAgent{
    private class AgentActionTuple implements Comparable<AgentActionTuple> {
        public ThingAction action;
        public DeviceAgent agent;

        public AgentActionTuple (DeviceAgent newAgent, ThingAction newAction) {
            action = newAction;
            agent = newAgent;
        }

        @Override
        public int compareTo(AgentActionTuple bTuple) {
            return action.compareTo(bTuple.action);
        }
    }

    ArrayList<DeviceAgent> myAgents;

    AgentActionTuple lastAgentAction = null;

    PriorityQueue<AgentActionTuple> lastCandidate;

    ThingAction undoAction;
    ArrayList<Cell> devices;
    private final int longRetPraise = 10;
    private final int longRetPunish = 10;
    private DeviceAgent paintAgent = null;
    AbstractContextArr lastCtx;
    public DeviceCentralAgent(Building building) {
        devices = building.getThingCells();
        myAgents = new ArrayList<DeviceAgent>(devices.size());
        for (Cell cell : devices){
            myAgents.add(new DeviceAgent((Thing) cell));
        }
    }
    public DeviceCentralAgent(Building building, DynamicContext.Alpha defaultAlpha) {
        devices = building.getThingCells();
        myAgents = new ArrayList<DeviceAgent>(devices.size());
        for (Cell cell : devices){
            DeviceAgent newAgent =new DeviceAgent((Thing) cell);
            newAgent.setDeviceUserAlpha(new DynamicContext.Alpha(defaultAlpha));
            myAgents.add(newAgent);

        }
    }

    public ThingAction getConfiguration(AbstractContextArr currContext, Class thingType) {
        //System.out.println(((SimpleSpaceTimeContext)currContext).x + " " + ((SimpleSpaceTimeContext)currContext).y);
//        lastAgentAction = null;
        if ((lastCtx == null) || (currContext.distanceTo(lastCtx) > 5)) {
            lastCandidate = new PriorityQueue<>(myAgents.size(), Collections.reverseOrder());
            for (DeviceAgent agent: myAgents){
                ThingAction newAction = agent.getConfiguration(currContext, thingType);
                if (newAction !=null ){
                    lastCandidate.add(new AgentActionTuple(agent, newAction));
                }
            }
        }
        lastCtx = currContext;
        AgentActionTuple newAction = lastCandidate.poll();
//        if (lastAgentAction != null){
//            while ((lastAgentAction.agent == newAction.agent) && (lastAgentAction.action.isSame(newAction.action))){
//                newAction = lastCandidate.poll();
//            }
//        }
        lastAgentAction = newAction;
        if (lastAgentAction == null){
            return null;
        }
//        System.out.println("chosen agent action device " + lastAgentAction.action.getActions().get(0).getDevice() + " chosen action = " + lastAgentAction.action.getActions().get(0).checkAction());
        lastAgentAction.action.doAction();
        return lastAgentAction.action;
    }
    public void resetAll(){
        for (Cell device: devices){
            ((Thing) device).reset();
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
    public void getSmallPunished(){}
    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_O){
            undoAction = getConfiguration(currContext, Light.class);
//            SimpleDirContext x = (SimpleDirContext)((DynamicContext)currContext).getContexts().get(2);
//            System.out.println(x.getAngle());
        }
        if (key == KeyEvent.VK_P){
            getPunished();
        }
        if (key == KeyEvent.VK_OPEN_BRACKET){
            getPraised();
        }
        if (key == KeyEvent.VK_I){
            undoAction = getConfiguration(currContext, Camera.class);
        }
        if (key == KeyEvent.VK_R){
            resetAll();
        }
    }
    @Override
    public void mouseCallback(MouseEvent e){
//        System.out.println(e.getX()+ " " + e.getY());
        paintAgent = null;
        for (DeviceAgent agent: myAgents){
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
        if (paintAgent == null){
            return;
        }
        paintAgent.paintNeed(g);
    }
}
