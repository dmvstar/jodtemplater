/** 
 *
 *  $Id: TemplateExampleData.java,v 0.0.2 2023-03-19 11:32:34  sdv Exp $
 *  
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               TemplateExampleData.java
 *  Description :        Example data for template
 *  Author's email :     dmvstar.devel@gmail.com
 *  Author's Website :  
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Created on 2023-03-19 11:32:34
 *
 */
package org.stardust.libreoffice.libretempla;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TemplateExampleData implements ITemplateDataFile {

    public static String templateTestName = "res/szablon-0.odt";
    private String templateDocumentFileURL;
    private HashMap<String, Object> templateDataMap;

    @Override
    public void buildData() throws TemplateException {
        try {
            templateDocumentFileURL = prepareTemplateFileUrl(templateTestName);
            templateDataMap = createTemplateDataMap();
        } catch (IOException ex) {
            Logger.getLogger(TemplateExampleData.class.getName()).log(Level.SEVERE, null, ex);
            throw new TemplateException(ex);
        }
    }

    @Override
    public HashMap<String, Object> createTemplateDataMap() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        //System.out.println("Start :  " +  dtf.format(now));

        HashMap<String, Object> input = new HashMap<>();
        input.put("Model", "Mercedes");
        input.put("Rok", "2023");
        input.put("Title", "NEW");
        input.put("Person", "Samuel");
        input.put("Opony", "Dębica Navigator 3");
        input.put("DateTime", dtf.format(now));
        input.put("TemplateName", templateTestName);

        HashMap<String, String> item;
        ArrayList models = new ArrayList<HashMap>();
        item = new HashMap<>();
        item.put("Model", "Mercedes L400");
        item.put("Year", "2008");
        item.put("Cost", "250000.00");
        models.add(item);
        item = new HashMap<>();
        item.put("Model", "Opel Astra");
        item.put("Year", "2007");
        item.put("Cost", "150000.00");
        models.add(item);
        item = new HashMap<>();
        item.put("Model", "Hyundai Matrix");
        item.put("Year", "2010");
        item.put("Cost", "250000.00");
        models.add(item);
        input.put("models", models);
        return input;
    }

    @Override
    public String getOutputDocumentFileName() {
        return templateTestName;
    }

    @Override
    public String getTemplateDocumentFileName() {
        return templateTestName;
    }

    @Override
    public String getTemplateDocumentFileURL() {
        return templateDocumentFileURL;
    }

    @Override
    public HashMap<String, Object> getTemplateDataMap() {
        return templateDataMap;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("TemplateDataFile").append("\n");
        ret.append("    templateDocumentFileName=").append(getTemplateDocumentFileName()).append("\n");
        ret.append("    templateDocumentFileURL=").append(getTemplateDocumentFileURL()).append("\n");
        ret.append("    outputDocumentFileName=").append(getOutputDocumentFileName()).append("\n");

        return ret.toString();
    }
    
    
    protected String prepareTemplateFileUrl(String templateDocumentFileName) throws IOException {
        String ret;
        // load template with User fields and bookmark
        java.io.File sourceFile = new java.io.File(templateDocumentFileName);
        StringBuilder sTemplateFileUrl = new StringBuilder("file:///");
        sTemplateFileUrl.append(sourceFile.getCanonicalPath().replace('\\', '/'));
        ret = sTemplateFileUrl.toString();
        return ret;
    }
    
}
