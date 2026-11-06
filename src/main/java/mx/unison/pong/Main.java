package mx.unison.pong;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        JFrame f = new JFrame("Frontenis");

        f.setResizable(false);

        JPanel p = new JPanel(new BorderLayout());
        Board board = new Board();
        p.add(board, BorderLayout.CENTER);

        f.addKeyListener( board );
        f.setContentPane(p);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}