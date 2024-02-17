package org.example.gui;

import org.example.backend.Dama;
import org.example.backend.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

public class Board extends JPanel {
    private static int SQ_DIM = 80; // Dimensione cella scacchiera
    private static int IN_R = 20; // Dimensione della pedina di gioco
    private Dama Dama;
    private int[][] m = new int[8][8];
    private Vector<Move> LegalMoves = null;
    private Vector<Point> current_m_coo = new Vector<Point>();

    private Color d_brown = new Color(106,53,18);
    private Color l_brown = new Color(255,225,184);
    private ArrayList<Rectangle> coordinates_list = new ArrayList<>();
    private Point Start;
    private Point End;

    public Board(){
        super(true);
        setBackground(l_brown);
        setDoubleBuffered(true);


        //Inizializzo la lista delle coordinate per le correlazioni
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                if ((col % 2) == (row % 2))
                {
                    coordinates_list.add(new Rectangle(col * 80, row * 80, 80, 80));
                }

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for(Rectangle r : coordinates_list)
                        if(r.contains(e.getPoint())){
                            Start = new Point(r.y / SQ_DIM, r.x / SQ_DIM);
                            break;
                        }
                }
        });

        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ((m[Start.x][Start.y] == Dama.WHITE || m[Start.x][Start.y] == Dama.D_WHITE))
                    for (Rectangle r : coordinates_list)
                        if (r.contains(e.getPoint())){
                            End = new Point(r.y / SQ_DIM, r.x / SQ_DIM);
                            moveWhite();
                            break;
                        }
            }
        });
    }

    private void moveWhite(){
        if (Start.equals(End))
            return;

        /**
         * Controlla se ci sono mosse legali disponibili per le pedine nere nel gioco della dama.
         * Se non ce ne sono, il giocatore Nero vince.
         * Altrimenti, le coordinate del movimento delle pedine vengono memorizzate nel vettore current_m_coo.
         */
        if (current_m_coo.isEmpty()){
            LegalMoves = Dama.RetriveMoves(0);
            if (LegalMoves.size() == 0){
                JOptionPane.showMessageDialog(null,"No moves available! BLACK Wins!");
                Reset_Game();
                return;
            }
            current_m_coo.add(Start);
            current_m_coo.add(End);
        }
        else {
            current_m_coo.add(End);
        }

        /**
         * Gestisce il movimento delle pedine nel gioco della dama, esegue controlli di validità delle mosse,
         * aggiorna lo stato del gioco e gestisce eventuali errori durante il processo.
         */
        if (Dama.CheckMoveAndExecute(LegalMoves,current_m_coo)){
            m = Dama.CopyMatrix();
            System.gc(); // Richiamo al Garbage Collector
            current_m_coo.clear();
            this.repaint();
            try {
                Dama.ExecutePCMove(0);
            } catch (UnsupportedOperationException e) {
                JOptionPane.showMessageDialog(null,e.toString().split(":")[1]);
                Reset_Game();
                return;
            }
            m = Dama.CopyMatrix();
            this.repaint();
            System.gc();
            CheckWins();
            return;
        }

        /**
         * Gestisce il movimento delle pedine nel gioco della dama, verifica se una mossa è valida e aggiorna
         * lo stato del gioco di conseguenza, includendo la possibilità di promozione delle pedine a dame bianche.
         */
        for(Move mo : LegalMoves)
            if (Check(mo.Sequence, current_m_coo)){
                if (m[End.x][End.y] == Dama.BLANK)
                {
                    m[End.x][End.y] = m[Start.x][Start.y];	//switch pedina
                    m[Start.x][Start.y] = Dama.BLANK;

                    if (Math.abs(Start.x - End.x) != 1){
                        m[Start.x + ((End.x - Start.x) / 2)][Start.y + ((End.y - Start.y)/2)] = Dama.BLANK;
                    }

                }

                if (End.x == 0)
                    m[End.x][End.y] = Dama.D_WHITE;
                this.repaint();
                return;
            }

        /**
         *  Lo svuotamento del vettore quando contiene solo due coordinate serve a mantenere il vettore pulito
         *  e pronto per memorizzare le coordinate di nuovi movimenti delle pedine durante il gioco.
         */
        if (current_m_coo.size() == 2){
            current_m_coo.clear();
        }

        /**
         * A questo punto la mossa era illegale
         */
        JOptionPane.showMessageDialog(null, "Move not allowed!");
        return;

    }

    /**
     * Confronta due vettori di punti per determinare se contengono gli stessi punti nello stesso ordine.
     * Se i vettori sono uguali, il metodo restituisce true; altrimenti, restituisce false.
     * Controlla se le mosse che il giocatore vuole effettuare sono valide o meno.
     * @param LegalM
     * @param CurrM
     * @return
     */
    private boolean Check (Vector<Point> LegalM, Vector<Point> CurrM){
        for (int i = 0; i < CurrM.size(); i++)
            if (!LegalM.elementAt(i).equals(CurrM.elementAt(i)))
                return false;
        return true;
    }

    /**
     * Reimposta tutte le variabili e gli oggetti associati al gioco della dama alla loro condizione iniziale,
     * preparando così il gioco per un nuovo round o una nuova partita.
     */
    public void     Reset_Game(){
        this.Dama = new Dama();
        this.m = Dama.CopyMatrix();
        current_m_coo.clear();
        LegalMoves = null;
    }

    /**
     * controlla il numero di pedine nere e bianche sulla scacchiera e determina se uno dei giocatori ha vinto la
     * partita in base a ciò.
     * Se uno dei giocatori ha esaurito tutte le sue pedine, viene visualizzato un messaggio di vittoria
     * corrispondente e il gioco viene resettato.
     */
    private void CheckWins() {
        int numBLACK = 0, numWHITE = 0;
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
            {
                if (m[row][col] == Dama.WHITE || m[row][col] == Dama.D_WHITE)
                    numWHITE++;
                else
                if (m[row][col] == Dama.BLACK || m[row][col] == Dama.D_BLACK)
                    numBLACK++;
            }

        if (numBLACK == 0){
            JOptionPane.showMessageDialog(null, "No moves available for PC! WHITE Wins!");
            Reset_Game();
        }

        if (numWHITE == 0){
            JOptionPane.showMessageDialog(null, "No moves available for PC! BLACK Wins!");
            Reset_Game();
        }
    }

    public void paintComponent(Graphics g){
        int x, y; // per memorizzare le coordinate x e y di ogni cella della scacchiera o delle pedine.
        setDoubleBuffered(true);
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); // Imposta l'antialiasing
        // per migliorare la qualità del rendering, rendendo i bordi delle forme più lisci e meno "scolpiti".

        // disegno la scacchiera
        g2.setColor(d_brown);
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                if((row % 2) == (col % 2))
                    g2.fillRect(col * SQ_DIM, row * SQ_DIM, SQ_DIM, SQ_DIM);

        // disegno le pedine
        // Se il valore è 1, viene disegnata una pedina nera.
        // Se il valore è 2, viene disegnata una pedina bianca.
        // Se il valore è 3, viene disegnata una dama nera con una corona rossa.
        // Se il valore è 4, viene disegnata una dama bianca con una corona grigia.
        for(int i = 0; i < 8; i++)
            for(int j = 0; j < 8; j++)
                switch (m[i][j]){
                    case 0:
                        break;
                    case 1:
                        g2.setColor(Color.BLACK);
                        g2.fillOval(j * SQ_DIM + 5, i * SQ_DIM + 5, SQ_DIM - 10, SQ_DIM - 10);
                        break;
                    case 2:
                        g2.setColor(new Color(224, 226, 213));
                        g2.fillOval(j * SQ_DIM + 5, i * SQ_DIM + 5, SQ_DIM - 10, SQ_DIM - 10);
                        break;
                    case 3:
                        x = j * SQ_DIM;
                        y = i * SQ_DIM;
                        g2.setColor(Color.BLACK);
                        g2.fillOval(x + 5, y + 5, SQ_DIM - 10, SQ_DIM - 10);
                        g2.setColor(Color.RED);
                        g2.fillOval(x + IN_R, y + IN_R, 2 * IN_R, 2 * IN_R);
                        break;
                    case 4:
                        x = j * SQ_DIM;
                        y = i * SQ_DIM;
                        g2.setColor(new Color(224, 226, 213));
                        g2.fillOval(x + 5, y + 5, SQ_DIM - 10, SQ_DIM - 10);
                        g2.setColor(Color.DARK_GRAY);
                        g2.fillOval(x + IN_R, y + IN_R, 2 * IN_R, 2 * IN_R);
                        break;
                }
    }
}
