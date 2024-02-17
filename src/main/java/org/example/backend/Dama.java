package org.example.backend;

import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

public class Dama {
    //Definizione Pedine e Dame
    public final static int BLANK = 0;
    public final static int BLACK = 1; // Pedina Nera
    public final static int WHITE = 2; // Pedina Bianca

    public final static int D_BLACK = 3; // Dama Nera
    public final static int D_WHITE = 4; // Dama Bianca

    public static int MAX_DEPTH = 6;

    private int matrix[][];

    public Dama() {
        /**
         * Inizializza la matrice con tutte le pedine
         * -> if ((riga % 2) == (col % 2)): controlla se la somma degli indici di riga e colonna è pari o dispari.
         *    Se la somma è pari, significa che ci troviamo su una casella nera della scacchiera.
         */
        this.matrix = new int[8][8];
        for(int riga = 0; riga<8; riga++)
            for(int col = 0; col<8; col++)
                if ((riga % 2) == (col % 2)) {
                    if (riga<3)
                        this.matrix[riga][col] = BLACK;
                    else if (riga>4)
                        this.matrix[riga][col] = WHITE;
                    else
                        this.matrix[riga][col] = BLANK;
                } else
                    this.matrix[riga][col] = BLANK;
    }

    public int[][] CopyMatrix() {
        int [][] newM = new int[8][8];
        for(int i = 0; i < 8; i++){
            newM[i] = Arrays.copyOf(matrix[i],8);
        }
        return newM;
    }

    /**
     * Restituisce un vettore (Vector) di oggetti di tipo Move, che rappresentano le mosse valide.
     * @param color colore per il quale si desiderano calcolare le mosse valide 0-White 1-Black
     * @return
     */
    public Vector<Move> RetriveMoves(int color){
        MovesEngine me = new MovesEngine(matrix, color);
        return me.PossibleBestMooves;
    }

    /**
     * Cerca una sequenza di mosse all'interno di un vettore di mosse.
     * Se trova una corrispondenza, sostituisce la matrice corrente con la matrice finale della mossa trovata e
     * restituisce true. Se non trova corrispondenze, restituisce false.
     *
     * @param m
     * @param p
     * @return
     */
    public boolean CheckMoveAndExecute(Vector<Move> m, Vector<Point> p){
        for(Move move : m)
            if (move.Sequence.equals(p)) {
                for(int i = 0; i < 8; i++)
                    this.matrix[i] = Arrays.copyOf(move.FinalMatrix[i],8);
                return true;
            }
        return false;
    }

    /*
    turn  = 0 -> Muove il black
    turn  = 1 -> Muove il white
     */

    /**
     * Calcola e esegue la mossa migliore per il computer, utilizzando un oggetto DamaTree per calcolare le possibili
     * mosse e quindi eseguendo la mossa migliore sulla matrice corrente del gioco.
     * Successivamente, libera la memoria utilizzata dall'oggetto DamaTree.
     *
     * @param turn
     * @throws UnsupportedOperationException
     */
    public void ExecutePCMove(int turn) throws UnsupportedOperationException{
        DamaTree dt = new DamaTree(this.matrix, MAX_DEPTH, turn);
        this.matrix = dt.ExecuteBestMove(); // Restituisce una nuova matrice che rappresenta la mossa migliore
        // calcolata dall'albero delle mosse
        dt = null;
        System.gc();
    }

    /**
     * FUNZIONE DI VALUTAZIONE
     * Valuta lo stato del gioco rappresentato dalla matrice di input e restituisce un punteggio in base alla
     * distribuzione dei pezzi sulla scacchiera, tenendo conto della posizione dei pezzi e aggiungendo una
     * componente casuale al punteggio finale.
     *
     * @param matrix
     * @return
     */
    public static int Valute(int[][] matrix){
        int score = 0;
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                if (matrix[row][col] == WHITE) {
                    score -= 100;
                    score -= 1 * (7-row) * (7-row);
                }
                else if (matrix[row][col] == D_WHITE){
                    score -= 200;

                    if (row == 0 || row == 7)		//check  WHITE
                        score += 10;

                    if (col == 0 || col == 7)
                        score += 10;
                }
                else if (matrix[row][col] == D_BLACK) {
                    score += 200;

                    if (row == 0 || row == 7)		//check BLACK
                        score -= 10;

                    if (col == 0 || col == 7)
                        score -= 10;
                }
                else if (matrix[row][col] == BLACK) {
                    score += 100;
                    score += 1 * row * row;
                }

        score += (int)(Math.random() * 10); // Potrebbe introdurre una componente casuale nel punteggio totale.
        return score;
    }

