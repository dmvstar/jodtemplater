/**
 *
 *  $Id: TemplateDocumentFiller.java, v 0.0.2 2007/10/02 17:20    sdv Exp $
 *  $Id: TemplateLibreFiller.java,    v 0.0.1 2023-03-07 15:05:22 sdv Exp $
 *
 *  Copyright (C) 2006-2007 Dmitri Starzhynski
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               TemplateDocumentFiller.java
 *  File :               TemplateLibreFiller.java
 *  Description :        Replace template strings in OpenOffice
 *  Author's email :     dvstar@users.sourceforge.net
 *                       dmvstar.devel@gmail.com
 *  Author's Website :   http://swirl.sourceforge.net
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

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XText;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XSearchable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Replace in OpenOffice document (writer or calc) template string to values
 * from data file.
 *
 * @author Starjinsky Dmitry
 * @version 0.0.2
 */
public class TemplateLibreFiller implements TemplateConstants {

    /**
     * OpenOffice UNO remote context
     */
    private XComponentContext mxRemoteContext = null;
    /**
     * OpenOffice UNO remote service manager
     */
    private XMultiComponentFactory mxRemoteServiceManager = null;
    private ITemplateDataFile mTemplateDataFile;
    private XDesktop mxDesktop;
    private XComponent mxTemplateComponent;
    private boolean mCloseOnExit = false;
    private boolean mTermOnExit  = false;
    private boolean mHiddenMode  = false; 
        
    private Properties mParams;
   

    /**
     * Main class and main method
     *
     * @param args JSON data file name
     */
    public static void main(String[] args) {
        System.out.println("Hello TemplateLibreFiller ! " + args.length);

        Locale locale_ru_RU = new Locale("ru", "RU");
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.bundle", locale_ru_RU);
        System.out.println(resourceBundle.getString("welcome"));

        TemplateLibreFiller textDocumentsFiller = new TemplateLibreFiller();

        InputStream docTypesInputStream = textDocumentsFiller.getClass().getClassLoader().getResourceAsStream("data/docTypes.json");
        System.out.println(docTypesInputStream);

        try {
            if (args.length > 0) {
                TemplateDataFile templateDataFile = new TemplateDataFile(args[0], OUT_FILE_TYPE_SAME);
                templateDataFile.buildData();
                System.out.println(templateDataFile);
                textDocumentsFiller.mTemplateDataFile = templateDataFile;
                //textDocumentsFiller.templateFieldsDataMap = templateDataFile.createTemplateDataMap();
            } else {
                TemplateExampleData templateExampleData = new TemplateExampleData();
                templateExampleData.buildData();
                textDocumentsFiller.mTemplateDataFile = templateExampleData;
                System.out.println("Hello TemplateLibreFiller ! " + templateExampleData);
                //textDocumentsFiller.templateFieldsDataMap = templateExampleData.createTemplateDataMap();
            }
            textDocumentsFiller.processTemplate(textDocumentsFiller.mTemplateDataFile.getTemplateDocumentFileName());

        } catch (TemplateException | BootstrapException | com.sun.star.uno.Exception | InterruptedException | IOException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("end ...");
            System.exit(0);
        }
    }

    public static void usage() {
        System.out.println("Not enough arguments.");
        System.out.println("  Usage:");
        System.out.println("  TemplateLibreFiller <DataFileName>");
    }

   

    /**
     * Default constructor
     */
    public TemplateLibreFiller() {
        Properties params = new Properties();
        params.setProperty(PARAM_KEY_CLOSEONEXIT, "false");
        params.setProperty(PARAM_KEY_TERMONEXIT, "false");
        params.setProperty(PARAM_KEY_SHOWTEMP, "false");      
        this.mParams = params;
        parceParams();        
    }
    
    /**
     * Constructor with params
     * @param params  
     */
    public TemplateLibreFiller(Properties params){
        this.mParams = params;
        parceParams();               
    }

