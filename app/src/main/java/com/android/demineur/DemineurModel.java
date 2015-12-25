package com.android.demineur;

import java.util.Random;

public final class DemineurModel {

    /**
     * Min columns for a grid
     */
    public static final int MIN_WIDTH = 5;

    /**
     * Min rows for a grid
     */
    public static final int MIN_HEIGHT = 5;

    /**
     * Min number of mines
     */
    public static final int MIN_MINES = 2;

    /**
     * Max columns for a grid
     */
    public static final int MAX_WIDTH = 20;
    /**
     * Max rows for a grid
     */
    public static final int MAX_HEIGHT = 20;

    /**
     * Cell content for the grid
     */
    public enum Cell {
        MINE,
        EMPTY,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT
    }

    /**
     * Number of columns
     */
    private final int WIDTH;
    /**
     * Number of rows
     */
    private final int HEIGHT;

    /**
     * Number of mines
     */
    private final int MINES;

    /**
     * The grid
     */
    private Cell cells[][];

    /**
     * Array referencing discovered cells.
     * A true value means that the corresponding cell in the game grid has been discovered
     */
    private boolean discovered[][];

    /**
     * The number of discovered cells
     */
    private int countDiscoveredCells;

    /**
     * Array referencing marked cells.
     * A true value means that the corresponding cell in the game has been marked with a flag
     */
    private boolean marked[][];

    /**
     * The number of marked cells
     */
    private int countMarkedCells;

    /**
     * If true the player will mark cells instead of revealing them
     */
    private boolean flagMode;

    /**
     * True if the game has been lost, false otherwise
     */
    private boolean lost;

    /**
     * True if the game has been won, false otherwise
     */
    private boolean won;

