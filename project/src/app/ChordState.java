package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app.file_util.FileInfo;
import mutex.TokenMutex;
import servent.message.AskPullMessage;
import servent.message.InformAboutAddMessage;
import servent.message.Message;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState{

		public static int CHORD_SIZE;

		public static int chordHash(String value) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				messageDigest.update(value.getBytes());
				BigInteger hash = new BigInteger(messageDigest.digest());
				hash = hash.mod(BigInteger.valueOf(64));
				return hash.intValue();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return -1;
			}
		}

		private int chordLevel; //log_2(CHORD_SIZE)

		private ServentInfo[] successorTable;
		private ServentInfo predecessorInfo;

		//we DO NOT use this to send messages, but only to construct the successor table
		private List<ServentInfo> allNodeInfo;

//	private Map<Integer, Integer> valueMap;

		private Map<String, FileInfo> storageMap;

		public List<FileInfo> pulledFiles;
		public int amountToPull;
		public int amountPulled;

		public ChordState() {
			this.chordLevel = 1;
			int tmp = CHORD_SIZE;
			while (tmp != 2) {
				if (tmp % 2 != 0) { //not a power of 2
					throw new NumberFormatException();
				}
				tmp /= 2;
				this.chordLevel++;
			}

			successorTable = new ServentInfo[chordLevel];
			for (int i = 0; i < chordLevel; i++) {
				successorTable[i] = null;
			}

			predecessorInfo = null;
//		valueMap = new HashMap<>();
			allNodeInfo = new ArrayList<>();

			storageMap = new ConcurrentHashMap<>();
			pulledFiles = new ArrayList<>();
			amountToPull = 0;
			amountPulled = 0;
		}


		public void init(WelcomeMessage welcomeMsg) {
			//set a temporary pointer to next node, for sending of update message
			successorTable[0] = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort());
			this.storageMap = new ConcurrentHashMap<>(welcomeMsg.getStorageMap());

			//Kreiramo sve fajlove u nasem skladistu kao i njihove stare verzije todo vrati ako treba cuvanje filova lokalno
