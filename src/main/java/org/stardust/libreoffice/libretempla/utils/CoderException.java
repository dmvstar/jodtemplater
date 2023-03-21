/*
 *
 *  $Id: CoderException,v 0.0.2 2007/10/02 17:20 sdv Exp $
 *
 *  Copyright (C) 2006-2007 Dmitry Starjinsky
 *
 *  File :               CoderException.java
 *  Description :
 *  Author's email :     dvstar@users.sourceforge.net
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
 * Created on 9 жовтня 2007, 15:38
 *
 */

package org.stardust.libreoffice.libretempla.utils;

/**
 *
 * @author Sdv
 */
public class CoderException  extends Throwable {
    
    /**
     * 
     * Creates a new instance of TemplateException
     */
    public CoderException() {
        super();
    }
    
    /**
     * Creates a new instance of TemplateException 
     * 
     * 
     * @param cause super
     */
    public CoderException(Throwable cause) {
        super(cause);
    }

    public CoderException(String cause) {
        super(cause);
    }
    
    
}
