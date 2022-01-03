/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tom_roush.pdfbox.contentstream.operator.color;

import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColorSpace;

/**
 * SC: Sets the colour to use for stroking stroking operations.
 *
 * @author John Hewson
 */
public class SetStrokingColor extends SetColor
{
    /**
     * Returns the stroking color.
     *
     * @return The stroking color.
     */
    @Override
    protected PDColor getColor()
    {
        return context.getGraphicsState().getStrokingColor();
    }

    /**
     * Sets the stroking color.
     *
     * @param color The new stroking color.
     */
    @Override
    protected void setColor(PDColor color)
    {
        context.getGraphicsState().setStrokingColor(color);
    }

    /**
     * Returns the stroking color space.
     *
     * @return The stroking color space.
     */
    @Override
    protected PDColorSpace getColorSpace()
    {
        return context.getGraphicsState().getStrokingColorSpace();
    }

    @Override
    public String getName()
    {
        return "SC";
    }
}