//		for (Map.Entry<Integer, FileInfo> m : storageMap.entrySet()) {
//			if (m.getValue().isFile()) {
//				FileUtils.storeFile(AppConfig.STORAGE_DIR, m.getValue());
//			}
//		}

			//tell bootstrap this node is not a collider
			try {
				Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

				PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
				bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

				bsWriter.flush();
				bsSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		/**
		 * Returns true if we are the owner of the specified key.
		 */
		public boolean isKeyMine(int key) {
			if (predecessorInfo == null) {
				return true;
			}
			int predecessorChordId = predecessorInfo.getChordId();
			int myChordId = AppConfig.myServentInfo.getChordId();
			if (predecessorChordId < myChordId) { //no overflow
				if (key <= myChordId && key > predecessorChordId) {
					return true;
				}
			} else { //overflow
				if (key <= myChordId || key > predecessorChordId) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Main chord operation - find the nearest node to hop to to find a specific key.
		 * We have to take a value that is smaller than required to make sure we don't overshoot.
		 * We can only be certain we have found the required node when it is our first next node.
		 */
		public ServentInfo getNextNodeForKey(int key) {
			if (isKeyMine(key)) {
				return AppConfig.myServentInfo;
			}

			//normally we start the search from our first successor
			int startInd = 0;

			//if the key is smaller than us, and we are not the owner,
			//then all nodes up to CHORD_SIZE will never be the owner,
			//so we start the search from the first item in our table after CHORD_SIZE
			//we know that such a node must exist, because otherwise we would own this key
			if (key < AppConfig.myServentInfo.getChordId()) {
				int skip = 1;
				while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
					startInd++;
					skip++;
				}
			}

			int previousId = successorTable[startInd].getChordId();

			for (int i = startInd + 1; i < successorTable.length; i++) {
				if (successorTable[i] == null) {
					AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
					break;
				}

				int successorId = successorTable[i].getChordId();

				if (successorId >= key) {
					return successorTable[i-1];
				}
				if (key > previousId && successorId < previousId) { //overflow
					return successorTable[i-1];
				}
				previousId = successorId;
			}
			//if we have only one node in all slots in the table, we might get here
			//then we can return any item
			return successorTable[0];
		}

		private void updateSuccessorTable() {
			//first node after me has to be successorTable[0]

			int currentNodeIndex = 0;
			ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
			successorTable[0] = currentNode;

			int currentIncrement = 2;

			ServentInfo previousNode = AppConfig.myServentInfo;

			//i is successorTable index
			for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
				//we are looking for the node that has larger chordId than this
				int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

				int currentId = currentNode.getChordId();
				int previousId = previousNode.getChordId();

				//this loop needs to skip all nodes that have smaller chordId than currentValue
				while (true) {
					if (currentValue > currentId) {
						//before skipping, check for overflow
						if (currentId > previousId || currentValue < previousId) {
							//try same value with the next node
							previousId = currentId;
							currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
							currentNode = allNodeInfo.get(currentNodeIndex);
							currentId = currentNode.getChordId();
						} else {
							successorTable[i] = currentNode;
							break;
						}
					} else { //node id is larger
						ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
						int nextNodeId = nextNode.getChordId();
						//check for overflow
						if (nextNodeId < currentId && currentValue <= nextNodeId) {
							//try same value with the next node
							previousId = currentId;
							currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
							currentNode = allNodeInfo.get(currentNodeIndex);
							currentId = currentNode.getChordId();
						} else {
							successorTable[i] = currentNode;
							break;
						}
					}
				}
			}
		}

		/**
		 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
		 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
		 *
		 */
		public void addNodes(List<ServentInfo> newNodes) {
			allNodeInfo.addAll(newNodes);

			allNodeInfo.sort(new Comparator<ServentInfo>() {
				@Override
				public int compare(ServentInfo o1, ServentInfo o2) {
					return o1.getChordId() - o2.getChordId();
				}

			});

			List<ServentInfo> newList = new ArrayList<>();
			List<ServentInfo> newList2 = new ArrayList<>();

			int myId = AppConfig.myServentInfo.getChordId();
			for (ServentInfo serventInfo : allNodeInfo) {
				if (serventInfo.getChordId() < myId) {
					newList2.add(serventInfo);
				} else {
					newList.add(serventInfo);
				}
			}

			allNodeInfo.clear();
			allNodeInfo.addAll(newList);
			allNodeInfo.addAll(newList2);
			if (newList2.size() > 0) {
				predecessorInfo = newList2.get(newList2.size()-1);
			} else {
				predecessorInfo = newList.get(newList.size()-1);
			}

			updateSuccessorTable();
		}

		//todo storage add file

		//todo storage pull file



		public ServentInfo[] getSuccessorTable() {
			return successorTable;
		}

		public void setStorageMap(Map<String, FileInfo> storageMap) {
			this.storageMap = storageMap;
		}

		public Map<String, FileInfo> getStorageMap() {
			return storageMap;
		}

		public int getNextNodePort() {
			return successorTable[0].getListenerPort();
		}

		public String getNextNodeIp() {
			return successorTable[0].getIpAddress();
		}

		public ServentInfo getPredecessor() {
			return predecessorInfo;
		}

		public void setPredecessor(ServentInfo newNodeInfo) {
			this.predecessorInfo = newNodeInfo;
		}

		public boolean isCollision(int chordId) {
			if (chordId == AppConfig.myServentInfo.getChordId()) {
				return true;
			}
			for (ServentInfo serventInfo : allNodeInfo) {
				if (serventInfo.getChordId() == chordId) {
					return true;
				}
			}
			return false;
		}

    public void addToStorage(FileInfo fileInfo, String requesterIp, int requesterPort) {
		if (!storageMap.containsKey(fileInfo.getPath())) { //Check if file is alredy added
			storageMap.put(fileInfo.getPath(), new FileInfo(fileInfo));
			AppConfig.timestampedStandardPrint("File " + fileInfo.getPath() + " stored successfully.");


			String nextNodeIp = AppConfig.chordState.getNextNodeIp();
			int nextNodePort = AppConfig.chordState.getNextNodePort();

			AppConfig.timestampedErrorPrint(AppConfig.myServentInfo.getIpAddress());

			Message addInfoMsg = new InformAboutAddMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
					nextNodeIp, nextNodePort, requesterIp, requesterPort, fileInfo);
			AppConfig.timestampedStandardPrint("Sending inform message " + addInfoMsg);
			MessageUtil.sendMessage(addInfoMsg);

			for (Map.Entry<String, FileInfo> map: storageMap.entrySet()) {
				System.out.println("storage = " + map.getKey() + " : " + map.getValue());
			}
		}
		else {
			AppConfig.timestampedStandardPrint("We already have " + fileInfo.getPath());
		}
    }

    public void pullFile(String path) {
		//dohvatimo iz storagea fajlove koje je potrebno pulovati
		List<FileInfo> filesToPull = pullFromStorage(path);

		if (filesToPull == null) {
			AppConfig.timestampedErrorPrint("Bad pull path - " + path);
			return;
		}

		if (filesToPull.isEmpty()) {
			AppConfig.timestampedErrorPrint("No files found to pull - " + path);
			return;
		}

		//posaljemo svima ask pull msg
		pulledFiles.clear();
		amountPulled = 0;

		AppConfig.timestampedErrorPrint(filesToPull.toString());
		for (FileInfo fileToPull : filesToPull) {
			Message askMessage = new AskPullMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
					getNextNodeIp(), getNextNodePort(), AppConfig.myServentInfo.getChordId(), fileToPull);
			MessageUtil.sendMessage(askMessage);
			AppConfig.timestampedErrorPrint("sent ask msg " + askMessage);
		}
		amountToPull = filesToPull.size();
    }

	private List<FileInfo> pullFromStorage(String path) {
		if (storageMap.containsKey(path)){
			List<FileInfo> filesToReturn = new ArrayList<>();

			FileInfo requestedFileInfo = storageMap.get(path); //

			if (!requestedFileInfo.isDirectory()) {//ako je file, nasli smo ga i samo ga vrati
				filesToReturn.add(requestedFileInfo);
				return filesToReturn;
			}

			//ako je dir onda iskopaj sve filove koji su nam unutar dira
			List<String> allDirSubFilePaths = getAllFilesFromDir(requestedFileInfo);

			for (String pathKey: allDirSubFilePaths){
				if (storageMap.containsKey(pathKey))
					filesToReturn.add(storageMap.get(pathKey));
			}


			return filesToReturn;
		}
		else return null;
	}

	private List<String> getAllFilesFromDir(FileInfo requestedFileInfo) {
		List<String> filePaths = new ArrayList<>();
		for (String path: requestedFileInfo.getSubFiles()) {
			if (path.contains(".")) filePaths.add(path);//ako ima tacku znaci da je neki file
			else filePaths.addAll(getAllFilesFromDir(storageMap.get(path))); //ako nema znaci da je dir i daj mi njegove subdirove
		}
		return filePaths;
	}

	public void addPulledFile(FileInfo fileInfo) {
		pulledFiles.add(fileInfo);
		amountPulled++;
		if (amountPulled == amountToPull){
			printPulledFiles();
		}
	}

	private void printPulledFiles() {
		AppConfig.timestampedStandardPrint("Printing pulled files");
		for (FileInfo pulledFile: pulledFiles){
			System.out.println("\n-----" + pulledFile.getPath() + "-----");
			System.out.println(pulledFile.getContent());
		}
	}
}

