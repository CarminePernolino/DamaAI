package org.example.backend;

import java.awt.*;
import java.util.Vector;

public class Move {
    public Vector<Point> Sequence;
    public int[][] FinalMatrix;
    public Move(Vector<Point> seq, int[][] m){
        this.Sequence = seq;
        CheckForNewDame(m);
        this.FinalMatrix = m;
    }

    /**
     * Controlla se ci sono pedine bianche o nere nella prima o nell'ultima riga della scacchiera e le promuove a
     * dame se soddisfano i criteri di promozione.
     * @param matrix
     */
    private static void CheckForNewDame(int[][] matrix) {
        for(int col = 0; col < 8; col++) {
            if (matrix[0][col] == Dama.WHITE) // check dame bianche
                matrix[0][col] = Dama.D_WHITE;

            if (matrix[7][col] == Dama.BLACK) // check dame nere
                matrix[7][col] = Dama.D_BLACK;
        }
    }
}
