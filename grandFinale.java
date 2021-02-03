/*
I designed my grandFinale based off my answer to Ex3 so that it could solve both prim as well as loopy mazes. I used elements of Route B
in my final answer. At the end of its first run, the stack called headings will have stored every heading for the robot's movement that contains
a direct path to the target. The program then refers to the stack's items chronologically and leads the robot through the shortest path (from information in first run) to the target. 
However, while this works very well on prim mazes, it isn't the most efficient solution for loopy mazes. The shortest possible path is not guaranteed
to be found for a loopy maze because the robot can only use information from its first run and in loopy mazes its second run will be based on its
previous run, which might have been a more circuitous path. It is always guaranteed though, that the second run will be shorter and more efficient than
the first for both prim and loopy mazes. After the first run, all subsequent runs will be identical. This is because all subsequent runs will run the same path of headings in the stack.
When a new maze is generated the robot will begin to explore and gather information again and then will use that information for the subsequent runs.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Stack;

public class grandFinale
{
	private int pollRun = 0;
	private RobotData robotData;
	private int counter = 0;

		public void controlRobot(IRobot robot) {
			
			//runs only for the first run
			if (robot.getRuns() == 0) {
				robotData = new RobotData(); //initializes a new robotData
				//clears the stack on the first run of a new maze
				if (pollRun == 0)
					robotData.clearStack();

				//explores new passages, backtracks if there are none surrounding the robot
				if (passageExits(robot) > 0) {
					exploreControl(robot);
				} else {
					backtrackControl(robot);
				}

			//runs for all subsequent runs
			} else {

				//sets counter to 0 on first move of any run
				if (pollRun == 0)
					counter = 0;

				//sets heading according to the items in the array
				robot.setHeading(robotData.returnHeading(counter));
				counter++; //increments counter variable for next move
			}

			pollRun++; //increments pollRun so program knows what move robot is on
		}
	

	//explores any unexplored pathways in the maze
	public void exploreControl(IRobot robot) {

		if (nonWallExits(robot) == 1) {
			robot.face(deadEnd(robot));
		} else if (nonWallExits(robot) == 2) {
			robot.face(corridor(robot));
		} else {
			robot.face(junction(robot));
		}
		//pushes all selected headings when exploring onto the stack
		robotData.recordHeadings(robot);
	}

	//backtracks through the exact route the robot took until it reaches an unexplored pathway
	public void backtrackControl(IRobot robot) {
		robot.setHeading(robotData.searchHeadings(robot)); //pops last heading off stack
		robot.face(IRobot.BEHIND); //returns down the direction the robot first came
	}

	//runs when Reset button clicked
	public void reset() {
		pollRun = 0; //sets pollRun back to 0 for next run
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

class RobotData 
{
	private static Stack<Integer> headings = new Stack<Integer>(); //declaration of stack of headings

	//pushes robot's current heading onto stack when called
	public void recordHeadings(IRobot robot) {
		headings.push(robot.getHeading());
	}

	//pops last heading in the stack when called
	public int searchHeadings(IRobot robot) {
		return headings.pop();
	}

	//returns value at a specific index of the stack
	public int returnHeading(int x){
		return headings.get(x);
	}

	//clears all items in a stack
	public void clearStack() {
		while (!headings.empty()) {
			headings.pop();
		}
	}
}