/**
 *
 */
package org.ricts.abstractmachine.components;

/**
 * @author Jevon
 *
 */
public class Device {
    protected static int bitMaskOfWidth(int width){
        int bitMask = 0;
        for(int x=0; x != width; ++x){
            bitMask |= (1<<x);
        }
        return bitMask;
    }

    protected static int bitWidth(int number){
        int index = 1;

        while(number >= Math.pow(2,index)){
            ++index;
        }

        return index;
    }

    protected static int getWordFrom(int number, int width, int offset){
        int numberBitMask = bitMaskOfWidth(width) << offset;
        return (number & numberBitMask) >>> offset;
    }

    protected static int setWordIn(int number, int word, int width, int offset){
        int wordBitMask = bitMaskOfWidth(width);
        int value = (word & wordBitMask) << offset;

        int numberBitMask = ~(wordBitMask << offset);
        int intactNumberBits = number & numberBitMask;

        return intactNumberBits | value ;
    }

    protected static boolean bitAtIndex(int bitIndex, int number){
        return ((number & (1<<bitIndex)) != 0);
    }

    protected static int setBitAtIndex(int bitIndex, int number){
        int bitMask = (1<<bitIndex);
        return number | bitMask;
    }

    protected static int clearBitAtIndex(int bitIndex, int number){
        int bitMask = ~(1<<bitIndex);
        return number & bitMask;
    }

    protected static int setBitValueAtIndex(int bitIndex, int number, boolean value){
        if(value){
            return setBitAtIndex(bitIndex, number);
        }
        else {
            return clearBitAtIndex(bitIndex, number);
        }
    }

    public static String formatNumberInHex(int number, int bitWidth){
        String temp = Integer.toHexString(number & bitMaskOfWidth(bitWidth));

        return "0x" + zeroPad((int) Math.ceil(bitWidth/4.0) - temp.length()) + temp;
    }

    private static String zeroPad(int count){
        String ret = "";

        for(int x=0; x!= count; ++x){
            ret += "0";
        }

        return ret;
    }
}
