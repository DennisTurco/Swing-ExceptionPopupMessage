package com.dennisturco.exceptionpopupmessage;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A utility class for displaying exception messages to users with options
 * to copy the stack trace to the clipboard or report the error via a URL.
 *
 * This class shows a Swing dialog for error presentation and allows limited user interaction.
 * It is especially useful for desktop Java applications that need to provide user-friendly
 * error reporting mechanisms.
 */
public class ExceptionPopupMessage {

    private String title;
    private Icon icon;
    private boolean copyErrorToClipboard = true;
    private boolean reportError = true;
    private String reportUrl;
    private final int maxDialogWidth = 500;
    private String instructions = "Please report this error, either with an image of the screen or by copying the following error text (it is appreciable to provide a description of the operations performed before the error):";
    private String closeButtonText = "Close";
    private String copyButtonText = "Copy error to the clipboard";
    private String reportButtonText = "Report the error";
    private ExceptionDialogListener listener = null;
    private Component parentComponent = null;

    /**
     * Constructs an ExceptionPopupMessage with clipboard copy and error reporting options.
     *
     * @param title                The title of the error dialog.
     * @param icon                 The icon to display in the dialog (null for default).
     * @param copyErrorToClipboard Whether to allow copying the error to the clipboard.
     * @param reportError          Whether to allow reporting the error.
     * @param reportErrorUrl       The URL to open when reporting the error (must not be null or empty if reportError is true).
     * @throws IllegalArgumentException if reportError is true and the URL is null or empty.
     */
    public ExceptionPopupMessage(String title, Icon icon, boolean copyErrorToClipboard, boolean reportError, String reportErrorUrl) {
        if (reportError && (reportErrorUrl == null || reportErrorUrl.isEmpty())) {
            throw new IllegalArgumentException("URL for the error report is required when reporting is enabled");
        }
        this.title = title;
        this.icon = icon;
        this.copyErrorToClipboard = copyErrorToClipboard;
        this.reportError = reportError;
        this.reportUrl = reportErrorUrl;
    }

    /**
     * Constructs an ExceptionPopupMessage with clipboard copy and no reporting URL.
     *
     * @param title                The title of the error dialog.
     * @param icon                 The icon to display in the dialog (null for default).
     * @param copyErrorToClipboard Whether to allow copying the error to the clipboard.
     * @param reportError          Whether to allow reporting the error (must be false in this constructor).
     * @throws IllegalArgumentException if reportError is true.
     */
    public ExceptionPopupMessage(String title, Icon icon, boolean copyErrorToClipboard, boolean reportError) {
        if (reportError) {
            throw new IllegalArgumentException("URL for the error report is required when reporting is enabled");
        }
        this.title = title;
        this.icon = icon;
        this.copyErrorToClipboard = copyErrorToClipboard;
        this.reportError = reportError;
    }

    /**
     * Displays the error dialog to the user.
     *
     * @param errorMessage A short message describing the error.
     * @param throwable The exception.
     */
    public void openExceptionMessage(String errorMessage, Throwable throwable) {
        Object[] options = setupOptions();
        String stackTrace = getStackTraceAsString(throwable);
        String stackTraceMessage = getStackTraceMessage(errorMessage, stackTrace);
        int choice;
        do {
            if (stackTraceMessage.length() > 1500) {
                stackTraceMessage = stackTraceMessage.substring(0, 1500) + "...";
            }

            JTextArea messageArea = new JTextArea(stackTraceMessage);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setColumns(50);
            messageArea.setSize(new Dimension(maxDialogWidth, Integer.MAX_VALUE));
            messageArea.setPreferredSize(new Dimension(maxDialogWidth, messageArea.getPreferredSize().height));

            JScrollPane scrollPane = new JScrollPane(messageArea);
            scrollPane.setPreferredSize(new Dimension(maxDialogWidth, 300));

            choice = JOptionPane.showOptionDialog(
                parentComponent,
                scrollPane,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                icon,
                options,
                options[0]
            );

            if (choice == 1 && copyErrorToClipboard) {
                copyMessageToTheClipboard(stackTrace);
                if (listener != null) {
                    listener.onCopyToClipboard(stackTrace);
                }
            } else if (choice == 2 && reportError) {
                reportError();
                if (listener != null) {
                    listener.onReportError(reportUrl);
                }
            }

        } while (choice == 1 || choice == 2);

        if (choice == 0 && listener != null) {
            listener.onClose();
        }
    }

    /**
     * Sets a listener to respond to dialog events such as copy, report, or close.
     *
     * @param listener the listener to set
     */
    public void setExceptionDialogListener(ExceptionDialogListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the instructional message that will appear above the stack trace in the dialog.
     *
     * @param instructions A custom message to guide the user.
     */
    public void setInstructionsMessage(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Sets a custom label for the "Close" button in the dialog.
     *
     * @param text the new label text
     */
    public void setCloseButtonText(String text) {
        this.closeButtonText = text;
    }

    /**
     * Sets a custom label for the "Copy" button in the dialog.
     *
     * @param text the new label text
     */
    public void setCopyButtonText(String text) {
        this.copyButtonText = text;
    }

    /**
     * Sets a custom label for the "Report" button in the dialog.
     *
     * @param text the new label text
     */
    public void setReportButtonText(String text) {
        this.reportButtonText = text;
    }

    /**
     * Sets the parent component for the popup.
     *
     * @param parentComponent the parent component
     */
    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    // Copies the stack trace to the system clipboard.
    private void copyMessageToTheClipboard(String stackTrace) {
        StringSelection selection = new StringSelection(stackTrace);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        JOptionPane.showMessageDialog(null, "Error text has been copied to the clipboard");
    }

    // Opens the error reporting URL in the system browser.
    private void reportError() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(reportUrl));
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to open the report URL.", e);
        }
    }

    // Prepares the list of available options based on feature flags.
    private Object[] setupOptions() {
        return Stream.of(
            closeButtonText,
            copyErrorToClipboard ? copyButtonText : null,
            reportError ? reportButtonText : null
        ).filter(Objects::nonNull).toArray();
    }

    // Builds the complete message to be displayed, combining instructions and stack trace.
    private String getStackTraceMessage(String errorMessage, String stackTrace) {
        if (errorMessage == null) {
            errorMessage = "";
        }
        stackTrace = !errorMessage.isEmpty() ? errorMessage + "\n" + stackTrace : errorMessage + stackTrace;
        return instructions + "\n" + stackTrace;
    }

    // Extract the stackTrace from a Throwable object
    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
