import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IconBuilder {

    private static final Color BACKGROUND = new Color(26, 39, 68);

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length > 0 ? args[0] : ".");
        Path png = root.resolve("src/main/resources/icon.png");
        Path ico = root.resolve("src/main/resources/icon.ico");

        BufferedImage source = ImageIO.read(png.toFile());
        BufferedImage flat = flatten(source);
        ImageIO.write(flat, "png", png.toFile());

        int[] sizes = {16, 32, 48, 64, 128, 256};
        List<byte[]> images = new ArrayList<>();
        List<Integer> sizeList = new ArrayList<>();
        for (int size : sizes) {
            images.add(encodeBmp(scale(flat, size)));
            sizeList.add(size);
        }
        writeIco(ico, images, sizeList);
        System.out.println("Created " + ico.toAbsolutePath());
    }

    private static BufferedImage flatten(BufferedImage source) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return out;
    }

    private static BufferedImage scale(BufferedImage source, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, size, size);
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();
        return out;
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
                int argb = image.getRGB(x, y);
                out.write(argb & 0xFF);
                out.write((argb >> 8) & 0xFF);
                out.write((argb >> 16) & 0xFF);
                out.write((argb >> 24) & 0xFF);
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
