package at.fhhagenberg.sqelevator;

/**
 * High-level interface to access and control the elevator system.
 * 
 * <p>The elevator's behavior is impacted by the following parameters:
 * <ul><i>Elevator capacity.</i> The maximum number of passengers that can fit on an 
 * elevator.</ul>
 * <ul><i>Elevator speed.</i> The maximum speed an elevator can travel at in feet/sec. 
 * It is assumed to be the same for all elevators.</ul>
 * <ul><i>Elevator acceleration.</i> The rate at which the elevator can increase or decrease
 * speed in ft/sec2. It is assumed to be the same for all elevators. The higher the acceleration,
 * the faster the elevator can accelerate and decelerate, allowing its average speed to be faster.</ul>
 * <ul><i>Number of floors.</i> The number of floors in the building, including the ground floor. It is
 * assumed there are no floors below ground level. All elevators service the
 * ground floor but may, as described below, not necessarily service all other floors. Each floor has 
 * an up and a down button for the purposes of passengers calling the elevator. Floor numbering starts 
 * at 0 for floor 1.</ul> 
 * <ul><i>Floor height.</i> The height of the floors in feet. It is assumed that each floor is 
 * the same height.</ul>
 * <ul><i>Number of elevators.</i> The number of elevators in the building. Elevator numbering starts 
 * at zero for elevator 1.</ul>
 * 
 * <p>Some elevator behavior is inherent and cannot be controlled:
 * <ul><i>Door opening & closing.</i> Elevator doors automatically open when arriving at a floor and 
 * close before departing. When passengers are waiting to board an elevator, the doors will 
 * stay open until all passengers are boarded. Each open and close action takes 1 second.</ul>
 * <ul><i>Elevator buttons.</i> Arriving passengers press the appropriate elevator button and the 
 * light is automatically turned off when the elevator arrives at the associated floor.</ul>
 * <ul><i>Elevator speed and acceleration.</i> The elevator will automatically accelerate to the maximum 
 * speed possible until there is only enough time left to slow down to arrive at the target floor.</ul>
 * 
 * <p>There is substantial elevator status information available:
 * <ul><i>Elevator speed.</i> The current speed of the elevator. Positive speed indicates an elevator
 * heading up while negative indicates an elevator going down.</ul>
 * <ul><i>Elevator acceleration.</i> The acceleration of the elevators.</ul>
 * <ul><i>Elevator button status.</i> For each elevator, which floor buttons on the elevator have 
 * been pressed.</ul>
 * <ul><i>Elevator position.</i> The current position of the elevator, both in feet and to 
 * the closest floor. Position is measured in feet above ground floor with zero being the bottom.</ul>
 * <ul><i>Elevator door status.</i> Whether the doors for an elevator are open or closed. The 
 * status may also indicate a transition between open or closed.</ul>
 * <ul><i>Elevator target.</i> The current floor target of the elevator as set by the controller. The
 * elevator will travel to that target and stop until directed to the next target.</ul>
 * <ul><i>Elevator committed direction.</i> The current committed direction of the elevator. Elevators
 * responding to a passenger floor button must have a committed direction, up or down. Otherwise, 
 * passengers wishing to go up would board the same elevators as passengers wishing to go down. 
 * Accordingly, each elevator can have an up, down or uncommitted committed direction set in response 
 * to passenger travel..</ul>
 * <ul><i>Elevator floor service.</i> Whether a particular elevator services a particular floor. When 
 * elevators are allowed to only service certain floors, this can help to achieve greater passenger 
 * service. Every elevator must service the ground floor.</ul>
 * <ul><i>Elevator weight.</i> This provides the current weight of the elevator less the weight of the 
 * empty elevator - hence the weight of the passengers on board. This can be useful for detecting
 * when the elevator is getting full.</ul>
 * 
 * <p>The elevators can be controlled by setting the following parameters:
 * <ul><i>Elevator target.</i> Sets the floor target of a particular elevator. Typically, this target 
 * is set in response to a given passenger selecting a floor up or down button, or selecting a floor 
 * button in an elevator. The elevator will travel to that target and stop until directed to the 
 * next target.</ul>
 * <ul><i>Elevator committed direction.</i> Sets the current committed direction of the elevator. This 
 * is set so that passengers board elevators going in their desired direction. This is indicated by the
 * controller by whether the up or down floor button has been pressed on a given floor. This is
 * an important concept and it should be recognized that in some cases, an elevator may be 
 * going down, for example, but its committed direction is up - reflecting the fact it has been 
 * dispatched down to pick up a passenger who is traveling up. Whenever an elevator is being
 * dispatched to a target floor, it should have a committed direction set.</ul>
 * <ul><i>Elevator floor service.</i> Sets whether a particular elevator services a particular floor. 
 * In larger buildings, it is typically better to set elevator service to a particular subset of floors. 
 * Note that the ground floor will always get service.</ul>
 *
 * @version 0.1
 */

public interface IElevator extends java.rmi.Remote {
	
	/** State variable for elevator doors open.	 */
	public final static int ELEVATOR_DOORS_OPEN = 1;	
	/** State variable for elevator doors closed. */
	public final static int ELEVATOR_DOORS_CLOSED = 2;
	/** State variable for elevator doors opening. */
	public final static int ELEVATOR_DOORS_OPENING = 3;
	/** State variable for elevator doors closing. */
	public final static int ELEVATOR_DOORS_CLOSING = 4;
		
