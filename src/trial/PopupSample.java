package trial;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

public class PopupSample {
  public static void main(String args[]) {
    JFrame frame = new JFrame("Popup Example");
    Container content = frame.getContentPane();

    final JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem1 = new JMenuItem("Option 1");
    popup.add(menuItem1);

    JMenuItem menuItem2 = new JMenuItem("Option 2");
    popup.add(menuItem2);

    final JTextField textField = new JTextField();
    content.add(textField, BorderLayout.NORTH);

    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          int dotPosition = textField.getCaretPosition();
          Rectangle popupLocation = textField
              .modelToView(dotPosition);
          popup.show(textField, popupLocation.x, popupLocation.y);
        } catch (BadLocationException badLocationException) {
          System.out.println("Oops");
        }
      }
    };
    KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0,
        false);
    textField.registerKeyboardAction(actionListener, keystroke,
        JComponent.WHEN_FOCUSED);

    frame.setSize(250, 150);
    frame.setVisible(true);
  }
}