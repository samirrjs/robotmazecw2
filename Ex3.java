/*
The previous implementations in Ex1 and Ex2 were unable to solve loopy mazes because it was possible to encounter an old junction for the
second time without having to backtrack to it. This resulted in many bugs, with the robot crashing into walls or not having a direction to 
move in. I fixed this issue by modifying my code to Ex2 slightly. I coded it so that as long as there were unexplored pathways the robot
would keep exploring them, but if there were none it would backtrack the exact path it came until it encountered an unexplored path. The 
specification of this program closely resembles Tremaux' algorithm with a few differences. This robot will explore all unexplored pathways
and will only backtrack through a certain area once, meaning no area of the maze will be traversed more than twice just as Tremaux states. 
Furthermore, if the target is blocked off, the robot will traverse the whole maze and return to the starting block with an empty stack.
I also understand that due to the storage of not just the headings at junctions, but rather headings for all movement that this robot can be 
memory inefficient. However, it works to solve both loopy mazes and prim mazes and at the end contains a stack of the exact route to the 
solution based on the route it took to get there on its first run (which will prove useful in the grandFinale).
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Stack;

public class Ex3 
{
	private int pollRun = 0;
	private RobotData robotData;

	public void controlRobot(IRobot robot) {

		if ((robot.getRuns() == 0) && (pollRun == 0)){
			robotData = new RobotData();
		}

		//calls exploreControl if there are any unexplored passages around the robot, otherwise calls backtrackControl
		if (passageExits(robot) > 0) {
			exploreControl(robot);
		} else {
			backtrackControl(robot);
		}
		
		//increment pollRun so that RobotData is not overwritten
		pollRun++; //keeps count of robot's movements this run
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
		robotData.clearStack(); //clears the stack of headings
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

//RobotData class stores information on all of the robot's movements
class RobotData 
{
	private static Stack<Integer> headings = new Stack<Integer>(); //declares stack of headings

	//pushes robot's current heading to stack when called
	public void recordHeadings(IRobot robot) {
		headings.push(robot.getHeading());
	}

	//pops last item off stack when called
	public int searchHeadings(IRobot robot) {
		return headings.pop();
	}

	//iteratively clears all items in a stack
	public void clearStack() {
		if(!headings.empty()){
			for(int i = 0; i < headings.size(); i++){
				headings.pop();
			}
		}
	}
}