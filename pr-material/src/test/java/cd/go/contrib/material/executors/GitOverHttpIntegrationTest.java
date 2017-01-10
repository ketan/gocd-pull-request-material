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
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.http.server.GitServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.ProcessResult;

import javax.servlet.DispatcherType;
import java.io.File;
import java.util.EnumSet;

import static cd.go.contrib.material.executors.BasicAuthenticationFilter.LOGIN_PASSWORD;
import static cd.go.contrib.material.executors.BasicAuthenticationFilter.LOGIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

public class GitOverHttpIntegrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File gitRepositoriesRoot;
    private Server server;

    @Before
    public void setUp() throws Exception {
        File basedir = temporaryFolder.newFolder("source-root");
        new GitRepository(basedir).initialize();

        gitRepositoriesRoot = temporaryFolder.newFolder("git-root");
        FileUtils.copyDirectory(basedir, new File(gitRepositoriesRoot, "/public/my-project"));
        FileUtils.copyDirectory(basedir, new File(gitRepositoriesRoot, "/private/my-project"));

        server = new Server(0);
        ServletHandler servlet = new ServletHandler();

        ServletHolder servletHolder = servlet.addServletWithMapping(GitServlet.class, "/git/*");
        servletHolder.setInitParameter("base-path", gitRepositoriesRoot.getAbsolutePath());
        servletHolder.setInitParameter("export-all", "true");

        servlet.addFilterWithMapping(BasicAuthenticationFilter.class, "/git/private/*", EnumSet.of(DispatcherType.REQUEST));
        server.setHandler(servlet);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.join();
    }

    private String publicHttpUrl() {
        return "http://localhost:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/git/public/my-project";
    }

    private String privateHttpUrl() {
        return "http://localhost:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/git/private/my-project";
    }

    private String badHttpUrl() {
        return "http://localhost:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/bad-url";
    }

    @Test
    public void shouldConnectToRemoteHttpRepository() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(publicHttpUrl())
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(0);
//
//        GoPluginApiResponse response = new CheckConnectionExecutor(config).execute();
//        GoPluginApiResponseAssert.assertThat(response).isSuccessful();
    }

    @Test
    public void shouldFailOnBadHttpRepositoryUrl() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(badHttpUrl())
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(128);

        assertThat(result.getOutput().getUTF8()).contains("fatal");
        assertThat(result.getOutput().getUTF8()).contains("not found");
    }

    @Test
    public void shouldConnectToHttpUrlWithAuthorization() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(privateHttpUrl())
                .username(LOGIN_USER)
                .password(LOGIN_PASSWORD)
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(0);
//
//        GoPluginApiResponse response = new CheckConnectionExecutor(config).execute();
//        GoPluginApiResponseAssert.assertThat(response).isSuccessful();
    }

    @Test
    public void shouldFailWithBadAuthenticationOnHttp() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(privateHttpUrl())
                .username("bob")
                .password("bad-password")
                .build();


        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(128);
        assertThat(result.getOutput().getUTF8()).contains("fatal");
        assertThat(result.getOutput().getUTF8()).contains("Authentication failed for");
    }

    @Test
    public void shouldFailWithBadAuthenticationOnHttpWhenCredentialsNotProvided() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(privateHttpUrl())
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(128);
        assertThat(result.getOutput().getUTF8()).contains("fatal");
        assertThat(result.getOutput().getUTF8()).contains("Authentication failed");
    }
}
