package edu.vccs.email.tas23466.copyright;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Class for the copyRight GUI, most of the work is done in the constructor
 * 04/21/19
 * @author tim shumeyko
 */
public class copyRightGUI extends JFrame {
   // Used for the disclaimer
   private final JDialog               dialog     = new JDialog(this, "EUA", true);
   private final String           disclamerText =
       "CopyRIGHT helps BRCC staff and employees make a good faith\n"
       + "effort to determine whether use of copyrighted material for Blue\n"
       + "Ridge Community College purposes adheres to fair use standards. \n"
       + "The app relies on your input to help you make an informed decision.\n"
       + "As such, your best effort in giving logical, informed answers to each\n"
       + "question is necessary. In using any copyrighted materials, you are \n"
       + "ultimately responsible for the decision you make.\n\n Do you agree to the following terms?";
   private final JOptionPane      optionPane;                          // Option pane presenting the disclaimer
   private ArrayList<Question>    questionList  = new ArrayList<>();   // Stores all of the questions
   private static JButton         backButton;                          // Back button
   private static JPanel          questionsPanel;                      // Panel which displays the current question
   private static JButton[]       buttons;                             // Array of buttons which display answers
   private static Stack<Question> questionStack = new Stack<>();       // Stack which stores previous questions
   private static JPanel          backButtonPanel;                     // Panel which displays the back button if applicable
   private static int             totalScore    = 0;                   // Variable for the running total score
   private static Stack<Integer>  scoreStack    = new Stack<>();       // Stack which stores all previous scores from questions
   private static final int       THRESHOLD     = 49;                  // Used for evaluating pass/fail
   private static JButton resetButton;                                 // Used to reset progress and start again
   private static boolean askingPermsPref = false;                     // Whether permission should be asked or not

   /**
    * Constructor for the class, runs methods which parse questions, create the questions database, and
    * loads the first question
    * @throws Exception may be thrown if the file is not parsed correctly
    */
   public copyRightGUI() throws Exception{
      // Title of our window
      super("copyRight");

      // Run the disclaimer
      optionPane = new JOptionPane(disclamerText, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
      dialog.setContentPane(optionPane);
      optionPane.addPropertyChangeListener(
          new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible()
                    && (e.getSource() == optionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                   dialog.setVisible(false);
                }
             }
          });
      dialog.pack();
      dialog.setVisible(true);

      // If user presses yes, continue. If not, quit the app
      int value = ((Integer)optionPane.getValue()).intValue();
      if (value == JOptionPane.YES_OPTION) {
         System.out.println("User clicked Yes");
      } else if (value == JOptionPane.NO_OPTION) {
         System.out.println("User clicked NO");
         //System.exit is apparently "dangerous", might need a workaround.
         System.exit(0);
      }

      // Initialize the window
      Container contentPane = getContentPane();
      contentPane.setLayout(new BorderLayout());

      // Parse questions
      loadFromFile(questionList);

      // Comments, delete later
      //System.out.println("Size " + questionList.size());
      //System.out.println(questionList.get(1).getQuestion());

      // Display the first question to the user.
      loadQuestion(questionList, this, 0);

      // Set other values
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setMinimumSize(new Dimension(700, 300));
      setMaximumSize(new Dimension(700, 300));
      pack();

      SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                       setVisible(true);
                                    }
                                 });
   }

   /**
    * Main method, simply calls the constructor
    * @param args not used
    * @throws Exception if file cannot be parsed in the loadFromFile() method.
    */
   public static void main(String[] args) throws Exception{
      new copyRightGUI().setResizable(false);
   }

   /**
    * Class which returns a JPanel with the question
    * @param text the question
    * @return a JPanel with the question in text
    */
   private static JPanel setQuestionText(String text){
      JPanel northPanel = new JPanel();            // JPanel to be returned
      JLabel label = new JLabel(text);             // Label which displays the question
      char tempChar;
      String tempString = "";

      if (text.length() > 100){
         tempChar = text.charAt(100);
         int i = 100;
         while (tempChar != ' ' && text.length() > i){
            tempChar = text.charAt(i-1);
            i--;
         }
         tempString = text.substring(i);
         text = text.substring(0, i);
      }
      label = new JLabel("<html>" + text + "<br/>" + tempString + "</html>", SwingConstants.CENTER);

      // Add the label to the panel and return the panel
      northPanel.add(label);
      return northPanel;
   }

   /**
    * Creates a JPanel with a button for every question
    * @param question whose answers will be displayed
    * @param gui the copyRight gui
    * @param questionsList the complete list of all of the questions
    * @return a JPanel with buttons which represent the answers for the question
    */
   private static JPanel setAnswersText(Question question, copyRightGUI gui, ArrayList<Question> questionsList){
      JLabel label = new JLabel();              // Used for the final determination question
      JLabel optPermissionLaber = new JLabel(); // If asking for permission is preferred, display text

      // If the buttons array was previously filled, clear it
      if (buttons != null) {
         for (JButton btn : buttons
         ) {
            if (btn != null) {
               questionsPanel.remove(btn);
            }
         }
      }

      // create a new panel for the questions
      questionsPanel = new JPanel();

      // Create a new array with the specific amount of buttons needed
      buttons = new JButton[question.getNumAnswers()];

      // For the last question
      if (question.getQuestion().equals("Final Determination")){

         if (totalScore > THRESHOLD) {
            label.setText("Fair use suggested");
            label.setForeground(Color.GREEN);
            questionsPanel.add(label);
            if (askingPermsPref){
               optPermissionLaber.setText("...but asking for permission is preferred");
               questionsPanel.add(optPermissionLaber);
            }
         } else {
            label.setText("Fair use not suggested");
            label.setForeground(Color.red);
            questionsPanel.add(label);
         }
         return questionsPanel;
      }


      // For loop which adds buttons to the panel and creates their actions
      for (int i = 0; i < question.getNumAnswers(); i++){
         buttons[i] = new JButton(question.getAnswer(i));
         questionsPanel.updateUI();
         questionsPanel.add(buttons[i]);

         int selectedAnswer = i;  // set the selected answer of the question

         // Add an actionListener for every question
         buttons[i].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               question.setSelectedAnswer(selectedAnswer);
               // Create a temporary question which will hold the previous question data
               Question tempQuest;

               // Check if the quesiton has a special case and act correspondingly
               switch (question.getSpecialCase()){
                  case 0:
                  case 4:
                     if (question.getScore() > 0){
                        totalScore += question.getScore();
                        scoreStack.push(question.getScore());
                     }
                     break;
                  case 1:
                     tempQuest = questionStack.peek();
                     if ((tempQuest.getSelectedAnswer() == 0 && question.getSelectedAnswer() == 1)){
                        scoreStack.push(20);
                        totalScore+= 20;
                     } else if (tempQuest.getSelectedAnswer() == 1 && question.getSelectedAnswer() == 0) {
                        scoreStack.push(20);
                        totalScore+=20;
                        askingPermsPref = true;
                     } else if (tempQuest.getSelectedAnswer() == 1 && question.getSelectedAnswer() == 1){
                        scoreStack.push(40);
                        totalScore+= 40;
                     } else if (tempQuest.getSelectedAnswer() == 0 && question.getSelectedAnswer() == 0){
                        scoreStack.push(0);
                     }
                     break;
                  case 2:
                     tempQuest = questionStack.peek();
                     if (tempQuest.getSelectedAnswer() == 0 && question.getSelectedAnswer() == 0){
                        scoreStack.push(10);
                        totalScore+=10;
                     } else if (tempQuest.getSelectedAnswer() == 0 && question.getSelectedAnswer() == 1){
                        scoreStack.push(0);
                     } else if (tempQuest.getSelectedAnswer() == 1 && question.getSelectedAnswer() == 0){
                        scoreStack.push(40);
                        totalScore+=40;
                     } else if (tempQuest.getSelectedAnswer() == 1 && question.getSelectedAnswer() == 1){
                        for (int k = 0; k < questionsList.size(); k++){
                           if (questionsList.get(k).getQuestion().equals("Is gaining permission to use the film available?")){
                              gui.revalidate();
                              loadQuestion(questionsList, gui, k);
                              gui.revalidate();
                              return;
                           }

                        }
                        break;
                     }
                     break;
                  case 3:
                     try {
                        Desktop.getDesktop().browse(new URL("https://creativecommons.org/licenses/").toURI());
                     } catch (Exception r) {
                        System.out.println("Something went wrong with opening the link. Make sure the link"
                                               + "exists and is still valid");
                        r.printStackTrace();
                     }
                     break;
                  case 5:
                     if (question.getScore() > 0){
                        totalScore += question.getScore();
                        scoreStack.push(question.getScore());
                     }
                     if (question.getSelectedAnswer() == 0) {
                        askingPermsPref = true;
                     }
                     default:
                        break;

               }

               // testing purposes
               //System.out.println(question.getNextQuestion());

               // load the next question
               loadNextQuestion(question, questionsList, gui);


            }
         });
      }

      // return questionPanel which contains a panel with buttons
      return questionsPanel;
   }

   /**
    * Finds the next question of question and loads it
    * @param question the questions whose next questions will be loaded
    * @param questionsList list containing all of the questions
    * @param gui copyRight gui
    */
   public static void loadNextQuestion(Question question, ArrayList<Question> questionsList, copyRightGUI gui){
      // Run throught the questionsList, find the next question and load it
      for (int k = 0; k < questionsList.size(); k++) {

         if (question.getNextQuestion().equals(questionsList.get(k).getQuestion())) {
            // push question onto questionStack
            questionStack.push(question);

            // revalidate gui
            gui.revalidate();

            // load question
            loadQuestion(questionsList, gui, k);

            // revalidate gui, again
            gui.revalidate();
            break;
         }
      }
   }

   /**
    * Displays a back button if one is available and also controls actions of the stack, making decisions
    * @param question boolean indicating if backButton is or is not available
    * @param questionsList the list of all questions
    * @param gui copyRight gui
    * @return a jpanel with or without a back button
    */
   private static JPanel backButton(Question question, ArrayList<Question> questionsList, copyRightGUI gui){
      // If a button was previously on the panel, remove it
      if (backButtonPanel != null && backButton != null){
         backButtonPanel.remove(backButton);
      }
      if (backButtonPanel != null && resetButton != null){
         backButtonPanel.remove(resetButton);
      }


      // create a new jpanel for the back button
      backButtonPanel = new JPanel();
      // new back button
      backButton = new JButton("Back");


      // If a back button is available, add it to the jpanel, and create appropriate actions
      if (question.isBackAvailable()){
         backButtonPanel.add(backButton);
         backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               // pop the previous question off of the stack, so that the program can load it
               Question poppedQuestion = questionStack.pop();
               for (int i = 0; i < questionsList.size(); i++){
                  if (questionsList.get(i).getQuestion().equals(poppedQuestion.getQuestion())){

                     // Go through any special cases, and act accordingly
                     if (poppedQuestion.getSpecialCase() == 1 || poppedQuestion.getSpecialCase() == 5){
                        totalScore -= scoreStack.peek();
                        scoreStack.pop();
                        askingPermsPref = false;
                     } else if (!scoreStack.isEmpty() && poppedQuestion.getScore() > 0){
                        totalScore -= scoreStack.peek();
                        scoreStack.pop();
                     }
                     // revalidate gui, load questions, and revalidate again
                     gui.revalidate();
                     loadQuestion(questionsList, gui, i);
                     gui.revalidate();
                  }
               }
            }
         });
         if (question.getSpecialCase() == 4){
            resetButton = new JButton("Restart");
            backButtonPanel.add(resetButton);
            resetButton.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e) {
                  scoreStack.empty();
                  questionStack.empty();
                  totalScore = 0;
                  askingPermsPref = false;
                  gui.revalidate();
                  loadQuestion(questionsList, gui, 0);
                  gui.revalidate();
               }
            });
         }
      }



      // return panel with(out) button
      return backButtonPanel;
   }

   /**
    * Loads a new question onto the screen
    * @param questionList the list containing all of the questions
    * @param gui the gui to which the panels will be added
    * @param index index of the question to be loaded
    */
   private static void loadQuestion(ArrayList<Question> questionList, copyRightGUI gui, int index) {

      // Debugging purposes
      System.out.println("Score: " + totalScore);
//      System.out.println("ScoreStack");
//      System.out.println("Questions stack " + questionStack.size());
//      for (int k = 0; k < scoreStack.size(); k++){
//         System.out.println(scoreStack.get(k));
//      }


      //gui.revalidate();

      // Load the question, set question text, all answers, and the back button
      gui.add(setQuestionText(questionList.get(index).getQuestion()), BorderLayout.NORTH);
      gui.add(setAnswersText(questionList.get(index), gui, questionList), BorderLayout.CENTER);
      gui.add(backButton(questionList.get(index), questionList, gui),  BorderLayout.SOUTH);


      }

   /**
    * Loads questions from the question_data file, parses the data into objects
    * @param questionsList the list which the data will be written to
    * @throws Exception if the file is not found or cannot be parsed correctly
    */
   private void loadFromFile(ArrayList<Question> questionsList) {
         String             tempString = "";                               // Holds the "identifier" of the line
         String             question = "";                                 // text containing actual question
         ArrayList<Integer> scoreList = new ArrayList<>();     // contains scores of questions
         ArrayList<String>  answers = new ArrayList<>();        // contains answers of questions
         ArrayList<String>  nextQuestions = new ArrayList<>();  // contains next question of current question
         int                specialCase = 0;                                  // special case of question
         boolean            backExists = false;                           // boolean indicating if back button does(n't) exist
         Question           tempQuestion;                               // temporary question which is later added to the list

         // Attempt to open a the question_data file and read its contents
         try {
            InputStream in = getClass().getResourceAsStream("question_data");
            Scanner sc = new Scanner(in);

            while (sc.hasNextLine()) {
               tempString = sc.nextLine();
               if (tempString.equals("")) {
                  continue;
               }

               switch (tempString.charAt(0)) {
                  case '#':
                     // Anything beginning with '#' is ignored.
                     break;
                  case 'Q':
                     // 'Q' indicated the text of the question
                     question = tempString.substring(3);
                     break;
                  case 'A':
                     // 'A' indicates an answer
                     answers.add(tempString.substring(3));
                     break;
                  case 'B':
                     // 'B' indicates if a back button exists or not
                     backExists = Boolean.parseBoolean(tempString.substring(3));
                     break;
                  case 'N':
                     // 'N' indicates the next question of every answer
                     nextQuestions.add(tempString.substring(3));
                     break;
                  case 'S':
                     // 'S' indicates the score of every answer, can be set to -1 if no score is needed
                     scoreList.add(Integer.parseInt(tempString.substring(3)));
                     break;
                  case 'C':
                     // Special case. Every special case (number other than 0) has to be accounted for...
                     specialCase = Integer.parseInt(tempString.substring(3));
                     break;
                  case 'T':
                     // 'T' stands for terminate and create. A 'T' should be placed at the end of a
                     // question and all of its answers. This indicated the creation of the question.

                     // Create new question, set its answer, reset all lists, etc.
                     tempQuestion = new Question(question, backExists);
                     tempQuestion.setSpecialCase(specialCase);
                     for (int i = 0; i < answers.size(); i++) {
                        tempQuestion
                            .addAnswer(answers.get(i), nextQuestions.get(i), scoreList.get(i));
                        tempQuestion.setBackAvailable(backExists);
                     }
                     questionsList.add(tempQuestion);
                     answers.clear();
                     scoreList.clear();
                     nextQuestions.clear();
                     specialCase = 0;
                     break;
                  default:
                     break;
               }

            }
         } catch (Exception e){
            System.out.println(System.getProperty("user.dir"));
            System.out.println("An error occurred while parsing the file, make sure the file exists."
                                   + "and/or has the correct filename (question_data)");

         }
      }


}



