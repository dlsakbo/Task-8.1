package com.example.chatbot;

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
public class AppContextInstrumentedTest {
    @Test
    public void verifyAppContext() {
        // Context of the app under test.
        Context applicationContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.chatbot", applicationContext.getPackageName());
    }
}
