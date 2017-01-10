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
import cd.go.contrib.material.models.Field;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GetScmViewExecutorTest {

    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetScmViewExecutor().execute();
        assertThat(response.responseCode()).isEqualTo(200);
        JsonFluentAssert.assertThatJson(response.responseBody()).isEqualTo("{\"displayName\":\"Git PR\",\"template\":\"${json-unit.ignore}\"}");
    }

    @Test
    public void allFieldsShouldBePresentInView() throws Exception {
        GoPluginApiResponse response = new GetScmViewExecutor().execute();

        Map<String, String> map = new Gson().fromJson(response.responseBody(), Map.class);
        String template = map.get("template");


        for (Map.Entry<String, Field> field : ScmConfiguration.FIELDS.entrySet()) {
            if (field.getKey().equals("EnableTracing")){
                continue;
            }
            assertThat(template).contains("ng-model=\"" + field.getKey() + "\"");
            assertThat(template).contains("<span class=\"form_error form-error\" ng-class=\"{'is-visible': GOINPUTNAME[" +
                    field.getKey() + "].$error.server}\" ng-show=\"GOINPUTNAME[" +
                    field.getKey() + "].$error.server\">{{GOINPUTNAME[" +
                    field.getKey() + "].$error.server}}</span>");
        }
    }
}
