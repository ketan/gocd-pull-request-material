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

package cd.go.contrib.material;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class ScmConfigurationTest {

    @Test
    public void shouldDeserialize() throws Exception {
        String json = "{\n" +
                "    \"scm-configuration\": {\n" +
                "        \"Url\": {\n" +
                "            \"value\": \"git://example.com/git/my-project\"\n" +
                "        },\n" +
                "        \"Username\": {\n" +
                "            \"value\": \"bob\"\n" +
                "        },\n" +
                "        \"Password\": {\n" +
                "            \"value\": \"s3cr3t\"\n" +
                "        },\n" +
                "        \"SshKey\": {\n" +
                "            \"value\": \"some-ssh-key\"\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        ScmConfiguration config = ScmConfiguration.fromJSON(json);

        assertThat(config.url()).isEqualTo("git://example.com/git/my-project");
        assertThat(config.username()).isEqualTo("bob");
        assertThat(config.password()).isEqualTo("s3cr3t");
        assertThat(config.sshKey()).isEqualTo("some-ssh-key");
    }

    @Test
    public void shouldNotValidateIfUrlIsMissing() throws Exception {
        GoPluginApiResponse response = ScmConfiguration.validate(new HashMap<>());
        assertThat(response.responseCode()).isEqualTo(200);
        assertThatJson(response.responseBody()).isEqualTo("[\n" +
                "  {\n" +
                "    \"key\": \"Url\",\n" +
                "    \"message\": \"Url must not be blank.\"\n" +
                "  }\n" +
                "]");
    }

    @Test
    public void shouldValidateIfUrlIsPresent() throws Exception {
        GoPluginApiResponse response = ScmConfiguration.validate(Collections.singletonMap("Url", "https://github.com/user/repo"));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThatJson(response.responseBody()).isEqualTo("[]");
    }

    @Test
    public void shouldNotValidateIfUnknownPropertiesArePassed() throws Exception {
        HashMap<String, String> config = new HashMap<>();
        config.put("Url", "https://github.com/user/repo");
        config.put("UnknownKey", "UnknownValue");
        GoPluginApiResponse response = ScmConfiguration.validate(config);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThatJson(response.responseBody()).isEqualTo("[\n" +
                "  {\n" +
                "    \"key\": \"UnknownKey\",\n" +
                "    \"message\": \"Unknown key UnknownKey.\"\n" +
                "  }\n" +
                "]");
    }
}