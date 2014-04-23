package schallplatten;

/**
 * Date: 08.04.2013, 22.04.2014
 * Author: Sergey Serebryakov (sergey@serebryakov.info)
 */

import static schallplatten.Constants.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import schallplatten.wav.WavFile;
import schallplatten.wav.WavFileException;

public class SoundToImage {
    // Linear transformation from frequency to brightness.
    private static final double K = (BRIGHT_MAX - BRIGHT_MIN) / (FREQ_MAX - FREQ_MIN);
    private static final double B = BRIGHT_MIN - K * FREQ_MIN;

    public static void main(String[] args) throws IOException, WavFileException {
        String inputSoundName = args[0];
        String outputImageName = args[1];
        double[] frames = readWavFile(inputSoundName);

        // Preparing the output image.
        BufferedImage out = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        double centerX = IMAGE_WIDTH / 2, centerY = IMAGE_HEIGHT / 2;

        // The spiral goes counter-clockwise inwards.
        // x and y are the Cartesian coordinates. radius and angle are the polar coordinates.
        int startX1 = IMAGE_WIDTH - IMAGE_MARGIN, startY1 = IMAGE_HEIGHT / 2;
        int startX2 = startX1 + TRACK_WIDTH, startY2 = IMAGE_HEIGHT / 2;

        double startRadius = Math.abs(startX1 - centerX);
        double startAngle = START_ANGLE;

        int framesPerTurn = frames.length / TURN_COUNT;
        double angleStep = 2 * Math.PI / framesPerTurn;

        int frameCount = drawSpiral(centerX, centerY, startRadius, startAngle, angleStep, frames, 0, g);

        System.out.println("Frame count: " + frameCount);

        System.err.println("Saving image");
        ImageIO.write(out, "PNG", new File(outputImageName));
    }

    private static int drawSpiral(double centerX, double centerY, double startRadius, double startAngle, double angleStep, double[] frames, int frameCount, Graphics g) {
        double innerRadius = startRadius;
        for (int turn = 0; turn < TURN_COUNT; turn++) {
            System.err.println("Encoding turn #" + turn);

            if (ROUND_SPIRAL) {
                frameCount = drawRoundTurn(centerX, centerY, innerRadius, startAngle, angleStep, frames, frameCount, g);
            } else {
                frameCount = drawPolyTurn(centerX, centerY, innerRadius, startAngle, angleStep, frames, frameCount, g);
            }

            innerRadius = innerRadius - RADIUS_STEP;
        }
        return frameCount;
    }

    private static double[] readWavFile(String wavFileName) throws IOException, WavFileException {
        WavFile wavFile = WavFile.openWavFile(new File(wavFileName));
        try {
            wavFile.display();

            if (wavFile.getNumChannels() != 1) {
                System.err.println("WAV file is not mono! Exiting...");
                System.exit(0);
            }

            int frameCount = (int) wavFile.getNumFrames();
            double[] buffer = new double[frameCount];
            int framesRead = wavFile.readFrames(buffer, frameCount);

            if (framesRead != frameCount) {
                System.err.println("Not all WAV frames are read! Exiting...");
                System.exit(0);
            }

            return buffer;
        } finally {
            wavFile.close();
        }
    }

