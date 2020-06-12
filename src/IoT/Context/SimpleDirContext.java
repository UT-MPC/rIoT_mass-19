package IoT.Context;

import java.util.ArrayList;

public class SimpleDirContext extends BaseContext{
    /*
        The angle system is clockwise:
                270
            180     0
                90

     */
    public static double DIRBIAS = 4.0;     //scale 0-180 to 0-720
    private static int diffAngle(int a, int b){
        if ((a == -1) || (b == -1)){
            return 0;
        }
        return 180 - Math.abs(Math.abs(a - b) - 180);
    }

    public static boolean clkwiseLeq(int angleA, int angleB){
        if (angleA == angleB) return true;
        if ((angleA == -1) || (angleB == -1)){
            return true;
        }
        if (angleA < angleB){
            if ((angleB - angleA) > diffAngle(angleA, angleB)){
                return false;
            } else{
                return true;
            }

        }else{
            if ((angleA - angleB) > diffAngle(angleA, angleB)){
                return true;
            } else{
                return false;
            }

        }
    }


    private int angle;
    public SimpleDirContext(int angle) {
        this.angle = angle;
    }
    public SimpleDirContext(double dX, double dY){
        if (Math.abs(dX) + Math.abs(dY) < 0.5){
            angle = -1;
            return;
        }
        angle = (((int)Math.toDegrees(Math.atan2(dY,dX)) + 360) % 360);
//        System.out.println(angle);
    }

    public void setAngle(int newAngle) {
        angle = newAngle;
    }

    public int getAngle() {
        return angle;
    }

    @Override
    public SimpleDirContext makeClone(){
        return new SimpleDirContext(angle);
    }

    @Override
    public double distanceTo (BaseContext ctx){
        if (!(ctx instanceof SimpleDirContext)){
            return 0;
        }
        int diffA = diffAngle(angle,((SimpleDirContext) ctx).getAngle());
        return (int)(diffA * DIRBIAS);
    }

    @Override
    public boolean isBetween(BaseContext A, BaseContext B){
        if (!SimpleDirContext.class.isInstance(A) || !SimpleDirContext.class.isInstance(B)){
            return false;
        }
        int angleA = ((SimpleDirContext)A).getAngle();
        int angleB = ((SimpleDirContext)B).getAngle();

        if (clkwiseLeq(angleB, angleA)){
            int tmp = angleA;
            angleA = angleB;
            angleB = tmp;
        }
        return clkwiseLeq(angleA, angle) && clkwiseLeq(angle, angleB);
//        if ((angleB - angleA) > diffAngle(angleA, angleB)){
//            int tmp = angleA;
//            angleA = angleB;
//            angleB = tmp + 360;
//        }
//        int test1 = (angleB - angle ) % 360;
//        int test2 = (angle - angleA ) % 360;
//        if (test1 * test2 >= 0) {
//            return true;
//        }
//        test1 = (angleB - angle ) % 360;
//        test2 = (angle - angleA ) % 360;
//        return true;
    }

    @Override
    public SimpleDirContext getOffset(double offset){
        if (angle == -1) return new SimpleDirContext(-1);
        int newA = (int) (angle + offset);
        while (newA < 0){
            newA += 360;
        }
        newA = newA % 360;
        return new SimpleDirContext(newA);
    }

    @Override
    public SimpleDirContext getMidCtx(BaseContext ctxB, double ratio){
        if (!(ctxB instanceof SimpleDirContext)){
            return makeClone();
        }
        int aAngle = angle;
        int bAngle = ((SimpleDirContext) ctxB).getAngle();
        if ((aAngle == -1) || (bAngle == -1)){
            return makeClone();
        }
        if (aAngle > bAngle){
            int tmp = aAngle;
            aAngle = bAngle;
            bAngle = tmp;
            ratio = 1 - ratio;
        }
        if ((bAngle - aAngle) > diffAngle(aAngle, bAngle)){
            int tmp = aAngle;
            aAngle = bAngle;
            bAngle = tmp + 360;
            ratio = 1 - ratio;
        }
        int newA = (int)(aAngle * ratio + bAngle * (1.0 - ratio) + 0.5);
        newA = newA % 360;
        return new SimpleDirContext(newA);
    }

    @Deprecated
    public double longAxis(BaseContext ctxB){
        return distanceTo(ctxB);
    }

    @Deprecated
    public void splitLongAxis(BaseContext massCtx, BaseContext oldMin, BaseContext oldMax, BaseContext newMin, BaseContext newMax, double ratio){
        int massAngle = ((SimpleDirContext)massCtx).getAngle();
        SimpleDirContext midCtx = getMidCtx(massCtx, ratio / (ratio + 1));

        if (clkwiseLeq(angle, massAngle)){
            ((SimpleDirContext)newMax).setAngle(midCtx.getAngle());
            ((SimpleDirContext)oldMin).setAngle(midCtx.getAngle());
        }else{
            ((SimpleDirContext)newMin).setAngle(midCtx.getAngle());
            ((SimpleDirContext)oldMax).setAngle(midCtx.getAngle());
        }

    }

    @Override
    public ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio){
        return null;
    }
}
