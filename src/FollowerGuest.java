public class FollowerGuest extends Labyrinth.Guest {
  private boolean alreadyEaten = false;

  @Override
  public Decision decide(boolean seesCupcake) {
    if (!alreadyEaten && seesCupcake) {
      alreadyEaten = true;
      return Decision.LEAVE_NO_CUPCAKE;
    }
    return seesCupcake ? Decision.LEAVE_CUPCAKE : Decision.LEAVE_NO_CUPCAKE;
  }
}
