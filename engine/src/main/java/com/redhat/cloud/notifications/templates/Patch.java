package com.redhat.cloud.notifications.templates;

import com.redhat.cloud.notifications.models.EmailSubscriptionType;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

public class Patch implements EmailTemplate {

    private static final String InstantAdvisories = "instant-advisories";

    @Override
    public TemplateInstance getTitle(String eventType, EmailSubscriptionType type) {
        if (type == EmailSubscriptionType.INSTANT) {
            if (eventType.equals(Patch.InstantAdvisories)) {
                return Templates.instantAdvisoriesEmailTitle();
            }
        }

        throw new UnsupportedOperationException(String.format(
        "No email title template for Patch event_type: %s and EmailSubscription: %s found.",
        eventType, type));
    }

    @Override
    public TemplateInstance getBody(String eventType, EmailSubscriptionType type) {
        if (type == EmailSubscriptionType.INSTANT) {
            if (eventType.equals(Patch.InstantAdvisories)) {
                return Templates.instantAdvisoriesEmailBody();
            }
        }

        throw new UnsupportedOperationException(String.format(
        "No email body template for Patch event_type: %s and EmailSubscription: %s found.",
        eventType, type));
    }

    @Override
    public boolean isSupported(String eventType, EmailSubscriptionType type) {
        return (type == EmailSubscriptionType.INSTANT &&
                (eventType.equals(Patch.InstantAdvisories)));
    }

    @Override
    public boolean isEmailSubscriptionSupported(EmailSubscriptionType type) {
        return type == EmailSubscriptionType.INSTANT;
    }

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {

        public static native TemplateInstance instantAdvisoriesEmailTitle();

        public static native TemplateInstance instantAdvisoriesEmailBody();
    }
}
