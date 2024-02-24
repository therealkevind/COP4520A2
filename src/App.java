import java.util.Arrays;
import java.util.Random;

public class App {
  public static void main(String[] args) {
    // parse common command line arguments
    if (args.length > 4 || args.length < 1) {
      System.err.println("Expected one to three arguments, got " + args.length);
      System.exit(1);
      return;
    }
    final int threadCount = args.length > 1 ? parseInt(args[1]) : 8;
    final Random random = new Random();
    boolean hasExtraArg = false;
    if (args.length > 2) {
      try {
        random.setSeed(Long.parseLong(args[2]));
        if (args.length > 3) hasExtraArg = true;
      } catch (NumberFormatException e) {
        if (args.length > 3) {
          System.err.println("Expected a long integer, got \"" + args[2] + "\".");
          System.exit(1);
        } else hasExtraArg = true;
      }
    }
    // problem-specific stuff
    switch (args[0]) {
      case "1": {
        Labyrinth.Guest[] guests = new Labyrinth.Guest[threadCount];
        Arrays.setAll(guests, i -> new FollowerGuest());
        guests[0] = new LeaderGuest(threadCount);
        final double simTime;
        if (hasExtraArg) {
          if ("-".equals(args[args.length - 1])) {
            simTime = 0;
          } else simTime = parseDouble(args[args.length - 1]);
        } else simTime = 30.0;
        new Labyrinth().simulate(guests, random, simTime);
      } break;
      case "2": {
        Showroom.Guest[] guests = new Showroom.Guest[threadCount];
        final double wantProb;
        if (hasExtraArg) {
          wantProb = parseDouble(args[args.length - 1]);
        } else wantProb = 0.99;
        if (wantProb >= 1) {
          System.err.println("Expected a probability less than 1, got \"" + args[args.length - 1] + "\".");
          System.exit(1);
        }
        Arrays.setAll(guests, i -> new ShowroomGuest(wantProb, random));
        new Showroom().simulate(guests);
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

  private static double parseDouble(String arg) {
    double x = 0;
    try {
      x = Double.parseDouble(arg);
    } catch (NumberFormatException e) {
    }
    if (x <= 0) {
      System.err.println("Expected a positive double-precision float, got \"" + arg + "\".");
      System.exit(1);
    }
    return x;
  }
}
