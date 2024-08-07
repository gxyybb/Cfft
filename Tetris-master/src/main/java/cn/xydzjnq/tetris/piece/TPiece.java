package cn.xydzjnq.tetris.piece;

import java.util.Arrays;
import java.util.Random;

import static cn.xydzjnq.tetris.Main1Activity.BOARDCULUMN;

public class TPiece extends Piece {
    private String shape = "T";
    private int[][] pieceArrays = new int[][]{
            {
                    1, 1, 1, 0,
                    0, 1, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
            },
            {
                    0, 0, 1, 0,
                    0, 1, 1, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 0
            },
            {
                    0, 0, 0, 0,
                    0, 1, 0, 0,
                    1, 1, 1, 0,
                    0, 0, 0, 0
            },
            {
                    1, 0, 0, 0,
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
        int[] simpleBlockShape = new int[8];
        switch (state) {
            case 0:
                initalRow = 3;
                simpleBlockShape = Arrays.copyOf(pieceArrays[state], 8);
                break;
            case 2:
                initalRow = 2;
                simpleBlockShape = Arrays.copyOfRange(pieceArrays[state], 4, 12);
                break;
        }
        return simpleBlockShape;
    }

    @Override
    public int[] nextStatePieceArray() {
        switch (state) {
            case 0:
                state = 1;
                break;
            case 1:
                state = 2;
                break;
            case 2:
                state = 3;
                break;
            case 3:
                state = 0;
                break;
        }
        return getPieceArray();
    }

    @Override
    public int[] previousStatePieceArray() {
        switch (state) {
            case 0:
                state = 3;
                break;
            case 1:
                state = 0;
                break;
            case 2:
                state = 1;
                break;
            case 3:
                state = 2;
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
                return culumn < 0 || culumn >= BOARDCULUMN - 2;
            case 2:
                return culumn < 1 || culumn > BOARDCULUMN - 2;
            case 3:
                return culumn < 1 || culumn > BOARDCULUMN - 1;
        }
    }

    @Override
    public String getShape() {
        return shape;
    }

    private void initState() {
        Random random = new Random();
        int randomInt = random.nextInt(2);
        switch (randomInt) {
            case 0:
                state = 0;
                break;
            case 1:
                state = 2;
                break;
        }
    }
}
