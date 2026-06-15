package de.steingaming.hqol.fabric;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirmamentWarningFrame {
    static void main() {
        System.exit(showWarningWindow() ? 0 : 1);
    }
    public static boolean showWarningWindow() {
        var dialog = new JDialog((Frame) null, "!!! FIRMAMENT DETECTED !!!", true);
        var textPane = new JPanel();
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.PAGE_AXIS));
        textPane.add(new JLabel("<html><p style=\"font-size: 32px\">YOU HAVE FIRMAMENT INSTALLED.</p></htm>"));
        var infoText = new JLabel(
                """
                            <html>
                            <p style="font-size: 12px">
                            Why is this a warning really? Because they are trying to get you banned!
                            |One of their Features is <a href="https://github.com/FirmamentMC/Firmament/blob/mc-1.21.11/src/main/kotlin/features/misc/ModAnnouncer.kt">ModAnnouncer</a>.
                            |This sends any mod you have installed to the server. Why? Because they want to "be honest about themselves"... And this is all done without the users knowledge OR agreement!
                            |I myself would recommend you to uninstall it immediately, or seek a modified version without this feature.
                            |This is a breach of trust, and I do not know if they will continue this kind of behavior.
                            |
                            |Again, as stated in the license: I DO NOT GIVE OUT WARRENTIES, but I do not want people to get banned because of my mod.
                            <br><br><br><br>
                            |This warning is only displayed once.
                            |You can now either exit minecraft, or continue starting Minecraft.
                            </p>
                            </html>
                        """.trim().replace("|", "<br>")
        );
        infoText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://github.com/FirmamentMC/Firmament/blob/mc-1.21.11/src/main/kotlin/features/misc/ModAnnouncer.kt"));
                } catch (IOException _ignored) {
                }
            }
        });
        textPane.add(infoText);
        var buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        AtomicBoolean status = new AtomicBoolean(false);
        var exitButton = new JButton("EXIT!");
        exitButton.addActionListener((l) -> {
            status.set(false);
            dialog.dispose();
        });
        buttonPane.add(exitButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        var continueButton = new JButton("I understand the risks, continue.");
        continueButton.addActionListener((l) -> {
            status.set(true);
            dialog.dispose();
        });
        buttonPane.add(continueButton);
        var contentPane = dialog.getContentPane();
        contentPane.add(textPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
        dialog.setMinimumSize(new Dimension(1000, 500));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return status.get();
    }
}
