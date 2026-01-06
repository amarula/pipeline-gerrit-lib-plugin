package org.jenkinsci.plugins.pipeline.gerrit.library;

import hudson.Extension;
import hudson.model.Job;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.traits.IgnoreOnPushNotificationTrait;
import jenkins.plugins.git.traits.RefSpecsSCMSourceTrait;
import jenkins.plugins.git.traits.RefSpecsSCMSourceTrait.RefSpecTemplate;

import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import org.jenkinsci.plugins.workflow.libs.LibraryResolver;
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever;

/**
 * Resolves libraries from a configured Gerrit server.
 */
@Extension
public class GerritLibraryResolver extends LibraryResolver {

    @Override
    public boolean isTrusted() {
        return false;
    }

    @Override
    public Collection<LibraryConfiguration> forJob(Job<?,?> job, Map<String,String> libraryVersions) {
        GerritLibraryConfiguration descriptor = GerritLibraryConfiguration.get();
        if (descriptor == null) {
            throw new RuntimeException("Unable to access global configuration");
        }
        String server = descriptor.getGerritServer();
        String protocol = descriptor.getAccessMethod();
        String credsId = descriptor.getCredentialsId();

        if (server == null || server.isEmpty()) {
            return Collections.emptyList();
        }

        List<LibraryConfiguration> libs = new ArrayList<>();

        for (Map.Entry<String,String> entry : libraryVersions.entrySet()) {
            String libraryName = entry.getKey();
            String revParse = entry.getValue();
            String remoteUrl;

            if ("ssh".equalsIgnoreCase(protocol)) {
                int port = descriptor.getSshPort();
                remoteUrl = "ssh://" + server + ":" + port + "/" + libraryName + ".git";
            } else {
                String baseUrl = server.startsWith("http") ? server : "https://" + server;
                remoteUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + libraryName + ".git";
            }

            GitSCMSource scm = new GitSCMSource(remoteUrl);

            if (credsId != null && !credsId.isEmpty()) {
                scm.setCredentialsId(credsId);
            }

            scm.setTraits(Arrays.asList(
                    new IgnoreOnPushNotificationTrait(),
                    new RefSpecsSCMSourceTrait(Arrays.asList(
                        new RefSpecTemplate("+refs/" + revParse + ":refs/remotes/origin/" + revParse)
                    ))
            ));

            LibraryConfiguration lib = new LibraryConfiguration(libraryName, new SCMSourceRetriever(scm));
            lib.setDefaultVersion("master");
            libs.add(lib);
        }
        return libs;
    }
}
