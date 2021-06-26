package kdg.colonia.userService.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class UserEmailService
{
    private final JavaMailSender mailSender;

    @Value("${frontent.address}")
    private String link;
    public UserEmailService(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    /**
     * Method that builds and sends the password reset email with a valid token.
     * It uses the address of the website host to send the user to a form with this token.
     * @param recipientEmail from the user who wants to reset their password by email.
     * @param token with which a user can change their password once.
     */
    public void SendResetPassword(String recipientEmail,String token){
        try{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("ip2.colonia@gmail.com", "Colonia Support");
        helper.setTo(recipientEmail);

        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link +"#/resetPassword?token="+token+ "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("Could not send email.");
        }
    }



    /**
     * Method that builds and sends the activation email.
     * It uses the address of the website host to send the user to a form with this token.
     * @param recipientEmail from the user who wants to reset their password by email.
     * @param token with which a user can activate their account
     */
    public void sendActivationEmail(String recipientEmail,String token){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom("ip2.colonia@gmail.com", "Colonia Support");
            helper.setTo(recipientEmail);

            String subject = "Activate your account.";

            String content = "<p>Hello,</p>"
                    + "<p>You have created an account on the colonia website.</p>"
                    + "<p>Click the link below to activate your account please:</p>"
                    + "<p><a href=\"" + link +"#/activateAccount?token="+token+ "\">Activate my account</a></p>"
                    + "<br>"
                    + "<p>Ignore this email if you did not sign up for our website.";

            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("Could not send email.");
        }
    }

    /**
     * Method that builds and sends an invitation email to register a user that does not exist yet.
     * It uses the address of the website host to send the user to a form with .
     * @param recipientEmail from the user who wants to reset their password by email.
     * @param lobbyId the lobby to which the user should be added on creation.
     */
    public void sendInvitationMail(String recipientEmail,String lobbyId){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom("ip2.colonia@gmail.com", "Colonia Support");
            helper.setTo(recipientEmail);

            String subject = "You have been invited to play Colonia!";

            String content = "<p>Hello,</p>"
                    + "<p>You have been invited to play a game on the colonia website.</p>"
                    + "<p>Click the link below to create an account and join the game.</p>"
                    + "<p><a href=\"" + link +"#/register?invitation="+lobbyId+ "\">Create my account</a></p>"
                    + "<br>";
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("Could not send invitation email.");
        }
    }

}
