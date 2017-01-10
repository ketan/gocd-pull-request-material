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
import com.jcraft.jsch.JSchException;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.tools.ant.types.Commandline;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GitOverSshIntegrationTest {
    private static final String GIT_REMOTE_PATH = "git/my-project";
    private static final String LOGIN_PASSWORD = "p@ssw0rd";
    private static final String LOGIN_USER = "git";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final String sshKeyPassphrase = UUID.randomUUID().toString();
    private SshServer sshd;
    private File hostKeyFile;
    private KeyPair keyPair1;
    private KeyPair keyPair2;
    private String encryptedPrivateKey2;
    private File gitRepositoriesRoot;

    @Before
    public void setUp() throws Exception {
        File basedir = temporaryFolder.newFolder("source-root");
        new GitRepository(basedir).initialize();

        gitRepositoriesRoot = temporaryFolder.newFolder("git-root");
        FileUtils.copyDirectory(basedir, new File(gitRepositoriesRoot, GIT_REMOTE_PATH));
        hostKeyFile = temporaryFolder.newFile();

        keyPair1 = keyPairWithPassphrase();
        keyPair2 = keyPairWithPassphrase();
        encryptedPrivateKey2 = encrypt(keyPair2.getPrivate(), sshKeyPassphrase);

        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(hostKeyProvider(hostKeyFile));
        sshd.setPasswordAuthenticator((username, password, session) -> LOGIN_USER.equals(username) && LOGIN_PASSWORD.equals(password));
        sshd.setPublickeyAuthenticator(new UserPublickeyAuthenticator(keyPair1.getPublic(), keyPair2.getPublic()));
        sshd.setCommandFactory(command -> new ProcessShellFactory(new Commandline(command).getCommandline()).create());
        sshd.start();
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
    }

    private static String encrypt(PrivateKey privateKey, String passphrase) throws OperatorCreationException, IOException, JSchException {
        JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-128-CBC");
        PEMEncryptor encryptor = encryptorBuilder.build(passphrase.toCharArray());
        JcaMiscPEMGenerator pemGenerator = new JcaMiscPEMGenerator(privateKey, encryptor);
        return toPem(pemGenerator.generate());
    }

    private static String toPem(Object pem) throws IOException {
        StringWriter out = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(out);
        writer.writeObject(pem);
        writer.close();
        return out.toString();
    }

    private static KeyPair keyPairWithPassphrase() throws GeneralSecurityException, JSchException {
        KeyPairGenerator keyPairGenerator = SecurityUtils.getKeyPairGenerator("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    private static AbstractGeneratorHostKeyProvider hostKeyProvider(File hostKeyFile) {
        AbstractGeneratorHostKeyProvider generatorHostKeyProvider = SecurityUtils.createGeneratorHostKeyProvider(hostKeyFile.toPath());
        generatorHostKeyProvider.setAlgorithm("RSA");
        generatorHostKeyProvider.setKeySize(1024);
        generatorHostKeyProvider.loadKeys();
        return generatorHostKeyProvider;
    }

    public String sshUrl() {
        return String.format("ssh://%s@localhost:%d/%s", LOGIN_USER, sshd.getPort(), new File(gitRepositoriesRoot, GIT_REMOTE_PATH).getAbsolutePath());
    }

    private String badSshUrl() {
        return String.format("ssh://%s@localhost:%d%s", LOGIN_USER, sshd.getPort(), "/git/bad-url");
    }

    @Test
    public void shouldAllowSshConnectionUsingSSHKeyWithNoPassphrase() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(sshUrl())
                .sshKey(toPem(keyPair1.getPrivate()))
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(0);
    }

    @Test
    public void shouldAllowSshConnectionUsingSSHPassword() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(sshUrl())
                .password(LOGIN_PASSWORD)
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(0);
    }

    @Test
    public void shouldFailIfSshConnectionFailsBecauseOfMissingSshKey() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(sshUrl())
                .build();
        GoPluginApiResponse response = new CheckConnectionExecutor(config).execute();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getOutput().getUTF8()).contains("com.jcraft.jsch.JSchException: Auth fail");
        assertThat(result.getOutput().getUTF8()).contains("fatal");
        assertThat(result.getExitValue()).isEqualTo(128);
    }

    @Test
    public void shouldFailIfSshConnectionFailsBecauseOfBadRepositoryPathInUrl() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(badSshUrl())
                .password(LOGIN_PASSWORD)
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getOutput().getUTF8()).contains("fatal");
        assertThat(result.getOutput().getUTF8()).contains("/git/bad-url");
        assertThat(result.getExitValue()).isEqualTo(128);
    }

    @Test
    public void shouldAllowSshConnectionForSshKeyWithPassphrase() throws Exception {
        ScmConfiguration config = ScmConfiguration.newBuilder()
                .url(sshUrl())
                .sshKey(encryptedPrivateKey2)
                .sshKeyPassphrase(sshKeyPassphrase)
                .build();

        ProcessResult result = Git.newBuilder().args("ls-remote", "${git.url}").configuration(config).build().execute();
        assertThat(result.getExitValue()).isEqualTo(0);
    }

    private static class UserPublickeyAuthenticator extends KeySetPublickeyAuthenticator {
        UserPublickeyAuthenticator(PublicKey... keys) {
            super(Arrays.asList(keys));
        }

        @Override
        public boolean authenticate(String username, PublicKey key, ServerSession session, Collection<? extends PublicKey> keys) {
            return LOGIN_USER.equals(username) && super.authenticate(username, key, session, keys);
        }
    }
}
