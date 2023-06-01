package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import mutex.TokenMutex;
import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private String getSomeServentPort() {

		String bsAddress = AppConfig.BOOTSTRAP_ADDRESS;
		int bsPort = AppConfig.BOOTSTRAP_PORT;

		String retVal = null;

		try {
			Socket bsSocket = new Socket(bsAddress, bsPort);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			try {
				bsScanner.nextInt();
			} catch (InputMismatchException e) {
				//Dobili smo ip:port kao odgovor a ne kod
				retVal = bsScanner.nextLine();
			}

			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}


		return retVal;
	}

	@Override
	public void run() {
		String someServentPortt = getSomeServentPort();
		if (someServentPortt == null) {
			AppConfig.timestampedStandardPrint("First node in Chord system.");
			TokenMutex.init();
			//Bootstrap gave us ip:port of a node - let that node tell our successor that we are here
		}
		else {
			String someServentIp = someServentPortt.substring(0, someServentPortt.indexOf(':'));
			int someServentPort = Integer.parseInt(someServentPortt.substring(someServentIp.length() + 1));
			System.out.println("# MY CONNECTING PARTNERS PORT IS " + someServentPort);
			NewNodeMessage newNodeMessage = new NewNodeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), someServentIp, someServentPort);
			MessageUtil.sendMessage(newNodeMessage);
		}
	}

}
