package powerups;

import mx.unison.pong.BallRunner;
import mx.unison.pong.Board;
import mx.unison.pong.Paddle;

public class TripleBallPowerUp extends PowerUp {

    public TripleBallPowerUp(double x, double y) {
        super(x, y, "PWRtripleball");
    }

    @Override
    public void applyEffect(Board board, BallRunner ballRunner, Paddle paddle) {
        board.spawnExtraBalls(); // Invoca 2 bolas adicionales en el tablero
    }
}