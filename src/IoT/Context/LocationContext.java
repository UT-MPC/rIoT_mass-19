package IoT.Context;

import java.util.ArrayList;

public class LocationContext extends BaseContext{
    private Point2D location;

    public LocationContext(Point2D lc){
        location = lc;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public static double rangeX = 1400; // range(0-1400,0-800)
    public static double rangeY = 800; // range(0-1400,0-800)
    public static double range = 1612.45;

    @Override
    public LocationContext makeClone(){
        return new LocationContext(new Point2D(location));
    }
    @Override
    public double distanceTo (BaseContext ctx){
        if (LocationContext.class.isInstance(ctx)){
            return location.distance(((LocationContext) ctx).getLocation()) / range;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isBetween(BaseContext A, BaseContext B){
        if (!LocationContext.class.isInstance(A) ||!LocationContext.class.isInstance(B)){
            return false;
        }
        return location.isBetween(((LocationContext)A).getLocation(), ((LocationContext)B).getLocation());
    }

    @Override
    public LocationContext getOffset(double offset){
//        System.out.println(offset + " " + range);
//        System.out.println((int) (location.x + offset * range) + " "+ (int)(location.y + offset * range));
        return new LocationContext(new Point2D((int) (location.x + offset * range), (int)(location.y + offset * range)));
    }

    @Override
    public LocationContext getMidCtx(BaseContext ctxB, double ratio){
        LocationContext ctx = (LocationContext) ctxB;
        double newX = location.x * ratio + ctx.location.x * (1 - ratio);
        double newY = location.y * ratio + ctx.location.y * (1 - ratio);
        return new LocationContext(new Point2D((int)newX, (int)newY));
    }

    public double longAxis(BaseContext ctxB){
        LocationContext ctx = (LocationContext) ctxB;
        return Math.max(Math.abs(location.x - ctx.getLocation().x), Math.abs(location.y - ctx.getLocation().y));
    }

    public void splitLongAxis(BaseContext massCtx, BaseContext oldMin, BaseContext oldMax, BaseContext newMin, BaseContext newMax, double ratio){
        LocationContext ctx = (LocationContext) massCtx;
        int deltaX = Math.abs(location.x - ctx.getLocation().x);
        int deltaY = Math.abs(location.y - ctx.getLocation().y);
        LocationContext midCtx = getMidCtx(massCtx, ratio / (ratio + 1));
        if (deltaX > deltaY){
            if (location.x < ctx.getLocation().x){
                ((LocationContext) newMax).getLocation().x = midCtx.getLocation().x;
                ((LocationContext) oldMin).getLocation().x = midCtx.getLocation().x;
            }else{
                ((LocationContext) newMin).getLocation().x = midCtx.getLocation().x;
                ((LocationContext) oldMax).getLocation().x = midCtx.getLocation().x;
            }
        }else{
            if (location.y < ctx.getLocation().y){
                ((LocationContext) newMax).getLocation().y = midCtx.getLocation().y;
                ((LocationContext) oldMin).getLocation().y = midCtx.getLocation().y;
            }else{
                ((LocationContext) newMin).getLocation().y = midCtx.getLocation().y;
                ((LocationContext) oldMax).getLocation().y = midCtx.getLocation().y;
            }
        }
    }

    @Override
    public ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio){
        if (bContext instanceof LocationContext){
            LocationContext bCtx = (LocationContext) bContext;
            int x1 = location.x;
            int x2 = bCtx.getLocation().x;
            int y1 = location.y;
            int y2 = bCtx.getLocation().y;
            int xPoint = (int)(x1 + (x2 - x1) * ratio);
            int yPoint = (int)(y1 + (y2 - y1) * ratio);
            LocationContext newMax1 = new LocationContext(new Point2D(xPoint, y2));
            LocationContext newMin1 = new LocationContext(new Point2D(xPoint, y1));
            LocationContext newMax2 = new LocationContext(new Point2D(x2, yPoint));
            LocationContext newMin2 = new LocationContext(new Point2D(x1, yPoint));
            ArrayList<BaseContext[]> output = new ArrayList<>();
            output.add(new BaseContext[]{this, newMax1, newMin1, bContext});
            output.add(new BaseContext[]{this, newMax2, newMin2, bContext});
            return output;
        }
        return null;
    }
}
