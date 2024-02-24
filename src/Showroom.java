import java.util.ArrayDeque;
import java.util.Queue;

public class Showroom {
  private volatile boolean isOccupied = false;
  private final Queue<Guest> queue = new ArrayDeque<>();
  private volatile boolean isRunning;

  // output things, not used for simulation
  private volatile int numVisits;
  private Guest occupant;

  /**
   * Simulates the showroom until no guest wants to see the vase anymore.
   * <p>
   * (Assumes that guests, once they don't want to see the vase, will never want to again, as with {@link ShowroomGuest}s.)
   * @param guests Guests who may or may not want to see the vase.
   */
  public void simulate(Guest[] guests) {
    numVisits = 0;
    isRunning = true;
    isOccupied = false;
    occupant = null;
    for (Guest guest : guests) {
      guest.showroom = this;
      guest.start();
    }
    for (Guest guest : guests) {
      while (guest.wantsVase()) Thread.yield();
    }
    isRunning = false;
    System.out.printf("Guests visited %d time%s.\n", numVisits, numVisits == 1 ? "" : "s");
  }

  // these methods are only synchronized to make them atomic, because they include additional checks to demonstrate correctness.
  // without those checks, these methods would just set isOccupied, no need to be synchronized
  private synchronized void enter(Guest guest) {
    if (isOccupied) {
      System.err.println("Guest tried to enter an occupied showroom!");
      System.exit(1);
    }
    isOccupied = true;
    occupant = guest;
  }
  private synchronized void exit(Guest guest) {
    if (!isOccupied || occupant != guest) {
      System.err.println("Guest tried to leave a showroom they weren't occupying!");
      System.exit(1);
    }
    isOccupied = false;
    occupant = null;
    numVisits++;
  }

  public static abstract class Guest extends Thread {
    private Showroom showroom;
    private volatile boolean isNotified = false;

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
      while (showroom.isRunning) {
        while (!wantsVase()) {
          party();
          if (!showroom.isRunning)
            return;
        }
        final boolean noNotification;
        synchronized (showroom.queue) {
          // get in line
          showroom.queue.add(this);
          // if you aren't at the front of the line with the door open to begin with,
          noNotification = showroom.queue.peek() == this && !showroom.isOccupied;
        }
        // you have to wait for the guest before you to let you know it's time.
        if (!noNotification) while (!isNotified) Thread.yield();
        while (showroom.isOccupied) Thread.yield();
        isNotified = false;
        // leave the queue, enter the showroom.
        synchronized (showroom.queue) {
          if (showroom.queue.remove() != this)  // sanity check
            throw new IllegalStateException("expected to be at the front of the queue");
          showroom.enter(this);
        }
        admireVase();
        // leave the room, and notify anyone who's there.
        synchronized (showroom.queue) {
          showroom.exit(this);
          final Guest nextGuest = showroom.queue.peek();
          if (nextGuest != null) nextGuest.isNotified = true;
        }
      }
    }

    /**
     * Simulate partying. Called while busy-waiting.
     */
    public void party() {
      Thread.yield();
    }
    /**
     * @return Whether this Guest wants to see the vase.
     */
    public abstract boolean wantsVase();
    /**
     * Simulates regarding the vase fondly.
     */
    public void admireVase() {}
  }
}
