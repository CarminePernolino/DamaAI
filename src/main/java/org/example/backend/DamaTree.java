package org.example.backend;

import java.util.Arrays;
import java.util.Vector;

public class DamaTree {
    // MASSIMA PROFONDITÀ DI COSTRUZIONE DELL'ALBERO
    public int score; // Punteggio associato alla mossa corrente.
    public int[][] matrix; // Stato della scacchiera corrente.

    private Vector<DamaTree> sons = new Vector(); // Contiene i nodi figlio dell'albero di ricerca.
    private int turn; // Rappresenta il turno corrente (0 per il giocatore umano, 1 per il computer).

    /**
     * Questo costruttore crea l'albero di ricerca e valuta le possibili mosse fino alla profondità specificata
     * utilizzando l'algoritmo MIN-MAX.
     */
    public DamaTree(int[][] matrix, int depth, int turn){
        this.matrix = matrix;
        this.turn = turn;
        if(depth == 0){ // Controlla se la profondità dell'albero di ricerca è pari a 0
            score = Dama.Valute(matrix); // Valuta la mossa
            return;
        }
        int color;

        /**
         * Calcola il colore del giocatore corrente in base al turno e alla profondità dell'albero.
         * Se è il turno del giocatore umano (turn == 0), il colore sarà 0 per il bianco e 1 per il nero;
         * altrimenti, sarà il contrario. Questo calcolo è utile per determinare quali mosse considerare durante
         * la creazione dell'albero di ricerca.
         */
        if (turn == 0)
            color = depth % 2 == 0 ? 1 : 0; // 0 se è il turno del bianco; 1 se è turno del computer
        else
            color = depth % 2 == 0 ? 0 : 1;

        MovesEngine me = new MovesEngine(matrix, color); // Crea un oggetto MovesEngine per generare le possibili
        // mosse disponibili per il giocatore corrente sulla scacchiera corrente.

        /**
         * CREAZIONE ALBERO DI RICERCA
         * Per ogni mossa possibile generata dall'oggetto MovesEngine, crea un nuovo nodo figlio dell'albero di
         * ricerca DamaTree con la matrice risultante dalla mossa, decrementando la profondità di ricerca e
         * mantenendo lo stesso turno.
         */
        for(Move m : me.PossibleBestMooves){
            sons.add(new DamaTree(m.FinalMatrix, depth-1, turn));
        }

        /**
         * APPLICAZIONE ALGORITMO MIN-MAX
         * cercano di determinare il punteggio migliore tra i nodi figlio dell'albero di ricerca, massimizzando o
         * minimizzando il punteggio a seconda del turno del giocatore corrente (computer o giocatore umano).
         */
        if (color == 1){
            this.score = Integer.MIN_VALUE; // Rappresenta il punteggio più basso possibile
            // cerco il MAX e lo metto nel mio score
            for (DamaTree s : sons)
                if (s.score > this.score)
                    this.score = s.score;

        }
        else {
            this.score = Integer.MAX_VALUE; // Rappresenta il punteggio più alto possibile
            // cerco il MIN e lo metto nello score
            for (DamaTree s : sons)
                if (s.score < this.score)
                    this.score = s.score;

        }
    }

    /**
     * Restituisce la mossa migliore calcolata dall'algoritmo di ricerca. Se non ci sono mosse disponibili,
     * solleva un'eccezione.
     * Se ci sono mosse disponibili, restituisce la mossa con il punteggio più alto (per il computer) o più
     * basso (per il giocatore umano) tra i nodi figlio dell'albero di ricerca.
     *
     * @return
     * @throws UnsupportedOperationException
     */
    public int[][] ExecuteBestMove() throws UnsupportedOperationException{
        int matrix[][] = new int[8][8]; // Memorizza la configurazione della scacchiera della mossa migliore.

        if (sons.size() == 0) // Controlla se non ci sono mosse disponibili
            throw new UnsupportedOperationException("No moves available for PC! "
                    + (turn == 0 ? "WHITE" : "BLACK")
                    + " Wins!");

        for (DamaTree s : sons)
            if (s.score == this.score){ // Identifica la mossa migliore tra i nodi figlio.
                for(int i = 0; i < 8; i++)
                    matrix[i] = Arrays.copyOf(s.matrix[i],8);

                return matrix;
            }
        return matrix;
    }
}
