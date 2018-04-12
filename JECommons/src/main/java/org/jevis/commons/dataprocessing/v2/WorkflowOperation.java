/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.v2;

import java.awt.image.BufferedImage;

/**
 * Interface for the Dataprocessing Workflow Operations. This operations will
 * allow to implemnt some logic into the workflow like "if Attribute is from
 * Type do .. else .." .
 *
 * @author Florian Simon
 */
public interface WorkflowOperation {

    /**
     * Uniqe id of the operation within this workflow.
     *
     * @return
     */
    public String getID();

    /**
     * Retuns an icon for the GUI Workflow builder.
     *
     * @return
     */
    public BufferedImage getIcon();
}
