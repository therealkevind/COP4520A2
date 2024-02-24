public class FollowerGuest extends Labyrinth.Guest {
  private boolean alreadyEaten = false;

  @Override
  public boolean leaveCupcake(boolean seesCupcake) {
    if (!alreadyEaten && seesCupcake) {
      alreadyEaten = true;
      return false;
    }
    return seesCupcake;
  }

  @Override
  public boolean believesComplete() {
    return false;
  }
}
