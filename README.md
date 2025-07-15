# ExceptionPopupMessage

`ExceptionPopupMessage` is a Java utility class for desktop applications that simplifies exception reporting. It presents a user-friendly popup dialog with the option to:

- Display detailed error information.
- Copy the stack trace to the clipboard.
- Open a URL to report the issue.

It is particularly useful for Java Swing applications that require convenient and guided error feedback from users.

## Features

- Show error messages with full stack traces.
- Let users copy the error to the clipboard.
- Allow users to report the error through a predefined URL.
- Customize button labels, instructions, and icons.
- Optionally attach a listener to track user interaction with the dialog.


## Installation
If you are using a **Maven** project, you can add this dependency to your `pom.xml` file:
```xml
<dependency>
  <groupId>io.github.DennisTurco</groupId>
  <artifactId>exceptionpopupmessage</artifactId>
  <version>1.0.0</version>
</dependency>
```
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

## Usage

### Basic Example

```java
import remindme.Managers.ExceptionPopupMessage;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Simulate an exception
            throw new RuntimeException("Simulated error for testing");
        } catch (Exception e) {
            ExceptionPopupMessage popup = new ExceptionPopupMessage(
                "Application Error",
                UIManager.getIcon("OptionPane.errorIcon"),
                true, // allow copy to clipboard
                true, // allow reporting
                "https://yourapp.com/report"
            );

            popup.openExceptionMessage("An unexpected error occurred.", e);
        }
    }
}
```

### With Listener

```java
popup.setExceptionDialogListener(new ExceptionPopupMessage.ExceptionDialogListener() {
    @Override
    public void onCopyToClipboard(String stackTrace) {
        System.out.println("Error copied to clipboard:\n" + stackTrace);
    }

    @Override
    public void onReportError(String reportUrl) {
        System.out.println("User chose to report the error: " + reportUrl);
    }

    @Override
    public void onClose() {
        System.out.println("User closed the error dialog.");
    }
});
```


## Customization

You can modify the appearance and behavior of the popup:

```java
popup.setInstructionsMessage("Oops! Something went wrong. Please help us fix it.");
popup.setCloseButtonText("Dismiss");
popup.setCopyButtonText("Copy Details");
popup.setReportButtonText("Send Report");
popup.setParentComponent(yourFrame); // Optional: attach to a specific parent window
```

---

## License

This utility is provided under the MIT License.