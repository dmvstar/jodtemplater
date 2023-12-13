/**
 *
 *  $Id: TemplateDataFile.java,v 0.0.2 2023-03-19 11:32:34 sdv Exp $
 *
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               TemplateDataFile.java
 *  Description :        Example data file parcer
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
 * Created on 31 серпня 2007, 10:32
 *
 */
package org.stardust.libreoffice.libretempla;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static org.stardust.libreoffice.libretempla.TemplateConstants.DATA_KEY;
import static org.stardust.libreoffice.libretempla.TemplateConstants.DATA_VAL;
import static org.stardust.libreoffice.libretempla.TemplateConstants.TEMPLATE_DATA_KEY;

/**
 *
 * @author sdv
 */
public class TemplateDataFile implements TemplateConstants, ITemplateDataFile {

    /**
     * File with data for fill
     */
    private final String mDataFileName;
    private Path mDataFilePath;

    private String mTemplateDataString;
    private JSONObject mTemplateDataJson;
    private String mTemplateDocumentFileName;
    private String mTemplateDocumentFileURL;
    private String mOutputDocumentFileName;
    private final int mOutputDocumentFileType;
    private Properties mTemplateParams;    
    private HashMap<String, Object> mTemplateDataMap;
    private String mOutputDocumentFileExt;

    public TemplateDataFile(String aDataFileName, int aOutputDocumentFileType) {
        this.mDataFileName = aDataFileName;
        this.mOutputDocumentFileType = aOutputDocumentFileType;
    }

