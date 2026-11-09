package powerups;

import mx.unison.pong.BallRunner;
import mx.unison.pong.Board;
import mx.unison.pong.Paddle;

public class SlowTimePowerUp extends PowerUp {

    public SlowTimePowerUp(double x, double y) {
        super(x, y, "PWRslowtime");
    }

    @Override
    public void applyEffect(Board board, BallRunner ballRunner, Paddle paddle) {
        ballRunner.applySlowTime(5000); // Reduce velocidad durante 5 segundos
    }
}