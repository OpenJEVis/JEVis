package org.jevis.jeconfig.tool;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.ParsedURL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders SVG files into JavaFX images using Batik. Vector images are rendered at the requested
 * size, so they stay sharp when the target is resized.
 */
public final class SvgImages {

    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
    /* the opening tag of the root element (no namespace prefix), attributes may span several lines */
    private static final Pattern ROOT_ELEMENT = Pattern.compile("<svg(?=[\\s>/])[^>]*>");
    /* an attribute named exactly "href"; "xlink:href" is preceded by ':' and does not match */
    private static final Pattern SVG2_HREF = Pattern.compile("(\\s)href\\s*=");
    private static final Pattern XML_ENCODING = Pattern.compile("^\\s*<\\?xml[^>]*encoding\\s*=\\s*[\"']([A-Za-z0-9._-]+)[\"']");

    static {
        registerBitmapDecoders();
    }

    private SvgImages() {
    }

    /**
     * @return {@code true} if the file name ends with ".svg" or the content looks like an SVG document
     */
    public static boolean isSvg(String fileName, byte[] bytes) {
        if (fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".svg")) {
            return true;
        }
        if (bytes == null) {
            return false;
        }
        String start = new String(bytes, 0, Math.min(bytes.length, 1024), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        return start.contains("<svg");
    }

    /**
     * Renders the SVG to an image of the given size. The aspect ratio of the SVG is kept
     * (according to its preserveAspectRatio), the remaining area is transparent.
     *
     * @param width  target width in pixels, values below 1 use the size of the SVG
     * @param height target height in pixels, values below 1 use the size of the SVG
     */
    public static Image render(byte[] svg, double width, double height) throws TranscoderException {
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        if (width >= 1) {
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
        }
        if (height >= 1) {
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
        }
        transcoder.transcode(new TranscoderInput(new ByteArrayInputStream(normalize(svg))), null);
        return SwingFXUtils.toFXImage(transcoder.image, null);
    }

    /**
     * Batik 1.8 only understands SVG 1.1. Browsers also accept two things that are common in current files:
     * links written as {@code href} instead of {@code xlink:href} (SVG 2) and a root element without the
     * SVG namespace. Both are converted to the SVG 1.1 form.
     */
    static byte[] normalize(byte[] svg) {
        Charset charset = StandardCharsets.UTF_8;
        Matcher declaration = XML_ENCODING.matcher(new String(svg, 0, Math.min(svg.length, 200), StandardCharsets.ISO_8859_1));
        if (declaration.find()) {
            try {
                charset = Charset.forName(declaration.group(1));
            } catch (Exception ignored) {
                /* unknown encoding, Batik will report it */
            }
        }
        String text = new String(svg, charset);
        Matcher root = ROOT_ELEMENT.matcher(text);
        if (!root.find()) {
            return svg;
        }

        String rootTag = root.group();
        String newRootTag = rootTag;
        if (!rootTag.matches("(?s).*\\sxmlns\\s*=.*")) {
            newRootTag = newRootTag.replaceFirst("^<svg", "<svg xmlns=\"" + SVG_NS + "\"");
        }

        String body = text.substring(root.end());
        String newBody = SVG2_HREF.matcher(body).replaceAll("$1xlink:href=");
        String newRootAttributes = SVG2_HREF.matcher(newRootTag).replaceAll("$1xlink:href=");
        boolean usesXlink = !newBody.equals(body) || !newRootAttributes.equals(newRootTag) || text.contains("xlink:");
        if (usesXlink && !newRootAttributes.contains("xmlns:xlink")) {
            newRootAttributes = newRootAttributes.replaceFirst("^<svg", "<svg xmlns:xlink=\"" + XLINK_NS + "\"");
        }

        if (newRootAttributes.equals(rootTag) && newBody.equals(body)) {
            return svg;
        }
        return (text.substring(0, root.start()) + newRootAttributes + newBody).getBytes(charset);
    }

    /**
     * @return a short, readable reason for a failed image, e.g. the SVG feature Batik does not support
     */
    public static String describeError(Throwable t) {
        Throwable cause = t;
        while (true) {
            Throwable next = cause instanceof TranscoderException ? ((TranscoderException) cause).getException() : cause.getCause();
            if (next == null || next == cause) break;
            cause = next;
        }
        String message = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
        /* Batik prefixes the position in the document, which is unknown ("null:-1") for files from the database */
        message = message.replaceFirst("^null:-?\\d+\\s*", "").trim();
        if (message.length() > 300) {
            message = message.substring(0, 300) + "...";
        }
        return "Image can not be shown: " + message;
    }

    /**
     * Batik 1.8 (batik-codec) declares decoders for embedded bitmaps that are missing in its jar, so images
     * inside an SVG fail with "URL data in unsupported format or corrupt". Register decoders based on ImageIO.
     */
    private static void registerBitmapDecoders() {
        ImageTagRegistry registry = ImageTagRegistry.getRegistry();
        registry.register(new ImageIORegistryEntry("PNG", "png", "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}));
        registry.register(new ImageIORegistryEntry("JPEG", "jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8}));
        registry.register(new ImageIORegistryEntry("GIF", "gif", "image/gif", new byte[]{'G', 'I', 'F'}));
        registry.register(new ImageIORegistryEntry("BMP", "bmp", "image/bmp", new byte[]{'B', 'M'}));
    }

    private static class ImageIORegistryEntry extends MagicNumberRegistryEntry {

        ImageIORegistryEntry(String name, String extension, String mimeType, byte[] magicNumber) {
            super(name, extension, mimeType, 0, magicNumber);
        }

        @Override
        public Filter handleStream(InputStream inputStream, ParsedURL origURL, boolean needRawData) {
            try {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    return ImageTagRegistry.getBrokenLinkImage(this, ERR_URL_FORMAT_UNREADABLE, new Object[]{getFormatName(), origURL});
                }
                return new RedRable(GraphicsUtil.wrap(image));
            } catch (IOException ex) {
                return ImageTagRegistry.getBrokenLinkImage(this, ERR_STREAM_UNREADABLE, new Object[]{getFormatName(), origURL, ex.getMessage()});
            }
        }
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage image, TranscoderOutput output) {
            this.image = image;
        }
    }
}
