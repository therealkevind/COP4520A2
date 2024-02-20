public class FollowerGuest implements Labyrinth.Guest {
  private boolean alreadyEaten = false;

  @Override
  public void run() {
    
  }

  @Override
  public Labyrinth.Guest.Decision decide(boolean seesCupcake) {
    if (!alreadyEaten && seesCupcake) {
      alreadyEaten = true;
      return Decision.LEAVE_NO_CUPCAKE;
    }
    return seesCupcake ? Decision.LEAVE_CUPCAKE : Decision.LEAVE_NO_CUPCAKE;
  }
}
