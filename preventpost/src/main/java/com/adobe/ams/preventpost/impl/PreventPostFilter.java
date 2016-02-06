package com.adobe.ams.preventpost.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.auth.Authenticator;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kalyanar on 16/10/15.
 */
@Component(metatype=true, label="Filter to prevent post to configured urls",
        description="Only admin can override this.",configurationFactory = true)
@Service(value = {Filter.class})
@Properties( {
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "PreventPostFilter"),
        @Property(name = Constants.SERVICE_VENDOR, value = "Adobe"),

        @Property(name = "pattern", value = "/system/console/.*"
                ),
        @Property(name = Constants.SERVICE_RANKING, intValue = Integer.MIN_VALUE)

})
public class PreventPostFilter implements Filter {

    private static final Logger log = LoggerFactory
            .getLogger(PreventPostFilter.class);

    private static final String[] DEFAULT_ALLOWED_USERS = {"admin"};
    @Property(
            label = "Users allowed to post",
            description = "List of users allowed to post to this path pattern",
            cardinality = Integer.MAX_VALUE)
    private static final String PROP_USERS_LIST = "users";

    private Set<String> users = new HashSet<String>();

    @Reference
    private  AuthenticationSupport authenticationSupport;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
   
   
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if(request instanceof HttpServletRequest){
            HttpServletRequest httpServletRequest =
                    (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse)
                    response;
            if(!httpServletRequest.getMethod().equals("GET")
                  ){
                String userId="";
              boolean b=  authenticationSupport.handleSecurity
                        (httpServletRequest,
                        httpServletResponse);
                final ResourceResolver resourceResolver = (ResourceResolver) httpServletRequest.getAttribute
         (AuthenticationSupport.REQUEST_ATTRIBUTE_RESOLVER);
                if(resourceResolver==null
                        ){
                  userId=getUserName(httpServletRequest);

                }else {
                    userId=  resourceResolver.getUserID();
                }

                if(users.contains(userId)){
                    chain.doFilter(request,response);
                }
                else {
                    httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }

            }else{
                chain.doFilter(request,response);
            }
        }else{
            chain.doFilter(request,response);
        }
    }

    @Override
    public void destroy() {

    }
  private String getUserName(HttpServletRequest httpServletRequest){
      String authHeader = httpServletRequest.getHeader( "Authorization" );
      if ( authHeader != null && authHeader.length() > 0 )
      {
          authHeader = authHeader.trim();
          int blank = authHeader.indexOf( ' ' );
          if ( blank > 0 )
          {
              String authInfo = authHeader.substring( blank ).trim();
              byte[][] userPass = base64Decode( authInfo );
              final String username = toString( userPass[0] );
                return username;
          }
      }
      return null;
  }
    private static byte[][] base64Decode( String srcString )
    {
        byte[] transformed = Base64.getDecoder().decode( srcString );
        for ( int i = 0; i < transformed.length; i++ )
        {
            if ( transformed[i] == ':' )
            {
                byte[] user = new byte[i];
                byte[] pass = new byte[transformed.length - i - 1];
                System.arraycopy( transformed, 0, user, 0, user.length );
                System.arraycopy( transformed, i + 1, pass, 0, pass.length );
                return new byte[][]
                        { user, pass };
            }
        }

        return new byte[][]
                { transformed, new byte[0] };
    }


    private static String toString( final byte[] src )
    {
        try
        {
            return new String( src, "ISO-8859-1" );
        }
        catch ( UnsupportedEncodingException uee )
        {
            return new String( src );
        }
    }

    @Activate
    private void activate(Map<String, Object> props) {
      configurePathMap(PropertiesUtil.toStringArray(props.get
                (PROP_USERS_LIST),DEFAULT_ALLOWED_USERS));
    }
    private void configurePathMap(String[] usersArray){
        users = new HashSet<String>();
        for(String user:usersArray){
            users.add(user);
        }
    }
}