    private void parceParams() {
        if(mParams.getProperty(PARAM_KEY_CLOSEONEXIT)!=null) {
            this.mCloseOnExit = Boolean.parseBoolean(mParams.getProperty(PARAM_KEY_CLOSEONEXIT));
        }
        if(mParams.getProperty(PARAM_KEY_TERMONEXIT)!=null) {
            this.mTermOnExit  = Boolean.parseBoolean(mParams.getProperty(PARAM_KEY_TERMONEXIT));
        }
        if(mParams.getProperty(PARAM_KEY_HIDEMODE)!=null) {
            this.mHiddenMode  = Boolean.parseBoolean(mParams.getProperty(PARAM_KEY_HIDEMODE));
        }
    }
    
    /**
     * Run process template fill
     *
     * @param templateDocumentFileName
     * @throws TemplateException
     * @throws IOException
     * @throws BootstrapException
     * @throws com.sun.star.uno.Exception
     * @throws InterruptedException
     */
    protected void processTemplate(String templateDocumentFileName)
            throws TemplateException, IOException, BootstrapException, com.sun.star.uno.Exception, InterruptedException {

        String templateDocumentFileURL = mTemplateDataFile.getTemplateDocumentFileURL();
        System.out.println("sTemplateFileUrl = " + templateDocumentFileURL);
        mxTemplateComponent = prepareDocComponentFromTemplate(templateDocumentFileURL);

        XReplaceable xReplaceable;
        com.sun.star.util.XSearchable xSearchable;
        HashMap templateFieldsDataMap = mTemplateDataFile.getTemplateDataMap();

        Iterator keyIterator = templateFieldsDataMap.keySet().iterator();
        //while(keyIterator.hasNext()){
        //    keyIterator.next(); 
        //}
        //Enumeration keys = templateFieldsDataMap.keys();
        xReplaceable = (XReplaceable) UnoRuntime.queryInterface(XReplaceable.class, mxTemplateComponent);
        xSearchable = UnoRuntime.queryInterface(com.sun.star.util.XSearchable.class, mxTemplateComponent);

        int curCount = 0;
        //while (keys.hasMoreElements()) {
        while (keyIterator.hasNext()) {
            //String key = (String) keys.nextElement();
            String key = (String) keyIterator.next();
            Object oval = templateFieldsDataMap.get(key);
            String val = (String) oval.toString();
            if (oval instanceof String) {
                //Thread.sleep(500);
                if (findWordTemplate(xSearchable, "${" + key + "}")) {
                    replaceWordTemplate(++curCount, xReplaceable, "${" + key + "}", val);
                }
            } else {
                if (oval instanceof ArrayList) {
                    replaceTableTemplate(mxTemplateComponent, xReplaceable, key, (ArrayList) oval);
                }
            }
        }
        saveOutputDocument();
    }

    /**
     * Replace single template in document
     *
     * @param curCount
     * @param xReplaceable
     * @param frStr Temlate string like ${user}
     * @param toStr Value string for template like star
     */
    protected void replaceWordTemplate(int curCount, XReplaceable xReplaceable, String frStr, String toStr) {
        XReplaceDescriptor xReplaceDescr;
        System.out.println("[" + curCount + "] Replace " + frStr + " -> " + toStr);
        //xReplaceable = (com.sun.star.util.XReplaceable) UnoRuntime.queryInterface(
        //        com.sun.star.util.XReplaceable.class, xTemplateComponent);
        // You need a descriptor to set properies for Replace
        if (xReplaceable != null) {
            xReplaceDescr = (XReplaceDescriptor) xReplaceable.createReplaceDescriptor();
            xReplaceDescr.setSearchString(frStr);
            xReplaceDescr.setReplaceString(toStr);
            // Replace all words
            xReplaceable.replaceAll(xReplaceDescr);
        }
    }

