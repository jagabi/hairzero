package com.android.volley.misc;

import java.io.ByteArrayInputStream;

public class Exif {
    private static final String TAG = "CameraExif";

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a8, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getOrientation(java.io.InputStream r17, long r18) {
        /*
            r0 = r17
            r1 = r18
            r3 = 0
            if (r0 != 0) goto L_0x0008
            return r3
        L_0x0008:
            r4 = 16
            r5 = 4
            com.android.volley.misc.InputStreamBuffer r6 = new com.android.volley.misc.InputStreamBuffer
            r7 = 16
            r6.<init>(r0, r7, r3)
            r7 = 0
            r8 = 0
            r9 = 1
            boolean r10 = has(r6, r1, r9)
            if (r10 == 0) goto L_0x0030
            byte r10 = r6.get(r3)
            r11 = -1
            if (r10 != r11) goto L_0x002c
            byte r10 = r6.get(r9)
            r11 = -40
            if (r10 != r11) goto L_0x002c
            r10 = r9
            goto L_0x002d
        L_0x002c:
            r10 = r3
        L_0x002d:
            if (r10 != 0) goto L_0x0030
            return r3
        L_0x0030:
            int r10 = r7 + 3
            boolean r10 = has(r6, r1, r10)
            java.lang.String r11 = "CameraExif"
            r12 = 8
            r13 = 2
            r14 = 4
            if (r10 == 0) goto L_0x00b5
            int r10 = r7 + 1
            byte r7 = r6.get(r7)
            r15 = 255(0xff, float:3.57E-43)
            r7 = r7 & r15
            if (r7 != r15) goto L_0x00b4
            byte r7 = r6.get(r10)
            r7 = r7 & r15
            if (r7 != r15) goto L_0x0052
            r7 = r10
            goto L_0x0030
        L_0x0052:
            int r10 = r10 + 1
            r15 = 216(0xd8, float:3.03E-43)
            if (r7 == r15) goto L_0x00b0
            if (r7 != r9) goto L_0x005b
            goto L_0x00b0
        L_0x005b:
            r15 = 217(0xd9, float:3.04E-43)
            if (r7 == r15) goto L_0x00a9
            r15 = 218(0xda, float:3.05E-43)
            if (r7 != r15) goto L_0x0064
            goto L_0x00a9
        L_0x0064:
            int r8 = pack(r6, r10, r13, r3)
            if (r8 < r13) goto L_0x00a3
            int r15 = r10 + r8
            int r15 = r15 - r9
            boolean r15 = has(r6, r1, r15)
            if (r15 != 0) goto L_0x0074
            goto L_0x00a3
        L_0x0074:
            r15 = 225(0xe1, float:3.15E-43)
            if (r7 != r15) goto L_0x0098
            if (r8 < r12) goto L_0x0098
            int r15 = r10 + 2
            int r15 = pack(r6, r15, r14, r3)
            r9 = 1165519206(0x45786966, float:3974.5874)
            if (r15 != r9) goto L_0x0098
            int r9 = r10 + 6
            int r9 = pack(r6, r9, r13, r3)
            if (r9 != 0) goto L_0x0098
            int r9 = r10 + 8
            int r8 = r8 + -8
            int r10 = r9 + -4
            r6.advanceTo(r10)
            r7 = r9
            goto L_0x00b5
        L_0x0098:
            int r9 = r10 + r8
            r8 = 0
            int r10 = r9 + -4
            r6.advanceTo(r10)
            r7 = r9
            r9 = 1
            goto L_0x0030
        L_0x00a3:
            java.lang.String r9 = "Invalid length"
            android.util.Log.e(r11, r9)
            return r3
        L_0x00a9:
            int r9 = r10 + -4
            r6.advanceTo(r9)
            r7 = r10
            goto L_0x00b5
        L_0x00b0:
            r7 = r10
            r9 = 1
            goto L_0x0030
        L_0x00b4:
            r7 = r10
        L_0x00b5:
            if (r8 <= r12) goto L_0x0129
            int r9 = pack(r6, r7, r14, r3)
            r10 = 1229531648(0x49492a00, float:823968.0)
            if (r9 == r10) goto L_0x00cb
            r12 = 1296891946(0x4d4d002a, float:2.14958752E8)
            if (r9 == r12) goto L_0x00cb
            java.lang.String r10 = "Invalid byte order"
            android.util.Log.e(r11, r10)
            return r3
        L_0x00cb:
            if (r9 != r10) goto L_0x00d0
            r16 = 1
            goto L_0x00d2
        L_0x00d0:
            r16 = r3
        L_0x00d2:
            r10 = r16
            int r12 = r7 + 4
            int r12 = pack(r6, r12, r14, r10)
            int r12 = r12 + r13
            r14 = 10
            if (r12 < r14) goto L_0x0123
            if (r12 <= r8) goto L_0x00e2
            goto L_0x0123
        L_0x00e2:
            int r7 = r7 + r12
            int r8 = r8 - r12
            int r14 = r7 + -4
            r6.advanceTo(r14)
            int r14 = r7 + -2
            int r12 = pack(r6, r14, r13, r10)
        L_0x00ef:
            int r14 = r12 + -1
            if (r12 <= 0) goto L_0x0129
            r12 = 12
            if (r8 < r12) goto L_0x0129
            int r9 = pack(r6, r7, r13, r10)
            r12 = 274(0x112, float:3.84E-43)
            if (r9 != r12) goto L_0x0118
            int r12 = r7 + 8
            int r12 = pack(r6, r12, r13, r10)
            switch(r12) {
                case 1: goto L_0x0117;
                case 3: goto L_0x0114;
                case 6: goto L_0x0111;
                case 8: goto L_0x010e;
                default: goto L_0x0108;
            }
        L_0x0108:
            java.lang.String r13 = "Unsupported orientation"
            android.util.Log.i(r11, r13)
            return r3
        L_0x010e:
            r3 = 270(0x10e, float:3.78E-43)
            return r3
        L_0x0111:
            r3 = 90
            return r3
        L_0x0114:
            r3 = 180(0xb4, float:2.52E-43)
            return r3
        L_0x0117:
            return r3
        L_0x0118:
            int r7 = r7 + 12
            int r8 = r8 + -12
            int r12 = r7 + -4
            r6.advanceTo(r12)
            r12 = r14
            goto L_0x00ef
        L_0x0123:
            java.lang.String r13 = "Invalid offset"
            android.util.Log.e(r11, r13)
            return r3
        L_0x0129:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.misc.Exif.getOrientation(java.io.InputStream, long):int");
    }

    private static int pack(InputStreamBuffer bytes, int offset, int length, boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }
        int value = 0;
        while (true) {
            int length2 = length - 1;
            if (length <= 0) {
                return value;
            }
            value = (value << 8) | (bytes.get(offset) & 255);
            offset += step;
            length = length2;
        }
    }

    private static boolean has(InputStreamBuffer jpeg, long byteSize, int index) {
        if (byteSize >= 0) {
            return ((long) index) < byteSize;
        }
        return jpeg.has(index);
    }

    @Deprecated
    public static int getOrientation(byte[] jpeg) {
        return getOrientation(new ByteArrayInputStream(jpeg), (long) jpeg.length);
    }
}
