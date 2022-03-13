package io.github.notfoundry.elevatorsimulator.floor;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;

import io.github.notfoundry.elevatorsimulator.elevator.ElevatorMain;
import io.github.notfoundry.elevatorsimulator.elevator.ElevatorSubsystem;
import io.github.notfoundry.elevatorsimulator.elevator.StopReceiver;
import io.github.notfoundry.elevatorsimulator.floor.FloorSubsystem;
import io.github.notfoundry.elevatorsimulator.scheduler.LocalSchedulerSubsystem;
import io.github.notfoundry.elevatorsimulator.scheduler.SchedulerMain;
/**
 * GUI component of the project
 * Incomplete
 * 
 * @author Chukwuka, Vis, Lazar
 *
 */
public class GUI extends JFrame implements Runnable {
	private static JPanel mainPanel;
	private JPanel floorNumPanel;
	private static JPanel elevatorInfoPanel;
	private ArrayList<JButton> floorNumberLabelList; 
	private ArrayList<JPanel> allElevatorsPanelList; //stores all elevator panels
	private static ArrayList<JPanel> allElevatorsInfoPanelList;
	private static ArrayList<JTextArea> allElevatorInfoTextAreaList;
	private ArrayList<ArrayList<JButton>> allElevatorsButtonList; //stores all elevator button lists
	private static ElevatorSubsystem elevator, elevator2, elevator3, elevator4;
	private static StopReceiver receiver, receiver2, receiver3, receiver4;
	private static FloorSubsystem floor;
	private static LocalSchedulerSubsystem scheduler;
	private static ArrayList <ElevatorSubsystem> elevators;
	private static ArrayList <StopReceiver> receivers;
	
	
	public GUI() throws Exception {
		mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(/**2 + LocalSchedulerSubsystem.regElevators().size()*/));
		
		mainPanel.add(floorNumPanel());
		allElevatorsPanelList();
		for(int i=0; i<allElevatorsPanelList.size(); i++) {
			mainPanel.add(allElevatorsPanelList.get(i));
		}
		
		mainPanel.add(elevatorInfoPanel());
		
		
		add(mainPanel);
		setTitle("Elevator System Simulation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		pack();
		setVisible(true);
		toFront();
	}
	
	private JPanel floorNumPanel() {
		floorNumPanel = new JPanel();
		floorNumPanel.setLayout(new BoxLayout(floorNumPanel, BoxLayout.PAGE_AXIS));
		floorNumPanel.setBorder(BorderFactory.createTitledBorder("Floors"));
		floorNumPanel.setSize(200, 200);
		floorNumberLabelList = new ArrayList<>();
		for(int i=0; i<ElevatorSubsystem.TOP_FLOOR; i++) {
			JButton button = new JButton("" + (ElevatorSubsystem.TOP_FLOOR-i));
			button.setOpaque(true);
			button.setBorderPainted(false);
			floorNumberLabelList.add(button);
			floorNumPanel.add(button);
		}
		return floorNumPanel;
	}
	
