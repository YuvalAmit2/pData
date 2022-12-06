import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;


//Quick note: You'll find that a lot of a lot of files are being removed because I've found
//that PMC didn't create a txt file for every xml file. Large pauses in the algorithm are caused
//by it not finding the matching txt files (this happens a lot in the beginning). I'm pretty sure
//that the txt zip files I downloaded from PMC correspond correctly to the xml files I downloaded,
//but it's possible they don't and the txt files we're looking for are just somewhere else in the
//database that I didn't download. If that's the case, then this issue will be fixed when we run the
//algorithm with the entire PMC database.

//Note: I have all the data going into a numbers file. However, the maximum number of rows that a
//numbers file can fit is 1 million.


public class Main {
   static ArrayList<Article> articles = new ArrayList<Article>();
   static ArrayList<Pattern> keys = new ArrayList<Pattern>();
   static int index = 0;
   static int indexAll = 0;
   static String superData = "";
   static File myObj;
   static File tableFile;
   static FileWriter tableWriter;
   static String mainDir;
   public static void main (String[] args) throws IOException {
      keys.add(Pattern.compile(".*\\bp(\\s*|\\s-\\s)(value|val)(s)\\b.*"));
      //p val(ue)(s), p-val(ue)(s), pval(ue)(s), p - value(ue)(s)
      keys.add(Pattern.compile(".*\\bp\\s*(<|>|=|equals)\\b.*"));
      //p<, p <, p>, p >, p=, p =, p equals

      try {
         myObj = new File("pdata.txt");
         tableFile = new File("pdata.csv");
         tableWriter = new FileWriter(tableFile);
         if (myObj.createNewFile()) {
            System.out.println("File created: " + myObj.getName());
         } else {
            System.out.println("File already exists.");
         }
         if (tableFile.createNewFile())  {
            System.out.println("File created: " + myObj.getName());
         } else {
            System.out.println("File already exists.");
         }
      } catch (IOException e) {
         System.out.println("An error occurred.");
         e.printStackTrace();
      }
      tableWriter.write("PMC Name|Publishing Date|Journal Name|P Value?\n");
      mainDir = args.length == 0 ? "/Users/yuvalamit/Downloads/PMC" : args[0];
      File testDir = new File(mainDir + "/xml");
      String[] subDirs = testDir.list();
      for (String dir: subDirs) {
         System.out.println("Checking " + testDir + "/" + dir);
         createArticles(testDir, dir);
      }
      tableWriter.close();

      System.out.println(index);
      System.out.println(indexAll);
   }

   private static void createArticles(File parentDir, String dir) throws IOException {
      File f = new File(parentDir, dir);
      if (f.isDirectory()) {
         System.out.println("*****" + f);
         String[] bigFiles = f.list();
         for (String bStr: bigFiles) {
            if (!bStr.equals(".DS_Store")) {
               //               File f2 = new File(f.toString(), bStr);
               //              String[] files = f2.list();
               //               for (String str : files) {
                  if (bStr.endsWith(".xml")) {
                     indexAll++;
                     Article article = processFile(f, bStr);
                     if (article.getDate() == null) {
                        String name = article.getName();
                        String jName = article.getJournalName();
                        String s = name + "|NULL|" + jName + "|NULL\n";
                        tableWriter.write(s);
                     } else {
                        // articles.add(article);
                        textScanFiles(article);
                     }
                  }
                  //               }
               }
            }
         }
      }

      private static void textScanFiles(Article art) {
         String name = art.getName();
         String jName = art.getJournalName();
         String date = art.dateToString();
         String s;
         try {
            boolean b = searchInFile((art.getName() + ".txt"));
            art.setpVal(b);
            boolean pVal = art.getpVal();
            s = name + "|" + date + "|" + jName + "|" + pVal;
         } catch (IOException e) {
            s = name + "|" + date + "|" + jName + "|" + "NULL";
         }
         System.out.println(s);
         try {
            tableWriter.write(s + "\n");
         } catch (IOException e) {
            System.err.println("Error writing to CSV!!");
            System.exit(1);
         }
      }


      private static Article processFile(File f, String str) throws IOException {
         File f3 = new File(f.toString(), str);
         //System.out.println(f2);
         FileInputStream testFis = new FileInputStream(f3);
         Document testDoc = Jsoup.parse(testFis, null, "", Parser.xmlParser());
         Article article = new Article(f3.getName().substring(0, f3.getName().length() - 4));
         processArticle(article, testDoc);
         //System.out.println(f2.getName());
         return article;
      }

      private static void processArticle(Article article, Document doc) throws IOException{
         Elements e = doc.select("pub-date");
         int index = 0;
         try {
            while (e.get(index).children().size() < 3) {
               index++;
            }
         } catch(Exception ex) {
            article.setDate(null);
            return;
         }
         Elements e2 = e.get(index).children();
         int day = Integer.parseInt(e2.get(0).text());
         int month = Integer.parseInt(e2.get(1).text());
         int year = Integer.parseInt(e2.get(2).text());
         String journalName = doc.select("journal-title").first().text();
         article.setDate(new Date(year, month, day));
         article.setJournalName(journalName);
      }

      public static boolean searchInFile(String fileName) throws IOException {
         String s = txtToStr(fileName);
         for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).matcher(s).matches()) {
               return true;
            }
         }
         return false;
      }

      public static String txtToStr(String fileName) throws IOException {
         String retString = "";
         File file = findTxtFileNew(fileName);
         if (file == null) { return ""; }
         Scanner fileScanner = new Scanner(file);
         while (fileScanner.hasNextLine()) {
            retString += fileScanner.nextLine();
         }
         return retString;
      }

      public static File findTxtFileNew(String fileName) throws IOException {
         File testDir = new File(mainDir + "/txt");
         String[] subDirs = testDir.list();
         for (String dir: subDirs) {
            File f0 = new File(testDir, dir);
            File f = new File(f0, fileName);
            if (f.exists()) {
               return f;
            }
         }
         System.out.println("Can't find " + fileName);
         throw new FileNotFoundException(fileName);
      }
   }
