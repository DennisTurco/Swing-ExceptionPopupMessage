package com.dennisturco.exceptionpopupmessage;

/**
* Listener interface for handling user actions from the exception dialog.
*/
public interface ExceptionDialogListener {
    /**
     * Called when the user chooses to copy the stack trace to the clipboard.
     *
     * @param stackTrace the full stack trace string
     */
    default void onCopyToClipboard(String stackTrace) {}

    /**
     * Called when the user chooses to report the error.
     *
     * @param reportUrl the URL used for reporting the error
     */
    default void onReportError(String reportUrl) {}

    /**
     * Called when the user closes the error dialog.
     */
    default void onClose() {}
}