package org.example.backend;

import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

public class MovesEngine {
    // if BLACK: 0, if WHITE: 1;
    private int color;


    // Max Moves
    private int MaxEaten = 0; // Tiene traccia del massimo numero di pezzi mangiati durante una mossa.
    private String BestSeq = new String(); // Memorizzare la sequenza migliore di mosse.
    private int MaxType; // 0= pedina ; 1=Dama
    public Vector<Move> PossibleBestMooves = new Vector(); // Memorizza le possibili migliori mosse disponibili.

    // End Max Mooves

    /**
     * Questo costruttore inizializza un oggetto MovesEngine con una matrice e un colore specifici, e poi calcola
     * le mosse disponibili per tutti i pezzi della stessa colorazione all'interno della matrice fornita.
     *
     * @param matrix
     * @param color
     */
    public MovesEngine(int [][] matrix, int color){
        this.color = color;

        for(int row = 0; row < 8; row++)
            for(int col = 0; col < 8; col++)
                if (matrix[row][col] != Dama.BLANK && matrix[row][col] % 2 == this.color) { // verifica se la cella
                    // contiene un pezzo e se quel pezzo ha lo stesso colore specificato nell'oggetto MovesEngine.
                    CalculateMoves(matrix, new Point(row,col), new Vector<Point>(), new String()); // Calcolare le
                    // mosse disponibili per il pezzo nella posizione attuale.
                }
    }

    /**
     * Verifica se le coordinate fornite (x, y) si trovano all'interno dei limiti di una scacchiera 8x8 e
     * restituisce true se sono all'interno di tali limiti e false altrimenti.
     * Viene utilizzata per verificare se una determinata posizione è all'interno della scacchiera prima di
     * eseguire determinate operazioni su di essa.
     *
     * @param x
     * @param y
     * @return
     */
    private boolean CheckCoordinates(int x, int y){
        return ((x > -1 && x < 8) && ((y > -1) && (y < 8)));
    }

    /**
     * Confronta due stringhe contando il numero di occorrenze del carattere 'd' in ciascuna di esse e restituisce un
     * valore intero che indica il risultato del confronto tra le occorrenze.
     *
     * @param s
     * @return
     */
    private int CompareValue(String s){
        int occ1 = 0, occ2 = 0; // Terranno traccia del numero di occorrenze del carattere 'd' all'interno delle due stringhe.
        for(int i = 0; i< s.length(); i++){
            if (s.charAt(i) == 'd')
                occ1++;
            if (this.BestSeq.charAt(i) == 'd')
                occ2++;
        }
        // Per confrontare il numero di occorrenze di 'd' nelle due stringhe e restituire:
        // -> -1 se occ1 è minore di occ2.
        // -> 0 se occ1 è uguale a occ2.
        // -> 1 se occ1 è maggiore di occ2.
        return Integer.signum(occ1 - occ2);
    }

    /**
     * Confronta le mosse in base a diversi criteri (numero di pezzi mangiati, tipo di pezzo mangiato e sequenza
     * di mosse) e restituisce un valore intero che indica il risultato del confronto.
     *
     * @param type
     * @param eaten
     * @param seq
     * @return
     */
    private int MovesCompare(int type,  int eaten, String seq){
        // Viene effettuato un controllo per verificare se il numero di pezzi mangiati e il massimo numero di pezzi
        // mangiati sono entrambi 0
        if (eaten == 0 && MaxEaten == 0)
            return 0;

        // Se il numero di pezzi mangiati è uguale al massimo numero di pezzi mangiati
        if (eaten == this.MaxEaten){
            // Se il tipo di pezzo mangiato è uguale al tipo massimo di pezzo mangiato
            if (type == this.MaxType){
                switch (CompareValue(seq)) {
                    case 0:
                        return seq.compareToIgnoreCase(this.BestSeq); // Confronta le due stringhe ignorando il caso.
                        // Restituisce:
                        // -> 0 se le sequenze di mosse sono identiche.
                        // -> 1 se la sequenza seq è "maggiore" (alfabeticamente) di BestSeq.
                        // -> -1 se la sequenza seq è "minore" (alfabeticamente) di BestSeq.
                    case 1: return 1;
                    case -1: return -1;
                }
            }
            else if(type > this.MaxType)
                return 1;
            else
                return -1;
        } else if (eaten > this.MaxEaten)
            return 1;

        return -1;
    }

