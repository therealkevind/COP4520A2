public class LeaderGuest extends Labyrinth.Guest {
  private final int numGuests;
  private int numRequested = 0;
  private boolean hasVisited = false;  // 

  public LeaderGuest(int numGuests) {
    this.numGuests = numGuests;
  }

  @Override
  public boolean leaveCupcake(boolean seesCupcake) {
    if (!seesCupcake) numRequested++;
    hasVisited = true;
    return true;
  }

  @Override
  public boolean believesComplete() {
    return numRequested >= numGuests - 1 && hasVisited;
  }
}
