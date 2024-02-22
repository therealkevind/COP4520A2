public class LeaderGuest extends Labyrinth.Guest {
  private final int numGuests;
  private int numRequested = 0;

  public LeaderGuest(int numGuests) {
    this.numGuests = numGuests;
  }

  @Override
  public Decision decide(boolean seesCupcake) {
    if (!seesCupcake) numRequested++;
    if (numRequested >= numGuests - 1) return Decision.ANNOUNCE;
    return Decision.LEAVE_CUPCAKE;
  }
}
