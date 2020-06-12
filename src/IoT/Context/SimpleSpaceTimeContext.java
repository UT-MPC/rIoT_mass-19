package IoT.Context;

import IoT.Actor;

import java.time.LocalTime;

@Deprecated
public class SimpleSpaceTimeContext extends AbstractContextArr {
    private Point2D location;
    public int timeOfDay;
    static public int timeBias = 120;


    public SimpleSpaceTimeContext(SimpleSpaceTimeContext clone){
        super();
        this.location = new Point2D(clone.location);
        this.timeOfDay = clone.timeOfDay;
    }
    public SimpleSpaceTimeContext(int x, int y, int time){
        super();
        this.location = new Point2D(x,y);
        this.timeOfDay = time;
    }

    public int getX(){return location.x;}
    public int getY(){return location.y;}
    public static SimpleSpaceTimeContext generateContext(Actor user, LocalTime time){
        return new SimpleSpaceTimeContext(user.getTopLeftX(), user.getTopLeftY(), time.toSecondOfDay());
    }
    public double distanceTo(AbstractContextArr toContext){
        SimpleSpaceTimeContext bContext = (SimpleSpaceTimeContext) toContext;
        int timeDifference = Math.abs(timeOfDay / timeBias - bContext.timeOfDay / timeBias);                   // in minute
        int spaceDifference = this.location.distance(bContext.location);
        return spaceDifference + timeDifference;
    }
    public SimpleSpaceTimeContext getOffsetContext(int offset){
        return new SimpleSpaceTimeContext(location.x + offset, location.y + offset, timeOfDay + offset * timeBias);
    }
    /*
        Bias should be between 0-1
     */
    public SimpleSpaceTimeContext getMidContext(SimpleSpaceTimeContext bContext, double bias){
        double newX = (double)location.x * bias + (double)bContext.location.x * (1 - bias);
        double newY = (double)location.y * bias + (double)bContext.location.y * (1 - bias);
        double newTime = (double)timeOfDay * bias + (double)bContext.timeOfDay * (1 - bias);
        return new SimpleSpaceTimeContext( (int) newX, (int)newY, (int)newTime);
    }

    public boolean isBetween(SimpleSpaceTimeContext aContext, SimpleSpaceTimeContext bContext){
        if (!location.isBetween(aContext.location, bContext.location)){
            return false;
        }
        if ((timeOfDay < aContext.timeOfDay) || (timeOfDay > bContext.timeOfDay)){
            return false;
        }
        return true;
    }
    /*

     */
    static public SimpleSpaceTimeContext[] stateSpliter(SimpleSpaceTimeContext oldMin, SimpleSpaceTimeContext oldMax,
                                                        SimpleSpaceTimeContext massA, SimpleSpaceTimeContext massB, double ratio)
    {
        int deltaX = Math.abs(massA.location.x - massB.location.x);
        int deltaY = Math.abs(massA.location.y - massB.location.y);
        int deltaTime = Math.abs(massA.timeOfDay - massB.timeOfDay) / timeBias;
        SimpleSpaceTimeContext midContext = massA.getMidContext(massB, ratio / (ratio + 1));

        SimpleSpaceTimeContext newMin = new SimpleSpaceTimeContext(oldMin);
        SimpleSpaceTimeContext newMax = new SimpleSpaceTimeContext(oldMax);
        if ((deltaX > deltaY) && (deltaX > deltaTime)){
            if (massA.location.x < massB.location.x){
                newMax.location.x = midContext.location.x;
                oldMin.location.x = midContext.location.x;
            }else{
                newMin.location.x = midContext.location.x;
                oldMax.location.x = midContext.location.x;
            }
        }
        if ((deltaY > deltaX) && (deltaY > deltaTime)){
            if (massA.location.y < massB.location.y){
                newMax.location.y = midContext.location.y;
                oldMin.location.y = midContext.location.y;
            }else{
                newMin.location.y = midContext.location.y;
                oldMax.location.y = midContext.location.y;
            }
        }
        if ((deltaTime > deltaX) && (deltaTime > deltaY)){
            if (massA.timeOfDay < massB.timeOfDay){
                newMax.timeOfDay = midContext.timeOfDay;
                oldMin.timeOfDay = midContext.timeOfDay;
            }else{
                newMin.timeOfDay = midContext.timeOfDay;
                oldMax.timeOfDay = midContext.timeOfDay;
            }
        }
        return new SimpleSpaceTimeContext[]{newMin, newMax};
    }
}
