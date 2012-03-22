import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*; //For date and array list.
import java.text.DateFormat; //For date.
import java.text.SimpleDateFormat; //For date.

/** TO DO:
    MAKE IT THE RIGHT SIZE!
    OUTPUT FILE TO SEE FFMPEG WORKING!?
    STRING ARGS!
    EXTRACT EXE TO WORK ANYTIME!
    AUDIO OPTIONS!
    VIDEO OPTIONS!
    BATCH FILES!?!? NOW ONE AT A TIME!?
    CONVERT FILE NOT SAME TYPE! (DIFFERENT FILE SAVING TYPES)
  */

/** An instance is the Video Converter program. */
public class videoConverter extends JFrame implements ActionListener {
  
  public Container pane = getContentPane(); //Content pane of the current window.
  public Container selectFilePane, fileListPane, topRight, selectFormat, logPane, convertPane; //Corresponding panes to add to pane.
  public JButton open, test3, convertButton, cancelButton, saveLogButton; //Various buttons.
  public JTextField fileSelected; //Text which stores the file selected.
  public JFileChooser fc; //The file chooser!
  public JScrollPane scrollForLog; //Scroll pane for the log text area.
  public JScrollPane scrollForFileList; //Scroll pane for the file list text area.
  public JTextArea log = new JTextArea("Log"+"\n"+"==="+"\n", 5, 30); //Log to store what user does - initialized with the string.
  public JTextArea fileList = new JTextArea("Please note that you can only add files for now."+"\n"+"\n"+"Files selected:"+"\n"+"============"+"\n", 5, 30); //Log to store files user has selected.
  public static final String[] formatsSupported = new String[] {"Select a type...", ".mp4", ".avi", ".wmv", ".mpeg", ".mpg"}; //Formats currently supported.
  public JComboBox<String> convertFrom = new JComboBox<String>(formatsSupported); //Drop down list selecting which format want to convert from.
  public JComboBox<String> convertTo = new JComboBox<String>(formatsSupported); //Drop down list selecting which format want to convert to.
  public FileOutputStream outFile; //The file that the log is written to.
  public PrintStream printingToFile; //What is writing to the file.
  public String dateOfLog; //Date log was saved at.
  public ArrayList<File> filesToConvert = new ArrayList<File>(); //Array of all files to convert - max 10!
  public File currentOne; //The current selected file.
  public JScrollBar verticalScrollLog; //So this these can be referred to below.
  public JScrollBar verticalScrollFileList; //So that this can be referred to below.
  
  /** So an app can be made. */
  public static void main(String[] args) {
    new videoConverter();
  }
  
  /** Constructor: start it all up. *** */
  public videoConverter() {
    super("Video Converter"); //Sets title of the window to be "Video Converter".
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //When window is closed, kill this instance.
    pane.setLayout(new GridLayout(2, 3)); //2 rows by 3 columns.
    setupAllPanes(); //Set up all the panes.
    addAllPanes(); //Add all the panes.
    pack(); //Sets it to the right size.
    setVisible(true); //Shows the current window.
  }
  
  /** Set up all the panes. *** */
  private void setupAllPanes() {
    setupSelectFile();
    setupFileListPane();
    setupTR();
    setupSelectingFormatPane();
    setupLogPane();
    setupConvertPane();
  }
  
  /** Adds all of the panes to our window. */
  private void addAllPanes() {
    pane.add(selectFilePane);
    pane.add(fileListPane);
    pane.add(topRight);
    pane.add(selectFormat);
    pane.add(logPane);
    pane.add(convertPane);
  }
  
  /** Setup the select file pane. */
  private void setupSelectFile() {
    selectFilePane = new Container();
    selectFilePane.setLayout(new GridLayout(2, 1));
    open = new JButton("Select a file...");
    fileSelected = new JTextField("");
    open.addActionListener(this);
    selectFilePane.add(open);
    selectFilePane.add(fileSelected);
  }

  /** Setup the file list pane. */
  private void setupFileListPane() {
    fileListPane = new Container();
    fileListPane.setLayout(new BorderLayout());
    fileList.setEditable(false);
    fileList.setMargin(new Insets(5, 5, 5, 5));
    scrollForFileList = new JScrollPane(fileList);
    fileListPane.add(scrollForFileList);
    verticalScrollFileList = scrollForFileList.getVerticalScrollBar();
  }  
  
  private void setupTR() {
    topRight = new Container();
    topRight.setLayout(new GridLayout(1, 1));
    test3 = new JButton("Coming soon!");
    test3.addActionListener(this);
    topRight.add(test3);
  }

  /** Setup the selecting format pane. */
  private void setupSelectingFormatPane() {
    //Put in all of the possible choices:
    selectFormat = new Container();
    selectFormat.setLayout(new GridLayout(2, 1));
    convertFrom.setEnabled(false);
    selectFormat.add(convertFrom);
    selectFormat.add(convertTo);
  }
  
  /** Setup the log pane. */
  private void setupLogPane() {
    logPane = new Container();
    logPane.setLayout(new BorderLayout());
    log.setEditable(false); //Make log unable to be edited.
    log.setMargin(new Insets(5, 5, 5, 5)); //Make margins of 5 pixels on all sides.
    scrollForLog = new JScrollPane(log);
    saveLogButton = new JButton("Save Log");
    saveLogButton.addActionListener(this);
    logPane.add(scrollForLog, BorderLayout.CENTER);
    logPane.add(saveLogButton, BorderLayout.SOUTH);
    verticalScrollLog = scrollForLog.getVerticalScrollBar();
  }
  
