package com.jp.wasabeef.glide.transformations.internal;

import android.graphics.Bitmap;

/**
 * Copyright (C) 2015 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FastBlur {

    // Private constructor to prevent instantiation
    private FastBlur() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Bitmap blur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap = canReuseInBitmap ? sentBitmap : sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int div = radius + radius + 1;
        int[] dv = createDivTable(div);

        int[] r = new int[w * h];
        int[] g = new int[w * h];
        int[] b = new int[w * h];

        BlurParams params = new BlurParams(pix, r, g, b, w, h, radius, div, dv);
        horizontalBlur(params);
        verticalBlur(pix, r, g, b, w, h, radius, div, dv);

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private static int[] createDivTable(int div) {
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (int i = 0; i < dv.length; i++) {
            dv[i] = (i / divsum);
        }
        return dv;
    }
    private static class BlurParams {
        int[] pix, r, g, b, dv;
        int w, h, radius, div;

        BlurParams(int[] pix, int[] r, int[] g, int[] b, int w, int h, int radius, int div, int[] dv) {
            this.pix = pix;
            this.r = r;
            this.g = g;
            this.b = b;
            this.w = w;
            this.h = h;
            this.radius = radius;
            this.div = div;
            this.dv = dv;
        }
    }

    private static void horizontalBlur(BlurParams params) {
        int wm = params.w - 1;
        int wh = params.w * params.h;
        int r1 = params.radius + 1;
        int[][] stack = new int[params.div][3];
        int[] vmin = new int[Math.max(params.w, params.h)];
        int yi = 0, yw = 0;

        for (int y = 0; y < params.h; y++) {
            int rinsum = 0, ginsum = 0, binsum = 0;
            int routsum = 0, goutsum = 0, boutsum = 0;
            int rsum = 0, gsum = 0, bsum = 0;
            for (int i = -params.radius; i <= params.radius; i++) {
                int p = params.pix[yi + Math.min(wm, Math.max(i, 0))];
                int[] sir = stack[i + params.radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                int rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            int stackpointer = params.radius;
            for (int x = 0; x < params.w; x++) {
                params.r[yi] = params.dv[rsum];
                params.g[yi] = params.dv[gsum];
                params.b[yi] = params.dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                int stackstart = stackpointer - params.radius + params.div;
                int[] sir = stack[stackstart % params.div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + params.radius + 1, wm);
                }
                int p = params.pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % params.div;
                sir = stack[stackpointer % params.div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += params.w;
        }
    }
    }

    private static void verticalBlur(int[] pix, int[] r, int[] g, int[] b, int w, int h, int radius, int div, int[] dv) {
        int hm = h - 1;
        int wh = w * h;
        int r1 = radius + 1;
        int[][] stack = new int[div][3];
        int[] vmin = new int[Math.max(w, h)];

        for (int x = 0; x < w; x++) {
            int rinsum = 0, ginsum = 0, binsum = 0;
            int routsum = 0, goutsum = 0, boutsum = 0;
            int rsum = 0, gsum = 0, bsum = 0;
            int yp = -radius * w;
            for (int i = -radius; i <= radius; i++) {
                int yi = Math.max(0, yp) + x;
                int[] sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                int rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            int yi = x;
            int stackpointer = radius;
            for (int y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                int stackstart = stackpointer - radius + div;
                int[] sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                int p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
    }
}