    private static int drawRoundTurn(double centerX, double centerY, double startRadius, double startAngle, double angleStep, double[] frames, int frameCount, Graphics g) {
        double innerRadius = startRadius;
        double outerRadius = innerRadius + TRACK_WIDTH;

        int rx1 = (int) Math.round(centerX + innerRadius * Math.cos(startAngle));
        int ry1 = (int) Math.round(centerY - innerRadius * Math.sin(startAngle));
        int rx2 = (int) Math.round(centerX + outerRadius * Math.cos(startAngle));
        int ry2 = (int) Math.round(centerY - outerRadius * Math.sin(startAngle));

        double angle = startAngle;
        while (angle < 2 * Math.PI) {
            angle = angle + angleStep;
            innerRadius = innerRadius - RADIUS_STEP * angleStep / (2 * Math.PI);
            outerRadius = innerRadius + TRACK_WIDTH;

            int rx3 = rx2, ry3 = ry2, rx4 = rx1, ry4 = ry1;
            rx1 = (int) Math.round(centerX + innerRadius * Math.cos(angle));
            ry1 = (int) Math.round(centerY - innerRadius * Math.sin(angle));
            rx2 = (int) Math.round(centerX + outerRadius * Math.cos(angle));
            ry2 = (int) Math.round(centerY - outerRadius * Math.sin(angle));

            frameCount = paintTrapezium(frames, frameCount, g, rx1, ry1, rx2, ry2, rx3, ry3, rx4, ry4);
        }

        return frameCount;
    }

    private static int drawPolyTurn(double centerX, double centerY, double startRadius, double startAngle, double angleStep, double[] frames, int frameCount, Graphics g) {
        double innerRadius = startRadius;
        double outerRadius = innerRadius + TRACK_WIDTH;

        int rx1 = (int) Math.round(centerX + innerRadius * Math.cos(startAngle));
        int ry1 = (int) Math.round(centerY - innerRadius * Math.sin(startAngle));
        int rx2 = (int) Math.round(centerX + outerRadius * Math.cos(startAngle));
        int ry2 = (int) Math.round(centerY - outerRadius * Math.sin(startAngle));

        for (int thisVertex = 0; thisVertex < VERTICES; thisVertex++) {
            int nextVertex = thisVertex + 1;

            double thisVertexRadius = startRadius - (RADIUS_STEP / VERTICES) * thisVertex;
            double nextVertexRadius = startRadius - (RADIUS_STEP / VERTICES) * nextVertex;
            double thisVertexAngle = startAngle + (2 * Math.PI / VERTICES) * thisVertex;
            double nextVertexAngle = startAngle + (2 * Math.PI / VERTICES) * nextVertex;

            double thisVertexX = thisVertexRadius * Math.cos(thisVertexAngle);
            double thisVertexY = thisVertexRadius * Math.sin(thisVertexAngle);
            double nextVertexX = nextVertexRadius * Math.cos(nextVertexAngle);
            double nextVertexY = nextVertexRadius * Math.sin(nextVertexAngle);

            double angle = thisVertexAngle;
            while (angle < nextVertexAngle) {
                angle = angle + angleStep;

                double sin = Math.sin(angle), cos = Math.cos(angle);
                double kf = (thisVertexX * sin - thisVertexY * cos) / (cos * (nextVertexY - thisVertexY) - sin * (nextVertexX - thisVertexX));
                innerRadius = (thisVertexX + kf * (nextVertexX - thisVertexX)) / cos;
                outerRadius = innerRadius + TRACK_WIDTH;

                int rx3 = rx2, ry3 = ry2, rx4 = rx1, ry4 = ry1;
                rx1 = (int) Math.round(centerX + innerRadius * Math.cos(angle));
                ry1 = (int) Math.round(centerY - innerRadius * Math.sin(angle));
                rx2 = (int) Math.round(centerX + outerRadius * Math.cos(angle));
                ry2 = (int) Math.round(centerY - outerRadius * Math.sin(angle));

                frameCount = paintTrapezium(frames, frameCount, g, rx1, ry1, rx2, ry2, rx3, ry3, rx4, ry4);
            }
        }

        return frameCount;
    }

    // Really ugly frame counting.
    private static int paintTrapezium(double[] frames, int frameCount, Graphics g, int rx1, int ry1, int rx2, int ry2, int rx3, int ry3, int rx4, int ry4) {
        int brightness = (frameCount < frames.length) ? (int) (frames[frameCount++] * K + B) : 127;
        g.setColor(new Color(brightness, brightness, brightness));
        g.fillPolygon(new int[]{rx1, rx2, rx3, rx4}, new int[]{ry1, ry2, ry3, ry4}, 4);
        return frameCount;
    }
}
