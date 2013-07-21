package com.l2scoria.gameserver.model.spawn;

/**
 * RandomSpawn help class.
 */
public class RandomSpawn {

    private int X;
    private int Y;
    private int Z;
    public RandomSpawn(int x, int y, int z)
    {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public int getX(){return X;}
    public int getY(){return Y;}
    public int getZ(){return Z;}
}