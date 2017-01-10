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

import cd.go.contrib.material.ScmConfiguration;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.UUID;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class CheckConnectionExecutorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldBeSuccessfulOnLocalRepository() throws Exception {
        File basedir = temporaryFolder.newFolder("git-root");
        new GitRepository(basedir).initialize();
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(basedir.getAbsolutePath())
                .build();

        GoPluginApiResponse response = new CheckConnectionExecutor(config).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        GoPluginApiResponseAssert.assertThat(response).isSuccessful();
    }

    @Test
    public void shouldErrorOutOnBadLocalRepositoryPath() throws Exception {
        String url = new File(UUID.randomUUID().toString()).getAbsolutePath();
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(url)
                .build();

        GoPluginApiResponse response = new CheckConnectionExecutor(config).execute();
        assertThat(response.responseCode()).isEqualTo(200);
        GoPluginApiResponseAssert.assertThat(response).isUnsuccessful();

        assertThatJson(response.responseBody()).isEqualTo("{\n" +
                "  \"status\": \"failure\",\n" +
                "  \"messages\": [\n" +
                "    \"${json-unit.ignore}\"\n" +
                "  ]\n" +
                "}");

        GoPluginApiResponseAssert.assertThat(response).hasMessage("Could not verify connection. The git command exited with status");
        GoPluginApiResponseAssert.assertThat(response).hasMessage("fatal");
        GoPluginApiResponseAssert.assertThat(response).hasMessage(url);
    }
}