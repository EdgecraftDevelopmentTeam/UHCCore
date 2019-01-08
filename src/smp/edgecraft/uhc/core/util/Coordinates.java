package smp.edgecraft.uhc.core.util;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import static smp.edgecraft.uhc.core.util.Coordinates.CoordinateType.*;

public class Coordinates {

    /**
     * Turn string like 0, 0, 0, ... into double array.
     *
     * @param s List of doubles.
     * @param c Type of parsing to use.
     * @return Array of coordinates.
     */
    public static double[] parse(String s, CoordinateType c) {
        s = s.replaceAll(" ", "");
        String[] coords = s.split(",");
        double[] ds = new double[c == AREA ? 6 : c == ROTATION ? 5 : 3];
        for (int i = 0; i < Math.min(ds.length, coords.length); i++) {
            ds[i] = Double.parseDouble(coords[i]);
        }
        if (c == AREA) {
            if (coords.length == 6) for (int i = 0; i < 3; i++) {
                if (ds[i] > ds[i + 3]) {
                    double d = ds[i];
                    ds[i] = ds[i + 3];
                    ds[i + 3] = d;
                }
            }
            else System.arraycopy(ds, 0, ds, 3, 3);
        }
        return ds;
    }

    /**
     * Turn string like "0, 0, 0, ..." into a location.
     * POINT will just use world, x, y and z,
     * ROTATION does yaw and pitch (if present),
     * AREA will sort out minimum and maximum points and select center.
     *
     * @param w World to use when creating a location.
     * @param s List of doubles.
     * @return Bukkit location.
     */
    public static Location getLocation(World w, String s, CoordinateType c) {
        double[] ds = parse(s, c);
        return new Location(w, c == AREA ? (ds[0] + ds[3]) / 2 : ds[0], c == AREA ? (ds[1] + ds[4]) / 2 : ds[1], c == AREA ? (ds[2] + ds[5]) / 2 : ds[2], c == ROTATION ? (float) ds[3] : 0, c == ROTATION ? (float) ds[4] : 0);
    }

    /**
     * Turn string like "world, 0, 0, 0, ..." into a location.
     * POINT will just use world, x, y and z,
     * ROTATION does yaw and pitch (if present),
     * AREA will sort out minimum and maximum points and select center.
     * World must be present when parsing.
     *
     * @param s List of doubles.
     * @return Bukkit location.
     */
    public static Location getLocation(String s, CoordinateType c) {
        return getLocation(Bukkit.getWorld(s.substring(0, s.indexOf(','))), s.substring(s.indexOf(',') + 1), c);
    }

    /**
     * Types of coordinate parsing
     */
    public enum CoordinateType {
        /**
         * AREA will sort out minimum and maximum points.
         * AREA sort: [0-2] min point, [3-5] max point.
         */
        AREA,
        /**
         * POINT will just put x, y and z into an array.
         */
        POINT,
        /**
         * ROTATION does yaw and pitch (if present).
         */
        ROTATION
    }

}
