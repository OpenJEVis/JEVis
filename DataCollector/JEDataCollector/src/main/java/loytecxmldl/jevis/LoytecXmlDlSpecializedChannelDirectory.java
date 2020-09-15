package loytecxmldl.jevis;

/**
 * Container for specialized JEVis Loytec XML-DL Channel Directory information
 */
class LoytecXmlDlSpecializedChannelDirectory {
    private String name;
    private String technology;

    LoytecXmlDlSpecializedChannelDirectory(String name, String technology) {
        this.name =name;
        this.technology =technology;
    }

    public String getName() {
        return name;
    }

    public String getTechnology(){
        return technology;
    }
}