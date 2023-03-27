/** 
 *
 *  $Id: TemplateException.java,v 0.0.2 2007/10/02 17:20 sdv Exp $
 *  
 *  Copyright (C) 2006-2007 Dmitry Starzhynski
 *  Copyright (C) 2023 Dmitri Starzyński
 *
 *  File :               ReplaceTextDocuments.java
 *  Description :        Exception
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

/**
 * Exception on template process
 * @author sdv
 */
public class TemplateException extends Throwable {
    
    /**
     * Creates a new instance of TemplateException
     */
    public TemplateException() {
        super();
    }
    
    /**
     * Creates a new instance of TemplateException
     * @param info
     */
    public TemplateException(String info) {
        super(info);
    }
    
    /**
     * Creates a new instance of TemplateException 
     * @param cause super
     */
    public TemplateException(Throwable cause) {
        super(cause);
    }
    /**
     * Creates a new instance of TemplateException 
     * @param cause super
     * @param info 
     */
    public TemplateException(String info, Throwable cause) {
        super(info, cause);
    }
    
}
