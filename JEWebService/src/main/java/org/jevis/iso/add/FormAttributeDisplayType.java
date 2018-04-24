/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import org.jevis.iso.add.FormAttribute.FormAttributeType;

import static org.jevis.iso.add.FormAttribute.FormAttributeType.Boolean;
import static org.jevis.iso.add.FormAttribute.FormAttributeType.*;
import static org.jevis.iso.add.FormAttribute.FormAttributeType.Double;
import static org.jevis.iso.add.FormAttribute.FormAttributeType.Long;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class FormAttributeDisplayType {

    private FormAttributeType output = null;

    public FormAttributeDisplayType(int primitiveType, String displayType) {

        switch (primitiveType) {
            case 0:
                //String
                if (displayType != null) {
                    switch (displayType) {
                        case ("Text"):
                            //Simple String
                            output = Text;
                            break;
                        case ("Text Area"):
                            //Text Field
                            output = TextArea;
                            break;
                        case ("Password"):
                            //Text Password
                            output = TextPassword;
                            break;
                        case ("Date"):
                            //Date
                            output = Date;
                            break;
                        case ("Date Time"):
                            output = DateTime;
                            break;
                        case ("Schedule"):
                            output = Schedule;
                            break;
                        case ("Time Zone"):
                            output = TimeZone;
                            break;
                        case ("Object Target"):
                            output = ObjectTarget;
                            break;
                        default:
                            break;
                    }
                } else {
                    output = Text;
                }
                break;
            case 1:
                //Double
                //make choices for different type
                output = Double;
                break;
            case 2:
                //Long
                output = Long;
                break;
            case 3:
                //File
                output = File;
                break;
            case 4:
                //Boolean
                output = Boolean;
                break;
            case 5:
                //Selection
                output = Selection;
                break;
            case 6:
                //Multi Selection
                output = MultiSelection;
                break;
            case 7:
                //Password PBKDF2
                output = Password;
                break;
            default:
                break;
        }
    }

    public FormAttributeType getOutput() {

        return output;
    }
}
