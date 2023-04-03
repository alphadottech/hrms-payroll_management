/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alphadot.payroll.event.listener;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alphadot.payroll.event.OnPriorTimeApprovalEvent;
import com.alphadot.payroll.service.MailService;

import freemarker.template.TemplateException;

@Component
public class OnPriorTimeApprovedListener implements ApplicationListener<OnPriorTimeApprovalEvent> {

  //  private static final Logger logger = Logger.getLogger(OnPriorTimeApprovedListener.class);
    private final MailService mailService;

    @Autowired
    public OnPriorTimeApprovedListener(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * As soon as a registration event is complete, invoke the email verification
     * asynchronously in an another thread pool
     */
    @Override
    @Async
    public void onApplicationEvent(OnPriorTimeApprovalEvent onPriortimeApprovalEvent) {
        sendAccountChangeEmail(onPriortimeApprovalEvent);
    }

    /**
     * Send email verification to the user and persist the token in the database.
     */
    private void sendAccountChangeEmail(OnPriorTimeApprovalEvent event) {
      //  User user = event.getUser();
        String action = event.getAction();
        String actionStatus = event.getActionStatus();
        String recipientAddress = event.getPriortime().get().getEmail();

        try {
            mailService.sendAccountChangeEmail(action, actionStatus, recipientAddress);
        } catch (IOException | TemplateException | MessagingException e) {
           // logger.error(e);
            throw new MailSendException(recipientAddress);
        }
    }
}
