package com.adt.payroll.event.listener;


import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
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
public class OnEmployeeExpenseApprovedListener implements ApplicationListener<OnEmployeeExpenseAcceptOrRejectEvent> {

    //  private static final Logger logger = Logger.getLogger(OnPriorTimeApprovedListener.class);
	@Autowired
    private final CommonEmailService commonEmailService;

    @Autowired
    public OnEmployeeExpenseApprovedListener(CommonEmailService commonEmailService) {
        this.commonEmailService = commonEmailService;
    }

    /**
     * As soon as a registration event is complete, invoke the email verification
     * asynchronously in an another thread pool
     */
    @Override
    @Async
    public void onApplicationEvent(OnEmployeeExpenseAcceptOrRejectEvent onEmployeeExpenseApprovalEvent) {
        sendAccountChangeEmailApproved(onEmployeeExpenseApprovalEvent);
    }

    /**
     * Send email verification to the user and persist the token in the database.
     */
    private void sendAccountChangeEmailApproved(OnEmployeeExpenseAcceptOrRejectEvent event) {
        String action = event.getAction();
        String actionStatus = event.getActionStatus();
        String recipientAddress = event.getEmployeeExpenseDTO().getEmpEmail();
        try {
        	commonEmailService.sendEmployeeExpenseApprovalEmail(event, action, actionStatus, recipientAddress);
        } catch (IOException | TemplateException | MessagingException e) {
            throw new MailSendException(recipientAddress);
        }
    }

}
