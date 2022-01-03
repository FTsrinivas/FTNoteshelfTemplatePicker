package com.fluidtouch.noteshelf.textrecognition.helpers;

import com.dd.plist.NSData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class NSDataValueConverter {

    public static NSData dataWithRectValuesArray(ArrayList<NSValue> valuesArray) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        for (int i = 0; i < valuesArray.size(); i++) {
            NSValue value = valuesArray.get(i);
            try {
                writeReversedDouble(out, value.x);
                writeReversedDouble(out, value.y);
                writeReversedDouble(out, value.width);
                writeReversedDouble(out, value.height);
            } catch (Exception e) {
                return null;
            }
        }
        byte[] bytes = baos.toByteArray();
        return new NSData(bytes);
    }

    public static ArrayList<NSValue> rectValuesArrayFromData(NSData data) {

        ArrayList<NSValue> list = new ArrayList<>();
        byte[] bytes = data.bytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        try {
            while (in.available() > 0) {
                NSValue value = new NSValue();
                value.x = readReversedDouble(in);
                value.y = readReversedDouble(in);
                value.width = readReversedDouble(in);
                value.height = readReversedDouble(in);
                list.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void writeReversedDouble(DataOutputStream out, double doubleValue) throws IOException {

        long bits = Double.doubleToLongBits(doubleValue);
        byte byteArrayForFloat[] = new byte[8];
        byteArrayForFloat[0] = (byte) (bits & 0xff);
        byteArrayForFloat[1] = (byte) ((bits >> 8) & 0xff);
        byteArrayForFloat[2] = (byte) ((bits >> 16) & 0xff);
        byteArrayForFloat[3] = (byte) ((bits >> 24) & 0xff);
        byteArrayForFloat[4] = (byte) ((bits >> 32) & 0xff);
        byteArrayForFloat[5] = (byte) ((bits >> 40) & 0xff);
        byteArrayForFloat[6] = (byte) ((bits >> 48) & 0xff);
        byteArrayForFloat[7] = (byte) ((bits >> 56) & 0xff);
        out.write(byteArrayForFloat, 0, 8);
    }

    private static double readReversedDouble(DataInputStream in) throws IOException {

        byte byteArrayForFloat[] = new byte[8];

        byteArrayForFloat[7] = in.readByte();
        byteArrayForFloat[6] = in.readByte();
        byteArrayForFloat[5] = in.readByte();
        byteArrayForFloat[4] = in.readByte();

        byteArrayForFloat[3] = in.readByte();
        byteArrayForFloat[2] = in.readByte();
        byteArrayForFloat[1] = in.readByte();
        byteArrayForFloat[0] = in.readByte();

        return ByteBuffer.wrap(byteArrayForFloat).getDouble();
    }
}
