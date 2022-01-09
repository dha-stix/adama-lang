/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.extern.aws;

import com.amazonaws.services.simpleemailv2.AmazonSimpleEmailServiceV2Async;
import com.amazonaws.services.simpleemailv2.AmazonSimpleEmailServiceV2AsyncClientBuilder;
import com.amazonaws.services.simpleemailv2.model.*;
import org.adamalang.extern.Email;

import java.util.Collections;

public class SES implements Email {
  private final AWSConfig config;
  private final AmazonSimpleEmailServiceV2Async client;

  public SES(AWSConfig config) {
    this.config = config;
    this.client = AmazonSimpleEmailServiceV2AsyncClientBuilder.standard().withCredentials(config).withRegion(config.region).build();
  }

  @Override
  public void sendCode(String email, String code) {
    SendEmailRequest request = new SendEmailRequest();
    request.setReplyToAddresses(Collections.singleton(config.replyToEmailAddressForInit));
    request.setDestination(new Destination().withToAddresses(email));
    request.setContent(
        new EmailContent()
            .withSimple(
                new Message()
                    .withSubject(
                        new Content()
                            .withData("Your Super Secret Code for Adama Platform")
                            .withCharset("UTF-8"))
                    .withBody(
                        new Body()
                            .withText(
                                new Content()
                                    .withData("Your code is " + code)
                                    .withCharset("UTF-8")))));
    request.setFromEmailAddress(config.fromEmailAddressForInit);
    client.sendEmail(request);
  }
}