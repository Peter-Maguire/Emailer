package com.unacceptableuse.emailer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailer {
	public static Random rand = new Random();
	public Properties props = new Properties();
	public Properties mailProps = new Properties();
	public static String configPath = System.getProperty("user.dir")+"\\mailer.cfg";
	
	public static String comments = "Emailer config file. Explanations:\n"+
									"characters: the contents of the random strings is chosen from the selection.\n"+
									"body: the body of the email. Numbers in square brackets are replaced with random strings of that length."+
									"[10] would be replaced with a random string 10 characters long.\n"+
									"title: the title of the email. can also contain random strings.\n"+
									"emails: comma seperated list of emails to send to. For example: a@example.com,b@example.com,c@example.com\n"+
									"from: CAN BE ANY EMAIL ADDRESS but some servers verify that it is the legitimate source\n"+
									"host: the SMTP host\n"+
									"port: the SMTP port\n"+
									"username: email username\n"+
									"password: email password";
	
	
	public Emailer(){
		System.out.println("Emailer by UnacceptableUse... Loading config file...");
		try {
			props.load(new FileInputStream(configPath));

		} catch (FileNotFoundException e) {
			System.err.println("Could not find the properties file. Creating one at "+configPath);
		} catch (IOException e) {
			System.err.println("Properties file is corrupt or inaccessible. Delete it and re-run.");
			System.err.println("Error: "+e.toString());
			System.exit(-1);
		}finally{
			props.putIfAbsent("characters", "abcdefghijklmnopqrstuvwxyz0123456789");
			props.putIfAbsent("body", "Hi\n[128]\nFrom, [10]");
			props.putIfAbsent("title", "[16]");
			props.putIfAbsent("emails", "");
			props.putIfAbsent("from", "TJackets5832@gmail.com");
			props.putIfAbsent("host", "smtp.gmail.com");
			props.putIfAbsent("port", "465");
			props.putIfAbsent("username", "TJackets5832@gmail.com");
			props.putIfAbsent("password", "yourpasswordhere");
			try {
				saveProperties();
			} catch (IOException e) {
				System.err.println("Error saving properties file. Try running as admin or in a different directory.");
				System.err.println("Error: "+e.toString());
			}
		}
		
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.host", props.getProperty("host"));
        mailProps.put("mail.smtp.port", props.getProperty("port"));
        mailProps.put("mail.smtp.socketFactory.port", "587");
        mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailProps.put("mail.smtp.socketFactory.fallback", "false");
		
        
        String[] emails = props.getProperty("emails").split(",");
        for(String email : emails){
        	email(email, parse(props.getProperty("title")), parse(props.getProperty("body")));
        }
        System.out.println("Finished emailing");
	}
	
	public String parse(String message){
		for(int i = 0; i < message.length(); i++){
			int start = message.indexOf("[", i); 
			if(start > - 1){
				String replace = message.substring(start, message.indexOf("]", start)+1);
				int num = Integer.parseInt(message.substring(start+1, message.indexOf("]", start)));
				message = message.replace(replace, generateRandomString(num));
			}
		}
		return message;
	}
	
	public void email(String recipient, String title, String body){
		Session session = Session.getInstance(mailProps, new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication(){
				return new PasswordAuthentication(props.getProperty("username"), props.getProperty("password"));
			}
		});
		
		try{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(props.getProperty("from")));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			message.setSubject(title);
			message.setText(body);
			Transport.send(message);
			System.out.println("Successfully sent message to "+recipient);
		}catch(MessagingException e){
			System.err.println("Error sending message to "+recipient+": "+e.toString());
		}
	}
	

	
	public void saveProperties() throws IOException{
		File file = new File(configPath);
		if(!file.exists()){
			file.createNewFile();
		}
		props.store(new FileWriter(file), comments);
	}
	
	public static void main(String[] args){
		new Emailer();
	}
	
	
	public String generateRandomString(int length){
		StringBuilder stb = new StringBuilder();
		for(int i = 0; i < length; i++){
			int num = rand.nextInt(props.getProperty("characters").length()-1);
			stb.append(props.getProperty("characters").substring(num, num+1));
		}
		return stb.toString();
	}
}
