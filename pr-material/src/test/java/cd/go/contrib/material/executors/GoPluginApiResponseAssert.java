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
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.hamcrest.Matchers.containsString;

public class GoPluginApiResponseAssert extends AbstractAssert<GoPluginApiResponseAssert, GoPluginApiResponse> {

    public GoPluginApiResponseAssert(GoPluginApiResponse actual) {
        super(actual, GoPluginApiResponseAssert.class);
    }

    public static GoPluginApiResponseAssert assertThat(GoPluginApiResponse actual) {
        return new GoPluginApiResponseAssert(actual);
    }

    public GoPluginApiResponseAssert isSuccessful() {
        isNotNull();

        Assertions.assertThat(actual.responseCode()).isEqualTo(200);

        assertThatJson(actual.responseBody()).node("status").isEqualTo("success");

        hasMessage("The connection was successful!");
        return this;
    }

    public GoPluginApiResponseAssert isUnsuccessful() {
        isNotNull();

        Assertions.assertThat(actual.responseCode()).isEqualTo(200);

        assertThatJson(actual.responseBody()).node("status").isEqualTo("failure");

        return this;
    }

    public GoPluginApiResponseAssert hasMessage(String message) {
        assertThatJson(actual.responseBody()).node("messages[0]").matches(containsString(message));
        return this;
    }

}
