/*
I first designed the nonWallExits, passageExits, and beenBeforeExits methods to iteratively loop through the four possible directions
and use a counter to return the number of exits corresponding with the respective method's specifications. Then I coded the deadEnd, corridor,
and junction methods. The deadEnd iteratively finds the direction that is open and returns that direction. The corridor method keeps the robot 
facing forward, and only turns when there is a shift in the direction of the corridor. The junction method uses arrays to initially find and pick
a random unexplored passage, and if none are available then it will pick and return a random non-wall exit. I then wrote exploreControl and 
backtrackControl, where I had exploreControl call to deadEnd, corridor, and junction based on the number of walls surrounding it. Meanwhile, 
backtrackControl was called when the robot hit a deadend, and had to backtrack its way to its last known junction. There doesn't seem to be too
much repeated code to me, however I have noticed that my code for nonWallExits, passageExits, and beenBeforeExits is practically identical and that
there might be a more efficient way of writing this method and passing information into it in order to select between the different values of IRobot.WALL,
IRobot.PASSAGE, and IRobot.WALL.
I tested the robot over multiple mazes of different sizes and made sure it ran the way it was supposed to. I worked to improve effeciency by reducing certain
repeated code (for example using the code written in exploreControl to avoid rewriting it in backtrackControl).
The worst case scenario in this depth-first-search robot would be if the robot visited all the nodes (deadends) in the maze before finding the target node.
This would mean that the robot traversed through the entirety of the maze before finding the target square. I do not believe it is possible to calculate a 
maximum number of steps the robot can take before reaching the target as this is dependent on the size of the maze as well as how the different junctions and
branches are generated.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1 
{
	private int explorerMode; // 1 = explore, 0 = backtrack
	private int pollRun = 0;
	private RobotData robotData;

	public void controlRobot(IRobot robot) {

		// On the first move of the first run of a new maze
		if ((robot.getRuns() == 0) && (pollRun == 0)){
			robotData = new RobotData(); //reset the data store
		}

		//calls exploreControl or backtrackControl based on explorerMode value
		if (explorerMode == 1) {
			exploreControl(robot);
		}
		if (explorerMode == 0) {
			backtrackControl(robot);
		}
		//increments pollRun variable by 1, so that the robotData is only initialized on first run
		pollRun++; //keeps count of number of movements robot has made this run
	}

	//called when there are unexplored passages to search
	public void exploreControl(IRobot robot) {

		//calls a direction picking method based on the number of walls to determine robot's current position
		if (nonWallExits(robot) == 1) {
			robot.face(deadEnd(robot));
			if (pollRun != 0)
				explorerMode = 0; //sets explorerMode to 0 in order to call backtrackControl next run (if not at start)
		} else if (nonWallExits(robot) == 2) {
			robot.face(corridor(robot));
		} else {
			//if an unvisited junction is encountered, record the x, y, and arrived from heading at this junction
			//as well as printing those values out in the terminal
			if (beenBeforeExits(robot) <= 1) {
				robotData.recordJunction(robot);
				robotData.printJunction(robot);
			}
			robot.face(junction(robot));
		}
	}

	//called when robot needs to retrace its steps to last known junction
	public void backtrackControl(IRobot robot) {
		int heading;

		//if at a junction with no passages find the heading you arrived from and return down that direction
		if(nonWallExits(robot) > 2 && passageExits(robot) == 0) {
			heading = robotData.searchJunction(robot);
			robot.setHeading(heading);
			robot.face(IRobot.BEHIND);
		//otherwise, if in a corridor or deadend, calls those methods through exploreControl so as not to repeat code.
		} else {
			//if at a junction which has unexplored pathways, flip back to explorerMode 1.
			if (nonWallExits(robot) > 2 && passageExits(robot) > 0)
				explorerMode = 1;
			exploreControl(robot);
		}
	}

	//called whenever reset button is pressed
	public void reset() {
		robotData.resetJunctionCounter(); //sets JunctionCounter back to 0 for start of next run
		explorerMode = 1; //Sets explorerMode to 1 for start of next run
		pollRun = 0; //sets pollRun to 0 for the start of next run
	}

	//returns the number of non-wall exits around the robot
	public int nonWallExits(IRobot robot) {
		int nonWallExits = 0;

		//loops through and increments variable if no wall detected
		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) != IRobot.WALL)
			nonWallExits++;
		}
		return nonWallExits;
	}

	//returns the number of passage exits around the robot
	public int passageExits(IRobot robot) {
		int passageExits = 0;

		//loops through and increments variable if passage detected
		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) == IRobot.PASSAGE)
			passageExits++;
		}
		return passageExits;
	}

	//returns the number of beenbefore exits around the robot
	public int beenBeforeExits(IRobot robot) {
		int beenBeforeExits = 0;

		//loops through and increments variable if beenBefore detected
		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) == IRobot.BEENBEFORE)
			beenBeforeExits++;
		}
		return beenBeforeExits;
	}

	//returns the only available pathway around the robot. 
	//counters the first run issue, where the robot may be spawned at a deadend facing a wall.
	private int deadEnd(IRobot robot) {
		int direction = IRobot.AHEAD;
			for (int i = 0; i < 4; i++) {
				if (robot.look(IRobot.AHEAD+i) !=IRobot.WALL)
					direction = IRobot.AHEAD+i;
			}
		return direction;
	}

	//returns forward until a wall is encountered, then picks left/right based on which is open
	private int corridor(IRobot robot) {
		int direction = IRobot.AHEAD;
		if (robot.look(IRobot.AHEAD) == IRobot.WALL) {
			if (robot.look(IRobot.RIGHT) != IRobot.WALL) {
				direction = IRobot.RIGHT;
			} else {
				direction = IRobot.LEFT;
			}
		}
		return direction;
	}

	//returns a random passage exit from available ones. If there are none, then returns a random non-wall exit. 
	private int junction(IRobot robot) {
		int direction = 0;
		int openPassages = 0;
		int openBeenBefore = 0;
		int checkDirections[] = new int[3];
		int passageIndices[] = new int[3];
		int openIndices[] = new int[3];
		int directions[] = {IRobot.AHEAD, IRobot.RIGHT, IRobot.LEFT};

		//iteratively finds open unexplored passages
		for (int i=0; i<3; i++) {
			checkDirections[i] = robot.look(directions[i]);
			if (checkDirections[i] == IRobot.PASSAGE){
				passageIndices[openPassages] = i;
				openPassages++;
			}
			//iteratively finds any non wall exit
			if (checkDirections[i] != IRobot.WALL){
				openIndices[openBeenBefore] = i;
				openBeenBefore++;
			}
		}
		//randomly selects an unexplored path, and if none are available then randomly selects any non-wall exit
		if (passageExits(robot) > 0) {
			int random = (int)Math.floor(Math.random()*openPassages);
        	direction = directions[passageIndices[random]];
		} else {
			int random = (int)Math.floor(Math.random()*openBeenBefore);
        	direction = directions[openIndices[random]];
		}
		return direction;
	} 
}

//RobotData class that stores junction information
class RobotData 
{
	private static int maxJunctions = 10000; //max number of junctions likely to occur
	private static int junctionCounter = 0; //
	JunctionData[] junctions = new JunctionData[maxJunctions]; // declares an array of junctionData objects

	//when called, records the robot's current x and y coordinate and also their heading and stores this junctionData object in an array of objects
	public void recordJunction(IRobot robot) {
		//initializes the command to add new Junctions to array
		JunctionData addJunction = new JunctionData(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
		junctions[junctionCounter] = addJunction; //adds the new junctionData object
		junctionCounter++; //increments junctionCounter by 1 for the next run
	}

	//searches for the junction the robot is currently at and returns the initial arrived from heading at that junction
	public int searchJunction(IRobot robot) {
		int juncHeading = 0;
		//loops through the different junctionData objects in the junctions array and finds the initial arrived from heading
		//stores this value in the variable juncHeading
		for(int i = 0; i < junctionCounter; i++) {
			if(junctions[i].x == robot.getLocation().x && junctions[i].y == robot.getLocation().y)
				juncHeading = junctions[i].heading;
		}
		return juncHeading;
	}

	//method that sets junctionCounter variable to 0 (called by reset method in controlRobot)
	public void resetJunctionCounter() {
		junctionCounter = 0;
	}

	//prints out junctionData information for whichever junction the robot is currently at
	public void printJunction(IRobot robot) {
		System.out.println("Junction: " + (junctionCounter-1) + "(x=" + junctions[junctionCounter-1].x + ",y=" + junctions[junctionCounter-1].y + ") heading= " + junctions[junctionCounter-1].heading);
	}

}

//creats a JunctionData object that stores x, y coordinates and arrived from headings.
class JunctionData {
	public int x;
	public int y;
	public int heading;

	public JunctionData(int x, int y, int heading) {
		this.x = x;
		this.y = y;
		this.heading = heading;
	}
}