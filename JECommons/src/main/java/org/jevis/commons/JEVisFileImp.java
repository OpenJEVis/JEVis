/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.io.FilenameUtils;
import org.jevis.api.JEVisFile;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisFileImp implements JEVisFile {

    byte[] bytes;
    String fileName = "";

    /**
     * Create an new JEVisFile from an java java.io.File
     *
     * @param fileName
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public JEVisFileImp(String fileName, File file) throws FileNotFoundException, IOException {
        this.fileName = fileName;
        loadFromFile(file);
    }

    public JEVisFileImp(String fileName, byte[] bytes) throws FileNotFoundException, IOException {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public JEVisFileImp() {
    }

    @Override
    public void saveToFile(File file) throws IOException {
        FileOutputStream fileOuputStream
                = new FileOutputStream(file);
        fileOuputStream.write(bytes);
        fileOuputStream.close();
    }

    @Override
    public void loadFromFile(File file) throws IOException {

        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.read(bytes);
        this.bytes = bytes;

        f.close();

    }

    @Override
    public void setBytes(byte[] data) {
        bytes = data;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public void setFilename(String name) {
        fileName = name;
    }

    @Override
    public String getFileExtension() {
        return FilenameUtils.getExtension(fileName);
    }

}
