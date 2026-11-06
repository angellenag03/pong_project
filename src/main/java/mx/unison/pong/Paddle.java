package mx.unison.pong;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

public class Paddle {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    private final int[] xPositions = { 0,80,160,240,320,400,480,560};
    private final Rectangle2D.Double paddle;
    private int pos ;

    public Paddle() {
        super();
        pos = 4; // raqueta inicia al centro
        paddle = new Rectangle2D.Double(xPositions[pos],450,WIDTH,HEIGHT);
    }

    public synchronized void moveRight() {
        if( pos < 7 ) {
            pos++;
            paddle.x = xPositions[pos];
        }
    }

    public synchronized void moveLeft() {
        if( pos > 0 ) {
            pos--;
            paddle.x = xPositions[pos];
        }
    }

    // regresa el rectangulo que se va a dibujar
    public Rectangle2D.Double getPaddle() {
        return paddle;
    }

    // verifica si la pelota toco la raqueta
    public boolean check( int x, int y) {
        return paddle.contains(x,y);
    }

    // verifica si la pelota toco la raqueta
    public boolean intersects(RectangularShape shape) {
        boolean result;

        result = paddle.intersects( shape.getBounds2D() );

        return result;
    }
}
