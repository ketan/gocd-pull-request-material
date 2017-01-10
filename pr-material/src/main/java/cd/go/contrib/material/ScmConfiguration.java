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

import cd.go.contrib.material.models.BooleanField;
import cd.go.contrib.material.models.Field;
import cd.go.contrib.material.models.NonBlankField;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ScmConfiguration {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).serializeNulls().create();

    private static final Field GIT_URL = new NonBlankField("Url", "Url", null, true, true, false, "1");
    private static final Field GIT_USER = new Field("Username", "Username", null, false, false, false, "2");
    private static final Field GIT_PASS = new Field("Password", "Password", null, false, false, true, "3");
    private static final Field SSH_KEY = new Field("SshKey", "Ssh Key", null, false, false, true, "4");
    private static final Field SSH_EXTRA_ARGS = new Field("SshExtraArgs", "Extra Args", null, false, false, true, "5");
    private static final Field ENABLE_TRACING = new BooleanField("EnableTracing", "Enable git and SSH Tracing", false, false, false, false, "6");

    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(GIT_URL.key(), GIT_URL);
        FIELDS.put(GIT_USER.key(), GIT_USER);
        FIELDS.put(GIT_PASS.key(), GIT_PASS);
        FIELDS.put(SSH_KEY.key(), SSH_KEY);
        FIELDS.put(SSH_EXTRA_ARGS.key(), SSH_EXTRA_ARGS);
        FIELDS.put(ENABLE_TRACING.key(), ENABLE_TRACING);
    }

    private final String url;
    private final String username;
    private final String password;
    private final String sshKey;
    private final String sshKeyPassphrase;
    private final String sshExtraArgs;

    private ScmConfiguration(Builder builder) {
        url = builder.url;
        username = builder.username;
        password = builder.password;
        sshKey = builder.sshKey;
        sshKeyPassphrase = builder.sshKeyPassphrase;
        sshExtraArgs = builder.sshExtraArgs;
    }

    static ScmConfiguration fromJSON(String json) {
        Map<String, String> config = toProperties(json);
        return ScmConfiguration.newBuilder()
                .url(config.get(GIT_URL.key()))
                .username(config.get(GIT_USER.key()))
                .password(config.get(GIT_PASS.key()))
                .sshKey(config.get(SSH_KEY.key())).build();
    }

    private static Map<String, String> toProperties(String json) {
        Map<String, String> result = new HashMap<>();

        Map<String, Map<String, String>> settings = (Map<String, Map<String, String>>) GSON.fromJson(json, HashMap.class).get("scm-configuration");

        for (Map.Entry<String, Map<String, String>> entry : settings.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get("value"));
        }
        return result;
    }

    public static GoPluginApiResponse validate(String json) {
        return validate(toProperties(json));
    }

    static GoPluginApiResponse validate(Map<String, String> config) {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        for (Map.Entry<String, Field> entry : FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(config.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        HashSet<String> allKeys = new HashSet<>(config.keySet());
        allKeys.removeAll(FIELDS.keySet());

        for (String key : allKeys) {
            LinkedHashMap<String, String> error = new LinkedHashMap<>();
            error.put("key", key);
            error.put("message", "Unknown key " + key + ".");
            result.add(error);
        }
        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String url() {
        return url;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String sshKey() {
        return sshKey;
    }

    public String sshExtraArgs() {
        return sshExtraArgs;
    }

    public boolean hasPassword() {
        return isNotBlank(password);
    }

    public boolean hasSshExtraArgs() {
        return isNotBlank(sshExtraArgs);
    }

    public boolean hasSshKey() {
        return isNotBlank(sshKey());
    }

    public boolean enableTracing() {
        return false;
    }

    public String sshKeyPassphrase() {
        return sshKeyPassphrase;
    }

    public boolean hasSshKeyPassphrase() {
        return isNotBlank(sshKeyPassphrase);
    }

    public static final class Builder {
        private String url;
        private String username;
        private String password;
        private String sshKey;
        private String sshKeyPassphrase;
        private String sshExtraArgs;

        private Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder sshKey(String sshKey) {
            this.sshKey = sshKey;
            return this;
        }

        public Builder sshKeyPassphrase(String sshKeyPassphrase) {
            this.sshKeyPassphrase = sshKeyPassphrase;
            return this;
        }

        public Builder sshExtraArgs(String sshExtraArgs) {
            this.sshExtraArgs = sshExtraArgs;
            return this;
        }

        public ScmConfiguration build() {
            return new ScmConfiguration(this);
        }
    }
}
