package IoT.THING;

/* Action is a action to take when making a decision.
 *
 */
public interface Action {

    int checkAction();
    void doAction();
    void undoAction();
    Thing getDevice();
    boolean isSame(Action b);
    boolean isStateCompatible(int state);

    boolean actionFilter(Action b);
}
