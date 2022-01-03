/*
 * Copyright 2013, Edmodo, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.fluidtouch.noteshelf.document.imageedit.util;

import android.graphics.RectF;

import androidx.annotation.NonNull;

/**
 * Utility class for handling calculations involving a fixed aspect ratio.
 */
public class AspectRatio {

    public static int mAspectRatioX = 1;
    public static int mAspectRatioY = 1;

    /**
     * Calculates the aspect ratio given a rectangle.
     */
    public static float calculateAspectRatio(float left, float top, float right, float bottom) {
        final float width = right - left;
        final float height = bottom - top;
        return mAspectRatioX / mAspectRatioY;
    }

    /**
     * Calculates the aspect ratio given a rectangle.
     */
    public static float calculateAspectRatio(@NonNull RectF rect) {
        return mAspectRatioX / mAspectRatioY;
    }

    /**
     * Calculates the x-coordinate of the left edge given the other sides of the rectangle and an
     * aspect ratio.
     */
    public static float calculateLeft(float top, float right, float bottom, float targetAspectRatio) {

        final float height = bottom - top;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // right - left = targetAspectRatio * height
        return right - (getTargetAspectRatio() * height);
    }

    /**
     * Calculates the y-coordinate of the top edge given the other sides of the rectangle and an
     * aspect ratio.
     */
    public static float calculateTop(float left, float right, float bottom, float targetAspectRatio) {

        final float width = right - left;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // height = width / targetAspectRatio
        // bottom - top = width / targetAspectRatio
        return bottom - (width / getTargetAspectRatio());
    }

    /**
     * Calculates the x-coordinate of the right edge given the other sides of the rectangle and an
     * aspect ratio.
     */
    public static float calculateRight(float left, float top, float bottom, float targetAspectRatio) {

        final float height = bottom - top;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // right - left = targetAspectRatio * height
        return (getTargetAspectRatio() * height) + left;
    }

    /**
     * Calculates the y-coordinate of the bottom edge given the other sides of the rectangle and an
     * aspect ratio.
     */
    public static float calculateBottom(float left, float top, float right, float targetAspectRatio) {

        final float width = right - left;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // height = width / targetAspectRatio
        // bottom - top = width / targetAspectRatio
        return (width / getTargetAspectRatio()) + top;
    }

    /**
     * Calculates the width of a rectangle given the top and bottom edges and an aspect ratio.
     */
    public static float calculateWidth(float height, float targetAspectRatio) {
        return getTargetAspectRatio() * height;
    }

    /**
     * Calculates the height of a rectangle given the left and right edges and an aspect ratio.
     */
    public static float calculateHeight(float width, float targetAspectRatio) {
        return width / getTargetAspectRatio();
    }

    public static float getTargetAspectRatio() {
        return mAspectRatioX / (float) mAspectRatioY;
    }
}
