package at.fhhagenberg.sqelevator;

public class Floor {
    private boolean mButtonUpPressed = false;
    private boolean mButtonDownPressed = false;

    public boolean getButtonUpPressed()
    {
        return mButtonUpPressed;
    }

    public boolean getButtonDownPressed()
    {
        return mButtonDownPressed;
    }

    public boolean setButtonUpPressed(boolean state)
    {
        if (state != mButtonUpPressed) {
            mButtonUpPressed = state;
            return true;
        }
        return false;
    }

    public boolean setButtonDownPressed(boolean state)
    {
        if (state != mButtonDownPressed) {
            mButtonDownPressed = state;
            return true;
        }
        return false;
    }
}
