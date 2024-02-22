import java.util.Arrays;
import java.util.Random;

public class App {
  public static void main(String[] args) {
    // parse command line arguments
    if (args.length > 3 || args.length < 1) {
      System.err.println("Expected one to three arguments, got " + args.length);
      System.exit(1);
      return;
    }
    final int threadCount = args.length > 1 ? parseInt(args[1]) : 8;
    final Random random = new Random();
    if (args.length > 2) random.setSeed(Long.parseLong(args[2]));
    switch (args[0]) {
      case "1": {
        Labyrinth.Guest[] guests = new Labyrinth.Guest[threadCount];
        Arrays.setAll(guests, i -> new FollowerGuest());
        guests[0] = new LeaderGuest(threadCount);
        new Labyrinth().simulate(guests, random);
      } break;
      case "2": {
        // TODO
        System.out.println("Not implemented!");
      } break;
      default:
        System.err.println("Expected \"1\" or \"2\", got \"" + args[0] + "\".");
    }
  }
  
  private static int parseInt(String arg) {
    int x = 0;
    try {
      x = Integer.parseInt(arg);
    } catch (NumberFormatException e) {}
    if (x <= 0) {
      System.err.println("Expected a positive integer, got \"" + arg + "\".");
      System.exit(1);
    }
    return x;
  }
}
