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

public enum Request {

    REQUEST_SCM_CONFIGURATION("scm-configuration"),
    REQUEST_SCM_VIEW("scm-view"),
    REQUEST_VALIDATE_SCM_CONFIGURATION("validate-scm-configuration"),
    REQUEST_CHECK_SCM_CONNECTION("check-scm-connection"),
    REQUEST_PLUGIN_CONFIGURATION("go.plugin-settings.get-configuration"),
    REQUEST_PLUGIN_VIEW("go.plugin-settings.get-view"),
    REQUEST_VALIDATE_PLUGIN_CONFIGURATION("go.plugin-settings.validate-configuration"),

    REQUEST_LATEST_REVISION("latest-revision"),
    REQUEST_LATEST_REVISIONS_SINCE("latest-revisions-since"),
    REQUEST_CHECKOUT("checkout");

    private final String requestName;

    Request(String requestName) {
        this.requestName = requestName;
    }

    public static Request fromString(String requestName) {
        if (requestName != null) {
            for (Request request : Request.values()) {
                if (requestName.equalsIgnoreCase(request.requestName)) {
                    return request;
                }
            }
        }

        return null;
    }
}