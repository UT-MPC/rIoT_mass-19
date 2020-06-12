package IoT.THING;

public class LightAction implements Action {
    private Light myLight;
    private boolean turnOn;
    public LightAction(Light light, boolean isTurnOn){
        myLight = light;
        turnOn = isTurnOn;
    }

    @Override
    public int checkAction(){
        return turnOn?0:1;
    }

    @Override
    public void undoAction() {
        if (turnOn){
            myLight.turnOff();
        }else{
            myLight.turnOn();
        }
    }

    @Override
    public void doAction() {
        if (turnOn){
            myLight.turnOn();
        }else{
            myLight.turnOff();
        }
    }
    @Override
    public Thing getDevice(){
        return myLight;
    }


    @Override
    public boolean isSame(Action bAction){
        if (!(bAction instanceof LightAction)){
            return false;
        }
        if (!(myLight.equals(bAction.getDevice()))) {
            return false;
        }
        if (checkAction() != bAction.checkAction()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isStateCompatible(int thingState){
        if ((turnOn)&&(thingState==Light.onState)) {
            return false;
        }
        if ((!turnOn)&&(thingState==Light.offState)){
            return false;
        }
        return true;
    }

    @Override
    public boolean actionFilter(Action filter){
        if (filter == null) return true;
        if (!(filter instanceof LightAction)){
            return false;
        }
        if (checkAction() != filter.checkAction()){
            return false;
        }
        return true;
    }
}
