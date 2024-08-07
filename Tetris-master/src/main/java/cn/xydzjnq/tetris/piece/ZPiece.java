package cn.xydzjnq.tetris.piece;

import java.util.Arrays;

import static cn.xydzjnq.tetris.Main1Activity.BOARDCULUMN;

public class ZPiece extends Piece {
    private String shape = "Z";
    private int[][] pieceArrays = new int[][]{
            {
                    1, 1, 0, 0,
                    0, 1, 1, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
            },
            {
                    0, 1, 0, 0,
                    1, 1, 0, 0,
                    1, 0, 0, 0,
                    0, 0, 0, 0
            }
    };

    @Override
    public int[] getPieceArray() {
        return pieceArrays[state];
    }

    @Override
    public int[] getSimplePieceArray() {
        initState();
        initalRow = 3;
        return Arrays.copyOf(pieceArrays[state], 8);
    }

    @Override
    public int[] nextStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public int[] previousStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public boolean isCollision(int culumn) {
        switch (state) {
            default:
                return true;
            case 0:
                return culumn < 1 || culumn > BOARDCULUMN - 2;
            case 1:
                return culumn < 1 || culumn > BOARDCULUMN - 1;
        }
    }

    @Override
    public String getShape() {
        return shape;
    }

    private void initState() {
        state = 0;
    }
}
