package org.jevis.jeconfig.plugin.notes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NoteTag {

    String name = "";
    String id = "";

    public NoteTag(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static List<NoteTag> parseTags(String tagString) {
        System.out.println("parseTag: " + tagString);
        List<NoteTag> tags = new ArrayList<>();

        for (String tString : tagString.split(";")) {
            try {
                /**
                 if (tString.equals(TAG_WARNING.getName())) {
                 tags.add(TAG_WARNING);
                 } else if (tString.equals(TAG_PROCESS.getName())) {
                 tags.add(TAG_PROCESS);
                 } else if (tString.equals(TAG_PROCESS.getName())) {
                 tags.add(TAG_PROCESS);
                 } else if (tString.equals(TAG_USER_ERROR.getName())) {
                 tags.add(TAG_USER_ERROR);
                 } else if (tString.equals(TAG_TECHNICAL_ERROR.getName())) {
                 tags.add(TAG_TECHNICAL_ERROR);
                 } else if (tString.equals(TAG_REMINDER.getName())) {
                 tags.add(TAG_REMINDER);
                 } else if (tString.equals(TAG_CHECK.getName())) {
                 tags.add(TAG_CHECK);
                 }
                 **/

                switch (tString) {
                    case "1":
                        tags.add(TAG_AUDIT);
                        break;
                    case "2":
                        tags.add(TAG_TASK);
                        break;
                    case "3":
                        tags.add(TAG_REPORT);
                        break;
                    case "4":
                        tags.add(TAG_EVENT);
                        break;
                    case "5":
                        tags.add(TAG_REMINDER);
                        break;
                    case "6":
                        tags.add(TAG_ERROR);
                        break;
                    default:
                        if (!tagString.isEmpty()) {
                            tags.add(new NoteTag(tagString, tagString));
                        }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        List<NoteTag> listWithoutDuplicates = new ArrayList<>(
                new HashSet<>(tags));

        return listWithoutDuplicates;
    }

    @Override
    public String toString() {
        return "NoteTag{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public static NoteTag TAG_AUDIT = new NoteTag("Audit", "1");
    public static NoteTag TAG_TASK = new NoteTag("Aufgabe", "2");
    public static NoteTag TAG_REPORT = new NoteTag("Bericht", "3");
    public static NoteTag TAG_EVENT = new NoteTag("Ereigniss", "4");
    public static NoteTag TAG_REMINDER = new NoteTag("Errinerung", "5");
    public static NoteTag TAG_ERROR = new NoteTag("Fehler", "6");
    public static NoteTag TAG_QUESTION = new NoteTag("Frage", "7");
    public static NoteTag TAG_INSTANT = new NoteTag("Instantsetzung", "8");
    public static NoteTag TAG_KENNZAHL = new NoteTag("Kennzahl", "9");
    public static NoteTag TAG_COMMENT = new NoteTag("Kommentar", "10");
    public static NoteTag TAG_MASSNAHME = new NoteTag("Maßnahme", "11");
    public static NoteTag TAG_STOERUNG = new NoteTag("Störung", "12");
    public static NoteTag TAG_WARTUNG = new NoteTag("Wartung", "13");

    public static List<NoteTag> getAllTags() {
        ArrayList<NoteTag> arrayList = new ArrayList<>();
        arrayList.add(TAG_AUDIT);
        arrayList.add(TAG_TASK);
        arrayList.add(TAG_REPORT);
        arrayList.add(TAG_EVENT);
        arrayList.add(TAG_REMINDER);
        arrayList.add(TAG_ERROR);
        arrayList.add(TAG_QUESTION);
        arrayList.add(TAG_INSTANT);
        arrayList.add(TAG_KENNZAHL);
        arrayList.add(TAG_COMMENT);
        arrayList.add(TAG_MASSNAHME);
        arrayList.add(TAG_STOERUNG);
        arrayList.add(TAG_WARTUNG);


        return arrayList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NoteTag) {
            return ((NoteTag) obj).getId().equals(this.getId());
        }
        return false;
    }
}
