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
     * The elpased time since the game has started
     */
    private int elapsedTime;

    /**
     * True if the game is paused
     */
    private boolean pause;

    /**
     * True if the next moves will be the burst joker
     */
    private boolean burstModeJoker;

    /**
     * True if the burst joker has been used
     */
    private boolean burstJokerUsed;

    /**
     * True if the next moves will be a safe move
     */
    private boolean safeModeJoker;

    /**
     * True if the safe joker has been used
     */
    private boolean safeJokerUsed;

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
        elapsedTime = 0;
        pause = false;
        burstModeJoker = false;
        burstJokerUsed = false;
        safeModeJoker = false;
        safeJokerUsed = false;
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
            if((newMineI != i || newMineJ != j) && cells[newMineI][newMineJ] != Cell.MINE && isMineValidPosition(newMineI, newMineJ)) {
                cells[newMineI][newMineJ] = Cell.MINE;
                n++;
            }
        }
        for(i = 0; i < HEIGHT; i++) {
            for(j = 0; j < WIDTH; j++) {
                discovered[i][j] = false;
                if(cells[i][j] != Cell.MINE) {
                    switch(countAdjacent(i, j, "mine")) {
                        case 0 : cells[i][j] = Cell.EMPTY; break;
                        case 1 : cells[i][j] = Cell.ONE; break;
                        case 2 : cells[i][j] = Cell.TWO; break;
                        case 3 : cells[i][j] = Cell.THREE; break;
                        case 4 : cells[i][j] = Cell.FOUR; break;
                        case 5 : cells[i][j] = Cell.FIVE; break;
                        case 6 : cells[i][j] = Cell.SIX; break;
                        case 7 : cells[i][j] = Cell.SEVEN; break;
                        case 8 : cells[i][j] = Cell.EIGHT; break;
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
     * @param height : the grid number of rows
     * @param width : the grid number of columns
     * @return the maximum number of mines that a given grid should have
     */
    public int getMaxMines(int height, int width) {
        return height * width - 2 * (height + width);
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
     * @return the (i, j) cell content
     */
    public Cell getCell(int i, int j) {
        return cells[i][j];
    }

    /**
     *
     * @param i : the cell row
     * @param j : the cell column
     * @return true if the (i, j) cell has been discovered
     */
    public boolean isDiscovered(int i, int j) {
        return discovered[i][j];
    }

    /**
     *
     * @param i : the cell row
     * @param j : the cell column
     * @return true if the (i, j) cell has been marked
     */
    public boolean isMarked(int i, int j) {
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
        return MINES - getCountMarkedCells();
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
     * @return true if the game is over (won, lost)
     */
    public boolean isGameOver() {
        return isLost() || isWon() || cells == null;
    }

    /**
     *
     * @return the elapsed time since the game begun
     */
    public int getElapsedTime() {
        return elapsedTime;
    }

    /**
     *
     * @return true if the game is paused, false otherwise
     */
    public boolean isPause() {
        return pause;
    }

    /**
     *
     * @return true if the burst mode is activated
     */
    public boolean isBurstModeJoker() {
        return burstModeJoker;
    }

    /**
     *
     * @return true if the burst joker has been used
     */
    public boolean isBurstJokerUsed() {
        return burstJokerUsed;
    }

    /**
     *
     * @return true if the safe mode is activated
     */
    public boolean isSafeModeJoker() {
        return safeModeJoker;
    }

    /**
     *
     * @return true if the safe joker has been used
     */
    public boolean isSafeJokerUsed() {
        return safeJokerUsed;
    }

    /**
     * Activate the burst mode
     */
    public void activateBurstModeJoker() {
        if(!isBurstJokerUsed())
            this.burstModeJoker = true;
    }

    /**
     *
     * @param burstJokerUsed : true if the burst joker has been used
     */
    private void setBurstJokerUsed(boolean burstJokerUsed) {
        this.burstJokerUsed = burstJokerUsed;
    }

    /**
     * Activate the safe mode
     */
    public void activateSafeModeJoker() {
        if(!isSafeJokerUsed())
            this.safeModeJoker = true;
    }

    /**
     *
     * @param safeJokerUsed : true if the safe joker has been used
     */
    private void setSafeJokerUsed(boolean safeJokerUsed) {
        this.safeJokerUsed = safeJokerUsed;
    }

    /**
     * Deactivate joker mode
     */
    public void deactivateJoker() {
        this.safeModeJoker = false;
        this.burstModeJoker = false;
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
     *
     * @param elapsedTime : the enw elapsed time
     */
    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     *
     * @param pause : true if the game must be paused, false for resuming the game
     */
    public void setPause(boolean pause) {
        this.pause = pause;
    }

    /**
     * Put the game in the flag mode
     * @param flagMode true to get in the flag mode, false to cancel the flag mode
     */
    public void setFlagMode(boolean flagMode) {
        this.flagMode = flagMode;
    }

    /**
     * Calculate the number of a specific content of cells around the given cell
     * @param i : the cell row
     * @param j : the cell column
     * @param compared : a string that describe with which cell content the comparison must be done ("mine", "flag", or any else to count the number of neighbours)
     * @return the number of adjacent cells that contains flag or mine depending on compared value
     */
    private int countAdjacent(int i, int j, String compared) {
        int count = 0;
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                if(m == 0 && n == 0)
                    continue;
                try {
                    Cell cell = cells[i+m][j+n];
                    if(compared.equals("flag") ? isMarked(i+m, j+n) : !compared.equals("mine") || cell == Cell.MINE)
                        count++;
                }
                catch(IndexOutOfBoundsException e) {
                }
            }
        }
        return count;
    }

    /**
     * Checks if putting a mine on the (i, j) cell will make an adjacent mine have only mines neighbours
     * @param i : the cell row
     * @param j : the cell column
     * @return true if the mine can be placed there
     */
    private boolean isMineValidPosition(int i, int j) {
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                if(m == 0 && n == 0)
                    continue;
                try {
                    if(cells[i+m][j+n] == Cell.MINE && countAdjacent(i+m, j+n, "neighbours") == (countAdjacent(i+m, j+n, "mine") + 1)) { // we suppose that the (i, j) cell contains a mine
                        return false; // this cell has an incorrect number of adjacent mines, so the (i, j) cell cannot contain a mine
                    }
                }
                catch(IndexOutOfBoundsException e) {
                }
            }
        }
        return true;
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
                        case EMPTY : setAdjacentEmptyDiscovered(m + i, n + j); break;
                        default : setDiscovered(m + i, n + j); break;
                    }
                }
                catch(IndexOutOfBoundsException e) {
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
    private void basicMove(int i, int j) {
        if(discovered[i][j]) {
            return;
        }
        switch(cells[i][j]) {
            case MINE : setDiscovered(i, j); setLost(); return;
            case EMPTY : setAdjacentEmptyDiscovered(i, j); break;
            default : setDiscovered(i, j); break;
        }
        if(countDiscoveredCells == HEIGHT * WIDTH - MINES)
            setWon();
    }

    /**
     * Makes all the non-discovered adjacent cells of a discovered cell become discovered
     * The number of adjacent flags must be the same that the (i, j) cell's number
     * The player can lose if he marked a wrong cell
     * @param i : the cell row
     * @param j : the cell column
     */
    private void discoveredMove(int i, int j) {
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                if(m == 0 && n == 0)
                    continue;
                try {
                    if(!isMarked(m + i,  n + j))
                        basicMove(m + i, n + j);
                }
                catch(IndexOutOfBoundsException e) {
                }
            }
        }
    }

    /**
     * The move will be safe. Puts a flag in the cell if it's a mine, else discovers it.
     * The player has to play on an undiscovered and unmarked cell
     * @param i : the cell row
     * @param j : the cell column
     */
    private void safeMove(int i, int j) {
        if(isDiscovered(i, j) || isMarked(i, j))
            return;
        switch(cells[i][j]) {
            case MINE: setMarked(i, j); break;
            default : basicMove(i, j); break;
        }
        setSafeJokerUsed(true);
        safeModeJoker = false;
    }

    /**
     * All the adjacent cells will be discovered or marked depending on the cell nature, including the current cell.
     * The player has to play on an undiscovered and unmarked cell
     * @param i : the cell row
     * @param j : the cell column
     */
    private void burstMove(int i, int j) {
        if(isDiscovered(i, j) || isMarked(i, j))
            return;
        for(int m = -1; m <= 1; m++) {
            for(int n = -1; n <= 1; n++) {
                try {
                    switch(cells[i+m][j+n]) {
                        case MINE: if(!isMarked(i+m, j+n)) setMarked(i+m, j+n); break;
                        default : if(isMarked(i+m, j+n)) setMarked(i + m, j + n); basicMove(i+m, j+n); break;
                    }
                }
                catch(IndexOutOfBoundsException e) {
                }
            }
        }
        setBurstJokerUsed(true);
        burstModeJoker = false;
    }

    /**
     * Makes the grid changes depending on the player's move's type
     * @param i : the cell row
     * @param j : the cell column
     */
    public void move(int i, int j) {
        if(isFirstMove())
            initCells(i, j);
        if(isSafeModeJoker())
            safeMove(i, j);
        else if (isBurstModeJoker()) {
            burstMove(i, j);
        }
        else if(isFlagMode() && !isDiscovered(i, j)) {
            setMarked(i, j);
        } else if(isDiscovered(i, j) && countAdjacent(i, j, "flag") == cells[i][j].ordinal() - 1)
            discoveredMove(i, j);
        else if(!isMarked(i, j))
            basicMove(i, j);
    }
}