    /**
     * Registra le mosse effettuate durante il gioco, tenendo traccia della sequenza migliore di mosse fino a quel
     * momento e aggiornando le informazioni se una nuova sequenza risulta essere più conveniente o equipotenziale
     * rispetto alla sequenza attualmente registrata.
     *
     * @param finalMatrix
     * @param MCoord
     * @param isDama
     * @param seq
     */
    private void RegisterMoves(int[][] finalMatrix, Vector<Point> MCoord, boolean isDama, String seq) {
        // Registro le mosse solo se sono più convenienti oppure se sono esattamente equipotenziali

        // Confrontare le mosse appena eseguite con le mosse registrate precedentemente.
        switch (MovesCompare(isDama ? 1 : 0, seq.length(), seq)) {
            case 1: // Le mosse appena eseguite sono più convenienti delle mosse precedentemente registrate.
                this.MaxEaten = seq.length();
                this.MaxType = isDama ? 1 : 0;
                this.BestSeq = seq;
                PossibleBestMooves.clear();
                PossibleBestMooves.add(new Move(MCoord, finalMatrix));
                break;
            case 0: // Le mosse appena eseguite sono equipotenziali rispetto a quelle precedentemente registrate.
                PossibleBestMooves.add(new Move(MCoord, finalMatrix));
                break;
        }

    }

    /**
     * Crea una copia della matrice data e applica gli aggiornamenti necessari per rappresentare una mossa specifica,
     * come il movimento di un pezzo da una posizione all'altra e la rimozione di un pezzo mangiato, se presente.
     *
     * @param m
     * @param source
     * @param dest
     * @param eaten
     * @return
     */
    private static int[][] CloneMatrixAndUpdate( int[][] m, Point source,  Point dest, Point eaten){
        int [][] newM = new int[8][8];

        for(int row = 0; row < 8; row++){
            newM[row] = Arrays.copyOf(m[row],8);
        }

        newM[dest.x][dest.y] = newM[source.x][source.y]; // Rappresenta il pezzo spostato nella sua nuova posizione

        if (eaten != null) // Se c'è stato un pezzo mangiato durante la mossa
            newM[eaten.x][eaten.y] = Dama.BLANK; // Rappresenta una cella vuota

        newM[source.x][source.y] = Dama.BLANK; // Rappresenta una cella vuota, poiché il pezzo è stato spostato dalla
        // sua posizione originale.

        return newM;
    }

