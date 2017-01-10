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

import cd.go.contrib.material.executors.CheckConnectionExecutor;
import cd.go.contrib.material.executors.GetScmConfigurationExecutor;
import cd.go.contrib.material.executors.GetScmViewExecutor;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public class GitPlugin implements GoPlugin {
    private GoApplicationAccessor accessor;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) throws UnhandledRequestTypeException {
        switch (Request.fromString(requestMessage.requestName())) {
            case REQUEST_SCM_CONFIGURATION:
                return new GetScmConfigurationExecutor().execute();
            case REQUEST_SCM_VIEW:
                return new GetScmViewExecutor().execute();
            case REQUEST_VALIDATE_SCM_CONFIGURATION:
                return ScmConfiguration.validate(requestMessage.requestBody());
            case REQUEST_CHECK_SCM_CONNECTION:
                return new CheckConnectionExecutor(ScmConfiguration.fromJSON(requestMessage.requestBody())).execute();
            case REQUEST_PLUGIN_CONFIGURATION:
                break;
            case REQUEST_PLUGIN_VIEW:
                break;
            case REQUEST_VALIDATE_PLUGIN_CONFIGURATION:
                break;
            case REQUEST_LATEST_REVISION:
                break;
            case REQUEST_LATEST_REVISIONS_SINCE:
                break;
            case REQUEST_CHECKOUT:
                break;
        }

        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return Constants.PLUGIN_IDENTIFIER;
    }
}
