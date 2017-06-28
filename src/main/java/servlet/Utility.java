package servlet;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

/**
 * Created by ethanlam on 6/22/17.
 */
public class Utility {
    public HashMap<String, String> getSettings(){
        HashMap<String, String> settings = new HashMap<String, String>();
        try {
//            List<String> settingsFile = Files.readAllLines(Paths.get(System.getProperty("user.home") + "/MySchoolRankSettings"));
//            for(String line : settingsFile){
//                settings.put(line.split("=")[0], line.split("=")[1]);
//            }
            InputStream is = getClass().getClassLoader().getResourceAsStream("MySchoolRankSettings.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while((line = reader.readLine()) != null){
                settings.put(line.split("=")[0], line.split("=")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
    }

    public static Connection getSQLConnection() throws ClassNotFoundException, SQLException, IOException {
        HashMap<String, String> settings = new Utility().getSettings();

        Class.forName("com.mysql.jdbc.Driver");
        String DB_URL = "jdbc:mysql://localhost/MySchoolRank";
        String Username = settings.get("DB_USERNAME");
        String Password = settings.get("DB_PASSWORD");

        return DriverManager.getConnection(DB_URL, Username, Password);
    }

    public static void sendEmail(String recipientEmail, String subject, String content) throws Exception {
        String senderEmail = "myschoolrankcontact@gmail.com";
        String emailPassword = new Utility().getSettings().get("EMAIL_PASSWORD");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, emailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject(subject);
        message.setContent(content, "text/html");

        Transport.send(message);
    }

    //Uses SHA-512 and Returns Base64 Encoded Hash
    public static String hashContent(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update(content.getBytes());
        byte[] hash = digest.digest();

        return Base64.getEncoder().encodeToString(hash);
    }

    public static String getErrorMessage(String errorId){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("NotValidEmail", "Please Use Your School Email Address");
        hashMap.put("PasswordsDoNotMatch", "The Passwords Do Not Match");
        hashMap.put("EmptyPassword", "Please Enter A Password");
        hashMap.put("NotInDatabase", "The Email Address Submitted Is Not Contained In The Email Database. If There Is A Problem, Please Send An Email Notifying That Your Email Should Be In The Database.");
        hashMap.put("EmailAlreadyUsed", "The Email Address Is Already In Use");
        hashMap.put("EmailIsBeingVerified", "The Email Address Submitted Is Being Verified. If You Need Another Verification Link Sent, Visit The Homepage And Recover Your Account.");
        hashMap.put("FailedLogin", "Incorrect Email or Password");
        hashMap.put("NotRecoverableEmail", "Email Is Not A Recoverable Email Address");

        return (hashMap.get(errorId) != null && errorId != null) ? hashMap.get(errorId) : "";
    }

    public static String extractNameFromEmail(String email){
        StringBuilder result = new StringBuilder();
        String[] split = email.split("@")[0].split("\\.");
        //Uppercase First and Last Name
        result.append(split[0].substring(0, 1).toUpperCase() + split[0].substring(1) + " ");
        result.append(split[1].substring(0, 1).toUpperCase() + split[1].substring(1));
        return result.toString();
    }

    public static String getUserScore(String accountName){
        //Will work if email is input or not
        accountName = accountName.split("@")[0];
        String score = null;
        try {
            Connection connection = getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT score FROM Score WHERE account_name = ?");
            statement.setString(1, accountName);

            ResultSet resultSet = statement.executeQuery();
            //Check if there is a match
            if(resultSet.next()){
                score = String.valueOf(resultSet.getDouble("score"));
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return score;
    }

    public static void performRanking(int iterations, double dampingFactor){
        try {
            Connection connection = Utility.getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT account_name FROM Users");
            ResultSet allNames = statement.executeQuery();

            HashMap<String, ArrayList<String>> userToLinkers = new HashMap<String, ArrayList<String>>();
            HashMap<String, Double> userToScore = new HashMap<String, Double>();
            HashMap<String, Integer> userToNumberOfFriends = new HashMap<String, Integer>();

            //Loop through all users in DB and store data in HashMaps so that calculation can be performed without many DB queries
            while(allNames.next()){
                String userName = allNames.getString("account_name");
                ArrayList<String> linkers = new ArrayList<>();

                //Get the all users who said that `userName` was friend
                statement = connection.prepareStatement("SELECT user FROM Friends WHERE user_friend = ?");
                statement.setString(1, userName);
                ResultSet userLinkers = statement.executeQuery();
                while(userLinkers.next()){
                    linkers.add(userLinkers.getString("user"));
                }
                userToLinkers.put(userName, linkers);

                //Set the initial score for the user
                userToScore.put(userName, 0.0);

                //Get the number of friends that user has
                statement = connection.prepareStatement("SELECT COUNT(*) AS number_of_friends FROM Friends WHERE user = ?");
                statement.setString(1, userName);
                ResultSet numberOfFriends = statement.executeQuery();
                numberOfFriends.next();
                userToNumberOfFriends.put(userName, numberOfFriends.getInt("number_of_friends"));
            }

            //Iterate the ranking algorithm multiple times
            for (int i = 0; i < iterations; i++) {
                for(String user : userToLinkers.keySet()){
                    double newScore = 0.0;
                    for(String linker : userToLinkers.get(user)){
                        //Prevent divide by 0
                        if(userToNumberOfFriends.get(linker) != 0) {
                            newScore += userToScore.get(linker) / userToNumberOfFriends.get(linker);
                        }
                    }
                    newScore *= dampingFactor;
                    newScore += 1 - dampingFactor;
                    //Update the user's score
                    userToScore.put(user, newScore);
                }
            }

            //Submit new scores to the DB
            for(String user : userToScore.keySet()){
                statement = connection.prepareStatement("UPDATE Score SET score = ? WHERE account_name = ?");
                statement.setDouble(1, userToScore.get(user));
                statement.setString(2, user);
                statement.executeUpdate();
            }

            statement.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
