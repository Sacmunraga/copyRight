package edu.vccs.email.tas23466.copyright;

import java.util.ArrayList;

/**
 * A class which contains methods to create a question and its answers.
 * 04/26/19
 * @author tim shumeyko
 */
public class Question {
   private String            question ;         // Text of the question
   private ArrayList<Answer> answersList;       // list containing answers of current question
   private int               selectedAnswer ;   // selected answer of currrent question
   private int numAnswers;
   private boolean backAvailable;               // indicates if a previous question existed
   private int specialCase;                     // indicates if special action is required for question

   /**
    * Inner class containing methods to create answer and it's next questions as well as it's score
    */
    public static class Answer  {
      private String answer ;          // Answer text
      private String nextQuestion ;    // next question of answer
      private int score;              // score of current answer

      /**
       * No arg constructor, creates empty answer
       */
      private Answer(){
         this("","",-1);
      }

      /**
       * Multi-arg constructor which builds an answer with specified parameters
       * @param answer the answer
       * @param nextQuestion the next question of the answer
       * @param score the score of the answer
       */
      public Answer(String answer, String nextQuestion, int score){
         this.answer = answer;
         this.nextQuestion = nextQuestion;
         this.score = score ;
      }

      /**
       * Retrieves next question of answer
       * @return next question of answer
       */
      public String getNextQuestion() {
         return nextQuestion;
      }


      public void setNextQuestion(String nextQuestion) {
         this.nextQuestion = nextQuestion;
      }
   }

   /**
    * retrieves special case
    * @return special case
    */
   public int getSpecialCase() {
      return specialCase;
   }

   /**
    * Sets special case
    * @param specialCase the special case
    */
   public void setSpecialCase(int specialCase) {
      this.specialCase = specialCase;
   }

   /**
    * No arg contructor, creates empty question
    */
   public Question(){
      this("", false);
   }

   /**
    * indicates if a previous question existed
    * @return true or false
    */
   public boolean isBackAvailable() {
      return backAvailable;
   }

   /**
    * sets bacAvalable to true or false
    * @param backAvailable if back button is or is not available
    */
   public void setBackAvailable(boolean backAvailable) {
      this.backAvailable = backAvailable;
   }

   /**
    * Multi-arg constructor which creates a question and creates an answersList as well
    * @param question the question text
    * @param backAvailable indicates if back is (not) available
    */
   public Question(String question, boolean backAvailable) {
      this.backAvailable = backAvailable;
      setQuestion(question);
      answersList = new ArrayList<>();

   }

   /**
    * Adds an answer to a question
    * @param answer the answer
    * @param nextQuestion next question of answer
    * @param score the score of the answer
    * @return true always
    */
   public boolean addAnswer (String answer, String nextQuestion, int score){
         answersList.add(new Answer(answer, nextQuestion, score));
         return true ;

   }

   /**
    * Returns number of answers of a question
    * @return number of answers
    */
   public int getNumAnswers(){
      return answersList.size() ;
   }

   /**
    * Retrieves the selected answer of the question
    * @param answerNumber the index of the answer in the answers list
    * @return String containing the answer
    */
   public String getAnswer(int answerNumber ){
      return answersList.get(answerNumber).answer ;
   }


   /**
    * Retrieves score of selected answer
    * @return answer score
    */
   public int getScore (){
      return answersList.get(selectedAnswer).score ;
   }

   /**
    * Returns next question of selected answer
    * @return String containing the answer
    */
   public String getNextQuestion(){
      return answersList.get(selectedAnswer).getNextQuestion() ;
   }

   /**
    * Returns the literal question
    * @return the literal question
    */
   public String getQuestion() {
      return question;
   }

   /**
    * Sets the question
    * @param question text
    */
   public void setQuestion(String question) {
      this.question = question;
   }

   /**
    * Retrieves the selected answer of current question
    * @return selected answer of question
    */
   public int getSelectedAnswer() {
      return selectedAnswer;
   }

   /**
    * sets the selected answer of current question
    * @param selectedAnswer the selected answer
    */
   public void setSelectedAnswer(int selectedAnswer) {
         this.selectedAnswer = selectedAnswer;
   }
}

