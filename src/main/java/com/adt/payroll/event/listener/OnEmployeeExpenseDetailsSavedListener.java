package com.adt.payroll.event.listener;


import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.service.CommonEmailService;
import com.adt.payroll.service.MailService;

import freemarker.template.TemplateException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import javax.mail.MessagingException;
import java.io.IOException;

@Component
public class OnEmployeeExpenseDetailsSavedListener implements ApplicationListener<OnEmployeeExpenseDetailsSavedEvent>{
    
	@Autowired
	private final CommonEmailService mailService;

    public OnEmployeeExpenseDetailsSavedListener( CommonEmailService mailService) {
        this.mailService = mailService;
    }

    /**
     * As soon as a registration event is complete, invoke the email verification
     * asynchronously in an another thread pool
     */
    @Override
    @Async
    public void onApplicationEvent(OnEmployeeExpenseDetailsSavedEvent onEmployeeExpenseDetailsSavedEvent) {
        sendEmailVerification(onEmployeeExpenseDetailsSavedEvent);
    }

    /**
     * Send email verification to the user and persist the token in the database.
     */
    private void sendEmailVerification(OnEmployeeExpenseDetailsSavedEvent event) {
        EmployeeExpenseDTO employeeExpense = event.getEmployeeExpenseDTO();
        String recipientAddress = employeeExpense.getEmpEmail();
        String emailConfirmationUrl1 = event.getRedirectUrl1().toUriString();
//        String emailConfirmationUrl2 = event.getRedirectUrl2().toUriString();

        try {
            mailService.sendEmployeeExpenseVerification(event,emailConfirmationUrl1, recipientAddress);
        } catch (IOException | TemplateException | MessagingException e) {
            throw new MailSendException(recipientAddress);
        }
    }

}
