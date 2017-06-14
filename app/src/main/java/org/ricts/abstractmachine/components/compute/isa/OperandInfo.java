package org.ricts.abstractmachine.components.compute.isa;

import android.util.SparseArray;

import org.ricts.abstractmachine.components.devicetype.Device;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jevon on 02/10/2016.
 */

public class OperandInfo extends Device {
    private int dataWidth, dWidthMask;
    private Map<String, Integer> validStringMap;
    private SparseArray<String> reverseMap;
    private boolean mapped, mappedOneToOne;


    public OperandInfo(int width) {
        this(width, false);
    }

    public OperandInfo(int width, boolean hasMapping) {
        this(width, hasMapping, false);
    }

    public OperandInfo(int width, boolean hasMapping, boolean isMappedOneToOne) {
        dataWidth = width;
        dWidthMask = bitMaskOfWidth(dataWidth);
        mapped = hasMapping;
        mappedOneToOne = isMappedOneToOne;

        if(mapped) {
            validStringMap = new HashMap<>();

            if(mappedOneToOne) {
                reverseMap = new SparseArray<>();
            }
        }
    }

    public void addMapping(String mneumonic, int value){
        if(mapped) {
            int maskedValue = value & dWidthMask;
            validStringMap.put(mneumonic, maskedValue);

            if(mappedOneToOne) {
                reverseMap.put(maskedValue, mneumonic);
            }
        }
    }

    public void addMappingWithoutReplacement(String mneumonic, int value){
        if( (mappedOneToOne && reverseMap.get(value) == null) ||
            (mapped && validStringMap.get(mneumonic) == null)
           ) {
            addMapping(mneumonic, value);
        }
    }

    public void removeMapping(String mneumonic) {
        if(mapped) {
            Integer value = validStringMap.get(mneumonic);
            if(mappedOneToOne && value != null) {
                reverseMap.remove(value);
            }
            validStringMap.remove(mneumonic);
        }
    }

    public void removeMapping(int value) {
        if(mappedOneToOne) {
            String mneumonic = reverseMap.get(value);
            if(mneumonic != null){
                validStringMap.remove(mneumonic);
            }
            reverseMap.remove(value);
        }
    }

    public String getPrettyValue(int value){
        String mneumonic = null;
        if(mappedOneToOne) {
            mneumonic = reverseMap.get(value);
        }

        if(mneumonic == null) {
            mneumonic = formatNumberInHex(value, dataWidth);
        }
        return mneumonic;
    }

    public Set<String> validMneumonics(){
        return validStringMap.keySet();
    }

    public int decodeMneumonic(String mneumonic) {
        if(hasMneumonic(mneumonic)){
            return validStringMap.get(mneumonic);
        }

        return -1;
    }

    public boolean hasMneumonic(String mneumonic){
        return mapped && validStringMap.containsKey(mneumonic);
    }

    public Map<String, Integer> getMapping() {
        return validStringMap;
    }

    public int getDataWidth() {
        return dataWidth;
    }
}
