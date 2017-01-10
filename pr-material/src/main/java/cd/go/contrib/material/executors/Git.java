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
import cd.go.contrib.material.util.ScriptGenerator;
import cd.go.contrib.material.util.Util;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Git {
    private final String[] args;
    private final ScmConfiguration configuration;

    private Git(Builder builder) {
        args = builder.args;
        configuration = builder.configuration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public ProcessResult execute() throws InterruptedException, TimeoutException, IOException {
        ProcessExecutor git = new ProcessExecutor(args())
                .readOutput(true)
                .timeout(30, TimeUnit.SECONDS);

        File cliJar = null;
        File askPassExecutable = null;
        File sshExecutable = null;
        try {
            cliJar = Util.copyResourceIntoTempFile("/resources/cli.jar", "cli", ".jar");

            askPassExecutable = askpassExecutable(cliJar);
            sshExecutable = sshExecutable(cliJar);

            if (configuration.hasPassword()) {
                git.environment("PR_GIT_ASKPASS_USER", configuration.username());
                git.environment("PR_GIT_ASKPASS_PASS", configuration.password());
            }

            git.environment("GIT_ASKPASS", askPassExecutable.getAbsolutePath());
            git.environment("GIT_SSH", sshExecutable.getAbsolutePath());

            if (configuration.hasSshKey()) {
                git.environment("PR_SSH_KEY", configuration.sshKey());

                if (configuration.hasSshKeyPassphrase()) {
                    git.environment("PR_SSH_KEY_PASSPHRASE", configuration.sshKeyPassphrase());
                }
            }


            if (configuration.hasPassword()) {
                git.environment("PR_SSH_PASSWORD", configuration.password());
            }

            if (configuration.enableTracing()) {
                git.environment("GIT_TRACE", "/tmp/git-trace.log");
                git.environment("GIT_SSH_TRACE", "/tmp/git-ssh-trace.log");
                git.environment("GIT_CURL_VERBOSE", "1");
            }

            return git.execute();
        } finally {
            delete(cliJar);
            delete(askPassExecutable);
            delete(sshExecutable);
        }
    }

    private File sshExecutable(File cliJar) throws IOException {
        return new ScriptGenerator("git-ssh", "cd.go.contrib.material.cli.Ssh", singletonList(cliJar.getAbsolutePath()), emptyList()).write();
    }

    private File askpassExecutable(File cliJar) throws IOException {
        return new ScriptGenerator("git-ask-pass", "cd.go.contrib.material.cli.GitAskPass", singletonList(cliJar.getAbsolutePath()), emptyList()).write();
    }

    private ArrayList<String> args() {
        ArrayList<String> args = new ArrayList<>();
        args.add("git");

        for (String arg : this.args) {
            if ("${git.url}".equals(arg)) {
                args.add(configuration.url());
            } else {
                args.add(arg);
            }
        }
        return args;
    }

    private void delete(File file) {
        if (file != null && file.exists()) {
            file.deleteOnExit();
            file.delete();
        }
    }


    public static final class Builder {
        private String[] args;
        private ScmConfiguration configuration;

        private Builder() {
        }

        public Builder args(String... args) {
            this.args = args;
            return this;
        }

        public Builder configuration(ScmConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Git build() {
            return new Git(this);
        }
    }
}
