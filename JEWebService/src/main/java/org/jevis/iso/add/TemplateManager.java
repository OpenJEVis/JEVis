/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.jevis.rest.Config;

import java.io.File;
import java.io.IOException;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class TemplateManager {

    private Configuration cfg = new Configuration();

    public void setPath(File dir) throws IOException {
        cfg.setDirectoryForTemplateLoading(Config.getFreemarkerDir());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public Template getTemplate(String filename) throws IOException {
        return cfg.getTemplate(filename);
    }

}
