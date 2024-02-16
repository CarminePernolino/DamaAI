package org.example.gui;

import javax.swing.*;

public class Form extends JFrame {
    private int SQ_DIM = 80;
    public JLabel statusBar = new JLabel();
    private Board b = new Board(statusBar);

    public Form(String txt) {
        super(txt);
        setLocation(300, 15);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(this.SQ_DIM * 8, this.SQ_DIM * 8 + 27);
        setResizable(false);

        b.Reset_Game();

        this.getContentPane().add(b);
    }
}
