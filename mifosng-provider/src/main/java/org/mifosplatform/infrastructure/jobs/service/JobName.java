package org.mifosplatform.infrastructure.jobs.service;

public enum JobName {

    INVOICE("INVOICING"),REQUESTOR("REQUESTOR"),RESPONSOR("Responser"),SIMULATOR("SIMULATOR"),PUSH_NOTIFICATION("MESSAGING"),
    GENERATE_STATEMENT("STATEMENT"),MESSAGE_MERGE("MERGE_MESSAGE"),AUTO_EXIPIRY("AUTO_EXPIRY"), Middleware("MIDDLEWARE"),
    EVENT_ACTION_PROCESSOR("EVENT_ACTIONS"),REPORT_EMAIL("REPORTER"),REPORT_STATMENT("PDF");

    private final String name;

    JobName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
