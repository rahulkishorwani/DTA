package trial;

import java.awt.*;

import javax.swing.*;

class scrollPaneDemo
		extends 	JFrame
{
	private		JScrollPane scrollPane;

	public scrollPaneDemo()
	{
	       
	    	setSize(70, 60);
	        setTitle("Model Checker");
	        setBackground(Color.white);

	        JLabel label = new JLabel( "hihihihihihihihihihihihihihihihihihihihihihihihihihihihihihihi" );
	        JScrollPane scroll_pane = new JScrollPane(label,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	        //getContentPane().add(scroll_pane, BorderLayout.CENTER);
	        JPanel jPanel1 = new JPanel();
	        jPanel1.setLayout(new BorderLayout());
	        jPanel1.add(scroll_pane, BorderLayout.CENTER);
	        JTabbedPane tabbedPane = new JTabbedPane();
	        tabbedPane.addTab("Page 1", jPanel1 );
	        getContentPane().add(tabbedPane, BorderLayout.CENTER);

	        // setting state tool as default tool, not setting will not set
	        // any tool in default way
	    }


	public static void main( String args[] )
	{
		// Create an instance of the test application
		scrollPaneDemo mainFrame	= new scrollPaneDemo();
		mainFrame.setVisible( true );
	}
}
