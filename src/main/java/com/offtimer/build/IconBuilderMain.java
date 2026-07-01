package com.offtimer.build;

import com.offtimer.ui.AppIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class IconBuilderMain {

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length > 0 ? args[0] : ".");
        Path trayPng = root.resolve("src/main/resources/icon.png");
        Path appPng = root.resolve("src/main/resources/icon-app.png");
        Path ico = root.resolve("src/main/resources/icon.ico");

        ImageIO.write(AppIcon.renderTrayIcon(256), "png", trayPng.toFile());
        ImageIO.write(AppIcon.renderAppIcon(512), "png", appPng.toFile());

        int[] sizes = {16, 32, 48, 64, 128, 256};
        List<byte[]> images = new ArrayList<>();
        List<Integer> sizeList = new ArrayList<>();
        for (int size : sizes) {
            images.add(encodeBmp(AppIcon.renderAppIcon(size)));
            sizeList.add(size);
        }
        writeIco(ico, images, sizeList);
        System.out.println("Created " + ico.toAbsolutePath());
    }

    private static byte[] encodeBmp(BufferedImage image) throws IOException {
        int w = image.getWidth();
        int h = image.getHeight();
        int rowBytes = w * 4;
        int andRowBytes = ((w + 31) / 32) * 4;
        int xorSize = rowBytes * h;
        int andSize = andRowBytes * h;

        ByteArrayOutputStream out = new ByteArrayOutputStream(40 + xorSize + andSize);
        writeIntLE(out, 40);
        writeIntLE(out, w);
        writeIntLE(out, h * 2);
        writeShortLE(out, 1);
        writeShortLE(out, 32);
        writeIntLE(out, 0);
        writeIntLE(out, xorSize + andSize);
        writeIntLE(out, 0);
        writeIntLE(out, 0);
        writeIntLE(out, 0);
        writeIntLE(out, 0);

        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                out.write(rgb & 0xFF);
                out.write((rgb >> 8) & 0xFF);
                out.write((rgb >> 16) & 0xFF);
                out.write(255);
            }
        }

        out.write(new byte[andSize]);
        return out.toByteArray();
    }

    private static void writeIco(Path path, List<byte[]> images, List<Integer> sizes) throws IOException {
        int count = images.size();
        int offset = 6 + count * 16;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeShortLE(out, 0);
        writeShortLE(out, 1);
        writeShortLE(out, count);

        for (int i = 0; i < count; i++) {
            int size = sizes.get(i);
            out.write(size >= 256 ? 0 : size);
            out.write(size >= 256 ? 0 : size);
            out.write(0);
            out.write(0);
            writeShortLE(out, 1);
            writeShortLE(out, 32);
            writeIntLE(out, images.get(i).length);
            writeIntLE(out, offset);
            offset += images.get(i).length;
        }

        for (byte[] image : images) {
            out.write(image);
        }

        Files.write(path, out.toByteArray());
    }

    private static void writeShortLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    private static void writeIntLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }
}
