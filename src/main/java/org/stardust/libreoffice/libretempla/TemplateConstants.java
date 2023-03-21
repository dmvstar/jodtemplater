/*
 *
 *  $Id: TemplateConstants.java,v 0.0.2 2007/10/02 17:20 sdv Exp $
 *
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               TemplateConstants.java
 *  Description :        Constants for packeage
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
 * Created on 3 жовтня 2007, 21:18
 *
 */

package org.stardust.libreoffice.libretempla;

/**
 *
 * @author sdv
 */
public interface TemplateConstants {
    
    public static final String TEMPLATE_FILE_KEY = "templateFile";
    public static final String TEMPLATE_DATA_KEY = "data";
    public static final String TEMPLATE_DATA_HEADER = "header";
    public static final String DATA_KEY = "key";
    public static final String DATA_VAL = "val";
    
    public static final int OUT_FILE_TYPE_SAME = 0;
    public static final int OUT_FILE_TYPE_PDF = 1;
      
    /**
     * Template file is non recognized
     */
    public static final int TEMPLATE_FILE_NON = 0;

    /**
     * Template file is xtl - xml like
     */
    public static final int TEMPLATE_FILE_XTL = 1;
    
    /**
     * Template file in old dat format
     */
    public static final int TEMPLATE_FILE_DAT = 2;
    
    /**
     * Fill patterns like <tumbutu>
     */
    public static final int FILL_PATTERN = 1;
    /**
     * Fill patterns as user defined fields 
     */
    public static final int FILL_FIELDS  = 2;
    
    /**
     * Template mode oowriter
     */
    public static final int TEMPLATE_WORD = 1;
    /**
     * Template mode oocalc
     */
    public static final int TEMPLATE_CALC = 2;
    /**
     * Template mode oocalc
     */
    public static final int INSERT_CALC = 3;
    
    /**
     * Template parse mode for ODT file
     */
    public static final String IDENT_TEMPLATE_WORD_0 = "parserbyodt";
    /**
     * Template parse mode for DOC file
     */
    public static final String IDENT_TEMPLATE_WORD_1 = "parserbyword";
    /**
     * Template parse mode for ODS file
     */
    public static final String IDENT_TEMPLATE_CALC_0 = "parserbyods";
    /**
     * Template parse mode for XLS file
     */
    public static final String IDENT_TEMPLATE_CALC_1 = "parserbyexcel";
    /**
     * Template parse mode for XLS file
     */
    public static final String IDENT_TEMPLATE_CALC_2 = "insert2excel";
    /**
     * Template parse mode for XLS file
     */
    public static final String IDENT_TEMPLATE_CALC_3 = "insert2ods";
    
    
}