    /**
     * Construct a new Minesweeper model
     * @param width : number of columns
     * @param height : number of rows
     * @param mines : number of mines
     */
    public DemineurModel(int width, int height, int mines) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.MINES = mines;
        discovered = new boolean[HEIGHT][WIDTH];
        marked = new boolean[HEIGHT][WIDTH];
        countDiscoveredCells = 0;
        countMarkedCells = 0;
        flagMode = false;
        lost = false;
        won = false;
    }

    /**
     * Construct a new Minesweeper model
     * @param width : number of columns
     * @param height : number of rows
     */
    public DemineurModel(int width, int height) {
        this(width, height, width + height/2);
    }

    /**
     * Initialize the game grid depending on the first move, so the player can't lose at the beginning
     * @param i : the row of the first move
     * @param j : the column of the first move
     */
    private void initCells(int i, int j) {
        cells = new Cell[HEIGHT][WIDTH];
        countDiscoveredCells = 0;
        int n = 0;
        while(n < MINES) {
            int newMineI = new Random().nextInt(HEIGHT);
            int newMineJ = new Random().nextInt(WIDTH);
            if((newMineI != i || newMineJ != j) && cells[newMineI][newMineJ] != Cell.MINE) {
                cells[newMineI][newMineJ] = Cell.MINE;
                n++;
            }
        }
        for(i = 0; i < HEIGHT; i++) {
            for(j = 0; j < WIDTH; j++) {
                discovered[i][j] = false;
                if(cells[i][j] != Cell.MINE) {
                    switch(countAdjacentMines(i, j)) {
                        case 0 : cells[i][j] = Cell.EMPTY; break;
                        case 1 : cells[i][j] = Cell.ONE; break;
                        case 2 : cells[i][j] = Cell.TWO; break;
                        case 3 : cells[i][j] = Cell.THREE; break;
                        case 4 : cells[i][j] = Cell.FOUR; break;
                        case 5 : cells[i][j] = Cell.FIVE; break;
                        case 6 : cells[i][j] = Cell.SIX; break;
                        case 7 : cells[i][j] = Cell.SEVEN; break;
                        case 8 : cells[i][j] = Cell.EIGHT; break;
                        default : assert(false); //System.err.println("ERROR : error on neighbours numbers");
                    }
                }
            }
        }
    }

    /**
     *
     * @return the number of columns
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     *
     * @return the number of rows
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     *
     * @return the number of mines
     */
    public int getMines() {
        return MINES;
    }

    /**
     *
     * @return true if none move has been done yet
     */
    private boolean isFirstMove() {
        return cells == null;
    }

    /**
     *
     * @param i : the cell row
     * @param j : the cell column
     * @return true if the (i, j) cell has been discovered
     */
    public boolean isDiscovered(int i, int j) {
        //System.err.println("MODEL : width = " + WIDTH + ", height = " + HEIGHT);
        //System.err.println("MODEL : i = " + i + ", j = " + j);
        //System.err.println("MODEL : discover length = width " + discovered[0].length + ", height " + discovered.length);
        return discovered[i][j];
    }

    /**
     *
     * @param i : the cell row
     * @param j : the cell column
     * @return true if the (i, j) cell has been marked
     */
    public boolean isMarked(int i, int j) {
        //System.err.println("MODEL : width = " + WIDTH + ", height = " + HEIGHT);
        //System.err.println("MODEL : i = " + i + ", j = " + j);
        //System.err.println("MODEL : discover length = width " + discovered[0].length + ", height " + discovered.length);
        return marked[i][j];
    }

    /**
     *
     * @return the number of marked cells
     */
    public int getCountMarkedCells() {
        return countMarkedCells;
    }

    /**
     *
     * @return the difference between the number of mines and the number of marked cells
     */
    public int getRemainingCountMines() {
        return MINES - countMarkedCells;
    }

    /**
     *
     * @return true if the game is in the flag mode, false otherwise
     */
    public boolean isFlagMode() {
        return flagMode;
    }

    /**
     *
     * @return true if the game is lost
     */
    public boolean isLost() {
        return lost;
    }

    /**
     *
     * @return true if the game is won
     */
    public boolean isWon() {
        return won;
    }

    /**
     *
     * @param i : the cell row
     * @param j : the cell column
     * @return the (i, j) cell content
     */
    public Cell getCell(int i, int j) {
        return cells[i][j];
    }

    /**
     * The game has been lost
     */
    private void setLost() {
        this.lost = true;
    }

    /**
     * The game has been won
     */
    private void setWon() {
        this.won = true;
    }

    /**
     * Put the game in the flag mode
     * @param flagMode true to get in the flag mode, false to cancel the flag mode
     */
    public void setFlagMode(boolean flagMode) {
        this.flagMode = flagMode;
    }

    /**
     * Calculate the number of adjacent mines around the given cell
     * @param i : the cell row
     * @param j : the cell column
     * @return the number of adjacent mines
     */
    private int countAdjacentMines(int i, int j) {
        int count = 0;
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                if(m == 0 && n == 0)
                    continue;
                try {
                    if(cells[i+m][j+n] == Cell.MINE) {
                        count++;
                    }
                }
                catch(IndexOutOfBoundsException e) {
                    continue;
                }
            }
        }
        return count;
    }

    /**
     * Makes the (i, j) cell become a discovered cell
     * @param i : the cell row
     * @param j : the cell column
     */
    private void setDiscovered(int i, int j) {
        if(isDiscovered(i, j) || isMarked(i, j))
            return;
        discovered[i][j] = true;
        countDiscoveredCells++;
    }

    /**
     * An empty cell has been discovered, looks for adjacent empty cells
     * @param i : the cell row
     * @param j : the column row
     */
    private void setAdjacentEmptyDiscovered(int i, int j) {
        if(isDiscovered(i, j) || isMarked(i, j))
            return;
        setDiscovered(i, j);
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                if(m == 0 && n == 0)
                    continue;
                try {
                    Cell c = cells[m + i][n + j];
                    switch(c) {
                        case MINE : assert(false); //System.err.println("ERROR : invalid grid"); System.exit(-1);
                        case EMPTY : setAdjacentEmptyDiscovered(m + i, n + j); break;
                        default : setDiscovered(m + i, n + j); break;
                    }
                }
                catch(IndexOutOfBoundsException e) {
                    continue;
                }
            }
        }
    }

    /**
     * Marks the (i, j) cell with a flag if it wasn't, else removes the flag and updates the flags count
     * @param i : the cell row
     * @param j : the cell column
     */
    private void setMarked(int i, int j) {
        marked[i][j] = !marked[i][j];
        countMarkedCells += marked[i][j] ? 1 : -1;
    }

    /**
     * Plays in the cell (i, j)
     * @param i : the cell row
     * @param j : the cell column
     */
    public void move(int i, int j) {
        if(isFirstMove())
            initCells(i, j);
        if(discovered[i][j])
            return;
        if(isFlagMode()) {
            setMarked(i, j);
            return;
        }
        if(isMarked(i, j))
            return;
        switch(cells[i][j]) {
            case MINE : setDiscovered(i, j); setLost(); break;
            case EMPTY : setAdjacentEmptyDiscovered(i, j); break;
            default : setDiscovered(i, j); break;
        }
        if(countDiscoveredCells == HEIGHT * WIDTH - MINES)
            setWon();
    }
}