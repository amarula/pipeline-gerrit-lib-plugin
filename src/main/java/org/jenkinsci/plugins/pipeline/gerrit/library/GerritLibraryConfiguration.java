package org.jenkinsci.plugins.pipeline.gerrit.library;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import java.util.Collections;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

@Extension
@Symbol("GerritLibrary")
public class GerritLibraryConfiguration extends GlobalConfiguration {
    private String gerritServer;
    private String accessMethod = "ssh";
    private String credentialsId;
    private int sshPort = 29418;

    public GerritLibraryConfiguration() {
        load();
    }

    @Override
    public String getDisplayName() {
        return "Gerrit Library Resolver";
    }

    public String getGerritServer() {
        return gerritServer;
    }

    @DataBoundSetter
    public void setGerritServer(String gerritServer) {
        this.gerritServer = Util.fixEmptyAndTrim(gerritServer);
        save();
    }

    public String getAccessMethod() {
        return accessMethod;
    }

    @DataBoundSetter
    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
        save();
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
        save();
    }

    public int getSshPort() {
        return sshPort;
    }

    @DataBoundSetter
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
        save();
    }

    public static GerritLibraryConfiguration get() {
        return ExtensionList.lookupSingleton(GerritLibraryConfiguration.class);
    }

    public ListBoxModel doFillAccessMethodItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("SSH", "ssh");
        items.add("HTTPS", "https");
        return items;
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String uri) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return new StandardListBoxModel().includeEmptyValue();
        }

        return new StandardListBoxModel()
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM, item, com.cloudbees.plugins.credentials.common.StandardCredentials.class,
                        Collections.<DomainRequirement>emptyList());
    }
}
