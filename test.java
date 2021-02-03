import java.util.Stack;

public class test
{
	private static Stack<Integer> stack = new Stack<Integer>();
	private static int[] array = new int[4];

	public static void main(String[] args) {
		stack.push(1);
		stack.push(2);
		stack.push(3);
		stack.push(4);

		transfer();

		for (int x = 0; x < 4; x++)
			System.out.println(array[x]);

	}

	public static void transfer(){
		int stackSize = stack.size();
		for (int x = 0; x < stackSize; x++)
			array[x] = stack.pop();
	}

}