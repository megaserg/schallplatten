package schallplatten;

/**
 * Date: 08.04.2013, 22.04.2014
 * Author: Sergey Serebryakov (sergey@serebryakov.info)
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import schallplatten.wav.WavFile;
import schallplatten.wav.WavFileException;

import static schallplatten.Constants.*;

public class ImageToSound {
    // Whether to create a debug image. Can damage the resulting audio file.
    private static final boolean DEBUG = false;

    // The speed of the moving "needle". Should be given.
    // Computed as (number of turns on the image i.e. TURN_COUNT) / (length of audio in seconds).
    // Example: 40 turns, 52 seconds: speed is 0.7692307.
    // When less than intended, the resulting audio will sound "stretched".
    // When more than intended, the resulting audio will sound "squeezed".
    private static final double TURNS_PER_SECOND = TURN_COUNT / 52.0;

    // How many samples to take per turn. Can be adjusted to increase quality.
    private static final int SAMPLES_PER_TURN = 8000;

    public static void main(String[] args) throws IOException, WavFileException {
        String inputImageName = args[0];
        String outputSoundName = args[1];

        BufferedImage in = ImageIO.read(new File(inputImageName));

        double centerX = IMAGE_WIDTH / 2, centerY = IMAGE_HEIGHT / 2;

        // The start position of the "needle" is the farthest edge of the spiral.
        // x and y are the Cartesian coordinates. radius and angle are the polar coordinates.
        // The needle moves counter-clockwise inwards.
        int startX = IMAGE_WIDTH - IMAGE_MARGIN + TRACK_WIDTH / 2, startY = IMAGE_HEIGHT / 2;
        double startRadius = Math.abs(startX - centerX);
        double startAngle = START_ANGLE;

        double angleStep = 2 * Math.PI / SAMPLES_PER_TURN;

        List<Integer> brightnessList = getBrightnessList(centerX, centerY, startRadius, startAngle, angleStep, in);

        if (DEBUG) {
            // Write debug image.
            ImageIO.write(in, "PNG", new File("debug_needle.png"));
        }

        int frameCount = brightnessList.size();
        System.out.println("Frame count: " + frameCount);

        // Linear transformation from brightness to frequency.
        double k = (FREQ_MAX - FREQ_MIN) / (BRIGHT_MAX - BRIGHT_MIN);
        double b = FREQ_MIN - k * BRIGHT_MIN;

        double[] frequencies = new double[frameCount];
        for (int i = 0; i < brightnessList.size(); i++) {
            frequencies[i] = brightnessList.get(i) * k + b;
        }

        double[][] buffer = new double[1][];
        buffer[0] = frequencies;

        double seconds = TURN_COUNT / TURNS_PER_SECOND;
        int frameRate = (int) (frameCount / seconds); // frames per second

        System.err.println("Saving audio");
        WavFile wavFile = WavFile.newWavFile(new File(outputSoundName), 1, frameCount, 16, frameRate);
        wavFile.writeFrames(buffer, frameCount);
        wavFile.close();
    }

    private static List<Integer> getBrightnessList(double centerX, double centerY, double startRadius, double startAngle, double angleStep, BufferedImage in) {
        List<Integer> brightnessList = new ArrayList<Integer>();

        double radius = startRadius;
        for (int turn = 0; turn < TURN_COUNT; turn++) {
            System.err.println("Decoding turn #" + turn);

            if (ROUND_SPIRAL) {
                makeRoundTurn(centerX, centerY, radius, startAngle, angleStep, brightnessList, in);
            } else {
                makePolyTurn(centerX, centerY, radius, startAngle, angleStep, brightnessList, in);
            }

            // Avoid rounding errors by direct assignment.
            radius = radius - RADIUS_STEP;
        }
        return brightnessList;
    }

    private static void makeRoundTurn(double centerX, double centerY, double startRadius, double startAngle, double angleStep, List<Integer> brightnessList, BufferedImage in) {
        double radius = startRadius;
        double angle = startAngle;
        while (angle < 2 * Math.PI) {
            radius = radius - RADIUS_STEP * angleStep / (2 * Math.PI);
            angle = angle + angleStep;

            addReading(centerX, centerY, radius, angle, brightnessList, in);
        }
    }

    private static void makePolyTurn(double centerX, double centerY, double startRadius, double startAngle, double angleStep, List<Integer> brightnessList, BufferedImage in) {
        for (int thisVertex = 0; thisVertex < VERTICES; thisVertex++) {
            int nextVertex = thisVertex + 1;

            double thisVertexRadius = startRadius - (RADIUS_STEP / VERTICES) * thisVertex;
            double nextVertexRadius = startRadius - (RADIUS_STEP / VERTICES) * nextVertex;
            double thisVertexAngle = startAngle + (2 * Math.PI / VERTICES) * thisVertex;
            double nextVertexAngle = startAngle + (2 * Math.PI / VERTICES) * nextVertex;

            // Convert polar to Cartesian.
            double thisVertexX = thisVertexRadius * Math.cos(thisVertexAngle);
            double thisVertexY = thisVertexRadius * Math.sin(thisVertexAngle);
            double nextVertexX = nextVertexRadius * Math.cos(nextVertexAngle);
            double nextVertexY = nextVertexRadius * Math.sin(nextVertexAngle);

            double angle = thisVertexAngle;
            while (angle < nextVertexAngle) {
                // Compute radius as we gradually move from one vertex to another.
                angle = angle + angleStep;
                double sin = Math.sin(angle), cos = Math.cos(angle);
                double kf = (thisVertexX * sin - thisVertexY * cos) / (cos * (nextVertexY - thisVertexY) - sin * (nextVertexX - thisVertexX));
                double radius = (thisVertexX + kf * (nextVertexX - thisVertexX)) / cos;

                addReading(centerX, centerY, radius, angle, brightnessList, in);
            }
        }
    }

    private static void addReading(double centerX, double centerY, double radius, double angle, List<Integer> brightnessList, BufferedImage in) {
        int rx = (int) Math.round(centerX + radius * Math.cos(angle));
        int ry = (int) Math.round(centerY - radius * Math.sin(angle));

        brightnessList.add(in.getRGB(rx, ry) & 0xFF);

        if (DEBUG) {
            // Leave a mark on the debug image.
            in.setRGB(rx, ry, (255 << 24) | (255 << 16) | (255 << 8) | 255);
        }
    }
}
