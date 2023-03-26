/* 
 *
 *  $Id: ITemplateDataFile.java,v 0.0.2 2023-03-19 11:32:34  sdv Exp $
 *  
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               ITemplateDataFile.java
 *  Description :        Common interface for file data
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

import java.util.HashMap;

public interface ITemplateDataFile {

    public void buildData() throws TemplateException;

    public HashMap createTemplateDataMap() throws TemplateException;

    HashMap<String, Object> getTemplateDataMap();

    public String getOutputDocumentFileURL();
    
    public String getOutputDocumentFileName();
    
    public String getOutputDocumentFileExt();

    public String getTemplateDocumentFileName();

    public String getTemplateDocumentFileURL();
}