	private ArrayList<JPanel> allElevatorsPanelList() {
		allElevatorsPanelList = new ArrayList<>();
		allElevatorsButtonList = new ArrayList<>();
		for(int i=0; i<4; i++) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setSize(1000, 200);
			panel.setBorder(BorderFactory.createTitledBorder("Elevator " + (i+1)));
			ArrayList<JButton> list = new ArrayList<>();
			for(int j=0; j<ElevatorSubsystem.TOP_FLOOR; j++) {
				JButton button = new JButton("   ");
				button.setOpaque(true);
				button.setBorderPainted(true);
				button.setSize(500,500);
				list.add(button);
				panel.add(button);
			}
			allElevatorsButtonList.add(list);
			allElevatorsPanelList.add(panel);
		}
		return allElevatorsPanelList;
	}
	
	public static JPanel elevatorInfoPanel() {
		elevatorInfoPanel = new JPanel();
		elevatorInfoPanel.setLayout(new BoxLayout(elevatorInfoPanel, BoxLayout.PAGE_AXIS));
		elevatorInfoPanel.setSize(1000,1000);
		allElevatorsInfoPanelList();
		for(int i=0; i<allElevatorsInfoPanelList.size(); i++) {
			elevatorInfoPanel.add(allElevatorsInfoPanelList.get(i));
		}
		return elevatorInfoPanel;
	}
	
	private static ArrayList<JPanel> allElevatorsInfoPanelList() {
		allElevatorsInfoPanelList = new ArrayList<>();
		allElevatorInfoTextAreaList = new ArrayList<>();
		for(int i=0; i<4; i++) {
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Elevator " + (i+1) + " info"));
			JTextArea textArea = new JTextArea();
			textArea.setText(elevators.get(i).toString());
			panel.add(textArea);
			allElevatorInfoTextAreaList.add(textArea);
			allElevatorsInfoPanelList.add(panel);
		}
		return allElevatorsInfoPanelList;
	}
	
	private static void threadExecution (GUI g) throws Exception 
	{
		Thread schedulerThread = new Thread(scheduler);
		
		schedulerThread.start();
				
		Thread elevatorThread = new Thread(elevator);
		Thread elevatorThread2 = new Thread(elevator2);
		Thread elevatorThread3 = new Thread(elevator3);
		Thread elevatorThread4 = new Thread(elevator4);
		
		Thread receiverThread = new Thread(receiver);
		Thread receiverThread2 = new Thread(receiver2);
		Thread receiverThread3 = new Thread(receiver3);
		Thread receiverThread4 = new Thread(receiver4);
		
		Thread floorThread = new Thread(floor);
		
		receiverThread.start();
		receiverThread2.start();
		receiverThread3.start();
		receiverThread4.start();
		
		elevatorThread.start();
		elevatorThread2.start();
		elevatorThread3.start();
		elevatorThread4.start();

		floorThread.start();

		schedulerThread.join();
		floorThread.join();
		
	}
	
	private static void initializeSystems() throws Exception, SocketException, UnknownHostException, InterruptedException
	{
		scheduler = new LocalSchedulerSubsystem(1234, "localhost");
		if (!scheduler.initialize()) {
			return;
		}
		
		elevators = new ArrayList<>();
		receivers = new ArrayList<>();
		
		DatagramSocket socket = new DatagramSocket();

		elevator = new ElevatorSubsystem();
		elevator2 = new ElevatorSubsystem();
		elevator3 = new ElevatorSubsystem();
		elevator4 = new ElevatorSubsystem();
		
		elevator.init(1234, "localhost", socket);
		elevator2.init(1234, "localhost", socket);
		elevator3.init(1234, "localhost", socket);
		elevator4.init(1234, "localhost", socket);
	
		elevators.add(elevator);
		elevators.add(elevator2);
		elevators.add(elevator3);
		elevators.add(elevator4);
		
		receiver = new StopReceiver(elevator);
		receiver2 = new StopReceiver(elevator2);
		receiver3 = new StopReceiver(elevator3);
		receiver4 = new StopReceiver(elevator4);
		
		receiver.init(socket);
		receiver2.init(socket);
		receiver3.init(socket);
		receiver4.init(socket);
		
		receivers.add(receiver);
		receivers.add(receiver2);
		receivers.add(receiver3);
		receivers.add(receiver4);
		
		floor = new FloorSubsystem(Path.of("events.txt"));
		floor.init(1234, "localhost");
		
	}
	
	public static void main(String[] args) throws Exception
	{
		initializeSystems();
		GUI g = new GUI();
		Thread guiThread = new Thread(g);
		threadExecution(g);
		guiThread.start();
		guiThread.join();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("foobar foobar foobar");
			for(int i=0; i<4; i++) {
				allElevatorInfoTextAreaList.get(i).setText(elevators.get(i).toString());
			}
		}
	}
	
}
