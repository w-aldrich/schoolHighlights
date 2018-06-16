package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import server.CreateServer;

public class ServerInterface {
	
	public ServerInterface() {
		JFrame framework = new JFrame("Server Interface");
		framework.setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(3,1));
		JButton play = new JButton("Start Server");
		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				CreateServer server = new CreateServer();
//				Thread runServer = new Thread(server);
//				runServer.run();
			}
		});
		JButton stop = new JButton("End Server Session");
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				CreateServer.closeServer();
			}
		});
		JButton settings = new JButton("Settings");
		buttons.add(play);
		buttons.add(stop);
		buttons.add(settings);
		framework.add(buttons, BorderLayout.WEST);
		framework.add(log(), BorderLayout.CENTER);
		framework.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		framework.pack();
		framework.setLocationRelativeTo(null);
		framework.setVisible(true);
	}
	
	JPanel log() {
		JPanel log = new JPanel();
		log.setLayout(new BorderLayout());
		JLabel info = new JLabel("LOG");
		log.add(info, BorderLayout.NORTH);
		Dimension size = new Dimension(500, 500);
		log.setSize(size);
		log.setPreferredSize(size);
		return log;
	}

}
