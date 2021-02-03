/*
I used a stack of arrived from headings in order to save space in this exercise. The majority of the program is written in the same way as Ex1,
however when a new junction is encoutnered all the program does is "push" the arrived from heading onto the stack, and when backtracking, if we 
encounter a junction at which there are no unexplored passages the program then pops the last item off the stack and sets the robots heading to the 
opposite of the stack's last arrived from direction. This implementation works because on a prim maze you will encounter the junctions you have recorded
in the same order of the stack's "pops" when you are backtracking. Therefore, due to the "First in last out" nature of the stack there is no need to store the 
coordinates of junctions and find the values at the current junction.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Stack;

public class Ex2 
{
	private int explorerMode; // 1 = explore, 0 = backtrack
	private int pollRun = 0;
	private RobotData robotData;

	public void controlRobot(IRobot robot) {

		// On the first move of the first run of a new maze
		if ((robot.getRuns() == 0) && (pollRun == 0)){
			robotData = new RobotData(); //reset the data store
			explorerMode = 1;
		}

		//calls exploreControl or backtrackControl based on explorerMode value
		if (explorerMode == 1) {
			exploreControl(robot);
		}
		if (explorerMode == 0) {
			backtrackControl(robot);
		}
		//increments pollRun variable by 1, so that the robotData is only initialized on first run
		pollRun++; //keeps count of number of times robot has moved this run
	}

	//called when there are unexplored passages to search
	public void exploreControl(IRobot robot) {
		int direction;

		//calls a direction picking method based on the number of walls to determine robot's current position
		if (nonWallExits(robot) == 1) {
			robot.face(deadEnd(robot));
			if (pollRun != 0)
				explorerMode = 0; //sets explorerMode to 0 in order to call backtrackControl next run (if not at start)
		} else if (nonWallExits(robot) == 2) {
			robot.face(corridor(robot));
		} else {
			//if an unvisited junction is encountered, call recordJunction method
			if (beenBeforeExits(robot) <= 1) {
				robotData.recordJunction(robot);
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

	public void reset() {
		explorerMode = 1; //resets explorerMode to 1 for start of next run
		pollRun = 0; //sets pollRun to 0 for start of next run
		robotData.emptyStack(); //clears the stack of headings for the next run
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
	//written this way instead of just facing behind to counter the first run issue, where the robot may be spawned at a deadend facing a wall.
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
	//initializes a stack called junctions
	private static Stack<Integer> junctions = new Stack<Integer>();

	//pushes current robotHeading onto the stack when called
	public void recordJunction(IRobot robot) {
		junctions.push(robot.getHeading());
	}

	//pops last item in the stack when called
	public int searchJunction(IRobot robot) {
		return junctions.pop();
	}

	//iteratively clears all items in the junctions stack
	public void emptyStack() {
		if(!junctions.empty()){
			for(int i = 0; i < junctions.size(); i++){
				junctions.pop();
			}
		}
	}
}