
package utils;

import java.io.Serializable;

import oop_utils.OOP_Point3D;

public class Point3D implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final double EPS1 = 0.001;
    public static final double EPS2;
    public static final double EPS;
    public static final Point3D ORIGIN;
    private double _x;
    private double _y;
    private double _z;
    public static final int ONSEGMENT = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int INFRONTOFA = 3;
    public static final int BEHINDB = 4;
    public static final int ERROR = 5;
    public static final int DOWN = 6;
    public static final int UP = 7;
    
    static {
        EPS2 = Math.pow(0.001, 2.0);
        EPS = Point3D.EPS2;
        ORIGIN = new Point3D(0.0, 0.0, 0.0);
    }
    
    public Point3D(final double x, final double y, final double z) {
        this._x = x;
        this._y = y;
        this._z = z;
    }
    
    public Point3D(final Point3D p) {
        this(p.x(), p.y(), p.z());
    }
    
    public Point3D(final double x, final double y) {
        this(x, y, 0.0);
    }
    
    public Point3D(final String s) {
        try 
        {
			//here we limit the number of digits after the floating point to 16 digits to prevent inaccuracy
        	final String[] a = s.split(",");
			double x =(double)Math.round(Double.parseDouble(a[0]) * 10000000000000000d) / 10000000000000000d;
			double y =(double)Math.round(Double.parseDouble(a[1])* 10000000000000000d) / 10000000000000000d;
            this._x = x;
            this._y = y;
            this._z = Double.parseDouble(a[2]);
        }
        catch (IllegalArgumentException e) {
            System.err.println("ERR: got wrong format string for POint3D init, got:" + s + "  should be of format: x,y,x");
            throw e;
        }
    }
    
    public Point3D(OOP_Point3D rp) 
    {
    	this(rp.x(), rp.y(), rp.z());
	}

	public double x() {
        return this._x;
    }
    
    public double y() {
        return this._y;
    }
    
    public double z() {
        return this._z;
    }
    
    public int ix() {
        return (int)this._x;
    }
    
    public int iy() {
        return (int)this._y;
    }
    
    public int iz() {
        return (int)this._z;
    }
    
    public void add(final Point3D p) {
        this.add(p._x, p._y, p._z);
    }
    
    public void add(final double dx, final double dy, final double dz) {
        this._x += dx;
        this._y += dy;
        this._z += dz;
    }
    
    public void factor(final double d) {
        this._x *= d;
        this._y *= d;
        this._z *= d;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this._x) + "," + this._y + "," + this._z;
    }
    
    public double distance3D(final Point3D p2) {
        final double dx = this.x() - p2.x();
        final double dy = this.y() - p2.y();
        final double dz = this.z() - p2.z();
        final double t = dx * dx + dy * dy + dz * dz;
        return Math.sqrt(t);
    }
    
    public double distance3D() {
        return this.distance3D(Point3D.ORIGIN);
    }
    
    public double distance2D(final Point3D p2) {
        final double dx = this.x() - p2.x();
        final double dy = this.y() - p2.y();
        final double t = dx * dx + dy * dy;
        return Math.sqrt(t);
    }
    
    @Override
    public boolean equals(final Object p) {
        if (p == null || !(p instanceof Point3D)) {
            return false;
        }
        final Point3D p2 = (Point3D)p;
        return this._x == p2._x && this._y == p2._y && this._z == p2._z;
    }
    
    public boolean close2equals(final Point3D p2) {
        return this.distance3D(p2) < Point3D.EPS;
    }
    
    public boolean equalsXY(final Point3D p) {
        return p._x == this._x && p._y == this._y;
    }
    
    public String toString(final boolean all) {
        if (all) {
            return "[" + this._x + "," + this._y + "," + this._z + "]";
        }
        return "[" + (int)this._x + "," + (int)this._y + "," + (int)this._z + "]";
    }
    
    public String toFile() {
        return String.valueOf(this._x) + " " + this._y + " " + this._z + " ";
    }
    
    public String toFile1() {
        return "Point3D " + this._x + " " + this._y + " " + this._z;
    }
    
    public int pointLineTest2(final Point3D a, final Point3D b) {
        final int flag = this.pointLineTest(a, b);
        if (a._x < b._x) {
            if (a._x <= this._x && b._x > this._x) {
                if (flag == 1) {
                    return 6;
                }
                if (flag == 2) {
                    return 7;
                }
            }
        }
        else if (a._x > b._x && b._x <= this._x && a._x > this._x) {
            if (flag == 2) {
                return 6;
            }
            if (flag == 1) {
                return 7;
            }
        }
        return flag;
    }
    
    public int pointLineTest(final Point3D a, final Point3D b) {
        if (a == null || b == null || a.equalsXY(b)) {
            return 5;
        }
        final double dx = b._x - a._x;
        final double dy = b._y - a._y;
        final double res = dy * (this._x - a._x) - dx * (this._y - a._y);
        if (res < 0.0) {
            return 1;
        }
        if (res > 0.0) {
            return 2;
        }
        if (dx > 0.0) {
            if (this._x < a._x) {
                return 3;
            }
            if (b._x < this._x) {
                return 4;
            }
            return 0;
        }
        else if (dx < 0.0) {
            if (this._x > a._x) {
                return 3;
            }
            if (b._x > this._x) {
                return 4;
            }
            return 0;
        }
        else if (dy > 0.0) {
            if (this._y < a._y) {
                return 3;
            }
            if (b._y < this._y) {
                return 4;
            }
            return 0;
        }
        else {
            if (dy >= 0.0) {
                return 5;
            }
            if (this._y > a._y) {
                return 3;
            }
            if (b._y > this._y) {
                return 4;
            }
            return 0;
        }
    }
    
    public void rescale(final Point3D center, final Point3D vec) {
        if (center != null && vec != null) {
            this.rescale(center, vec.x(), vec.y(), vec.z());
        }
    }
    
    public void rescale(final Point3D center, final double size) {
        if (center != null && size > 0.0) {
            this.rescale(center, size, size, size);
        }
    }
    
    private void rescale(final Point3D center, final double sizeX, final double sizeY, final double sizeZ) {
        this._x = center._x + (this._x - center._x) * sizeX;
        this._y = center._y + (this._y - center._y) * sizeY;
        this._z = center._z + (this._z - center._z) * sizeZ;
    }
    
    public void rotate2D(final Point3D center, final double angle) {
        this._x -= center.x();
        this._y -= center.y();
        final double a = Math.atan2(this._y, this._x);
        final double radius = Math.sqrt(this._x * this._x + this._y * this._y);
        this._x = center.x() + radius * Math.cos(a + angle);
        this._y = center.y() + radius * Math.sin(a + angle);
    }
}
