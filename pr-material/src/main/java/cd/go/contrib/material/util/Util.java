/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.material.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Util {

    public static String readResource(String resourceFile) {
        try (InputStream in = Util.class.getResourceAsStream(resourceFile)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not find resource " + resourceFile, e);
        }
    }

    public static File copyResourceIntoTempFile(String resourceFile, String tempFilePrefix, String tempfileExtension) {
        try (InputStream in = Util.class.getResourceAsStream(resourceFile)) {
            File tempFile = File.createTempFile(tempFilePrefix, tempfileExtension);
            FileUtils.copyInputStreamToFile(in, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Could not find copy " + resourceFile, e);
        }
    }

    public static Collection<String> splitIntoLinesAndTrimSpaces(String lines) {
        if (isBlank(lines)) {
            return Collections.emptyList();
        }

        return Arrays.stream(lines.split("[\r\n]+")).map(String::trim).collect(Collectors.toList());
    }

}