    /**
     * Valuta una posizione di gioco sulla scacchiera, assegnando un punteggio in base alla distribuzione dei pezzi
     * e alla loro posizione sulla scacchiera.
     *
     * @param position
     * @return
     */
    public static int EvalPrima(int[][] position) {

        final int CHECKER = 100; // Peso di una pedina
        final int POS = 1; // Valore di una posizione
        final int KING = 200; // Peso di una dama
        int score = 0; // Azzero la variabile dove salvare il punteggio
        int[][] posKW = new int[12][2]; // Memorizza le posizioni delle dame bianche
        int[][] posKB = new int[12][2]; // Memorizza le posizioni delle dame nere
        int nKW = 0, nKB = 0; // Tengono traccia del numero di dame bianche e nere

        // Scorro la board per valutarla
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                if (position[row][col] == WHITE) {
                    score -= CHECKER;
                    score -= POS * row * row;
                    score -= supportoWHITE(position, row, col);
                } else if (position[row][col] == BLACK) {
                    score += CHECKER;
                    score += POS * (7 - row) * (7 - row);
                    score += supportoBLACK(position, row, col);
                } else if (position[row][col] == D_WHITE) {
                    posKW[nKW][0] = row;
                    posKW[nKW][1] = col;
                    nKW++;
                    score -= KING;
                    if (row == 0 || row == 7)
                        score += 10;
                    if (col == 0 || col == 7)
                        score += 10;
                } else if (position[row][col] == D_BLACK) {
                    posKB[nKB][0] = row;
                    posKB[nKB][1] = col;
                    nKB++;
                    score += KING;
                    if (row == 0 || row == 7)
                        score -= 10;
                    if (col == 0 || col == 7)
                        score -= 10;
                }

            }
        return score;
    }

    /**
     * Calcola il supporto per una pedina nera sulla scacchiera controllando le sue mosse possibili in avanti e
     * calcolando un punteggio basato sulle mosse disponibili.
     * La ricorsione viene utilizzata per esplorare tutte le mosse possibili della pedina nera.
     *
     * @param position
     * @param i
     * @param j
     * @return
     */
    public static int supportoBLACK(int[][] position, int i, int j){
        int score =0;
        int tmp=0;

        // Viene controllato se la cella corrente si trova all'interno della scacchiera e se quelle adiacenti
        // contengono pedine nere
        // Se le condizioni sono soddisfatte, il metodo prosegue eseguendo una ricorsione per esplorare le mosse
        // successive della pedina nera.
        if(!inRange(i,j) || (( inRange(i+1,j-1) && position[i+1][j-1] != BLACK ) &&
                (inRange(i+1,j+1) && position[i+1][j+1] != BLACK)))
            if(i==8)
                return 0;
            else
                return -1;
        else{
            tmp=supportoBLACK(position, i+1, j-1);
            if(tmp!=-1)
                score+=tmp+2;
            tmp=supportoBLACK(position, i+1, j+1);
            if(tmp!=-1)
                score+=tmp+2;
        }
        return score;
    }

    /**
     * Calcola il supporto per una pedina bianca sulla scacchiera controllando le sue mosse possibili in avanti e
     * calcolando un punteggio basato sulle mosse disponibili.
     * La ricorsione viene utilizzata per esplorare tutte le mosse possibili della pedina bianca.
     *
     * @param position
     * @param i
     * @param j
     * @return
     */
    public static int supportoWHITE(int[][] position, int i, int j){
        int score =0;
        int tmp=0;

        // Viene controllato se la cella corrente si trova all'interno della scacchiera e se quelle adiacenti
        // contengono pedine bianche
        // Se le condizioni sono soddisfatte, il metodo prosegue eseguendo una ricorsione per esplorare le mosse
        // successive della pedina bianca.
        if(!inRange(i,j) || (( inRange(i-1,j-1) && position[i-1][j-1] != WHITE ) && (inRange(i-1,j+1) && position[i-1][j+1] != WHITE)))
            if(i==-1)
                return 0;
            else
                return -1;
        else{
            tmp=supportoWHITE(position, i-1, j-1);
            if(tmp!=-1)
                score+=tmp+2;
            tmp=supportoWHITE(position, i-1, j+1);
            if(tmp!=-1)
                score+=tmp+2;
        }
        return score;
    }

    /**
     * Controlla se le coordinate fornite (i, j) si trovano all'interno dei limiti di una scacchiera 8x8 e restituisce
     * true se sono all'interno di tali limiti e false altrimenti.
     * Viene utilizzata per verificare se una determinata posizione è all'interno della scacchiera.
     *
     * @param i
     * @param j
     * @return
     */
    public static boolean inRange(int i, int j){
        return (i>-1 && i<8 && j>-1 && j<8);
    }
}
