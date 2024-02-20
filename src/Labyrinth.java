import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public abstract class Labyrinth {
  private Labyrinth() {}

  public static void simulate(Guest[] guests, Random random) {
    final boolean[] haveVisited = new boolean[guests.length];
    final ScheduledExecutorService[] executors = new ScheduledExecutorService[guests.length];
    for (int i = 0; i < guests.length; i++) {
      haveVisited[i] = false;
      executors[i] = Executors.newSingleThreadScheduledExecutor();
      executors[i].scheduleWithFixedDelay(guests[i], 1, 1, TimeUnit.MILLISECONDS);
    }
    
    boolean hasCupcake = true;
    for (boolean hasAnnounced = false; !hasAnnounced;) {
      final int i = random.nextInt(guests.length);
      final boolean hadCupcake = hasCupcake;
      final Future<Guest.Decision> future = executors[i].submit(() -> guests[i].decide(hadCupcake));
      final Guest.Decision decision;
      try {
        decision = future.get();
      } catch (InterruptedException e) {
        System.err.println("Simulation was interrupted!");
        e.printStackTrace();
        return;
      } catch (ExecutionException e) {
        System.err.println("Guest " + i + " failed to make a decision!");
        e.getCause().printStackTrace();
        return;
      }
      haveVisited[i] = true;
      System.out.print("Guest " + i);
      switch (decision) {
        case LEAVE_CUPCAKE: 
          hasCupcake = true;
          System.out.println(hadCupcake ? " leaves the cupcake." : " requests a cupcake.");
          break;
        case LEAVE_NO_CUPCAKE:
          hasCupcake = false;
          System.out.println(hadCupcake ? " eats the cupcake." : " leaves the plate empty.");
          break;
        case ANNOUNCE:
          hasAnnounced = true;
          System.out.println(" announces that all guests have visited!");
          break;
      }
    }

    for (ScheduledExecutorService exec : executors) {
      exec.shutdownNow();
    }

    final int[] notVisited = IntStream.range(0, guests.length).filter(i -> !haveVisited[i]).toArray();
    if (notVisited.length > 0) {
      if (notVisited.length == 1) System.out.print("  Guest " + notVisited[0] + " has not visited!");
      else {
        System.out.print("  Guests ");
        printArray(notVisited);
        System.out.print(" have not visited!");
      }
    } else {
      System.out.println(guests.length == 1 ? "  The only guest has visited!" : "  All " + guests.length + " guests have visited!");
    }
  }

  private static void printArray(int[] array) {
    switch (array.length) {
      case 0: break;
      case 1: System.out.print(array[0]); break;
      case 2: System.out.print(array[0] + " and " + array[1]); break;
      default: 
        for (int i = 0; i < array.length - 1; i++) {
          System.out.print(array[i] + ", ");
        }
        System.out.print("and " + array[array.length - 1]);
    }
  }

  public static interface Guest extends Runnable {
    enum Decision {
      LEAVE_CUPCAKE, LEAVE_NO_CUPCAKE, ANNOUNCE;
    };

    /**
     * Simulate partying. Called while busy-waiting.
     */
    @Override
    default void run() {

    }

    Decision decide(boolean seesCupcake);
  }
}
