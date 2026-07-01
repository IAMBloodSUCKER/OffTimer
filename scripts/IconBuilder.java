import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IconBuilder {

    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color BLUE_DARK = new Color(37, 99, 235);

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length > 0 ? args[0] : ".");
        Path png = root.resolve("src/main/resources/icon.png");
        Path ico = root.resolve("src/main/resources/icon.ico");

        BufferedImage source = renderIcon(512);
        ImageIO.write(source, "png", png.toFile());

        int[] sizes = {16, 32, 48, 64, 128, 256};
        List<byte[]> images = new ArrayList<>();
        List<Integer> sizeList = new ArrayList<>();
        for (int size : sizes) {
            images.add(encodeBmp(renderIcon(size)));
            sizeList.add(size);
        }
        writeIco(ico, images, sizeList);
        System.out.println("Created " + ico.toAbsolutePath());
    }

    private static BufferedImage renderIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(BLUE);
        g.fillRect(0, 0, size, size);

        int inset = Math.max(1, size / 18);
        g.setColor(BLUE_DARK);
        g.fill(new Ellipse2D.Float(inset, inset, size - inset * 2f, size - inset * 2f));

        g.setColor(Color.WHITE);
        float stroke = Math.max(1.8f, size * 0.085f);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = size / 2;
        int cy = size / 2 + Math.max(1, size / 24);
        int radius = Math.max(3, size / 4);
        g.drawArc(cx - radius, cy - radius, radius * 2, radius * 2, 52, 256);
        g.drawLine(cx, cy - radius - size / 14, cx, cy - size / 10);

        g.dispose();
        return image;
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
