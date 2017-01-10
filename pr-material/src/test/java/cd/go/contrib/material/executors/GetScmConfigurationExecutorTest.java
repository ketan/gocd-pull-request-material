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

package cd.go.contrib.material.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GetScmConfigurationExecutorTest {

    @Test
    public void shouldSerializeToJSON() throws Exception {
        GoPluginApiResponse response = new GetScmConfigurationExecutor().execute();
        assertThat(response.responseCode()).isEqualTo(200);
        assertThatJson(response.responseBody()).isEqualTo("{\n" +
                "  \"Url\": {\n" +
                "    \"display-name\": \"Url\",\n" +
                "    \"default-value\": null,\n" +
                "    \"part-of-identity\": true,\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"1\"\n" +
                "  },\n" +
                "  \"Username\": {\n" +
                "    \"display-name\": \"Username\",\n" +
                "    \"default-value\": null,\n" +
                "    \"part-of-identity\": false,\n" +
                "    \"required\": false,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"2\"\n" +
                "  },\n" +
                "  \"Password\": {\n" +
                "    \"display-name\": \"Password\",\n" +
                "    \"default-value\": null,\n" +
                "    \"part-of-identity\": false,\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"3\"\n" +
                "  },\n" +
                "  \"SshKey\": {\n" +
                "    \"display-name\": \"Ssh Key\",\n" +
                "    \"default-value\": null,\n" +
                "    \"part-of-identity\": false,\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"4\"\n" +
                "  },\n" +
                "  \"SshExtraArgs\": {\n" +
                "    \"display-name\": \"Extra Args\",\n" +
                "    \"default-value\": null,\n" +
                "    \"part-of-identity\": false,\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"5\"\n" +
                "  },\n" +
                "  \"EnableTracing\": {\n" +
                "    \"display-name\": \"Enable git and SSH Tracing\",\n" +
                "    \"default-value\": \"false\",\n" +
                "    \"part-of-identity\": false,\n" +
                "    \"required\": false,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"6\"\n" +
                "  }\n" +
                "}");
    }
}