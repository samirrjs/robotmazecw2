import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Explorer 
{
	private int explorerMode; // 1 = explore, 0 = backtrack
	private int pollRun = 0;
	private RobotData robotData;

	public void controlRobot(IRobot robot) {

		if ((robot.getRuns() == 0) && (pollRun == 0)){
			robotData = new RobotData();
			explorerMode = 1;
		}

		if (explorerMode == 1) {
			exploreControl(robot);
		}

		if (explorerMode == 0) {
			backtrackControl(robot);
		}
		pollRun++;
	}

	public void exploreControl(IRobot robot) {
		int direction;
		int nonWallExits = nonWallExits(robot);

		if (nonWallExits == 1) {
			direction = deadEnd(robot);
			explorerMode = 0;
		} else if (nonWallExits == 2) {
			direction = corridor(robot);
		} else {
			direction = junction(robot);
			if (beenBeforeExits(robot) <= 1) {
				robotData.recordJunction(robot, robot.getLocation().x, robot.getLocation().y, robot.getHeading());
				robotData.printJunction(robot);
			}
		}
		robot.face(direction);
	}

	public void backtrackControl(IRobot robot) {
		int heading;
		int oppHeading = 0;
		int direction;

		//checks if at junction
		if (nonWallExits(robot) > 2) {
			if (passageExits(robot) >= 1) {
				direction = junction(robot);
				robot.face(direction);
				explorerMode = 1;
			} else {
				heading = robotData.searchJunction(robot, robot.getLocation().x, robot.getLocation().y);
				if (heading == IRobot.SOUTH) {
					oppHeading = IRobot.NORTH;
				} else if (heading == IRobot.NORTH) {
					oppHeading = IRobot.SOUTH;
				} else if (heading == IRobot.EAST) {
					oppHeading = IRobot.WEST;
				} else if (heading == IRobot.WEST){
					oppHeading = IRobot.EAST;
				}
				robot.setHeading(oppHeading);
			}
		} else if (nonWallExits(robot) == 2) {
			direction = corridor(robot);
			robot.face(direction);
		} else {
			direction = deadEnd(robot);
			robot.face(direction);
		}
	}

	public void reset() {
		robotData.resetJunctionCounter();
		explorerMode = 1;
	}

	public int nonWallExits(IRobot robot) {
		int nonWallExits = 0;

		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) != IRobot.WALL)
			nonWallExits++;
		}
		return nonWallExits;
	}

	public int passageExits(IRobot robot) {
		int passageExits = 0;

		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) == IRobot.PASSAGE)
			passageExits++;
		}
		return passageExits;
	}

	public int beenBeforeExits(IRobot robot) {
		int beenBeforeExits = 0;

		for (int i = 0; i<4; i++) {
		if (robot.look(IRobot.AHEAD+i) == IRobot.BEENBEFORE)
			beenBeforeExits++;
		}
		return beenBeforeExits;
	}

	private int deadEnd(IRobot robot) {
		int direction = IRobot.AHEAD;
			for (int i = 0; i < 4; i++) {
				if (robot.look(IRobot.AHEAD+i) !=IRobot.WALL)
					direction = IRobot.AHEAD+i;
			}
		return direction;
	}

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

	private int junction(IRobot robot) {
		int direction;
		int counter = 0;
		int counter2 = 0;
		int checkDirections[] = new int[3];
		int passageIndices[] = new int[3];
		int openIndices[] = new int[3];
		int directions[] = {IRobot.AHEAD, IRobot.RIGHT, IRobot.LEFT};
		for (int i=0; i<3; i++) {
			checkDirections[i] = robot.look(directions[i]);
			if (checkDirections[i] == IRobot.PASSAGE){
				passageIndices[counter] = i;
				counter++;
			}
			if (checkDirections[i] != IRobot.WALL){
				openIndices[counter2] = i;
				counter2++;
			}
		}
		if (passageExits(robot) > 0) {
			int random = (int)Math.floor(Math.random()*counter);
        	direction = directions[passageIndices[random]];
		} else {
			int random = (int)Math.floor(Math.random()*counter2);
        	direction = directions[openIndices[random]];
		}
		return direction;
	} 
}

class RobotData 
{
	private static int maxJunctions = 10000;
	private static int junctionCounter = 0;
	JunctionData[] junctions = new JunctionData[maxJunctions]; // Array of junction objects

	public void recordJunction(IRobot robot, int x, int y, int heading) {
		JunctionData addJunction = new JunctionData(x, y, heading);
		junctions[junctionCounter] = addJunction;
		junctionCounter++;
	}

	public int searchJunction(IRobot robot, int x, int y) {
		int juncHeading = 0;
		for (int i = 0; i < junctionCounter; i++){
			if (junctions[i].x == x && junctions[i].y == y)
				juncHeading = junctions[i].heading;
		}
		return juncHeading;
	}

	public void resetJunctionCounter() {
		junctionCounter = 0;
	}

	public void printJunction(IRobot robot) {
		System.out.println("Junction: " + (junctionCounter-1) + "(x=" + junctions[junctionCounter-1].x + ",y=" + junctions[junctionCounter-1].y + ") heading= " + junctions[junctionCounter-1].heading);
	}

}

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
