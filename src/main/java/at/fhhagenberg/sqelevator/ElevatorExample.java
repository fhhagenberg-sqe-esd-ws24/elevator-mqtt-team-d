package at.fhhagenberg.sqelevator;

import java.rmi.Naming;
import java.rmi.RemoteException;


public class ElevatorExample {

	private IElevator controller;

	public ElevatorExample(IElevator controller) {
		this.controller = controller;
	}

	public static void main(String[] args) {

		try {
			IElevator controller = (IElevator) Naming.lookup("rmi://localhost/ElevatorSim");
			ElevatorExample client = new ElevatorExample(controller);

			client.displayElevatorSettings();
			client.runExample();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void displayElevatorSettings() throws RemoteException {
		System.out.println("ELEVATOR SETTINGS");
		
		System.out.println("Current clock tick: " + controller.getClockTick());
		
		System.out.println("Number of elevators: " + controller.getElevatorNum());
		System.out.println("Number of floor: " + controller.getFloorNum());
		System.out.println("Floor height: " + controller.getFloorHeight());
		
		System.out.print("Floor buttons Up pressed: ");
		for (int floor=0; floor<controller.getFloorNum(); floor++) {
			System.out.print(controller.getFloorButtonUp(floor) ? "1" : "0");
		}
		System.out.println();
		System.out.print("Floor buttons Down pressed: ");
		for (int floor=0; floor<controller.getFloorNum(); floor++) {
			System.out.print(controller.getFloorButtonDown(floor) ? "1" : "0");
		}
		System.out.println();		
		
		for (int elevator=0; elevator<controller.getElevatorNum(); elevator++) {
			System.out.println("Settings of elevator number: " + elevator);
			System.out.println("  Floor: " + controller.getElevatorFloor(elevator));
			System.out.println("  Position: " + controller.getElevatorPosition(elevator));
			System.out.println("  Target: " + controller.getTarget(elevator));
			System.out.println("  Committed direction: " + controller.getCommittedDirection(elevator));
			System.out.println("  Door status: " + controller.getElevatorDoorStatus(elevator));		
			System.out.println("  Speed: " + controller.getElevatorSpeed(elevator));
			System.out.println("  Acceleration: " + controller.getElevatorAccel(elevator));
			System.out.println("  Capacity: " + controller.getElevatorCapacity(elevator));
			System.out.println("  Weight: " + controller.getElevatorWeight(elevator));
			System.out.print("  Buttons pressed: ");
			for (int floor=0; floor<controller.getFloorNum(); floor++) {
				System.out.print(controller.getElevatorButton(elevator, floor) ? "1" : "0");
			}
			System.out.println();
		}
		
	}

	private void runExample() throws RemoteException {
		
		final int elevator = 0;
		final int numberOfFloors = controller.getFloorNum();
		final int sleepTime = 60;
		
		// First: Starting from ground floor, go up to the top floor, stopping in each floor
		
		// Set the committed direction displayed on the elevator to up
		controller.setCommittedDirection(elevator, IElevator.ELEVATOR_DIRECTION_UP);
		
		for (int nextFloor=1; nextFloor<numberOfFloors; nextFloor++) {
			
			// Set the target floor to the next floor above
			controller.setTarget(elevator, nextFloor);
			
			// Wait until closest floor is the target floor and speed is back to zero 
			while (controller.getElevatorFloor(elevator) < nextFloor || controller.getElevatorSpeed(elevator) > 0) {
				try { 
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {}
			}
			
			// Wait until doors are open before setting the next direction
			while (controller.getElevatorDoorStatus(elevator) != IElevator.ELEVATOR_DOORS_OPEN) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {}
			}
		}
		
		// Second, go back from the top floor to the ground floor in one move
		
		// Set the committed direction displayed on the elevator to down
		controller.setCommittedDirection(elevator, IElevator.ELEVATOR_DIRECTION_DOWN);
		
		// Set the target floor to the ground floor (floor number 0)
		controller.setTarget(elevator, 0);
		
		// Wait until ground floor has been reached
		while (controller.getElevatorFloor(elevator) > 0 || controller.getElevatorSpeed(elevator) > 0) {
			try { 
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {}
		}
		
		// Set the committed direction to uncommitted when back at the ground floor
		controller.setCommittedDirection(elevator, IElevator.ELEVATOR_DIRECTION_UNCOMMITTED);
		
	}
	
}