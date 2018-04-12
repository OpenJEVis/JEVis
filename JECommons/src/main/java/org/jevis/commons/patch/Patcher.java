/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.patch;

/**
 *
 * @author broder
 */
public class Patcher {

    //applies all patches of a given type
    public <T extends Enum<T> & Patch> void applyPatches(Class<T> patchSet) {
        for (Patch p : patchSet.getEnumConstants()) {
            p.apply();
        }
    }

    //undo all patches of a given type
    public <T extends Enum<T> & Patch> void undoPatches(Class<T> patchSet) {
        for (Patch p : patchSet.getEnumConstants()) {
            p.undo();
        }
    }

    //applys a specific version of a patch
    public <T extends Enum<T> & Patch> void applyPatch(Class<T> patchSet, String version) {
        for (Patch p : patchSet.getEnumConstants()) {
            if(p.getVersion().equals(version)){
                p.apply();
            }
        }
    }

}
