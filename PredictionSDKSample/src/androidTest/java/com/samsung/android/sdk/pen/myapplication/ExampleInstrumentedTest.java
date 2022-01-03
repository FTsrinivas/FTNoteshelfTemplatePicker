<<<<<<< HEAD:PredictionSDKSample/src/androidTest/java/com/myapplication/ExampleInstrumentedTest.java
package com.myapplication;
=======
package com.samsung.android.sdk.pen.myapplication;
>>>>>>> bf75d5bfa2b3e87267908c3ae667e689719ec67a:PredictionSDKSample/src/androidTest/java/com/samsung/android/sdk/pen/myapplication/ExampleInstrumentedTest.java

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

<<<<<<< HEAD:PredictionSDKSample/src/androidTest/java/com/myapplication/ExampleInstrumentedTest.java
        assertEquals("com.myapplication", appContext.getPackageName());
=======
        assertEquals("com.samsung.android.sdk.pen.myapplication", appContext.getPackageName());
>>>>>>> bf75d5bfa2b3e87267908c3ae667e689719ec67a:PredictionSDKSample/src/androidTest/java/com/samsung/android/sdk/pen/myapplication/ExampleInstrumentedTest.java
    }
}
