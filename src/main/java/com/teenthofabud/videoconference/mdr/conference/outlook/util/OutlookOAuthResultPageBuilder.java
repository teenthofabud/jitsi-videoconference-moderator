package com.teenthofabud.videoconference.mdr.conference.outlook.util;

/** Creates the small browser page shown after Microsoft OAuth redirects to Moderator. */
public final class OutlookOAuthResultPageBuilder {
    private OutlookOAuthResultPageBuilder() {
    }

    /**
     * Builds an HTML result page that informs the add-in of the OAuth result and closes the popup.
     *
     * @param success whether the connection step succeeded
     * @param message user-facing status text
     * @return HTML document for the OAuth callback response
     */
    public static String build(boolean success, String message) {
        String color = success ? "#107c10" : "#a4262c";
        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>TOAB Jitsi</title></head>"
                + "<body style=\"font:16px Arial,sans-serif;padding:24px\"><p style=\"color:" + color + "\">" + safe + "</p>"
                + "<script>if(window.opener){window.opener.postMessage({type:'toab-outlook-graph-connected'},'*');}"
                + "setTimeout(function(){window.close()},1200);</script></body></html>";
    }
}
