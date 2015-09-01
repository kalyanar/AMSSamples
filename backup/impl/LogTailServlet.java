package com.adobe.ams.kalyanar.utilities.logtail.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by kalyanar on 8/30/2015.
 */
public class LogTailServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String logFilePath = request.getRequestPathInfo().getSuffix();
    }
}
