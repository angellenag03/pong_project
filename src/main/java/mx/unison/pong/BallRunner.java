package mx.unison.pong;

import java.awt.geom.Ellipse2D;

public class BallRunner implements Runnable {

    public static final int MAX_X = 640;
    public static final int MAX_Y = 480;
    public static final int SIGN = -1;

    public static final int DX = 10;
    public static final int DY = 10;
    private Ellipse2D.Double ball;

    private Paddle paddle;

    private int ballX;
    private int ballY;

    private boolean isPaused ;

    public BallRunner( Ellipse2D.Double shape, Paddle p) {
        super();
        ball = ( Ellipse2D.Double) shape;
        paddle = p;
        ballX = 320;
        ballY = 240;
        ball.x = ballX ;
        ball.y = ballY;
        isPaused = false;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    @Override
    public void run() {
        int directionY = 1;
        int directionX = 1;

        while( true ) {

            if( isPaused ) {
               Thread.yield();
                continue;
            }

            int y = (int) ball.getMaxY();
            int x = (int)  ball.getX();

            if( paddle.instersects(ball) ) {
                directionY = directionY * SIGN;
               // sX = sX * SIGN;
                ballX = ballX + (DX * directionX) ;
                ballY = ballY + (DY * directionY);
                ball.x = ballX ;
                ball.y = ballY;
                continue;
            }

            if( ball.getMinY() < 0 ) {
                directionY = directionY * SIGN;
            }

            if( ball.getMinX() < 0 ) {
                directionX = directionX * SIGN;
            }
            if( ball.getMaxY() > MAX_Y ) {
                directionY = directionY * SIGN;
            }

            if( ball.getMaxX() > MAX_X ) {
                directionX = directionX * SIGN;
            }
            ballX = ballX + (DX * directionX) ;
            ballY = ballY + (DY * directionY);
            ball.x = ballX ;
            ball.y = ballY;
            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
