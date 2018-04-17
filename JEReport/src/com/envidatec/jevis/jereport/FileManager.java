package com.envidatec.jevis.jereport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Farid Naimi <farid.naimi@envidatec.com>
 */
public class FileManager {

    public final static String XLS_FILE_EXTENSION = "xls";
    private final static char EXTENSION_SEPERATOR = '.';
    private String templateFileName;
    private String destFileName;

    public FileManager(String templateFileName, String destFileName) {
        this.templateFileName = templateFileName;
        this.destFileName = destFileName;
    }

    public FileManager() {
    }

    public String getDestFileName() {
        return destFileName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setDestFileName(String destFileName) {
        this.destFileName = destFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    /**
     * checks if the filename has the given extension
     * @param fileName
     * @param extension
     * @return true if the filename has the given extension
     */
    public boolean validateExtension(String fileName, String extension) {
        int dot = fileName.lastIndexOf(EXTENSION_SEPERATOR);
        if (fileName.substring(dot + 1).equals(extension)) {
            return true;
        }
        return false;
    }

    /**
     * Converts a file to byte array
     * @param file
     * @return file as byte array
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {

        InputStream is = new FileInputStream(file);

        // Get the size of the fileAsByte
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            System.out.println("File is too large...");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Writes byte array to file
     * @param fileAsByte
     * @param filename
     */
    public static void writeBytesToFile(byte[] fileAsByte, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(fileAsByte);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
    }
    
    public void deleteFiles()
    {
        File dest = new File(destFileName);
        dest.delete();
        File template = new File(templateFileName);
        template.delete();
    }
}
