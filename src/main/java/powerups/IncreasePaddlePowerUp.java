package powerups;

import mx.unison.pong.BallRunner;
import mx.unison.pong.Board;
import mx.unison.pong.Paddle;

public class IncreasePaddlePowerUp extends PowerUp {

    public IncreasePaddlePowerUp(double x, double y) {
        super(x, y, "PWRincreasepaddle");
    }

    @Override
    public void applyEffect(Board board, BallRunner ballRunner, Paddle paddle) {
        paddle.expandWidth(120); // Aumenta el ancho del paddle
    }
}
