import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class Labyrinth {
  // currentGuest always unsets itself before completing the lastDecision
  private volatile Guest currentGuest;
  // lastDecision is always set to a fresh CompletableFuture before a currentGuest is selected
  private CompletableFuture<Guest.Decision> lastDecision;
  private boolean hasCupcake;
  private volatile boolean isRunning;

  public Labyrinth() {}

  public void simulate(Guest[] guests, Random random) {
    final boolean[] haveVisited = new boolean[guests.length];
    currentGuest = null;
    hasCupcake = true;
    isRunning = true;

    for (int i = 0; i < guests.length; i++) {
      guests[i].labyrinth = this;
      guests[i].setUncaughtExceptionHandler((t, e) -> {
        e.printStackTrace();
        // for emergencies
        // note that Thread.onSpinWait() isn't available until java 9
        while (lastDecision == null) Thread.yield();
        // ignores currentGuest in favor of stopping as soon as possible
        while (isRunning && !lastDecision.cancel(true)) Thread.yield();
      });
      guests[i].start();
    }

    while (isRunning) {
      final int i = random.nextInt(guests.length);
      lastDecision = new CompletableFuture<>();
      currentGuest = guests[i];
      final Guest.Decision decision;
      try {
        decision = lastDecision.get();
      } catch (CancellationException e) {
        isRunning = false;
        return;
      } catch (InterruptedException e) {
        System.err.println("Simulation was interrupted!");
        e.printStackTrace();
        return;
      } catch (ExecutionException e) {
        System.err.println("Guest " + i + " failed to make a decision!");
        e.getCause().printStackTrace();
        isRunning = false;
        return;
      }
      haveVisited[i] = true;
      System.out.print("Guest " + i);
      switch (decision) {
        case LEAVE_CUPCAKE:
          System.out.println(hasCupcake ? " leaves the cupcake." : " requests a cupcake.");
          hasCupcake = true;
          break;
        case LEAVE_NO_CUPCAKE:
          System.out.println(hasCupcake ? " eats the cupcake." : " leaves the plate empty.");
          hasCupcake = false;
          break;
        case ANNOUNCE:
          isRunning = false;
          System.out.println(" announces that all guests have visited!");
          break;
      }
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

  public static abstract class Guest extends Thread {
    public enum Decision {
      LEAVE_CUPCAKE, LEAVE_NO_CUPCAKE, ANNOUNCE;
    };

    private Labyrinth labyrinth;

    /**
     * Repeatedly calls {@link #party} until asked to visit, then {@link #decide}s.
     * Stops when the game's over (or when an exception is thrown).
     * <p>
     * Busy-waits instead of using a smarter waiting strategy
     * in order to better simulate how the guests are busy partying,
     * not just waiting for their turn in the labyrinth.
     */
    @Override
    public final void run() {
      while (labyrinth.isRunning) {
        while (labyrinth.currentGuest != this) {
          party();
          if (!labyrinth.isRunning) return;
        }
        final Decision decision;
        try {
          decision = decide(labyrinth.hasCupcake);
        } catch (Throwable e) {
          labyrinth.currentGuest = null;
          labyrinth.lastDecision.completeExceptionally(e);
          return;
        }
        labyrinth.currentGuest = null;
        labyrinth.lastDecision.complete(decision);
      }
    }

    /**
     * Simulate partying. Called while busy-waiting.
     */
    public void party() {
      Thread.yield();
    }

    public abstract Decision decide(boolean seesCupcake);
  }
}
