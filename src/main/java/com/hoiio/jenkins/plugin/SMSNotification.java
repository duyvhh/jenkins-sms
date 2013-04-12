/*
Copyright (C) 2013 Hoiio Pte Ltd (http://www.hoiio.com)

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/

package com.hoiio.jenkins.plugin;

import com.hoiio.sdk.Hoiio;
import com.hoiio.sdk.exception.HoiioException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
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

            String message = "Jenkins Build failed: " + build.getProject().getDisplayName() + " at " + getDateString(build.getTime());

            if (message.length() > 150) {
                message =  "Jenkins Build failed: " + build.getProject().getDisplayName().substring(0, 100) + "... " + " at " + getDateString(build.getTime());
            }

            List<String> receiverList = new ArrayList<String>();

            //Remove all spaces
            String recipientStr = recipients.trim().replaceAll("\\s","");
            receiverList.addAll(Arrays.asList(recipientStr.split(",")));

            try {
                Hoiio hoiio = new Hoiio(appId, accessToken);
                hoiio.getSmsService().sendBulk(receiverList, message);
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
     * See <tt>src/main/resources/hudson/plugin/hello_world/SMSNotification/*.jelly</tt>
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

        @Override
        public String getDisplayName() {
            return "SMS Notification";
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

        public FormValidation doNumberCheck(@QueryParameter String param) throws IOException, ServletException {
            if (param == null || param.trim().length() == 0) {
                return FormValidation.warning("You must fill recipients' numbers!");
            }

            param = param.trim().replaceAll("\\s","");
            for (String p: param.split(",")) {
                if (!PhoneNumberValidator.validatePhoneNumber(p)) {
                    return FormValidation.error("Formats of some recipients' numbers are invalid.");
                }
            }

            return FormValidation.ok();
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private String getDateString(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }
}

