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
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeoutException;

public class CheckConnectionExecutor implements RequestExecutor {
    private static final Gson GSON = new Gson();
    private final ScmConfiguration configuration;

    public CheckConnectionExecutor(ScmConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public GoPluginApiResponse execute() {
        Git build = Git.newBuilder().configuration(configuration).args("ls-remote", "${git.url}").build();

        try {
            ProcessResult result = build.execute();
            if (result.getExitValue() == 0) {
                return message("success", "The connection was successful!");
            } else {
                return message("failure", "Could not verify connection. The git command exited with status " + result.getExitValue() + " and printed: \n" + result.outputString());
            }
        } catch (InterruptedException | TimeoutException | IOException e) {
            return message("failure", "Could not verify connection. There was an unknown error executing 'git ls-remote " + configuration.url() + "'.");
        }
    }

    private GoPluginApiResponse message(String status, String... messages) {
        LinkedHashMap<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("status", status);
        jsonObject.put("messages", messages);
        return DefaultGoPluginApiResponse.success(GSON.toJson(jsonObject));
    }
}
