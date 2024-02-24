import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Labyrinth {
  // lastDecision is always set to a fresh CompletableFuture before a guest is selected
  private CompletableFuture<Boolean> lastDecision;
  private boolean hasCupcake;
  private volatile boolean isRunning;

  // for output purposes, not used for simulating

  private static void handleUncaughtException(Thread t, Throwable e) {
    e.printStackTrace();
    System.exit(2);
  }

  /**
   * Simulates the labyrinth game.
   * @param guests The guests to visit the labyrinth.
   * @param random A random number generator.
   * @param simTime The number of seconds to simulate for, or {@code 0} to simulate until a guest that just visited believes everyone's visited.
   */
  public void simulate(Guest[] guests, Random random, double simTime) {
    hasCupcake = true;
    isRunning = true;

    // for output purposes, not used for simulating
    final boolean[] haveVisited = new boolean[guests.length];
    AtomicInteger numVisits = new AtomicInteger();

    for (int i = 0; i < guests.length; i++) {
      guests[i].labyrinth = this;
      guests[i].setUncaughtExceptionHandler(Labyrinth::handleUncaughtException);
      guests[i].start();
    }

    final Thread minotaur = new Thread(() -> {
      while (isRunning) {
        final int i = random.nextInt(guests.length);
        lastDecision = new CompletableFuture<>();
        final Guest currentGuest = guests[i];
        if (!isRunning) return;  
        currentGuest.isAsked = true;
        final boolean decision;
        try {
          decision = lastDecision.get();
        } catch (CancellationException e) {
          // cancelled by timeout, this is normal
          isRunning = false;
          return;
        } catch (InterruptedException e) {
          System.err.println("Simulation was interrupted!");
          e.printStackTrace();
          isRunning = false;
          return;
        } catch (ExecutionException e) {
          System.err.println("Guest " + i + " failed to make a decision!");
          e.getCause().printStackTrace();
          System.exit(2);
          return;
        }
        numVisits.incrementAndGet();
        haveVisited[i] = true;
        if (simTime == 0) {
          System.out.print("Guest " + i);
          if (decision) System.out.println(hasCupcake ? " leaves the cupcake." : " requests a cupcake.");
          else System.out.println(hasCupcake ? " eats the cupcake." : " leaves the plate empty.");
        }
        hasCupcake = decision;
        if (simTime == 0 && currentGuest.believesComplete())
          isRunning = false;
      }
    });
    minotaur.setUncaughtExceptionHandler(Labyrinth::handleUncaughtException);
    minotaur.start();
    try {
      minotaur.join(Math.round(simTime * 1000));
      isRunning = false;
      if (lastDecision != null) lastDecision.cancel(true);  // in case the minotaur's waiting on lastDecision.get() but the currentGuest exited before deciding
      minotaur.join();
    } catch (InterruptedException e) {
      System.err.println("Simulation was interrupted!");
      e.printStackTrace();
      return;
    }

    // output a summary
    System.out.printf("Simulation is over after %d visit%s!\n", numVisits.get(), numVisits.get() == 1 ? "" : "s");
    final int[] believesComplete = IntStream.range(0, guests.length).filter(i -> guests[i].believesComplete()).toArray();
    System.out.print("  ");
    printArray(believesComplete);
    System.out.printf(" believe%s everyone's visited!\n", believesComplete.length > 1 ? "" : "s");

    final int[] notVisited = IntStream.range(0, guests.length).filter(i -> !haveVisited[i]).toArray();
    if (notVisited.length > 0) {
      System.out.print("  ");
      printArray(notVisited);
      System.out.printf(" %s not visited!\n", notVisited.length > 1 ? "have" : "has");
    } else System.out.println(guests.length == 1 ? "  The only guest has visited!" : "  All " + guests.length + " guests have visited!");
  }

  private static void printArray(int[] array) {
    switch (array.length) {
      case 0: System.out.print("No one"); break;
      case 1: System.out.print("Guest " + array[0]); break;
      case 2: System.out.print("Guests " + array[0] + " and " + array[1]); break;
      default:
        System.out.print("Guests ");
        for (int i = 0; i < array.length - 1; i++) {
          System.out.print(array[i] + ", ");
        }
        System.out.print("and " + array[array.length - 1]);
    }
  }

  public static abstract class Guest extends Thread {
    private volatile boolean isAsked = false;
    private Labyrinth labyrinth;

    /**
     * Repeatedly calls {@link #party} until asked to visit, then decides whether to {@link #leaveCupcake}.
     * Stops when the game's over (or when an exception is thrown).
     * <p>
     * Busy-waits instead of using a smarter waiting strategy
     * in order to better simulate how the guests are busy partying,
     * not just waiting for their turn in the labyrinth.
     */
    @Override
    public final void run() {
      while (true) {
        while (!isAsked) {
          party();
          if (!labyrinth.isRunning) return;
        }
        final boolean decision;
        try {
          decision = leaveCupcake(labyrinth.hasCupcake);
        } catch (Throwable e) {
          isAsked = false;
          labyrinth.lastDecision.completeExceptionally(e);
          return;
        }
        isAsked = false;
        labyrinth.lastDecision.complete(decision);
      }
    }

    /**
     * Simulate partying. Called while busy-waiting.
     */
    public void party() {
      Thread.yield();
    }

    public abstract boolean leaveCupcake(boolean seesCupcake);
    public abstract boolean believesComplete();
  }
}
