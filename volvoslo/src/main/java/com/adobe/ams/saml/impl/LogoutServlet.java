package com.adobe.ams.saml.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.osgi.framework.Constants;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by kalyanar on 16/10/15.
 */
@Component(metatype=true, label="Apache Sling Authentication Logout Servlet " +
        "to handle volvo slo",
        description="Servlet for logging out users through the authenticator " +
                "service for single logout scenario in volvo.")
@Service(value = {Servlet.class,Filter.class})
@Properties( {
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "Authenticator Logout Servlet"),
        @Property(name = Constants.SERVICE_VENDOR, value = "Adobe"),
        @Property(name = "sling.servlet.methods", value = { "GET", "POST" } ,
                label = "Method", description = "Supported Methdos",
                unbounded= PropertyUnbounded.ARRAY),
        @Property(name = "pattern", value = "/services/volvo/platform/service/logoutservice.html")

})
public class LogoutServlet extends SlingAllMethodsServlet implements Filter {
    /**
     * The servlet is registered on this path.
     */
    @Property(name = "sling.servlet.paths")
    public static final String SERVLET_PATH = "/services/volvo/platform/service/logoutservice";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Override
    protected void doGet(final SlingHttpServletRequest request,final SlingHttpServletResponse response) throws ServletException, IOException{

        clearCookie("login-token",request,response);

        response.sendRedirect("/content/error-page/aem_logout.html");


    }
    @Override
    protected void doPost(final SlingHttpServletRequest request,final
    SlingHttpServletResponse response) throws ServletException, IOException{

        clearCookie("login-token",request,response);

        response.sendRedirect("/content/error-page/aem_logout.html");


    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if(request instanceof HttpServletRequest){
            HttpServletRequest httpServletRequest =
                    (HttpServletRequest) request;
            if(httpServletRequest.getMethod().equals("POST")
                  ){

                chain.doFilter(new TokenCookieFilterRequestWrapper(
                        (HttpServletRequest)request),response);
            }else{
                chain.doFilter(request,response);
            }
        }else{
            chain.doFilter(request,response);
        }
    }
    private void clearCookie(String cookieName, final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    cookie.setValue("");
                    break;
                }
            }
        }

        final Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
class TokenCookieFilterRequestWrapper extends HttpServletRequestWrapper{

    /**
     * Create a wrapper for the supplied wrappedRequest
     *
     * @param wrappedRequest
     */
    public TokenCookieFilterRequestWrapper(HttpServletRequest
                                                   wrappedRequest) {
        super(wrappedRequest);
    }
    public Cookie[] getCookies() {
        return null;
    }
}
