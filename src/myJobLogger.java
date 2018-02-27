import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class myJobLogger {
	private static boolean logToFile;
	private static boolean logToConsole;
	private static boolean logMessage;
	private static boolean logWarning;
	private static boolean logError;
	private static boolean logToDatabase;
	private boolean initialized;
	private static Map dbParams;
	private static Logger logger;

	public myJobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
			boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
		logger = Logger.getLogger("MyLog");  
		logError = logErrorParam;
		logMessage = logMessageParam;
		logWarning = logWarningParam;
		logToDatabase = logToDatabaseParam;
		logToFile = logToFileParam;
		logToConsole = logToConsoleParam;
		dbParams = dbParamsMap;
	}
	
	public static void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {
		
		//control the message
		if (messageText == null || messageText.length() == 0) {
			throw new Exception("Message is null or empty");
		}
		messageText.trim();
		
		if (!logToConsole && !logToFile && !logToDatabase) {
			throw new Exception("Invalid configuration");
		}
		if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
			throw new Exception("Error or Warning or Message must be specified");
		}		
		
		//getting types if loggin
		ArrayList<String> types = getTypeOfLoggin(message, warning, error);
		
		//for each type get message and log
		for(String type:types) {
			String messageWithDate = getMessage(messageText, type);
			try {
				logMessage(messageWithDate, type);
			} catch (SecurityException e) {
				e.printStackTrace();
			}catch (SQLException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}			
	}
	
	//create array with the tipe of message to log
	private static ArrayList<String> getTypeOfLoggin(boolean message, boolean warning, boolean error) {
		ArrayList<String> type= new ArrayList<>();
		if (message && logMessage) {
			type.add("message");
		}

		if (error && logError) {
			type.add("error");
		}

		if (warning && logWarning) {
			type.add("warring");
		}
		return type;
	}
	
	//log message to a file, console, and/or Database
	private static void logMessage(String messageText, String type) throws SecurityException, SQLException, IOException {
		if(logToFile) {
			File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
			logger.addHandler(fh);
			logger.log(Level.INFO, messageText);
		}
		
		if(logToConsole) {
			ConsoleHandler ch = new ConsoleHandler();
			logger.addHandler(ch);
			logger.log(Level.INFO, messageText);
		}
		
		if(logToDatabase) {
			try {
				Connection connection = ConnectionDB.getConection(dbParams);
				Statement stmt = connection.createStatement();
				stmt.executeUpdate("insert into Log_Values('" + messageText + "', " + type + ")");//message its a boolean value
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}	
	
	//get message with type and date.
	private static String getMessage(String messageText, String type) {
		String messageTextDateType = "";
		String today = DateFormat.getDateInstance(DateFormat.LONG).format(new Date());
				
		messageTextDateType += type + today + messageText;
				
		return messageTextDateType;
	}
}