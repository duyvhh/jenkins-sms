package com.hoiio.jenkins.plugins;

import com.hoiio.sdk.Hoiio;
import com.hoiio.sdk.exception.HoiioException;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SMSNotification extends Notifier {

    private final String recipients;

    @DataBoundConstructor
    public SMSNotification(String recipients) {
        this.recipients = recipients;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {

            String appId = getDescriptor().getAppId();
            String accessToken = getDescriptor().getAccessToken();

            if (isEmpty(recipients)) {
                listener.error(
                        "No recipients");
                return true;
            }

            if (isEmpty(appId) || isEmpty(accessToken)) {
                listener.error(
                        "Hoiio credentials not configured; cannot send SMS notification");
                return true;
            }

            String message = "Build failed: " + build.getDisplayName() + " at " + getDateString(build.getTime());

            List<String> receiverList = new ArrayList<String>();
            receiverList.addAll(Arrays.asList(recipients.split(",")));

            try {
                Hoiio hoiio = new Hoiio(appId, accessToken);
                hoiio.getSmsService().sendBulk(receiverList, "Test");
            } catch (HoiioException e) {
                listener.error(
                        "Failed to send SMS notification: " + e.getMessage());
                build.setResult(Result.UNSTABLE);
            }
        }

        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public String getRecipients() {
        return recipients;
    }

    /**
     * Descriptor for {@link SMSNotification}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/SMSNotification/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String appId;
        private String accessToken;

        public DescriptorImpl() {
            super(SMSNotification.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }


        public String getDisplayName() {
            return "Send SMS Notification";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            appId = formData.getString("appId");
            accessToken = formData.getString("accessToken");
            save();
            return super.configure(req,formData);
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private String getDateString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(d);
    }
}

