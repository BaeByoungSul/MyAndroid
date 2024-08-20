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
 * File: MovingAverage.java
 * Date: 2018.10.16
 * Author: Tony Park, tonypark@apulsetech.com
 *
 ****************************************************************************
 */
package com.myapp.testrfid.util

import java.util.concurrent.ArrayBlockingQueue



class MovingAverage(size: Int) {
    protected var mSamples: ArrayBlockingQueue<Float>

    init {
        mSamples = ArrayBlockingQueue(size)
    }

    fun add(sample: Float) {
        if (mSamples.remainingCapacity() <= 0) {
            mSamples.poll()
        }
        mSamples.add(sample)
    }

    fun clear() {
        mSamples.clear()
    }

    fun average(): Float {
        val size = mSamples.size
        var total = 0f
        for (sample in mSamples) {
            total += sample
        }
        return total / size
    }

    companion object {
        @Suppress("unused")
        private val TAG = "MovingAverage"

        @Suppress("unused")
        private val D = true
    }
}
