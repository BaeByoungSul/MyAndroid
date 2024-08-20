/*
 * Copyright (C) Apulsetech,co.ltd
 * Apulsetech, Shenzhen, China
 *
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice is
 * included in all copies of any software which is or includes a copy or
 * modification of this software and in all copies of the supporting
 * documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY. IN PARTICULAR, NEITHER THE AUTHOR NOR APULSETECH MAKES ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY OF
 * THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 *
 *
 * Project: ⍺X11 SDK
 *
 * File: TagItem.java
 * Date: 2016.11.17
 * Author: Tony Park, tonypark@apulsetech.com
 *
 ****************************************************************************
 */
package com.myapp.testrfid.adapter
import com.myapp.testrfid.util.MovingAverage
data class  TagSaveItem(
    val tagHexValue: String,
    val tagType: String,
    val tagValue: String,
    var rssiValue: String?,
    var rssiMv: MovingAverage? =MovingAverage(20),
    var dupCount: Int
) {
    constructor(tagHexValue: String, tagType: String, tagValue: String, dupCount: Int)
            : this(tagHexValue, tagType, tagValue,"",MovingAverage(20), dupCount )
}