    /**
     * Replacr table template with array data
     *
     * @param xTemplateComponent
     * @param xReplaceable
     * @param tableName name of table
     * @param tableItems array of rows
     * @throws WrappedTargetException
     * @throws IndexOutOfBoundsException
     * @throws org.stardust.libreoffice.libretempla.TemplateException
     */
    protected void replaceTableTemplate(XComponent xTemplateComponent,
            XReplaceable xReplaceable,
            String tableName,
            ArrayList<HashMap> tableItems)
            throws WrappedTargetException, IndexOutOfBoundsException, TemplateException {

        XTextTablesSupplier xTablesSupplier = (XTextTablesSupplier) UnoRuntime.queryInterface(XTextTablesSupplier.class, xTemplateComponent);
        XNameAccess xNamedTables = xTablesSupplier.getTextTables();
        //System.out.println("xNamedTables " + xNamedTables);
        System.out.println("replaceTableTemplate  " + tableName + " src_count:[" + tableItems.size() + "]");
        try {
            Object oTable = xNamedTables.getByName(tableName);
            if (oTable != null) {
                //System.out.println("oTable " + oTable);
                XTextTable xTable = (XTextTable) UnoRuntime.queryInterface(XTextTable.class, oTable);
                XTableRows xRows = xTable.getRows();
                XTableColumns xCols = xTable.getColumns();
                System.out.println("xTable " + xTable + " rows:" + xRows.getCount() + " cols:" + xCols.getCount());
                //XRow xRow = xRows.getByIndex(1);
                xRows.insertByIndex(2, tableItems.size());
                XCellRange xCellRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, oTable);
                int row = 2, count = 1;
                System.out.println("tableItems " + tableItems.size());
                for (int i = 0; i < tableItems.size(); i++) { // data
                    System.out.println("r["+i+"] process row");
                    // data item    
                    //Object items = tableItems.get(i);   
                    //System.out.println("items " + items.getClass());
                    //System.out.println("items " + items);
                    HashMap<String, String> items = tableItems.get(i);
                    Object keys[] = items.keySet().toArray();
                    
                    for (int j = 0; j < xCols.getCount(); j++) { // cols           
                        XCell xCellTemp = xCellRange.getCellByPosition(j, 1);
                        XText xTextTemp = (XText) UnoRuntime.queryInterface(XText.class, xCellTemp);
                        XCell xCell = xCellRange.getCellByPosition(j, i + row);
                        XText xText = (XText) UnoRuntime.queryInterface(XText.class, xCell);                        
                        String cellDstText = xTextTemp.getString();                        
                    System.out.println("    r[" + i + "]c["+ j +"] cellDst=" + cellDstText);
                        if (xTextTemp.getString().equalsIgnoreCase("#{i}")) {
                            cellDstText = "" + count;
                        } else {
                            // for (int k = 0; k < keys.length; k++) { // keys
                            for (Object key : keys) {
                                // keys
                                String keyToReplace = "#{" + key + "}";
                                String valToReplace = items.get(key.toString());
                                if (cellDstText.equalsIgnoreCase(keyToReplace)) {
                                    System.out.println("    r[" + i + "]c["+ j +"] " + keyToReplace + " -> " + valToReplace);
                                    cellDstText = items.get(key.toString());
                                }
                                if (cellDstText.contains(keyToReplace)) {
                                    cellDstText = cellDstText.replace(keyToReplace, items.get(key.toString()));
                                }
                            }                            
                        }
                        xText.setString(cellDstText);
                    }
                    count++;
                }
                xRows.removeByIndex(1, 1);
            }
        } catch (NoSuchElementException ex) {
            throw new TemplateException("No elements for replace", ex);
        }
    }

    /**
     * Load a document as template
     *
     * @param loadUrl Url for template document
     * @return Loaded instance of office document for UNO
     * @throws com.sun.star.comp.helper.BootstrapException
     * @throws com.sun.star.uno.Exception
     */
    protected XComponent prepareDocComponentFromTemplate(String loadUrl) throws BootstrapException, com.sun.star.uno.Exception {
        // get the remote service manager
        mxRemoteServiceManager = this.prepareRemoteServiceManager();
        // retrieve the Desktop object, we need its XComponentLoader
        Object oDesktop = mxRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", mxRemoteContext);
        XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);

        // define load properties according to com.sun.star.document.MediaDescriptor
        // the boolean property AsTemplate tells the office to create a new document
        // from the given file
        PropertyValue[] loadProps = new PropertyValue[2];
        loadProps[0] = new PropertyValue();
        loadProps[0].Name = "AsTemplate";
        loadProps[0].Value = Boolean.TRUE;
        
        if(mHiddenMode) {
            loadProps[1] = new PropertyValue();
            loadProps[1].Name = "Hidden";
            loadProps[1].Value = Boolean.TRUE;
        }
        // load
        mxDesktop = mxDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, oDesktop);
        return xComponentLoader.loadComponentFromURL(loadUrl, "_blank", 0, loadProps);
    }

    /**
     * Getting remote service manager for OpenOffice
     *
     * @throws java.lang.Exception Exception
     * @return Instance of remote service manager
     */
    private XMultiComponentFactory prepareRemoteServiceManager() throws BootstrapException {
        //String oooExeFolder = "/usr/lib/libreoffice/program";
        if (mxRemoteContext == null && mxRemoteServiceManager == null) {
            // get the remote office context. If necessary a new office
            // process is started
            mxRemoteContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
            //mxRemoteContext = BootstrapSocketConnector.bootstrap(oooExeFolder);
            System.out.println("Connected to a running office ...");
            mxRemoteServiceManager = mxRemoteContext.getServiceManager();
        }
        return mxRemoteServiceManager;
    }

    private boolean findWordTemplate(XSearchable xSearchable, String sSearchString) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        boolean ret = false;
        com.sun.star.util.XSearchDescriptor xSearchDescriptor;
        com.sun.star.uno.XInterface xSearchInterface;
        xSearchDescriptor = xSearchable.createSearchDescriptor();
        xSearchDescriptor.setSearchString(sSearchString);
        com.sun.star.beans.XPropertySet xPropertySet;
        xPropertySet = UnoRuntime.queryInterface(
                com.sun.star.beans.XPropertySet.class, xSearchDescriptor);
        xPropertySet.setPropertyValue("SearchRegularExpression",
                Boolean.FALSE);
        xSearchInterface = (com.sun.star.uno.XInterface) xSearchable.findFirst(xSearchDescriptor);
        //System.out.println("xSearchInterface " + xSearchInterface);
        if (xSearchInterface != null) {
            ret = true;
        }
        return ret;
    }

    /**
     * Save result document
     *
     * @throws CloseVetoException
     * @throws com.sun.star.io.IOException
     */
    public void saveOutputDocument() throws CloseVetoException, com.sun.star.io.IOException {
        String sLoadUrl = mTemplateDataFile.getTemplateDocumentFileURL();
        String sSaveUrl = mTemplateDataFile.getOutputDocumentFileURL();
        //sSaveUrl = "file:////home/devel/repo/my/JODTemplater/res/szablon-ooooo.odt";
        if (sSaveUrl != null) {
            com.sun.star.frame.XStorable oDocToStore
                    = UnoRuntime.queryInterface(com.sun.star.frame.XStorable.class,
                            mxTemplateComponent);

            com.sun.star.beans.PropertyValue[] propertyValues
                    = new com.sun.star.beans.PropertyValue[2];
            propertyValues[0] = new com.sun.star.beans.PropertyValue();
            propertyValues[0].Name = "Overwrite";
            propertyValues[0].Value = Boolean.TRUE;
            if ("pdf".equals(mTemplateDataFile.getOutputDocumentFileExt())) {
                propertyValues[1] = new com.sun.star.beans.PropertyValue();
                propertyValues[1].Name = "FilterName";
                propertyValues[1].Value = "writer_pdf_Export";
            }
            String message = String.format("CloseOnExit=%b \nDocument: %s\naved As:", mCloseOnExit,sLoadUrl,sSaveUrl);
            //System.out.println("\nDocument: \"" + sLoadUrl + "\"\nSaved As: \""
            //        + sSaveUrl + "\"\n");
            System.out.println(message);
            //oDocToStore.storeAsURL(sSaveUrl, propertyValue);
            oDocToStore.storeToURL(sSaveUrl, propertyValues);
            
            // https://wiki.openoffice.org/wiki/Documentation/DevGuide/OfficeDev/Using_the_Desktop            
            if (mCloseOnExit) {
                com.sun.star.util.XCloseable xCloseable = UnoRuntime.queryInterface(com.sun.star.util.XCloseable.class,
                    oDocToStore);
                if (xCloseable != null) {
                    xCloseable.close(false);
                    if(mTermOnExit) {
                        mxDesktop.terminate();
                    }
                            
                    System.out.println("Desktop.terminate!");
                } else {
                    com.sun.star.lang.XComponent xComp = UnoRuntime.queryInterface(
                            com.sun.star.lang.XComponent.class, oDocToStore);
                    xComp.dispose();
                }
                System.out.println("Document closed!");
            }
        }
    }

}
