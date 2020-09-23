package org.jevis.loytecxmldl;

/**
 * Container for specialized JEVis Loytec XML-DL Channel Directory information
 */
class LoytecXmlDlSpecializedChannelDirectory {
    private final String name;
    private final String technology;

    LoytecXmlDlSpecializedChannelDirectory(String name, String technology) {
        this.name = name;
        this.technology = technology;
    }

    public String getName() {
        return name;
    }

    public String getTechnology() {
        return technology;
    }
}