  /** Setup the convert pane. */
  private void setupConvertPane() {
    convertPane = new Container();
    convertPane.setLayout(new GridLayout(1, 1));
    convertButton = new JButton("Convert!");
    convertButton.addActionListener(this);
    convertPane.add(convertButton);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == open) {
      log.append("Open button was pressed." + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      if (filesToConvert.size() == 10) {
        log.append("There are already 10 files selected!" + "\n"); 
        verticalScrollLog.setValue(verticalScrollLog.getMaximum());
        return;
      } //Max = 10!
      chooseTheFile();
      //While the extension of the file is not supported or the user did not select a file, keep choosing a file!
      while (!checkFileExtension(fileSelected.getText()) && !fileSelected.getText().equals("")) {
        currentOne = null;
        chooseTheFile();
        log.append("User selected an unsupported format." + "\n" + "Asking user to pick again..." + "\n");
        verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      }
      if (currentOne != null) {
        filesToConvert.add(currentOne);
        fileList.append("File " + filesToConvert.size() + ": " + currentOne.toString() + "\n");
        verticalScrollFileList.setValue(verticalScrollFileList.getMaximum());
        convertFrom.setSelectedItem(formatsSupported[selectIndex(fileSelected.getText())]); //Set type as the type in the drop down list.
      }
    }
    if (e.getSource() == test3) { System.out.println("Coming soon!"); }
    if (e.getSource() == convertButton) {
      log.append("Converted button was pressed." + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      convert();
    }
    if (e.getSource() == cancelButton) {
      log.append("Converting was canceled." + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      cancelConvert();
    }
    if (e.getSource() == saveLogButton) {
      if (saveLog() != 0) {
        log.append("There was an error saving the log.");
        verticalScrollLog.setValue(verticalScrollLog.getMaximum());
        return; } //Log was not saved.
      log.append("Log saved to \"log " + dateOfLog + ".txt\"!" + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
    }
  }
  
  /** Opens up a dialog for the user to choose a file. */
  private void chooseTheFile() {
    fc = new JFileChooser();
    int returnVal = fc.showOpenDialog(videoConverter.this);
    //Check if the file was selected or not:
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File theFileSelected = fc.getSelectedFile(); //The selected file.
      fileSelected.setText(theFileSelected.toString());
      log.append("The user selected the file: " + theFileSelected.toString() + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      currentOne = theFileSelected; //Set the current file to the one selected.
      //log.setCaretPosition(log.getDocument().getLength()); ***Don't know what this does.
    }
    else {
      log.append("The user did not select a file." + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      fileSelected.setText("");
    }
  }
  
  /** Checks the extension of s to see if it ends with any of the file extensions supported. */
  private static boolean checkFileExtension(String s) {
    for (int i = 0; i < formatsSupported.length; i = i + 1) {
      if (s.endsWith(formatsSupported[i]) || s.toLowerCase().endsWith(formatsSupported[i])) { return true; }
    }
      return false;
  }
  
  /** Returns the index at which the file is supported with in the files extensions supported array. */
  private static int selectIndex(String s) {
    for (int i = 0; i < formatsSupported.length; i = i + 1) {
      if (s.endsWith(formatsSupported[i])) { return i; }
    }
    return 0;
  }
  
  /** Saves the log and returns 0 if the log could not be saved. */
  private int saveLog() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //get current date time with Date()
    Date date = new Date();
    dateOfLog = new String("" + dateFormat.format(date));
    dateOfLog = dateOfLog.replaceAll("/", "");
    dateOfLog = dateOfLog.replaceAll(":", ""); //Date and time with no ":"s or "/"s.
    try {
      outFile = new FileOutputStream("log " + dateOfLog + ".txt");
      printingToFile = new PrintStream(outFile);
      printingToFile.println(log.getText());
      printingToFile.close();
    }
    catch (Exception e) {
      System.out.println("Exception: " + e);
      return 1;
    }
    return 0;
  }
  
  private void convert() {
    //See if there is at least one thing:
    if (filesToConvert.size() == 0) {
      log.append("First add a file for this program to convert!" + "\n");
      verticalScrollLog.setValue(verticalScrollLog.getMaximum());
      return;
    }
    log.append("Converting..." + "\n");
    verticalScrollLog.setValue(verticalScrollLog.getMaximum());
    //Replace the convert button with the cancel button.
    convertPane.remove(convertButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    convertPane.add(cancelButton);
    pack();
    //Convert everything in the list.
    for (int i = 0; i < filesToConvert.size(); i = i + 1) {
      File currentOne = filesToConvert.get(i);
      //Try to find the file:
      if (!(currentOne.exists())) {
        log.append("Cannot find the file: " + currentOne.toString() + ". Please try again." + "\n");
        verticalScrollLog.setValue(verticalScrollLog.getMaximum());
        return;
      }
      //See if the convertFrom and convertTo formats are the same:
      if (convertFrom.getSelectedItem().equals(convertTo.getSelectedItem())) {
        log.append("Outbound file type is same as inbound. In lame terms, you are converting to the same file type!");
        verticalScrollLog.setValue(verticalScrollLog.getMaximum());
        return;
      }
      //Code to put convert.
      try {
        System.out.println("ffmpeg.exe -i \"" + currentOne.toString() + "\" -y \"" + currentOne.toString() + convertTo.getSelectedItem() + "\"");
        Runtime.getRuntime().exec("cmd /c ffmpeg.exe -i \"" + currentOne.toString() + "\" -y \"" + currentOne.toString() + convertTo.getSelectedItem() + "\"");
      }
      catch (IOException e) {
        System.out.println("Error.");
      }
    }
  }
  
  private void cancelConvert() {
    log.append("Canceling does not work as of now." + "\n");
    verticalScrollLog.setValue(verticalScrollLog.getMaximum());
    //Replace the cancel button with the convert button.
    convertPane.remove(cancelButton);
    convertPane.add(convertButton);
    pack();
    //***Code to put in to stop.
  }
  
}