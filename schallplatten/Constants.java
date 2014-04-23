package schallplatten;

/**
 * Date: 08.04.2013, 22.04.2014
 * Author: Sergey Serebryakov (sergey@serebryakov.info)
 */

public class Constants {
    // The size of the image, in pixels.
    public static final int IMAGE_WIDTH = 2000, IMAGE_HEIGHT = 2000;

    // The distance between the outer edge of the spiral and the image border, in pixels.
    public static final int IMAGE_MARGIN = 50;

    // The number of turns in the spiral.
    public static final int TURN_COUNT = 40;

    // Whether the spiral is round.
    public static final boolean ROUND_SPIRAL = true;

    // The number of vertices in the spiral (N/A if round).
    public static final int VERTICES = 7;

    // The width of the track in pixels.
    public static final int TRACK_WIDTH = 6;

    // The polar angle where the spiral starts. 0 means 3 o'clock, π/2 means 12 o'clock.
    public static final double START_ANGLE = 0;

    // The distance between two consecutive turns of the spiral, in pixels.
    public static final double RADIUS_STEP = 12;

    // Minimum and maximum values of wave frequency.
    public static final double FREQ_MIN = -1, FREQ_MAX = 1;

    // Minimum and maximum values of pixel brightness.
    public static final int BRIGHT_MIN = 0, BRIGHT_MAX = 255;
}
