/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.systemupdatersample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.systemupdatersample.tests.R;
import com.google.common.io.CharStreams;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Tests for {@link UpdateConfig}
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class UpdateConfigTest {

    private static final String JSON_NON_STREAMING =
            "{\"name\": \"vip update\", \"url\": \"file:///builds/a.zip\", "
            + " \"ab_install_type\": \"NON_STREAMING\"}";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Context mContext;
    private Context mTargetContext;
    private String mJsonStreaming001;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getContext();
        mTargetContext = InstrumentationRegistry.getTargetContext();
        mJsonStreaming001 = readResource(R.raw.update_config_stream_001);
    }

    @Test
    public void fromJson_parsesNonStreaming() throws Exception {
        UpdateConfig config = UpdateConfig.fromJson(JSON_NON_STREAMING);
        assertEquals("name is parsed", "vip update", config.getName());
        assertEquals("stores raw json", JSON_NON_STREAMING, config.getRawJson());
        assertSame("type is parsed",
                UpdateConfig.AB_INSTALL_TYPE_NON_STREAMING,
                config.getInstallType());
        assertEquals("url is parsed", "file:///builds/a.zip", config.getUrl());
    }

    @Test
    public void fromJson_parsesStreaming() throws Exception {
        UpdateConfig config = UpdateConfig.fromJson(mJsonStreaming001);
        assertEquals("streaming-001", config.getName());
        assertEquals("http://foo.bar/update.zip", config.getUrl());
        assertSame(UpdateConfig.AB_INSTALL_TYPE_STREAMING, config.getInstallType());
        assertEquals("payload.bin",
                config.getStreamingMetadata().getPropertyFiles()[0].getFilename());
        assertEquals(195, config.getStreamingMetadata().getPropertyFiles()[0].getOffset());
        assertEquals(8, config.getStreamingMetadata().getPropertyFiles()[0].getSize());
    }

    @Test
    public void getUpdatePackageFile_throwsErrorIfStreaming() throws Exception {
        UpdateConfig config = UpdateConfig.fromJson(mJsonStreaming001);
        thrown.expect(RuntimeException.class);
        config.getUpdatePackageFile();
    }

    @Test
    public void getUpdatePackageFile_throwsErrorIfNotAFile() throws Exception {
        String json = "{\"name\": \"upd\", \"url\": \"http://foo.bar\","
                + " \"ab_install_type\": \"NON_STREAMING\"}";
        UpdateConfig config = UpdateConfig.fromJson(json);
        thrown.expect(RuntimeException.class);
        config.getUpdatePackageFile();
    }

    @Test
    public void getUpdatePackageFile_works() throws Exception {
        UpdateConfig c = UpdateConfig.fromJson(JSON_NON_STREAMING);
        assertEquals("/builds/a.zip", c.getUpdatePackageFile().getAbsolutePath());
    }

    private String readResource(int id) throws IOException {
        return CharStreams.toString(new InputStreamReader(
            mContext.getResources().openRawResource(id)));
    }
}