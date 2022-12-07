import java.util.Date;

public class Article {
   String name;
   String journalName;
   Date date;
   boolean pVal;
   public Article(String name) {
      this.name = name;
   }
   
   public Date getDate() {
      return date;
   }
   public void setDate(Date d) {
      date = d;
   }
   
   public boolean getpVal() {
      return pVal;
   }
   public void setpVal(boolean b) {
      pVal = b;
   }
   
   public String getJournalName() {
      return journalName;
   }
   public void setJournalName(String jName) {
      journalName = jName;
   }
   
   public String getName() {
      return name;
   }
      
   public String dateToString() {
      int month = date.getMonth() + 1;
      if (month == 0) {
         month = 12;
      }
      int day = date.getDate();
      int year = date.getYear() + 1900;
      String s = month + "/" + day + "/" + year;
      return s;
   }
   
   
}
