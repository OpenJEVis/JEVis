package org.jevis.jeconfig.application.jevistree.filter;

import javafx.scene.control.TreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

import java.util.Comparator;


/**
 * Default comparator for the JEVisTree
 * <p>
 * <p>
 * Default JEVisTree sort.
 * Order:
 * 1. Attributes before Objects
 * 2. Directory's before other JEVisClasses
 * 3. objects compared by JEVisClasses
 * 4. Objects compared by name
 * 5. Attributes by name
 */
public class TreeItemComparator implements Comparator<TreeItem<JEVisTreeRow>> {

    private static TreeItemComparator instance;

    public static TreeItemComparator getInstance() {
        if (TreeItemComparator.instance == null) {
            TreeItemComparator.instance = new TreeItemComparator();
        }
        return TreeItemComparator.instance;
    }

    @Override
    public int compare(TreeItem<JEVisTreeRow> o1, TreeItem<JEVisTreeRow> o2) {
        try {
            JEVisTreeRow row1 = o1.getValue();
            JEVisTreeRow row2 = o2.getValue();

            if (row1.getType() == row2.getType()) {

                /** if they are objects **/
                if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {

                    boolean o1isDir = DirectoryHelper.getInstance(row1.getJEVisObject().getDataSource()).getDirectoryNames().contains(row1.getJEVisObject().getJEVisClassName());
                    boolean o2isDir = DirectoryHelper.getInstance(row1.getJEVisObject().getDataSource()).getDirectoryNames().contains(row2.getJEVisObject().getJEVisClassName());

                    /** Check if one of this is an directory, if it will be first **/
                    if (o1isDir && !o2isDir) {
                        return -1;
                    } else if (!o1isDir && o2isDir) {
                        return 1;
                    }

                    /** Sort by Classname **/
                    int className = row1.getJEVisObject().getJEVisClassName().compareTo(row2.getJEVisObject().getJEVisClassName());
                    if (className == 0) {
                        /** if same class sort by name **/
                        return row1.getJEVisObject().getName().compareTo(row2.getJEVisObject().getName());
                    } else {
                        return className;
                    }


                } else if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                    /** attributes are sorted by name **/
                    return row1.getJEVisAttribute().getName().compareTo(row2.getJEVisAttribute().getName());
                }


            } else {/** one is object the other attribute, Object before attribute **/
                if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
                    return 1;
                } else {
                    return -1;
                }
            }
        } catch (Exception ex) {
        }

        /** if something goes wrong return equal **/
        return 0;
    }
}
