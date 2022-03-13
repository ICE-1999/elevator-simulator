

# elevator-simulator

This is the term assignment for SYSC3303A group 11, an (eventually) distributed discrete event simulator for modelling the behavior of elevators carrying passengers between floors in a building.

## Files
Files in this project are organized into several sub-packages: `elevator`, `floor`, and `scheduler`. Each package contains the files relevant to the subsystem defined by the package name.  A `Main` file is also defined outside any package, acting as a driver program to launch the various subsystems. A `test` package is also included to contain the project tests; it is not necessary to pick any individual test files unless you wish to.

## Setup Instructions (No GUI)
1. `git clone` this repository to your local machine
2. Open Eclipse, and and in the *File/Open Project From Filesystem* view, select the repository folder. Eclipse should detect the project configuration and import the project.
3. Right click on `SchedulerMain.java` and select `Run as -> 1 Java Application` to begin the Scheduler.
4. Do the same for `ElevatorMain.java`. This can be run multiple times to simulate multiple elevators.
5. Repeat step 3 for `FloorMain.java` which will execute the event simulation plan in `events.txt`.

## Setup Instructions (GUI)
1.	`git clone` this repository to your local machine
2.	Open Eclipse, and and in the *File/Open Project From Filesystem* view, select the repository folder. Eclipse should detect the project configuration and import the project.
3.	Right click on `GUI.java` and select select `Run as -> 1 Java Application` to begin the program.

## Testing Instructions
1.	`git clone` this repository to your local machine
2.	Open Eclipse, and and in the *File/Open Project From Filesystem* view, select the repository folder. Eclipse should detect the project configuration and import the project.
3.	Right click on `errorHandlingTest.java` and select `Run as -> 1 Junit Test` to begin the error handling test.
4.	Right click on `multpleElevatorTest.java` and select `Run as -> 1 Junit Test` to begin the multiple elevator test run.

#### Understanding `events.txt`
The `events.txt` file contains the sequence of button presses to call and direct elevators over the course of the simulator execution. The format of each line of the input text file is `$TIME $BUTTON $SOURCE $DESTINATION $ERROR_TYPE`.
 
 These variables have the following formats:
 - `$TIME`: Event time in number of seconds since program start
 - `$BUTTON`: **UP** or **DOWN**
 - `$SOURCE`: An integer for the source floor number
 - `$DESTINATION`: An integer for the destination floor number
 - `$ERROR_TYPE`: An integer for the type of error that will occur at the destination floor number.
 
 ## Work Breakdown (Iteration 1)
  - Chukwuka: Initial design of how the three subsystems would interconnect and pass information between one another based on assignment one
  - Mark: Implementation of the initial subsystem design, datastructures, and event file loading; setting up project
  - Visakan: Insight into how to design the three subsystems from a different perspective, UML/sequence diagrams for the class structure

 ## Work Breakdown (Iteration 2)
  - Since most of the iteration 2 code was actually completed in iteration 1, the only changes are the diagrams.
  - Lazar: Sequence diagram and state diagram.

 ## Work Breakdown (Iteration 3)
  - Lazar: Conversion from 1 main method to 3 separate executables (SchedulerMain, ElevatorMain, FloorMain), which use UDP to communicate. UML sequence diagram.
  - Visakan: UML Class diagram

 ## Work Breakdown (Iteration 4)
 -	Visakan: Door Class and error 'injection' and handling. UML Class diagram.
 - Lazar: Elevator path optimization for passenger wait time. Acts as a normal elevator should.
 - Chukwuka: Attempted to setup errorHandling & error testing
 - Mark: Timing Diagrams

 ## Work Breakdown (Iteration 5)
 - Visakan: Fixing Iteration 4 merge issues, refactored error handling. UML Class diagram. Final Report Writeup. Testing (multipleElevatorTest and errorHandlingTest). GUI system & thread integration
- Lazar: Fixing Iteration 4 merge issues, optimizing multiple elevator handling. UML Sequence Diagram & State Machine Diagram. Demo Video. Final report (Design Stages – Iteration 3 & 4). GUI fixing & updating
- Chukwuka: GUI class design and creation
- Mark: Timing Diagrams
