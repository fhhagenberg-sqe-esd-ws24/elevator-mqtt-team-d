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

    public void setButtonUpPressed(boolean state)
    {
        mButtonUpPressed = state;
    }

    public void setButtonDownPressed(boolean state)
    {
        mButtonDownPressed = state;
    }
}