	/** State variable for elevator status when going up */
	public final static int ELEVATOR_DIRECTION_UP = 0;				
	/** State variable for elevator status when going down. */
	public final static int ELEVATOR_DIRECTION_DOWN = 1;			
	/** State variables for elevator status stopped and uncommitted. */
	public final static int ELEVATOR_DIRECTION_UNCOMMITTED = 2;		
	
	/**
	 * Retrieves the committed direction of the specified elevator (up / down / uncommitted). 
	 * @param elevatorNumber - elevator number whose committed direction is being retrieved 
	 * @return the current direction of the specified elevator where up=0, down=1 and uncommitted=2
	 */
	public int getCommittedDirection(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Provides the current acceleration of the specified elevator in feet per sec^2. 
	 * @param elevatorNumber - elevator number whose acceleration is being retrieved 
	 * @return returns the acceleration of the indicated elevator where positive speed is acceleration and negative is deceleration

	 */
	public int getElevatorAccel(int elevatorNumber) throws java.rmi.RemoteException;

	/**
	 * Provides the status of a floor request button on a specified elevator (on/off).      
	 * @param elevatorNumber - elevator number whose button status is being retrieved
	 * @param floor - floor number button being checked on the selected elevator 
	 * @return returns boolean to indicate if floor button on the elevator is active (true) or not (false)
	 */
	public boolean getElevatorButton(int elevatorNumber, int floor) throws java.rmi.RemoteException;

	/**
	 * Provides the current status of the doors of the specified elevator (open/closed).      
	 * @param elevatorNumber - elevator number whose door status is being retrieved 
	 * @return returns the door status of the indicated elevator where 1=open and 2=closed
	 */
	public int getElevatorDoorStatus(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Provides the current location of the specified elevator to the nearest floor 
	 * @param elevatorNumber - elevator number whose location is being retrieved 
	 * @return returns the floor number of the floor closest to the indicated elevator
	 */
	public int getElevatorFloor(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the number of elevators in the building. 
	 * @return total number of elevators
	 */
	public int getElevatorNum() throws java.rmi.RemoteException; 

	/**
	 * Provides the current location of the specified elevator in feet from the bottom of the building. 
	 * @param elevatorNumber  - elevator number whose location is being retrieved 
	 * @return returns the location in feet of the indicated elevator from the bottom of the building
	 */
	public int getElevatorPosition(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Provides the current speed of the specified elevator in feet per sec. 
	 * @param elevatorNumber - elevator number whose speed is being retrieved 
	 * @return returns the speed of the indicated elevator where positive speed is up and negative is down
	 */
	public int getElevatorSpeed(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the weight of passengers on the specified elevator. 
	 * @param elevatorNumber - elevator number whose service is being retrieved
	 * @return total weight of all passengers in lbs
	 */
	public int getElevatorWeight(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the maximum number of passengers that can fit on the specified elevator.
	 * @param elevatorNumber - elevator number whose service is being retrieved
	 * @return number of passengers
	 */
	public int getElevatorCapacity(int elevatorNumber) throws java.rmi.RemoteException;
	
	/**
	 * Provides the status of the Down button on specified floor (on/off). 
	 * @param floor - floor number whose Down button status is being retrieved 
	 * @return returns boolean to indicate if button is active (true) or not (false)
	 */
	public boolean getFloorButtonDown(int floor) throws java.rmi.RemoteException; 

	/**
	 * Provides the status of the Up button on specified floor (on/off). 
	 * @param floor - floor number whose Up button status is being retrieved 
	 * @return returns boolean to indicate if button is active (true) or not (false)
	 */
	public boolean getFloorButtonUp(int floor) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the height of the floors in the building. 
	 * @return floor height (ft)
	 */
	public int getFloorHeight() throws java.rmi.RemoteException; 

	/**
	 * Retrieves the number of floors in the building. 
	 * @return total number of floors
	 */
	public int getFloorNum() throws java.rmi.RemoteException; 

	/** 
	 * Retrieves whether or not the specified elevator will service the specified floor (yes/no). 
	 * @param elevatorNumber elevator number whose service is being retrieved
	 * @param floor floor whose service status by the specified elevator is being retrieved
	 * @return service status whether the floor is serviced by the specified elevator (yes=true,no=false)
	 */
	public boolean getServicesFloors(int elevatorNumber, int floor) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the floor target of the specified elevator. 
	 * @param elevatorNumber elevator number whose target floor is being retrieved
	 * @return current floor target of the specified elevator
	 */
	public int getTarget(int elevatorNumber) throws java.rmi.RemoteException; 

	/**
	 * Sets the committed direction of the specified elevator (up / down / uncommitted). 
	 * @param elevatorNumber elevator number whose committed direction is being set
	 * @param direction direction being set where up=0, down=1 and uncommitted=2
	 */
	public void setCommittedDirection(int elevatorNumber, int direction) throws java.rmi.RemoteException;

	/**
	 * Sets whether or not the specified elevator will service the specified floor (yes/no). 
	 * @param elevatorNumber elevator number whose service is being defined
	 * @param floor floor whose service by the specified elevator is being set
	 * @param service indicates whether the floor is serviced by the specified elevator (yes=true,no=false)
	 */
	public void setServicesFloors(int elevatorNumber, int floor, boolean service) throws java.rmi.RemoteException; 

	/**
	 * Sets the floor target of the specified elevator. 
	 * @param elevatorNumber elevator number whose target floor is being set
	 * @param target floor number which the specified elevator is to target
	 */
	public void setTarget(int elevatorNumber, int target) throws java.rmi.RemoteException; 

	/**
	 * Retrieves the current clock tick of the elevator control system. 
	 * @return clock tick
	 */
	public long getClockTick() throws java.rmi.RemoteException;
	
}
