import java.util.Random;

public class ShowroomGuest extends Showroom.Guest {
  private long numWants;

  public ShowroomGuest(long numWants) {
    this.numWants = numWants;
  }
  // does the randomness up front
  public ShowroomGuest(double wantsProb, Random random) {
    numWants = 0;
    while (random.nextDouble() < wantsProb) numWants++;
  }

  @Override
  public boolean wantsVase() {
    return numWants > 0;
  }

  @Override
  public void admireVase() {
    numWants--;
    super.admireVase();
  }
}