    @Override
    public void buildData() throws TemplateException {
        if (mDataFileName == null) {
            throw new TemplateException("Not defined template data file name.");
        }
        mDataFilePath = Path.of(mDataFileName);
        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream;
        try {
            stream = Files.lines(mDataFilePath, StandardCharsets.UTF_8);
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            mTemplateDataString = contentBuilder.toString();
            if (getTemplateDataString() != null) {
                mTemplateDataJson = prepareTemplateDataJson(getTemplateDataString());
                mTemplateParams = prepareParams();
                mTemplateDocumentFileName = prepareTemplateName();
                mOutputDocumentFileExt  = prepareOutputDocumentFileExt();
                mOutputDocumentFileName = prepareOutputDocumentFileName();                
                mTemplateDataMap = createTemplateDataMap();
            } else {
                throw new TemplateException("No TemplateDataString");
            }
        } catch (IOException ex) {
            throw new TemplateException(ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("TemplateDataFile").append("\n");
        ret.append("    templateDocumentFileName=").append(getTemplateDocumentFileName()).append("\n");
        ret.append("    templateDocumentFileURL=").append(getTemplateDocumentFileURL()).append("\n");
        ret.append("    outputDocumentFileName=").append(getOutputDocumentFileName()).append("\n");

        ret.append("    templateDataString=").append(getTemplateDataString()).append("\n");

        return ret.toString();
    }

    /**
     * Prepare output file name
     *
     * @return name
     * @throws TemplateException
     */
    private String prepareOutputDocumentFileName() throws TemplateException {
        String ret = getOutputDocumentFileName();
        if (getTemplateDocumentFileName() == null) {
            throw new TemplateException("Not defined template document file.");
        }
        if (getOutputDocumentFileName() == null) {
            int dotPos = getTemplateDocumentFileName().lastIndexOf('.');
            String tempExt = "";
            String outNam = getTemplateDocumentFileName();
            if (dotPos > 0) {
                tempExt = getTemplateDocumentFileName().substring(dotPos + 1);
                outNam = getTemplateDocumentFileName().substring(0, dotPos);
            }
            String outExt = "";
            if (getOutputDocumentFileType() == OUT_FILE_TYPE_SAME) {
                outExt = tempExt;
            }
            if (getOutputDocumentFileType() == OUT_FILE_TYPE_PDF) {
                outExt = "pdf";
            }
            if(mOutputDocumentFileExt!=null) {
                outExt = mOutputDocumentFileExt;
            }
            mOutputDocumentFileName = outNam + "-out." + outExt;
            ret = getOutputDocumentFileName();
        }
        return ret;
    }

    /**
     * @return the mTemplateDocumentFileName
     */
    @Override
    public String getTemplateDocumentFileName() {
        return mTemplateDocumentFileName;
    }

    /**
     * @return the mTemplateDocumentFileURL
     */
    @Override
    public String getTemplateDocumentFileURL() {
        return mTemplateDocumentFileURL;
    }

    /**
     * @return the mOutputDocumentFileType
     */
    public int getOutputDocumentFileType() {
        return mOutputDocumentFileType;
    }

    /**
     * @return the mOutputDocumentFileName
     */
    @Override
    public String getOutputDocumentFileName() {
        return mOutputDocumentFileName;
    }

    /**
     * @return the mTemplateDataString
     */
    public String getTemplateDataString() {
        return mTemplateDataString;
    }

    /**
     * @return the mTemplateDataJson
     */
    public JSONObject getTemplateDataJson() {
        return mTemplateDataJson;
    }

    @Override
    public HashMap createTemplateDataMap() throws TemplateException {
        HashMap ret = new HashMap();
        if (mTemplateDataJson != null) {
            JSONArray data = mTemplateDataJson.getJSONArray(TEMPLATE_DATA_KEY);
            for (int i = 0; i < data.length(); i++) {
                //Iterator<?> keys = data.getJSONObject(i).keys();
                JSONObject itemj = data.getJSONObject(i);
            System.out.println("    Item: " + itemj);
                String key = itemj.getString(DATA_KEY);
                Object val = itemj.get(DATA_VAL);
                Object params = null;
                try {
                    params = itemj.get(DATA_PARAMS);
            System.out.println("        Item.params: " + params);
                } catch (JSONException ex) {
                    
                } 
                //System.out.println("        Key: " + key);
                //System.out.println("        Class: " + val.getClass());
                //System.out.println("        Value: " + val);
                if (val instanceof String) {
                    if (((String) val).contains("NOW()")) {
                        val = getNowDateTime();
                    }
                    ret.put(key, val);
                }
                if (val instanceof JSONArray) {
                    JSONArray valj = (JSONArray) val;
                    ArrayList list = new ArrayList();
                    for (int ii = 0; ii < valj.length(); ii++) {
                        JSONArray itemsj = valj.getJSONArray(ii);
                        //System.out.println("        LValue: " + itemsj);
                        HashMap itemsa = new HashMap();
                        for (int j = 0; j < itemsj.length(); j++) {
                            JSONObject itemaj = itemsj.getJSONObject(j);
                            //System.out.println("            LValue: " + itemaj);
                            String akey = itemaj.getString(DATA_KEY);
                            String aval = itemaj.getString(DATA_VAL);
                            itemsa.put(akey, aval);
                        }
                        list.add(itemsa);
                    }
                    ret.put(key, list);
                }
            }
        } else {
            throw new TemplateException("No JSON dara");
        }
        //throw new UnsupportedOperationException("Not supported yet.");
        return ret;
    }

    @Override
    public HashMap<String, Object> getTemplateDataMap() {
        return mTemplateDataMap;
    }

    /**
     * Get current date time
     *
     * @return string dd.MM.yyyy HH:mm:ss
     */
    private String getNowDateTime() {
        String ret;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        ret = dtf.format(now);
        return ret;
    }

    /**
     * Create JSON from string data
     *
     * @param aTemplateDataString
     * @return JSON data
     */
    private JSONObject prepareTemplateDataJson(String aTemplateDataString) {
        JSONObject ret;
        ret = new JSONObject(aTemplateDataString);
        return ret;
    }

    /**
     * Prepare template file name from JSON data
     *
     * @return name
     * @throws TemplateException
     */
    private String prepareTemplateName() throws TemplateException {
        String ret;
        if (getTemplateDataJson() == null) {
            throw new TemplateException("Not defined template data file.");
        }
        ret = getTemplateDataJson().getString(TEMPLATE_FILE_KEY);
        if (ret != null) {
            mTemplateDocumentFileName = ret;
            try {
                mTemplateDocumentFileURL = prepareTemplateFileUrl(getTemplateDocumentFileName());
            } catch (IOException ex) {
                throw new TemplateException("Not defined template data file Url.");
            }
            //System.out.println("TemplateDataFile getTemplateName " + mTemplateDocumentFileURL);
        }
        return ret;
    }

    private String prepareOutputDocumentFileExt() throws TemplateException {
        String ret = "odt"; 
        if (getTemplateDataJson() == null) {
            throw new TemplateException("Not defined template data file.");
        }   
        System.out.println("prepareOutputDocumentFileExt 1 " + ret);   
        try {
            JSONObject json = getTemplateDataJson();
            ret = json.getString(TEMPLATE_OUTEXT_KEY);
        } 
        catch (JSONException e) {
            ret = "odt"; 
        }        
        System.out.println("prepareOutputDocumentFileExt 2 " + ret);      
        return ret;
    }
    
    
    
    /**
     * Prepare template file Url for UNO
     *
     * @param templateDocumentFileName
     * @return URL
     * @throws IOException
     */
    protected String prepareTemplateFileUrl(String templateDocumentFileName) throws IOException {
        String ret;
        // load template with User fields and bookmark
        java.io.File sourceFile = new java.io.File(templateDocumentFileName);
        StringBuilder sTemplateFileUrl = new StringBuilder("file:///");
        sTemplateFileUrl.append(sourceFile.getCanonicalPath().replace('\\', '/'));
        ret = sTemplateFileUrl.toString();
        return ret;
    }

    @Override
    public String getOutputDocumentFileURL() {
        String ret;
        try {
            // load template with User fields and bookmark
            java.io.File sourceFile = new java.io.File(mOutputDocumentFileName);
            StringBuilder sTemplateFileUrl = new StringBuilder("file:///");
            sTemplateFileUrl.append(sourceFile.getCanonicalPath().replace('\\', '/'));
            ret = sTemplateFileUrl.toString();
        } catch (IOException ex) {
            ret = null;
        }
        return ret;
    }
    
    @Override
    public String getOutputDocumentFileExt() {        
        return mOutputDocumentFileExt;
    }

    private Properties prepareParams() throws TemplateException {
        Properties params = new Properties();
        if (getTemplateDataJson() == null) {
            throw new TemplateException("Not defined template data file.");
        }
        params.setProperty(PARAM_KEY_CLOSEONEXIT, "false");
        params.setProperty(PARAM_KEY_TERMONEXIT, "false");
        params.setProperty(PARAM_KEY_SHOWTEMP, "false");      
        String propValue;
        
        JSONObject json = getTemplateDataJson();
        propValue = getSafeJsonValue(json,PARAM_KEY_CLOSEONEXIT);
        if( propValue != null) {
            params.setProperty(PARAM_KEY_CLOSEONEXIT, propValue);
        }
        propValue = getSafeJsonValue(json,PARAM_KEY_TERMONEXIT);
        if( propValue != null) {
            params.setProperty(PARAM_KEY_TERMONEXIT, propValue);
        }
        propValue = getSafeJsonValue(json,PARAM_KEY_SHOWTEMP);
        if( propValue != null) {
            params.setProperty(PARAM_KEY_SHOWTEMP, propValue);
        }
        return params;
    }
    
    String getSafeJsonValue(JSONObject json, String key){
        String ret = null;
        try {
            ret = json.getString(key);
        } catch (JSONException ex) {
            ret = null;
        }
        return ret;
    }

    @Override
    public Properties getTemplateParams() {
        return mTemplateParams;
    }

}
