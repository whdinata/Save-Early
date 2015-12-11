package com.di.saveearly.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.di.saveearly.MainActivity;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by whdinata on 12/10/15.
 */
public class SaveEarlyNotification {

    private static SaveEarlyNotification notif;
    private Context context;
    private Session session;

    private SaveEarlyNotification(Context context){
        this.context = context;
        session = createSessionObject();
    }

    public static SaveEarlyNotification getInstance(Context context){
        if(notif == null){
            notif = new SaveEarlyNotification(context);
        }

        return notif;
    }

    public void sendSMS(String phoneNumber){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "Abie fell. Please help! You have to save him now\n\nHe is at this location: http://maps.google.com/?q=55.9492848,-3.1823497", null, null);
            //Toast.makeText(this, "SMS sent", Toast.LENGTH_LONG).show();
        } catch(Exception e){
            Toast.makeText(context, "SMS failed. Please try again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void sendMail(String email) {
        try {
            Message message = createMessage(email, session);
            new SendMailTask().execute(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Message createMessage(String email, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("whdinata@yahoo.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Abie fell. Please help! You have to save him now");
        message.setText("He is at this location: http://maps.google.com/?q=55.9492848,-3.1823497");
        return message;
    }

    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("whdinata@gmail.com", "HistoryMaker");
            }
        });
    }

    private class SendMailTask extends AsyncTask<Message, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = ProgressDialog.show(context, "Please wait", "Sending mail", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
