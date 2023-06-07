package org.jevis.jecc.plugin.notes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NoteTag {
    private static final Logger logger = LogManager.getLogger(NoteTag.class);
    public static NoteTag TAG_AUDIT = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.audit"), "1");
    public static NoteTag TAG_TASK = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.task"), "2");
    public static NoteTag TAG_REPORT = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.report"), "3");
    public static NoteTag TAG_EVENT = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.event"), "4");
    public static NoteTag TAG_REMINDER = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.reminder"), "5");
    public static NoteTag TAG_ERROR = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.error"), "6");
    public static NoteTag TAG_QUESTION = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.question"), "7");
    public static NoteTag TAG_SERVICE = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.service"), "8");
    public static NoteTag TAG_CHARACTERISTIC = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.characteristic"), "9");
    public static NoteTag TAG_COMMENT = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.comment"), "10");
    public static NoteTag TAG_MEASURE = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.measure"), "11");
    public static NoteTag TAG_DISTURBANCE = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.disturbance"), "12");
    public static NoteTag TAG_MAINTENANCE = new NoteTag(I18n.getInstance().getString("plugin.notes.tags.maintenance"), "13");
    String name = "";
    String id = "";
    public NoteTag(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static List<NoteTag> parseTags(String tagString) {
        //System.out.println("parseTag: " + tagString);
        List<NoteTag> tags = new ArrayList<>();

        for (String tString : tagString.split(";")) {
            try {

                if (tString.equals(TAG_AUDIT.getName())) {
                    tags.add(TAG_AUDIT);
                    continue;
                } else if (tString.equals(TAG_TASK.getName())) {
                    tags.add(TAG_TASK);
                    continue;
                } else if (tString.equals(TAG_REPORT.getName())) {
                    tags.add(TAG_REPORT);
                    continue;
                } else if (tString.equals(TAG_EVENT.getName())) {
                    tags.add(TAG_EVENT);
                    continue;
                } else if (tString.equals(TAG_REMINDER.getName())) {
                    tags.add(TAG_REMINDER);
                    continue;
                } else if (tString.equals(TAG_ERROR.getName())) {
                    tags.add(TAG_ERROR);
                    continue;
                } else if (tString.equals(TAG_QUESTION.getName())) {
                    tags.add(TAG_QUESTION);
                    continue;
                } else if (tString.equals(TAG_SERVICE.getName())) {
                    tags.add(TAG_SERVICE);
                    continue;
                } else if (tString.equals(TAG_CHARACTERISTIC.getName())) {
                    tags.add(TAG_CHARACTERISTIC);
                    continue;
                } else if (tString.equals(TAG_COMMENT.getName())) {
                    tags.add(TAG_COMMENT);
                    continue;
                } else if (tString.equals(TAG_DISTURBANCE.getName())) {
                    tags.add(TAG_DISTURBANCE);
                    continue;
                } else if (tString.equals(TAG_MAINTENANCE.getName())) {
                    tags.add(TAG_MAINTENANCE);
                    continue;
                }


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
                    case "7":
                        tags.add(TAG_QUESTION);
                        break;
                    case "8":
                        tags.add(TAG_SERVICE);
                        break;
                    case "9":
                        tags.add(TAG_CHARACTERISTIC);
                        break;
                    case "10":
                        tags.add(TAG_COMMENT);
                        break;
                    case "11":
                        tags.add(TAG_MEASURE);
                        break;
                    case "12":
                        tags.add(TAG_DISTURBANCE);
                        break;
                    case "13":
                        tags.add(TAG_MAINTENANCE);
                        break;
                    default:
                        logger.error("not parsable Tag: " + tString);
                        if (!tagString.isEmpty()) {
                            //makes trouble
                            // tags.add(new NoteTag(tagString, tagString));
                        }

                }

            } catch (Exception ex) {
                logger.error("Error while parsing tags", ex);
            }
        }

        return new ArrayList<>(new HashSet<>(tags));
    }

    public static List<NoteTag> getAllTags() {
        ArrayList<NoteTag> arrayList = new ArrayList<>();
        arrayList.add(TAG_AUDIT);
        arrayList.add(TAG_TASK);
        arrayList.add(TAG_REPORT);
        arrayList.add(TAG_EVENT);
        arrayList.add(TAG_REMINDER);
        arrayList.add(TAG_ERROR);
        arrayList.add(TAG_QUESTION);
        arrayList.add(TAG_SERVICE);
        arrayList.add(TAG_CHARACTERISTIC);
        arrayList.add(TAG_COMMENT);
        arrayList.add(TAG_MEASURE);
        arrayList.add(TAG_DISTURBANCE);
        arrayList.add(TAG_MAINTENANCE);


        return arrayList;
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

    @Override
    public String toString() {
        return "NoteTag{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NoteTag) {
            return ((NoteTag) obj).getId().equals(this.getId());
        }
        return false;
    }
}
