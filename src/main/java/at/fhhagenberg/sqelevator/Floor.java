package at.fhhagenberg.sqelevator;

/**
 * Class which represents a floor.
 */
public class Floor {
    /**< The status of the up button. */
    private boolean mButtonUpPressed = false;
    /**< The status of the down button. */
    private boolean mButtonDownPressed = false;

    /**
     * Returns the status of the up button.
     * @return The status of the up button.
     */
    public boolean getButtonUpPressed()
    {
        return mButtonUpPressed;
    }

    /**
     * Returns the status of the down button.
     * @return The status of the down button.
     */
    public boolean getButtonDownPressed()
    {
        return mButtonDownPressed;
    }

    /**
     * Sets the status of the up button.
     * @param state The status of the up button.
     * @return True if the status has changed, false otherwise.
     */
    public boolean setButtonUpPressed(boolean state)
    {
        if (state != mButtonUpPressed) {
            mButtonUpPressed = state;
            return true;
        }
        return false;
    }

    /**
     * Sets the status of the down button.
     * @param state The status of the down button.
     * @return True if the status has changed, false otherwise.
     */
    public boolean setButtonDownPressed(boolean state)
    {
        if (state != mButtonDownPressed) {
            mButtonDownPressed = state;
            return true;
        }
        return false;
    }
}
