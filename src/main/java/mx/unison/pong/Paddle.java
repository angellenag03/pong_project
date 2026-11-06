package mx.unison.pong;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

public class Paddle {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    private final int[] xPositions = { 0,80,160,240,320,400,480,560};
    private Rectangle2D.Double paddle;
    private int pos = 0;

    public Paddle() {
        super();
        pos = 4;
        paddle = new Rectangle2D.Double(xPositions[pos],450,WIDTH,HEIGHT);

    }

    public void moveRight() {
        if( pos < 7 ) {
            pos++;
            paddle.x = xPositions[pos];
        }
    }

    public void moveLeft() {
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

    public boolean instersects(RectangularShape shape) {
        boolean result = false;

        result = paddle.intersects(shape.getBounds2D());

        return result;
    }
}
