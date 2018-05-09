package de.tuberlin.tubit.gitlab.hagenanuth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class App {

	static private LinkedList<Node> nodes = new LinkedList<Node>();

	public static void main(String[] args) {
		App.log('i', "Yay! System started!");

		int numNodes = 0;
		int numMessages = 0;

		try {
			numNodes = Integer.parseInt(args[0]);
			numMessages = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			App.log('e', "USAGE: Provide number of Nodes and number of Messages as arguments.");
			System.exit(0);
		} catch (NumberFormatException e) {
			App.log('e', "NOT A NUMBER!");
			System.exit(0);
		}

		MessageSequencer messageSequencer = new MessageSequencer();
		Generator generator = new Generator(numMessages);

		/* Create and register Nodes */
		int nodeId = 0;
		for (int i = 0; i < numNodes; i++) {
			nodeId++;
			Node node = new Node(nodeId, messageSequencer.getQueue());
			messageSequencer.registerNode(node.getQueue());
			generator.registerNode(node.getQueue());
			nodes.add(node);
		}
		App.log('i', "Nodes registered.");

		Thread messageSequencerThread = new Thread(messageSequencer);
		messageSequencerThread.start();

		for (Node node : nodes) {
			Thread nodeThread = new Thread(node);
			nodeThread.start();
		}

		Thread generatorThread = new Thread(generator);
		generatorThread.start();

		App.log('s', "App.main() finished.");
	}

	public static void prepareShutdown() {

		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			App.log('f', "Shutdown failed!");
			e.printStackTrace();
		}

		/* To give another threads some time to finish their tasks */
		System.out.print("\nPrepare to shutdown");
		for (int i = 0; i < 4; i++) {
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				App.log('f', "Shutdown failed!");
				e.printStackTrace();
			}
			System.out.print(" .");
		}
		System.out.println("\n");

		/* Prints out stored messages of every node */
		nodes.stream().forEach(node -> node.retrieveStorage());

		System.out.println("\nSHUTDOWN!");
		System.exit(0);
	}

	public static void log(char type, String message) {

		switch (type) {
		case 'i':
			System.out.println(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " [INFO] " + message);
			break;
		case 'f':
			System.out.println(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " [FAIL] " + message);
			break;
		case 'w':
			System.out.println(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " [WARNING] " + message);
			break;
		case 's':
			System.out.println(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " [SUCCESS] " + message);
			break;
		case 'e':
			System.out.println(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " [ERROR] " + message);
			break;
		default:
			System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + message);
		}
	}
}
