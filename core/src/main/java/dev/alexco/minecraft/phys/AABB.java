package dev.alexco.minecraft.phys;

import dev.alexco.minecraft.Minecraft;
import static dev.alexco.minecraft.phys.AABBPool.AABBpool;
public class AABB {
     private double epsilon = 0.001F;

  public double x0;
  public double y0;
  public double x1;
  public double y1;

  public AABB(double x0, double y0, double x1, double y1) {
    this.x0 = x0 ;
    this.y0 = y0 ;
    this.x1 = x1 ;
    this.y1 = y1 ;
  }

  /**
   * Expands this box in the movement direction.
   */
  public AABB expand(double xa, double ya) {
    double _x0 = this.x0;
    double _y0 = this.y0;
    double _x1 = this.x1;
    double _y1 = this.y1;
    if (xa < 0.0F)
      _x0 += xa;
    if (xa > 0.0F)
      _x1 += xa;
    if (ya < 0.0F)
      _y0 += ya;
    if (ya > 0.0F)
      _y1 += ya;
      return AABBpool.get(_x0, _y0, _x1, _y1);

  }

  /**
   * Grows this box equally in both directions on each axis.
   */
  public AABB grow(double xa, double ya) {
    double _x0 = this.x0 - xa;
    double _y0 = this.y0 - ya;
    double _x1 = this.x1 + xa;
    double _y1 = this.y1 + ya;
    return AABBpool.get(_x0, _y0, _x1, _y1);
  }

  /**
   * Clips horizontal motion against another box.
   */
  public double clipXCollide(AABB c, double xa) {
    if (c.y1 <= this.y0 || c.y0 >= this.y1)
      return xa;
    if (xa > 0.0F && c.x1 <= this.x0) {
      double max = (this.x0 - c.x1 - this.epsilon);
      if (max < xa)
        xa = max;
    }
    if (xa < 0.0F && c.x0 >= this.x1) {
      double max = (this.x1 - c.x0 + this.epsilon);
      if (max > xa)
        xa = max;
    }
    return xa;
  }

  /**
   * Clips vertical motion against another box.
   */
  public double clipYCollide(AABB c, double ya) {
    if (c.x1 <= this.x0 || c.x0 >= this.x1)
      return ya;
    if (ya > 0.0F && c.y1 <= this.y0) {
      double max = (this.y0 - c.y1 - this.epsilon);
      if (max < ya)
        ya = max;
    }
    if (ya < 0.0F && c.y0 >= this.y1) {
      double max = (this.y1 - c.y0 + this.epsilon);
      if (max > ya)
        ya = max;
    }
    return ya;
  }

  /**
   * Returns true if this box overlaps another box.
   */
  public boolean intersects(AABB c) {
    if (c.x1 <= this.x0 || c.x0 >= this.x1)
      return false;
    if (c.y1 <= this.y0 || c.y0 >= this.y1)
      return false;
    return true;
  }

  /**
   * Translates this box by the given offsets.
   */
  public void move(double xa, double ya) {
    this.x0 += xa;
    this.y0 += ya;
    this.x1 += xa;
    this.y1 += ya;
  }

  /**
   * Overwrites this box coordinates.
   */
  public void set(double x0, double y0, double x1, double y1) {
    this.x0 = x0;
    this.y0 = y0;
    this.x1 = x1;
    this.y1 = y1;
}
}
