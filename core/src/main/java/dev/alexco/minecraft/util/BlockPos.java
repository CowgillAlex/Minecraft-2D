package dev.alexco.minecraft.util;

public class BlockPos {
    private int x;
    private int y;

    public BlockPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }


    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }


    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @param dX the x to change by
     */
    public void changeX(int dX){
        this.x += dX;
    }
    /**
     * @param dY the y to change by
     */
    public void changeY(int dY){
        this.y += dY;
    }
}