    /**
     * Esamina tutte le possibili mosse disponibili per il pezzo in posizione c, tenendo conto delle regole del
     * gioco delle dama, e registra queste mosse, inclusi i movimenti e le mangiate, aggiungendoli alla lista
     * delle mosse possibili.
     *
     * @param matrix
     * @param c
     * @param PrevC
     * @param score
     */
    private void CalculateMoves(int[][] matrix, Point c, Vector<Point> PrevC, String score){
        // Se il valore è maggiore di 2 (che è il valore assegnato alle dame), allora la variabile imDama sarà true,
        // altrimenti sarà false.
        boolean imDama = matrix[c.x][c.y] > 2;

        // Se la dimensione di PrevC è 0, significa che il pezzo non ha mosse precedenti,quindi può muoversi.
        // In questo caso, la variabile canMove sarà true, altrimenti sarà false.
        boolean canMove = PrevC.size() == 0;

        // Se il pezzo è una pedina nera (il cui valore nella matrice è dispari), la direzione sarà verso l'alto,
        // quindi dir sarà -1. Altrimenti, se il pezzo è una pedina bianca (il cui valore nella matrice è pari),
        // la direzione sarà verso il basso, quindi dir sarà 1.
        int dir = matrix[c.x][c.y] % 2 == 0 ? -1 : 1;

        int myType = matrix[c.x][c.y];
        PrevC.add(c); // PrevC tiene traccia del percorso delle mosse effettuate fino a quel momento.

        boolean stop = true;

        // Vado a vedere la casella verso destra nella direzione opportuna
        if (CheckCoordinates(c.x + dir, c.y+1)){

            // Posso spostarmi sulla posizione adiacente a destra
            if (matrix[c.x + dir][c.y + 1] == Dama.BLANK && canMove){
                Vector<Point> currC = new Vector<Point>(PrevC);
                Point newC = new Point(c.x + dir, c.y + 1);
                currC.add(newC);
                RegisterMoves(CloneMatrixAndUpdate(matrix, c, newC, null), currC, imDama, score);
                stop = false; // NON deve rimanere fermo nella sua posizione attuale.
            }
            // Se è occupata controllo se posso mangiare
            else if (matrix[c.x + dir][c.y + 1] % 2 != myType % 2 && CheckCoordinates(c.x+ 2*dir, c.y + 2) &&
                    matrix[c.x + 2*dir][c.y + 2] == Dama.BLANK && matrix[c.x + dir][c.y + 1] != Dama.BLANK){
                // Se quella è una dama e io sono una dama
                if (matrix[c.x + dir][c.y + 1] > 2 && myType > 2){
                    // MANGIO LA DAMA CON UNA DAMA
                    Point newPos = new Point(c.x + 2*dir, c.y + 2);
                    // La lettera "d" viene aggiunta alla stringa score per indicare una mangiata di dama.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x + dir, c.y +1)), newPos,
                            new Vector<Point>(PrevC), score.concat("d"));
                    stop = false;
                }
                // Altrimenti se è una pedina
                else if (matrix[c.x + dir][c.y + 1] < 3){
                    // MANGIO UNA PEDINA
                    Point newPos = new Point(c.x + 2*dir, c.y + 2);
                    // La lettera "p" viene aggiunta alla stringa score per indicare una mangiata di pedina.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x + dir, c.y +1)), newPos,
                            new Vector<Point>(PrevC), score.concat("p"));
                    stop = false;
                }
            }
        }

        // Vado a vedere la casella verso sinistra nella direzione opportuna
        if (CheckCoordinates(c.x + dir, c.y - 1)){

            // Posso spostarmi sulla cella adiacente a sinistra
            if (matrix[c.x + dir][c.y - 1] == Dama.BLANK && canMove){
                Vector<Point> currC = new Vector<Point>(PrevC);
                Point newC = new Point(c.x + dir, c.y - 1);
                currC.add(newC);
                RegisterMoves(CloneMatrixAndUpdate(matrix, c, newC, null), currC, imDama, score);
                stop = false; // NON deve rimanere fermo nella posizione attuale
            }
            // Se è occupata controllo se posso mangiare
            else if (matrix[c.x + dir][c.y - 1] % 2 != myType % 2 && CheckCoordinates(c.x+ 2*dir, c.y - 2) &&
                    matrix[c.x + 2*dir][c.y - 2] == Dama.BLANK && matrix[c.x + dir][c.y - 1] != Dama.BLANK){
                // Se quella è una dama e io sono una dama
                if (matrix[c.x + dir][c.y - 1] > 2 && myType > 2){
                    // MANGIO LA DAMA CON UNA DAMA
                    Point newPos = new Point(c.x + 2*dir, c.y - 2);
                    // La lettera "d" viene aggiunta alla stringa score per indicare una mangiata di dama.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x + dir, c.y -1)), newPos,
                            new Vector<Point>(PrevC), score.concat("d"));
                    stop = false;
                }
                // Altrimenti se è una pedina
                else if (matrix[c.x + dir][c.y - 1] < 3){
                    // MANGIO UNA PEDINA
                    Point newPos = new Point(c.x + 2*dir, c.y - 2);
                    // La lettera "p" viene aggiunta alla stringa score per indicare una mangiata di pedina.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x + dir, c.y -1)), newPos,
                            new Vector<Point>(PrevC), score.concat("p"));
                    stop = false;
                }
            }
        }

        // Se sono una dama posso guardare anche le mosse da poter fare nella direzione opposta alla mia
        if (imDama) {
            // Vado a vedere la casella verso destra nella direzione opportuna
            if (CheckCoordinates(c.x - dir, c.y+1)){

                // Posso spostarmi sulla destra
                if (matrix[c.x - dir][c.y + 1] == Dama.BLANK && canMove){
                    Vector<Point> currC = new Vector<Point>(PrevC);
                    Point newC = new Point(c.x - dir, c.y + 1);
                    currC.add(newC);
                    RegisterMoves(CloneMatrixAndUpdate(matrix, c, newC, null), currC, imDama, score);
                    stop = false; // NON devo rimanere fermo nella posizione attuale
                }
                // Se è occupata controllo se posso mangiare
                else if (matrix[c.x - dir][c.y + 1] % 2 != myType % 2 && CheckCoordinates(c.x- 2*dir, c.y + 2) &&
                        matrix[c.x - 2*dir][c.y + 2] == Dama.BLANK && matrix[c.x - dir][c.y + 1] != Dama.BLANK){
                    // MANGIO E IN BASE A COSA HO MANGIATO AGGIORNO LA STRINGA E LE EATEN
                    Point newPos = new Point(c.x - 2*dir, c.y + 2);
                    // La lettera "d" viene aggiunta alla stringa score per indicare una mangiata di dama.
                    // La lettera "p" viene aggiunta alla stringa score per indicare una mangiata di pedina.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x - dir, c.y +1)), newPos,
                            new Vector<Point>(PrevC), score.concat(matrix[c.x - dir][c.y + 1] > 2 ? "d" : "p"));
                    stop = false;
                }
            }

            // Vado a vedere la casella verso sinistra nella direzione opportuna
            if (CheckCoordinates(c.x - dir, c.y - 1)){

                // Posso spostarmi sulla sinistra
                if (matrix[c.x - dir][c.y - 1] == Dama.BLANK && canMove){
                    Vector<Point> currC = new Vector<Point>(PrevC);
                    Point newC = new Point(c.x - dir, c.y - 1);
                    currC.add(newC);
                    RegisterMoves(CloneMatrixAndUpdate(matrix, c, newC, null), currC, imDama, score);
                    stop = false; // NON devo rimanere fermo nella posizione attuale
                }
                // Se è occupata controllo se posso mangiare
                else if (matrix[c.x - dir][c.y - 1] % 2 != myType % 2 && CheckCoordinates(c.x- 2*dir, c.y - 2) &&
                        matrix[c.x - 2*dir][c.y - 2] == Dama.BLANK && matrix[c.x - dir][c.y - 1] != Dama.BLANK){
                    // MANGIO E IN BASE A COSA HO MANGIATO AGGIORNO LA STRINGA E LE EATEN
                    Point newPos = new Point(c.x - 2*dir, c.y - 2);
                    // La lettera "d" viene aggiunta alla stringa score per indicare una mangiata di dama.
                    // La lettera "p" viene aggiunta alla stringa score per indicare una mangiata di pedina.
                    CalculateMoves(CloneMatrixAndUpdate(matrix, c, newPos, new Point(c.x - dir, c.y -1)), newPos,
                            new Vector<Point>(PrevC), score.concat(matrix[c.x - dir][c.y - 1] > 2 ? "d" : "p"));
                    stop = false;
                }
            }
        }

        // Se sono semplicemente rimasta ferma, ma con almeno una mossa precedente (quindi una mangiata) registro
        // la mia posizione
        if (stop && PrevC.size() > 1){
            RegisterMoves(matrix, PrevC, imDama, score);
        }

    }
}
