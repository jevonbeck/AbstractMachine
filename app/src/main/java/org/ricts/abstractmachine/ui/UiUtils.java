package org.ricts.abstractmachine.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Jevon on 22/08/2015.
 */
public class UiUtils {
    public static AttributeSet makeAttributeSet(Context c, int resourceId){
        XmlPullParser parser = c.getResources().getXml(resourceId);

        // align parser with desired element (with attributes) before creating AttributeSet
        int eventType;
        try {
            eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    // XML resource has one main element, no need to parse further
                    break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Xml.asAttributeSet(parser);
    }
}
