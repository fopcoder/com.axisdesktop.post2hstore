package com.axisdesktop.post2hstore;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class Post2Hstore {
	static Connection conn;
	static int providerId = 1;

	public static void main( String[] args ) throws ClassNotFoundException, SQLException {
		conn = getConnection( args );
		getUI();
	}

	public static void saveData( String url, String data ) throws SQLException {
		if( url == null || url.equals( "" ) ) throw new IllegalArgumentException( "url is null" );
		if( data == null || data.equals( "" ) ) throw new IllegalArgumentException( "data is null" );

		Map<String, String> hstore = new HashMap<>();

		for( String row : data.split( "\n" ) ) {
			String[] kv = row.split( "\"=>\"" );
			kv[0] = kv[0].replaceFirst( "\"", "" ).trim();
			kv[1] = kv[1].replaceFirst( ",$", "" ).replaceFirst( "\"$", "" ).trim();
			hstore.put( kv[0], kv[1] );
		}

		String query = "INSERT INTO crawler.provider_url(provider_id, url, status_id, type_id, params ) VALUES( ?, ?, 4, 1, ?::hstore )";
		PreparedStatement stmt = conn.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
		stmt.setInt( 1, providerId );
		stmt.setString( 2, url );
		stmt.setObject( 3, hstore );

		stmt.executeUpdate();

		// try( ResultSet generatedKeys = stmt.getGeneratedKeys() ) {
		// if( generatedKeys.next() ) {
		// long id = generatedKeys.getLong( 1 );
		//
		// // query = "UPDATE crawler.provider_url SET params = ?::hstore WHERE id = ?";
		// // PreparedStatement stu = conn.prepareStatement( query );
		// // stu.setString( 1, data );
		// // stu.setLong( 2, id );
		// // System.out.println( stu.executeUpdate() );
		// //
		// // for( Entry<String, String> kv : hstore.entrySet() ) {
		// // stu.setString( 1, "\"" + kv.getKey() + "\"=>\"" + kv.getValue() + "\"" );
		// // stu.setLong( 2, id );
		// // System.out.println( stu.executeUpdate() );
		// // }
		//
		// }
		// else {
		// throw new SQLException( "Creating user failed, no ID obtained." );
		// }
		// }

	}

	public static Connection getConnection( String[] args ) throws SQLException, ClassNotFoundException {
		String url = args[0];
		String user = args[1];
		String password = args[2];

		DriverManager.registerDriver( new org.postgresql.Driver() );

		return DriverManager.getConnection( url, user, password );
	}

	public static Container getUI() {
		JFrame frame = new JFrame( "POST to hstore" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JTextArea srcTa = new JTextArea( "", 15, 100 );
		srcTa.setLineWrap( false );
		// srcTa.setText( "yff:66\ngg:17" );

		JTextArea dstTa = new JTextArea( "", 15, 100 );
		dstTa.setLineWrap( false );

		JTextField txtF = new JTextField( 50 );
		// txtF.setText( "http://test.com" );

		JLabel statusLabel = new JLabel( "status" );

		JButton butParse = new JButton( "Parse" );
		butParse.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				StringBuilder sb = new StringBuilder();

				for( String s : srcTa.getText().split( "\\n" ) ) {
					sb.append( "\"" + s.replaceFirst( ":", "\"=>\"" ) + "\",\n" );
				}
				sb.append( "\"method\"=>\"post\"" );

				dstTa.setText( sb.toString() );
			}
		} );

		JButton butSave = new JButton( "Save" );
		butSave.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				try {
					saveData( txtF.getText(), dstTa.getText() );
					// JOptionPane.showMessageDialog( frame, "Saved", "Saved", JOptionPane.INFORMATION_MESSAGE );
					statusLabel.setText( txtF.getText() + " OK" );
				}
				catch( Exception e1 ) {
					JOptionPane.showMessageDialog( frame, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
					e1.printStackTrace();
				}
			}
		} );

		JButton butClear = new JButton( "Clear" );
		butClear.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				srcTa.setText( "" );
				txtF.setText( "" );
				dstTa.setText( "" );
				statusLabel.setText( "" );
			}
		} );

		JPanel ctrlPanel = new JPanel();
		ctrlPanel.setLayout( new BoxLayout( ctrlPanel, BoxLayout.X_AXIS ) );
		ctrlPanel.add( butParse );
		ctrlPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
		ctrlPanel.add( txtF );
		ctrlPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
		ctrlPanel.add( butSave );
		ctrlPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
		ctrlPanel.add( butClear );

		JPanel srcPanel = new JPanel();
		srcPanel.add( new JScrollPane( srcTa ) );

		JPanel dstPanel = new JPanel();
		dstPanel.add( new JScrollPane( dstTa ) );

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout( new BorderLayout() );
		statusPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
		statusPanel.setPreferredSize( new Dimension( frame.getWidth(), 30 ) );
		statusPanel.add( statusLabel, BorderLayout.WEST );

		Container pane = frame.getContentPane();
		pane.setLayout( new BoxLayout( pane, BoxLayout.Y_AXIS ) );
		pane.add( srcPanel );
		pane.add( ctrlPanel );
		pane.add( dstPanel );
		pane.add( statusPanel );

		frame.pack();
		frame.setVisible( true );

		return frame;
	}
}
